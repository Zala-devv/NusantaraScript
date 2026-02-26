package com.crow6980.nusantarascript.listeners;

import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.registry.EventRegistry;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.HashMap;
import java.util.Map;

public class PlayerJoinListener implements Listener {

    private final EventRegistry registry;
    private final EnhancedScriptExecutor executor;

    public PlayerJoinListener(EventRegistry registry, EnhancedScriptExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @org.bukkit.event.EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 1. Get all handlers for PLAYER_JOIN from our scripts
        var handlers = registry.getHandlers(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_JOIN);
        
        if (handlers.isEmpty()) return;

        // 2. Prepare the context (variables like %player%)
        Map<String, Object> context = new HashMap<>();
        context.put("player", event.getPlayer());
        context.put("event", event);

        // 3. Execute each script handler
        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            executor.executeHandler(handler, context);
        }
    }
}