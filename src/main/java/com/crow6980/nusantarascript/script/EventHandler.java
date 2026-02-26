package com.crow6980.nusantarascript.script;

import com.crow6980.nusantarascript.condition.ConditionalBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single event handler in a script
 * Contains the event type, actions, and conditional blocks (jika statements)
 * 
 * @author crow6980
 */
public class EventHandler {
    
    /**
     * Supported event types
     * Maps Indonesian triggers to Bukkit events
     */
    public enum EventType {
        PLAYER_JOIN,      // saat pemain masuk
        PLAYER_QUIT,      // saat pemain keluar
        BLOCK_BREAK,      // saat blok dihancurkan
        PLAYER_CHAT,      // saat pemain chat
        PLAYER_DEATH,     // saat pemain mati
        PLAYER_RESPAWN,   // saat pemain hidup kembali
        PLAYER_DAMAGE,    // saat pemain terluka
        ENTITY_DAMAGE     // saat entity terluka
    }
    
    private final EventType eventType;
    private final int lineNumber;
    private final List<Action> actions;
    private final List<ConditionalBlock> conditionalBlocks;
    
    public EventHandler(EventType eventType, int lineNumber) {
        this.eventType = eventType;
        this.lineNumber = lineNumber;
        this.actions = new ArrayList<>();
        this.conditionalBlocks = new ArrayList<>();
    }
    
    public void addAction(Action action) {
        actions.add(action);
    }
    
    public void addConditionalBlock(ConditionalBlock block) {
        conditionalBlocks.add(block);
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }
    
    public List<ConditionalBlock> getConditionalBlocks() {
        return new ArrayList<>(conditionalBlocks);
    }
    
    @Override
    public String toString() {
        return "EventHandler{" + eventType + ", actions=" + actions.size() + 
               ", conditionals=" + conditionalBlocks.size() + "}";
    }
}
