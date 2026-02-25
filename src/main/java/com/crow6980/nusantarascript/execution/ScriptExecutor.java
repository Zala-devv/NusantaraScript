package com.crow6980.nusantarascript.execution;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.script.Action;
import com.crow6980.nusantarascript.script.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.Map;

/**
 * STEP 5: Execution Logic
 * 
 * Translates parsed script actions into actual Bukkit API calls.
 * This is where the magic happens - converting Indonesian commands into Java code!
 * 
 * Features:
 * - Variable replacement (e.g., %player% becomes player name)
 * - Context-aware execution (knows what data is available)
 * - Error handling
 * 
 * @author crow6980
 */
public class ScriptExecutor {
    
    private final NusantaraScript plugin;
    
    public ScriptExecutor(NusantaraScript plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Executes all actions in an event handler
     * 
     * @param handler The event handler to execute
     * @param context Map containing event-specific data (player, block, etc.)
     */
    public void execute(EventHandler handler, Map<String, Object> context) {
        try {
            // Execute each action in the handler
            for (Action action : handler.getActions()) {
                executeAction(action, context);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing script action: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Executes a single action
     * Translates the action into Bukkit API calls
     */
    private void executeAction(Action action, Map<String, Object> context) {
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
        }
    }
    
    /**
     * Executes "kirim ... ke pemain" action
     * Sends a message to the player involved in the event
     */
    private void executeSendMessage(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) {
            plugin.getLogger().warning("Cannot send message: no player in context");
            return;
        }
        
        // Replace variables in the message
        String message = replacePlaceholders(action.getParameter(), context);
        
        // Send the message
        player.sendMessage(message);
    }
    
    /**
     * Executes "broadcast" action
     * Broadcasts a message to all online players
     */
    private void executeBroadcast(Action action, Map<String, Object> context) {
        // Replace variables in the message
        String message = replacePlaceholders(action.getParameter(), context);
        
        // Broadcast to all players
        Bukkit.broadcastMessage(message);
    }
    
    /**
     * Executes "batalkan event" action
     * Cancels the Bukkit event if it's cancellable
     */
    private void executeCancelEvent(Action action, Map<String, Object> context) {
        Object event = context.get("event");
        
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        } else {
            plugin.getLogger().warning("Cannot cancel event: event is not cancellable");
        }
    }
    
    /**
     * Replaces placeholders in a message with actual values from context
     * 
     * Supported placeholders:
     * - %player% -> Player name
     * - %block% -> Block type
     * - More can be added easily!
     */
    private String replacePlaceholders(String message, Map<String, Object> context) {
        String result = message;
        
        // Replace %player% with player name
        if (context.containsKey("player")) {
            Player player = (Player) context.get("player");
            result = result.replace("%player%", player.getName());
        }
        
        // Replace %block% with block type
        if (context.containsKey("block")) {
            Block block = (Block) context.get("block");
            result = result.replace("%block%", block.getType().name());
        }
        
        // Add color code support
        result = result.replace("&", "§");
        
        return result;
    }
}
