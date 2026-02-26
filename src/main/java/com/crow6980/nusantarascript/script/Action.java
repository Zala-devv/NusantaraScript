package com.crow6980.nusantarascript.script;

import com.crow6980.nusantarascript.condition.ConditionalBlock;

/**
 * Represents a single action to be executed when an event fires
 * * @author crow6980
 */
public class Action {
    
    public enum ActionType {
    SEND_MESSAGE, BROADCAST, CANCEL_EVENT, HEAL_PLAYER, 
    FEED_PLAYER, SET_VARIABLE, ADD_VARIABLE, SUBTRACT_VARIABLE, 
    DELETE_VARIABLE, GIVE_ITEM, KICK_PLAYER, TELEPORT, 
    PLAY_SOUND, GIVE_EFFECT, NESTED_CONDITION, 
    STOP; // Add this line

        
        public boolean requiresPlayer() {
            return this == SEND_MESSAGE || this == HEAL_PLAYER || this == FEED_PLAYER ||
                   this == GIVE_ITEM || this == TELEPORT || this == KICK_PLAYER ||
                   this == PLAY_SOUND || this == GIVE_EFFECT;
        }
    }
    
    private final ActionType actionType;
    private final String parameter;
    private final String[] additionalParams;
    private final int lineNumber;
    private final ConditionalBlock nestedBlock; // Used for all block-based logic

    // Constructor for simple actions (e.g., batalkan event)
    public Action(ActionType type, int lineNumber) {
        this(type, (String) null, lineNumber);
    }

    // Constructor for actions with one parameter (e.g., broadcast "halo")
    public Action(ActionType actionType, String parameter, int lineNumber) {
        this.actionType = actionType;
        this.parameter = parameter;
        this.additionalParams = new String[0];
        this.lineNumber = lineNumber;
        this.nestedBlock = null;
    }
    
    // Constructor for actions with multiple parameters (e.g., beri_item)
    public Action(ActionType actionType, String parameter, String[] additionalParams, int lineNumber) {
        this.actionType = actionType;
        this.parameter = parameter;
        this.additionalParams = additionalParams != null ? additionalParams : new String[0];
        this.lineNumber = lineNumber;
        this.nestedBlock = null;
    }
    
    // Constructor for nested conditions (if/else logic)
    public Action(ActionType type, ConditionalBlock block, int lineNumber) {
        this.actionType = type;
        this.parameter = null;
        this.additionalParams = new String[0];
        this.nestedBlock = block; // This resolves the "block" variable from your parser
        this.lineNumber = lineNumber;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public String getParameter() {
        return parameter;
    }
    
    public String[] getAdditionalParams() {
        return additionalParams;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public ConditionalBlock getNestedBlock() {
        return nestedBlock;
    }
    
    @Override
    public String toString() {
        return "Action{" + actionType + ", param='" + parameter + "', line=" + lineNumber + ", nestedBlock=" + nestedBlock + "}";
    }
}