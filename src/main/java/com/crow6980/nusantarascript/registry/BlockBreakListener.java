package com.crow6980.nusantarascript.registry;

import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Bukkit listener for BlockBreakEvent
 * Executes all script handlers registered for "saat blok dihancurkan"
 * 
 * @author crow6980
 */
public class BlockBreakListener implements Listener {
    
    private final EventRegistry registry;
    private final EnhancedScriptExecutor executor;
    
    public BlockBreakListener(EventRegistry registry, EnhancedScriptExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var handlers = registry.getHandlers(com.crow6980.nusantarascript.script.EventHandler.EventType.BLOCK_BREAK);
        
        Map<String, Object> context = new HashMap<>();
        context.put("player", event.getPlayer());
        context.put("block", event.getBlock());
        context.put("event", event);
        
        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            executor.execute(handler, context);
        }
    }
}
