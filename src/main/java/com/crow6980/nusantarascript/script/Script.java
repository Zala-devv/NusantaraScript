package com.crow6980.nusantarascript.script;

import com.crow6980.nusantarascript.command.CustomCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete parsed script file
 * Contains all event handlers and custom commands defined in the script
 * 
 * @author crow6980
 */
public class Script {
    
    private final String filename;
    private final List<EventHandler> eventHandlers;
    private final List<CustomCommand> customCommands;
    
    public Script(String filename, List<EventHandler> eventHandlers) {
        this.filename = filename;
        this.eventHandlers = new ArrayList<>(eventHandlers);
        this.customCommands = new ArrayList<>();
    }
    
    public Script(String filename, List<EventHandler> eventHandlers, List<CustomCommand> customCommands) {
        this.filename = filename;
        this.eventHandlers = new ArrayList<>(eventHandlers);
        this.customCommands = new ArrayList<>(customCommands);
    }
    
    public String getFilename() {
        return filename;
    }
    
    public List<EventHandler> getEventHandlers() {
        return new ArrayList<>(eventHandlers);
    }
    
    public List<CustomCommand> getCustomCommands() {
        return new ArrayList<>(customCommands);
    }
    
    /**
     * Gets all event handlers of a specific type
     */
    public List<EventHandler> getHandlersForEvent(EventHandler.EventType eventType) {
        List<EventHandler> matching = new ArrayList<>();
        for (EventHandler handler : eventHandlers) {
            if (handler.getEventType() == eventType) {
                matching.add(handler);
            }
        }
        return matching;
    }
    
    @Override
    public String toString() {
        return "Script{" + filename + ", handlers=" + eventHandlers.size() + 
               ", commands=" + customCommands.size() + "}";
    }
}
