package com.crow6980.nusantarascript.registry;

import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Bukkit listener for PlayerQuitEvent
 * Executes all script handlers registered for "saat pemain keluar"
 * 
 * @author crow6980
 */
public class PlayerQuitListener implements Listener {
    
    private final EventRegistry registry;
    private final EnhancedScriptExecutor executor;
    private com.crow6980.nusantarascript.script.EventHandler scriptHandler;
    public PlayerQuitListener(EventRegistry registry, EnhancedScriptExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var handlers = registry.getHandlers(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_QUIT);
        
        Map<String, Object> context = new HashMap<>();
        context.put("player", event.getPlayer());
        context.put("event", event);
        
        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            executor.execute(handler, context);
        }
    }
}
