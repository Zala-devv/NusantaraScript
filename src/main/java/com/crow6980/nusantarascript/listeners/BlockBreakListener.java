package com.crow6980.nusantarascript.listeners;

import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.registry.EventRegistry;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import java.util.HashMap;
import java.util.Map;

public class BlockBreakListener implements Listener {

    private final EventRegistry registry;
    private final EnhancedScriptExecutor executor;

    public BlockBreakListener(EventRegistry registry, EnhancedScriptExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @org.bukkit.event.EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Fetch handlers for BLOCK_BREAK
        var handlers = registry.getHandlers(com.crow6980.nusantarascript.script.EventHandler.EventType.BLOCK_BREAK);
        
        if (handlers == null || handlers.isEmpty()) return;

        // Prepare context
        Map<String, Object> context = new HashMap<>();
        context.put("player", event.getPlayer());
        context.put("block", event.getBlock());
        context.put("event", event);
        // 'alat_benar' check logic in executor will look at the player's item in hand via this context

        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            executor.executeHandler(handler, context);
            
            // If a script action called "batalkan event", the executor should 
            // have called event.setCancelled(true) on the event object in the context.
        }
    }
}