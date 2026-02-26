package com.crow6980.nusantarascript.command;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.script.Action;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Handles the registration and execution of commands defined in .ns scripts.
 * Uses reflection to inject commands directly into Bukkit's CommandMap.
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
     * Accesses Bukkit's internal CommandMap via reflection.
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
     * Registers a command parsed from an .ns script into the server.
     * @param customCommand The command data container.
     */
    public void registerCommand(CustomCommand customCommand) {
        if (commandMap == null) return;
        
        String commandName = customCommand.getName().toLowerCase();
        // Strip leading slash if present
        if (commandName.startsWith("/")) commandName = commandName.substring(1);
        
        // Create the Bukkit-compatible command object on the fly
        Command bukkitCmd = new Command(commandName) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
                return executeCustomCommand(customCommand, sender, args);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
                // Future implementation: can be pulled from script metadata
                return Collections.emptyList(); 
            }
        };
        
        if (customCommand.getDescription() != null) {
            bukkitCmd.setDescription(customCommand.getDescription());
        }
        
        if (customCommand.getPermission() != null) {
            bukkitCmd.setPermission(customCommand.getPermission());
        }
        
        // Register using a fallback prefix 'nusantarascript'
        commandMap.register("nusantarascript", bukkitCmd);
        registeredCommands.put(commandName, customCommand);
        
        plugin.getLogger().info("Registered custom command: /" + commandName);
    }
    
    /**
     * Bridges the Minecraft command execution to the Script Executor.
     */
    private boolean executeCustomCommand(CustomCommand customCommand, CommandSender sender, String[] args) {
        // Permission Check
        if (customCommand.getPermission() != null && !sender.hasPermission(customCommand.getPermission())) {
            sender.sendMessage("§cKamu tidak memiliki izin untuk menjalankan perintah ini!");
            return true;
        }
        
        // Prepare context variables for the script
        Map<String, Object> context = new HashMap<>();
        if (sender instanceof Player player) {
            context.put("player", player);
        }
        
        context.put("sender", sender);
        
        // Map arguments so scripts can use {arg1}, {arg2}, etc.
        for (int i = 0; i < args.length; i++) {
            context.put("arg" + (i + 1), args[i]);
        }
        context.put("args_count", args.length);
        context.put("all_args", String.join(" ", args));

        // Execute the actions defined in the script
        try {
            for (Action action : customCommand.getActions()) {
                executor.executeAction(action, context);
            }
        } catch (Exception e) {
            sender.sendMessage("§cTerjadi kesalahan internal saat menjalankan perintah skrip!");
            plugin.getLogger().severe("Error executing custom command /" + customCommand.getName() + ": " + e.getMessage());
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        
        return true;
    }

    /**
     * Clears the registry. 
     * Note: Bukkit doesn't natively support unregistering commands easily at runtime, 
     * but clearing this map ensures old logic isn't triggered if the script is modified.
     */
    public void unregisterAll() {
        registeredCommands.clear();
    }
    
    public boolean isCommandRegistered(String commandName) {
        return registeredCommands.containsKey(commandName.toLowerCase());
    }

    public Map<String, CustomCommand> getRegisteredCommands() {
        return registeredCommands;
    }
}