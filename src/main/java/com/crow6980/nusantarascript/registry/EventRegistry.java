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
 * 
 * This class manages dynamic registration of Bukkit event listeners.
 * It only registers listeners for events that are actually used in scripts.
 * 
 * Key Features:
 * - Lazy listener registration (only register what's needed)
 * - Multiple scripts can handle the same event
 * - Clean unregistration on reload
 * 
 * @author crow6980
 */
public class EventRegistry {
    
    private final NusantaraScript plugin;
    private final EnhancedScriptExecutor executor;
    
    // Maps event types to their registered listeners
    private final Map<EventHandler.EventType, Listener> registeredListeners;
    
    // Maps event types to all handlers that should run for that event
    private final Map<EventHandler.EventType, List<EventHandler>> eventHandlers;
    
    public EventRegistry(NusantaraScript plugin, EnhancedScriptExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
        this.registeredListeners = new HashMap<>();
        this.eventHandlers = new HashMap<>();
    }
    
    /**
     * Registers all events used by a script
     * If an event type is already registered, just adds the handlers
     * 
     * @param script The script to register
     */
    public void registerScript(Script script) {
        for (EventHandler handler : script.getEventHandlers()) {
            EventHandler.EventType eventType = handler.getEventType();
            
            // Add handler to the list
            eventHandlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
            
            // Register the Bukkit listener if not already registered
            if (!registeredListeners.containsKey(eventType)) {
                registerBukkitListener(eventType);
            }
        }
    }
    
    /**
     * Registers a Bukkit event listener for a specific event type
     * This is where we dynamically create listeners based on what scripts need
     */
    private void registerBukkitListener(EventHandler.EventType eventType) {
        Listener listener = null;
        
        switch (eventType) {
            case PLAYER_JOIN:
                listener = new PlayerJoinListener(this, executor);
                break;
                
            case PLAYER_QUIT:
                listener = new PlayerQuitListener(this, executor);
                break;
                
            case BLOCK_BREAK:
                listener = new BlockBreakListener(this, executor);
                break;
                
            case PLAYER_CHAT:
                listener = new PlayerChatListener(this, executor);
                break;
        }
        
        if (listener != null) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            registeredListeners.put(eventType, listener);
            plugin.getLogger().info("Registered listener for event: " + eventType);
        }
    }
    
    /**
     * Gets all handlers for a specific event type
     * Called by the listeners when an event fires
     */
    public List<EventHandler> getHandlers(EventHandler.EventType eventType) {
        return eventHandlers.getOrDefault(eventType, new ArrayList<>());
    }
    
    /**
     * Unregisters all listeners and clears all handlers
     * Used when reloading scripts
     */
    public void unregisterAll() {
        // Unregister all Bukkit listeners
        for (Listener listener : registeredListeners.values()) {
            HandlerList.unregisterAll(listener);
        }
        
        registeredListeners.clear();
        eventHandlers.clear();
        
        plugin.getLogger().info("Unregistered all script listeners");
    }
    
    /**
     * Gets statistics about registered events
     */
    public Map<EventHandler.EventType, Integer> getEventStatistics() {
        Map<EventHandler.EventType, Integer> stats = new HashMap<>();
        for (Map.Entry<EventHandler.EventType, List<EventHandler>> entry : eventHandlers.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }
}
