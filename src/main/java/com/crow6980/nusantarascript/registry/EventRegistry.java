package com.crow6980.nusantarascript.registry;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.script.EventHandler;
import com.crow6980.nusantarascript.script.Script;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * STEP 4: Dynamic Event Registry
 * Manages the mapping of .ns script logic to Minecraft events.
 * * NOTE: Standard events are now handled by ScriptEventListener.
 * This class primarily manages the storage of script handlers.
 */
public class EventRegistry {
    
    private final NusantaraScript plugin;
    private final EnhancedScriptExecutor executor;
    
    // Maps event types to their active Bukkit Listener objects (for dynamic/non-standard events)
    private final Map<EventHandler.EventType, Listener> registeredListeners;
    
    // Maps event types to the list of script handlers that need to run
    private final Map<EventHandler.EventType, List<EventHandler>> eventHandlers;
    
    public EventRegistry(NusantaraScript plugin, EnhancedScriptExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
        this.registeredListeners = new HashMap<>();
        this.eventHandlers = new HashMap<>();
    }
    
    public void clear() {
        unregisterAll();
    }
    
    /**
     * Registers all events used by a script.
     */
    public void registerScript(Script script) {
        for (EventHandler handler : script.getEventHandlers()) {
            EventHandler.EventType eventType = handler.getEventType();
            
            // Link the handler logic to the event type
            eventHandlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
            
            // WE DO NOT call registerBukkitListener(eventType) here for 
            // JOIN, QUIT, CHAT, or BREAK because ScriptEventListener handles them.
            // Only use this for custom or extra dynamic events in the future.
        }
    }
    
    /**
     * Dynamically creates and registers specific Bukkit listeners
     * COMMENTED OUT: Most of these are now handled by ScriptEventListener
     */
    /*
    private void registerBukkitListener(EventHandler.EventType eventType) {
        Listener listener = null;
        
        switch (eventType) {
            // These are now handled in ScriptEventListener.java to avoid double-execution
            // case PLAYER_JOIN:
            //     listener = new PlayerJoinListener(this, executor);
            //     break;
            default:
                break;
        }
        
        if (listener != null) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            registeredListeners.put(eventType, listener);
            plugin.getLogger().info("Successfully registered dynamic listener: " + eventType);
        }
    }
    */
    
    /**
     * Returns the list of script handlers for a fired event.
     * This is called by ScriptEventListener to find which scripts to run.
     */
    public List<EventHandler> getHandlers(EventHandler.EventType eventType) {
        return eventHandlers.getOrDefault(eventType, Collections.emptyList());
    }
    
    /**
     * Completely unregisters all listeners and clears handlers.
     */
    public void unregisterAll() {
        for (Listener listener : registeredListeners.values()) {
            HandlerList.unregisterAll(listener);
        }
        
        registeredListeners.clear();
        eventHandlers.clear();
        
        plugin.getLogger().info("Cleared all script event handlers.");
    }
    
    /**
     * Returns statistics for debugging (/ns info)
     */
    public Map<EventHandler.EventType, Integer> getEventStatistics() {
        Map<EventHandler.EventType, Integer> stats = new HashMap<>();
        for (Map.Entry<EventHandler.EventType, List<EventHandler>> entry : eventHandlers.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }
}