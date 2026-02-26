package com.crow6980.nusantarascript.execution;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.condition.ConditionalBlock;
import com.crow6980.nusantarascript.manager.VariableManager;
import com.crow6980.nusantarascript.script.Action;
import com.crow6980.nusantarascript.script.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PHASE 2 - Enhanced Execution Logic
 * 
 * Now supports:
 * - Variable replacement ({variableName})
 * - Conditional execution (jika blocks)
 * - New action types (heal, feed, variables, etc.)
 * 
 * @author crow6980
 */
public class EnhancedScriptExecutor {
    
    private final NusantaraScript plugin;
    private final VariableManager variableManager;
    
    // Pattern to match {variableName} or {variableName.%player%}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    public EnhancedScriptExecutor(NusantaraScript plugin, VariableManager variableManager) {
        this.plugin = plugin;
        this.variableManager = variableManager;
    }
    
    /**
     * Executes all actions and conditional blocks in an event handler
     */
    public void execute(EventHandler handler, Map<String, Object> context) {
        context.put("variableManager", variableManager);
        try {
            for (Action action : handler.getActions()) {
                executeAction(action, context);
            }
            for (ConditionalBlock block : handler.getConditionalBlocks()) {
                executeConditionalBlock(block, context);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeConditionalBlock(ConditionalBlock block, Map<String, Object> context) {
        if (block.getCondition().evaluate(context)) {
            for (Action a : block.getActions()) {
                executeAction(a, context);
            }
        } else {
            for (Action a : block.getElseActions()) {
                // If this is an ELSEIF special Action, run its ConditionalBlock
                try {
                    java.lang.reflect.Method m = a.getClass().getMethod("getConditionalBlock");
                    ConditionalBlock elseifBlock = (ConditionalBlock) m.invoke(a);
                    if (elseifBlock != null) {
                        executeConditionalBlock(elseifBlock, context);
                        continue;
                    }
                } catch (Exception ignore) {}
                executeAction(a, context);
            }
        }
    }
    
    /**
     * Executes a single action - made public for CustomCommandRegistry
     */
    public void executeAction(Action action, Map<String, Object> context) {
        switch (action.getActionType()) {
            case SEND_MESSAGE:
                executeSendMessage(action, context);
                break;
                
            case BROADCAST:
                executeBroadcast(action, context);
                break;
                
            case CANCEL_EVENT:
                executeCancelEvent(action, context);
                break;
                
            case HEAL_PLAYER:
                executeHealPlayer(action, context);
                break;
                
            case FEED_PLAYER:
                executeFeedPlayer(action, context);
                break;
                
            case SET_VARIABLE:
                executeSetVariable(action, context);
                break;
                
            case ADD_VARIABLE:
                executeAddVariable(action, context);
                break;
                
            case SUBTRACT_VARIABLE:
                executeSubtractVariable(action, context);
                break;
                
            case DELETE_VARIABLE:
                executeDeleteVariable(action, context);
                break;
                
            case GIVE_ITEM:
                executeGiveItem(action, context);
                break;
                
            case KICK_PLAYER:
                executeKickPlayer(action, context);
                break;
                
            case TELEPORT:
                executeTeleport(action, context);
                break;
                
            case PLAY_SOUND:
                executePlaySound(action, context);
                break;
                
            case GIVE_EFFECT:
                executeGiveEffect(action, context);
                break;
            
            case CUSTOM:
                // CUSTOM is only used for internal parser logic (e.g., elseif/else blocks)
                // No direct execution needed here
                break;
        }
    }
    
    // ==================== ACTION IMPLEMENTATIONS ====================
    
    private void executeSendMessage(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        
        String message = replacePlaceholders(action.getParameter(), context);
        player.sendMessage(message);
    }
    
    private void executeBroadcast(Action action, Map<String, Object> context) {
        String message = replacePlaceholders(action.getParameter(), context);
        // Deprecated in Bukkit 1.20+, use for loop for all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(message);
        }
    }
    
    private void executeCancelEvent(Action action, Map<String, Object> context) {
        Object event = context.get("event");
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
    }
    
    private void executeHealPlayer(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }
    
    private void executeFeedPlayer(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }
    
    /**
     * Sets a variable value
     * Format: atur variabel {name} menjadi "value"
     */
    private void executeSetVariable(Action action, Map<String, Object> context) {
        String varName = action.getParameter();
        String value = action.getAdditionalParams().length > 0 ? action.getAdditionalParams()[0] : "";
        
        // Replace placeholders in value
        value = replacePlaceholders(value, context);
        
        // Check if it's a player variable (contains %player%)
        if (varName.contains("%player%")) {
            Player player = (Player) context.get("player");
            if (player == null) return;
            
            String actualVarName = varName.replace(".%player%", "").replace("%player%.", "");
            variableManager.setPlayer(player.getName(), actualVarName, value);
        } else {
            // Global variable
            variableManager.setGlobal(varName, value);
        }
    }
    
    /**
     * Adds to a variable
     * Format: tambah 1 ke variabel {name}
     */
    private void executeAddVariable(Action action, Map<String, Object> context) {
        String varName = action.getParameter();
        String amountStr = action.getAdditionalParams().length > 0 ? action.getAdditionalParams()[0] : "1";
        
        try {
            double amount = Double.parseDouble(amountStr);
            
            if (varName.contains("%player%")) {
                Player player = (Player) context.get("player");
                if (player == null) return;
                
                String actualVarName = varName.replace(".%player%", "").replace("%player%.", "");
                variableManager.add(player.getName(), actualVarName, amount);
            } else {
                variableManager.add(null, varName, amount);
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid number in add variable: " + amountStr);
        }
    }
    
    /**
     * Subtracts from a variable
     * Format: kurangi 1 dari variabel {name}
     */
    private void executeSubtractVariable(Action action, Map<String, Object> context) {
        String varName = action.getParameter();
        String amountStr = action.getAdditionalParams().length > 0 ? action.getAdditionalParams()[0] : "1";
        
        try {
            double amount = Double.parseDouble(amountStr);
            
            if (varName.contains("%player%")) {
                Player player = (Player) context.get("player");
                if (player == null) return;
                
                String actualVarName = varName.replace(".%player%", "").replace("%player%.", "");
                variableManager.subtract(player.getName(), actualVarName, amount);
            } else {
                variableManager.subtract(null, varName, amount);
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid number in subtract variable: " + amountStr);
        }
    }
    
    /**
     * Deletes a variable
     * Format: hapus variabel {name}
     */
    private void executeDeleteVariable(Action action, Map<String, Object> context) {
        String varName = action.getParameter();
        
        if (varName.contains("%player%")) {
            Player player = (Player) context.get("player");
            if (player == null) return;
            
            String actualVarName = varName.replace(".%player%", "").replace("%player%.", "");
            variableManager.deletePlayer(player.getName(), actualVarName);
        } else {
            variableManager.deleteGlobal(varName);
        }
    }
    
    /**
     * Gives an item to a player
     * Format: berikan DIAMOND 10 ke pemain
     */
    private void executeGiveItem(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        
        String materialName = action.getParameter();
        int amount = 1;
        
        if (action.getAdditionalParams().length > 0) {
            try {
                amount = Integer.parseInt(action.getAdditionalParams()[0]);
            } catch (NumberFormatException ignored) {}
        }
        
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            ItemStack item = new ItemStack(material, amount);
            player.getInventory().addItem(item);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown material: " + materialName);
        }
    }
    
    /**
     * Kicks a player
     * Format: keluarkan pemain dengan alasan "reason"
     */
    private void executeKickPlayer(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        
        // Deprecated in Bukkit 1.20+, use kick() without reason
        player.kick();
    }
    
    private void executeTeleport(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        
        // Implementation for teleport action
        plugin.getLogger().info("Teleport action not yet implemented");
    }
    
    private void executePlaySound(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        
        // Implementation for play sound action
        plugin.getLogger().info("Play sound action not yet implemented");
    }
    
    private void executeGiveEffect(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        
        // Implementation for give effect action
        plugin.getLogger().info("Give effect action not yet implemented");
    }
    
    // ==================== PLACEHOLDER REPLACEMENT ====================
    
    /**
     * Replaces all placeholders in a string
     * Supports:
     * - %player% -> Player name
     * - %block% -> Block type
     * - {variableName} -> Variable value
     * - {variableName.%player%} -> Player-specific variable
     * - & color codes
     */
    private String replacePlaceholders(String message, Map<String, Object> context) {
        String result = message;
        
        // Replace %player%
        if (context.containsKey("player")) {
            Player player = (Player) context.get("player");
            result = result.replace("%player%", player.getName());
        }
        
        // Replace %block%
        if (context.containsKey("block")) {
            Block block = (Block) context.get("block");
            result = result.replace("%block%", block.getType().name());
        }
        
        // Replace command arguments
        if (context.containsKey("args")) {
            String[] args = (String[]) context.get("args");
            // full argument string
            result = result.replace("%args%", String.join(" ", args));
            // indexed arguments 1-based
            for (int i = 0; i < args.length; i++) {
                result = result.replace("%arg" + (i + 1) + "%", args[i]);
                // also support arg-1 style (skript-like)
                result = result.replace("%arg-" + (i + 1) + "%", args[i]);
            }
            // simple %arg% = first argument if exists
            if (args.length > 0) {
                result = result.replace("%arg%", args[0]);
            }
        }
        
        // Replace {variables}
        result = replaceVariables(result, context);
        
        // Color codes
        result = result.replace("&", "§");
        
        return result;
    }
    
    /**
     * Replaces variable placeholders {variableName}
     */
    private String replaceVariables(String text, Map<String, Object> context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = null;
            
            // Check if it's a player variable
            if (varName.contains("%player%")) {
                Player player = (Player) context.get("player");
                if (player != null) {
                    String actualVarName = varName.replace(".%player%", "").replace("%player%.", "");
                    value = variableManager.getPlayer(player.getName(), actualVarName);
                }
            } else {
                // Global variable
                value = variableManager.getGlobal(varName);
            }
            
            // Replace with value or empty string
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
}
