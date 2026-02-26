package com.crow6980.nusantarascript.registry;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.script.EventHandler;
import com.crow6980.nusantarascript.script.Script;
// Import the specific classes instead of the whole package if the star wildcard fails
import com.crow6980.nusantarascript.listeners.PlayerJoinListener; 
// Add others here as you create them:
import com.crow6980.nusantarascript.listeners.PlayerQuitListener; // Ensure your specific listeners are in this package
import com.crow6980.nusantarascript.listeners.BlockBreakListener;
import com.crow6980.nusantarascript.listeners.PlayerChatListener;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * STEP 4: Dynamic Event Registry
 * * Manages dynamic registration of Bukkit event listeners.
 * It only registers listeners for events that are actually found in loaded .ns scripts.
 * * @author crow6980
 */
public class EventRegistry {
    
    private final NusantaraScript plugin;
    private final EnhancedScriptExecutor executor;
    
    // Maps event types to their active Bukkit Listener objects
    private final Map<EventHandler.EventType, Listener> registeredListeners;
    
    // Maps event types to the list of script handlers that need to run
    private final Map<EventHandler.EventType, List<EventHandler>> eventHandlers;
    
    public EventRegistry(NusantaraScript plugin, EnhancedScriptExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
        this.registeredListeners = new HashMap<>();
        this.eventHandlers = new HashMap<>();
    }
    
    /**
     * Clears all registered event handlers and removes listeners from Bukkit
     */
    public void clear() {
        unregisterAll();
    }
    
    /**
     * Registers all events used by a script.
     * If an event type is new, it injects a new Bukkit listener.
     * * @param script The script object containing event handlers
     */
    public void registerScript(Script script) {
        for (EventHandler handler : script.getEventHandlers()) {
            EventHandler.EventType eventType = handler.getEventType();
            
            // Link the handler logic to the event type
            eventHandlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
            
            // Only register a new Bukkit Listener if we aren't already listening for this event
            if (!registeredListeners.containsKey(eventType)) {
                registerBukkitListener(eventType);
            }
        }
    }
    
    /**
     * Dynamically creates and registers specific Bukkit listeners
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
            // Note: PLAYER_DEATH, DAMAGE, etc., are usually handled by a 
            // general ScriptEventListener for better performance on high-frequency events.
            default:
                break;
        }
        
        if (listener != null) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            registeredListeners.put(eventType, listener);
            plugin.getLogger().info("Successfully registered: " + eventType);
        }
    }
    
    /**
     * Returns the list of script handlers for a fired event
     */
    public List<EventHandler> getHandlers(EventHandler.EventType eventType) {
        return eventHandlers.getOrDefault(eventType, Collections.emptyList());
    }
    
    /**
     * Completely unregisters all listeners from the Bukkit event system.
     * Vital for reloads to prevent "Ghost Events" from old script versions.
     */
    public void unregisterAll() {
        for (Listener listener : registeredListeners.values()) {
            HandlerList.unregisterAll(listener);
        }
        
        registeredListeners.clear();
        eventHandlers.clear();
        
        plugin.getLogger().info("Unregistered all dynamic script listeners.");
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