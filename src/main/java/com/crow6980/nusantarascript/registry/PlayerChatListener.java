package com.crow6980.nusantarascript.registry;

import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Bukkit listener for AsyncChatEvent
 * Executes all script handlers registered for "saat pemain chat"
 * 
 * @author crow6980
 */
public class PlayerChatListener implements Listener {
    
    private final EventRegistry registry;
    private final EnhancedScriptExecutor executor;
    
    public PlayerChatListener(EventRegistry registry, EnhancedScriptExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        var handlers = registry.getHandlers(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_CHAT);
        
        Map<String, Object> context = new HashMap<>();
        context.put("player", event.getPlayer());
        context.put("event", event);
        
        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            executor.execute(handler, context);
        }
    }
}
