package com.crow6980.nusantarascript.listeners;

import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.registry.EventRegistry;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.HashMap;
import java.util.Map;

public class PlayerQuitListener implements Listener {

    private final EventRegistry registry;
    private final EnhancedScriptExecutor executor;

    public PlayerQuitListener(EventRegistry registry, EnhancedScriptExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @org.bukkit.event.EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Fetch handlers for PLAYER_QUIT event type
        var handlers = registry.getHandlers(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_QUIT);
        
        if (handlers == null || handlers.isEmpty()) return;

        // Prepare context for the script
        Map<String, Object> context = new HashMap<>();
        context.put("player", event.getPlayer());
        context.put("event", event);

        // Run the script actions
        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            executor.executeHandler(handler, context);
        }
    }
}