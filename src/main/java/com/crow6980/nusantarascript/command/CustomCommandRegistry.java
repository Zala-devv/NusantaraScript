package com.crow6980.nusantarascript.command;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

/**
 * PHASE 2 - STEP 4: Custom Command Registry
 * 
 * Dynamically registers custom commands from scripts using Bukkit's CommandMap.
 * This allows scripts to create new commands without declaring them in plugin.yml.
 * 
 * Uses reflection to access the internal CommandMap.
 * 
 * @author crow6980
 */
public class CustomCommandRegistry {
    
    private final NusantaraScript plugin;
    private final EnhancedScriptExecutor executor;
    private final Map<String, CustomCommand> registeredCommands;
    private CommandMap commandMap;
    
    public CustomCommandRegistry(NusantaraScript plugin, EnhancedScriptExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
        this.registeredCommands = new HashMap<>();
        this.commandMap = getCommandMap();
    }
    
    /**
     * Gets the Bukkit CommandMap using reflection
     * This is necessary because CommandMap is not exposed in the public API
     */
    private CommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to get CommandMap: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Registers a custom command from a script
     * 
     * @param customCommand The custom command to register
     */
    public void registerCommand(CustomCommand customCommand) {
        if (commandMap == null) {
            plugin.getLogger().warning("Cannot register command: CommandMap not available");
            return;
        }
        
        String commandName = customCommand.getName().toLowerCase();
        
        // Remove leading slash if present
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        
        // Create the Bukkit command
        Command command = new Command(commandName) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
                return executeCustomCommand(customCommand, sender, args);
            }
        };
        
        // Set command properties
        command.setDescription(customCommand.getDescription());
        if (customCommand.getPermission() != null) {
            command.setPermission(customCommand.getPermission());
        }
        
        // Register the command
        commandMap.register("nusantarascript", command);
        registeredCommands.put(commandName, customCommand);
        
        plugin.getLogger().info("Registered custom command: /" + commandName);
    }
    
    /**
     * Executes a custom command
     * 
     * @param customCommand The command to execute
     * @param sender The command sender
     * @param args Command arguments
     * @return true if command executed successfully
     */
    private boolean executeCustomCommand(CustomCommand customCommand, CommandSender sender, String[] args) {
        // Check permission
        if (customCommand.getPermission() != null && !sender.hasPermission(customCommand.getPermission())) {
            sender.sendMessage("§cKamu tidak memiliki izin untuk menggunakan perintah ini!");
            return true;
        }
        
        // Build context for executor
        Map<String, Object> context = new HashMap<>();
        
        if (sender instanceof Player) {
            context.put("player", sender);
        } else {
            // If not a player, check if command requires player context
            boolean requiresPlayer = customCommand.getActions().stream()
                .anyMatch(action -> action.getActionType().requiresPlayer());
            
            if (requiresPlayer) {
                sender.sendMessage("§cPerintah ini hanya bisa digunakan oleh pemain!");
                return true;
            }
        }
        
        // Add command arguments to context
        context.put("args", args);
        context.put("sender", sender);
        
        // Execute all actions in the command
        try {
            for (com.crow6980.nusantarascript.script.Action action : customCommand.getActions()) {
                executor.executeAction(action, context);
            }
        } catch (Exception e) {
            sender.sendMessage("§cTerjadi kesalahan saat menjalankan perintah!");
            plugin.getLogger().severe("Error executing custom command /" + customCommand.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    /**
     * Unregisters a custom command
     * 
     * @param commandName The command name to unregister
     */
    public void unregisterCommand(String commandName) {
        commandName = commandName.toLowerCase();
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        
        registeredCommands.remove(commandName);
        
        // Unregister from CommandMap
        if (commandMap != null) {
            try {
                Command command = commandMap.getCommand(commandName);
                if (command != null) {
                    command.unregister(commandMap);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to unregister command /" + commandName + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Unregisters all custom commands
     */
    public void unregisterAll() {
        List<String> commandNames = new ArrayList<>(registeredCommands.keySet());
        for (String commandName : commandNames) {
            unregisterCommand(commandName);
        }
        plugin.getLogger().info("Unregistered " + commandNames.size() + " custom commands");
    }
    
    /**
     * Gets all registered custom commands
     */
    public Map<String, CustomCommand> getRegisteredCommands() {
        return new HashMap<>(registeredCommands);
    }
    
    /**
     * Gets the number of registered commands
     */
    public int getCommandCount() {
        return registeredCommands.size();
    }
}
