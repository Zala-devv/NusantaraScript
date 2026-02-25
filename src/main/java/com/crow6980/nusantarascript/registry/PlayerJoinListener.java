package com.crow6980.nusantarascript.registry;

import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Bukkit listener for PlayerJoinEvent
 * Executes all script handlers registered for "saat pemain masuk"
 * 
 * @author crow6980
 */
public class PlayerJoinListener implements Listener {
    
    private final EventRegistry registry;
    private final EnhancedScriptExecutor executor;
    private com.crow6980.nusantarascript.script.EventHandler scriptHandler;
    public PlayerJoinListener(EventRegistry registry, EnhancedScriptExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Get all script handlers for this event type
        var handlers = registry.getHandlers(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_JOIN);
        
        // Create context for script execution
        Map<String, Object> context = new HashMap<>();
        context.put("player", event.getPlayer());
        context.put("event", event);
        
        // Execute each handler
        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            executor.execute(handler, context);
        }
    }
}
