package com.crow6980.nusantarascript.script;

import com.crow6980.nusantarascript.condition.ConditionalBlock;

/**
 * Represents a single action to be executed when an event fires
 * 
 * @author crow6980
 */
public class Action {
    
    /**
     * Supported action types
     * Maps Indonesian actions to Bukkit API calls
     */
    public enum ActionType {
        SEND_MESSAGE,       // kirim "text" ke pemain
        BROADCAST,          // broadcast "text"
        CANCEL_EVENT,       // batalkan event
        HEAL_PLAYER,        // pulihkan pemain
        FEED_PLAYER,        // beri makan pemain
        SET_VARIABLE,       // atur variabel {name} menjadi "value" (juga mendukung syntax "setel {name} = value")
        ADD_VARIABLE,       // tambah NUMBER ke variabel {name}
        SUBTRACT_VARIABLE,  // kurangi NUMBER dari variabel {name}
        DELETE_VARIABLE,    // hapus variabel {name}
        GIVE_ITEM,          // berikan MATERIAL NUMBER ke pemain
        TELEPORT,           // teleportasi pemain ke X Y Z
        KICK_PLAYER,        // keluarkan pemain dengan alasan "reason"
        PLAY_SOUND,         // mainkan suara "SOUND" ke pemain
        GIVE_EFFECT,        // berikan efek "EFFECT" level NUMBER durasi NUMBER ke pemain
        CUSTOM,             // special: used for internal parser logic (e.g., elseif/else blocks)
        NESTED_CONDITION;   // for nested if/else blocks
        
        /**
         * Checks if this action type requires a player in context
         */
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
    private final ConditionalBlock nestedBlock;
    
    public Action(ActionType actionType, String parameter, int lineNumber) {
        this.actionType = actionType;
        this.parameter = parameter;
        this.additionalParams = new String[0];
        this.lineNumber = lineNumber;
        this.nestedBlock = null;
    }
    
    public Action(ActionType actionType, String parameter, String[] additionalParams, int lineNumber) {
        this.actionType = actionType;
        this.parameter = parameter;
        this.additionalParams = additionalParams != null ? additionalParams : new String[0];
        this.lineNumber = lineNumber;
        this.nestedBlock = null;
    }
    
    public Action(ActionType type, ConditionalBlock block, int lineNumber) {
        this.actionType = type;
        this.parameter = null;
        this.additionalParams = null;
        this.nestedBlock = block;
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
        return "Action{" + actionType + ", param='" + parameter + "'}";
    }
}
