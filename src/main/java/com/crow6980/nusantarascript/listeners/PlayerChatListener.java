package com.crow6980.nusantarascript.listeners;

import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.registry.EventRegistry;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.HashMap;
import java.util.Map;

public class PlayerChatListener implements Listener {

    private final EventRegistry registry;
    private final EnhancedScriptExecutor executor;

    public PlayerChatListener(EventRegistry registry, EnhancedScriptExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @org.bukkit.event.EventHandler
    @SuppressWarnings("deprecation") // Suppress warnings for getMessage() in PlayerDeathEvent
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Fetch handlers for PLAYER_CHAT
        var handlers = registry.getHandlers(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_CHAT);
        
        if (handlers == null || handlers.isEmpty()) return;

        // Prepare context
        Map<String, Object> context = new HashMap<>();
        context.put("player", event.getPlayer());
        context.put("message", event.getMessage());
        context.put("event", event);

        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            executor.executeHandler(handler, context);
            
            // Note: If the script modifies the message or cancels the event,
            // the changes will be reflected in the actual Minecraft chat.
        }
    }
}