package com.crow6980.nusantarascript.command;

import com.crow6980.nusantarascript.script.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * PHASE 2 - STEP 4: Custom Command Model
 * 
 * Represents a custom command defined in a script.
 * 
 * Syntax:
 *     perintah /commandname:
 *         izin: "permission.node"
 *         aksi:
 *             kirim "Hello!" ke pemain
 *             pulihkan pemain
 * 
 * @author crow6980
 */
public class CustomCommand {
    
    private final String name;
    private final String permission;
    private final List<Action> actions;
    private final String description;
    private final int lineNumber;
    
    public CustomCommand(String name, int lineNumber) {
        this.name = name;
        this.permission = null;
        this.actions = new ArrayList<>();
        this.description = "Custom command from NusantaraScript";
        this.lineNumber = lineNumber;
    }
    
    public CustomCommand(String name, String permission, String description, int lineNumber) {
        this.name = name;
        this.permission = permission;
        this.actions = new ArrayList<>();
        this.description = description != null ? description : "Custom command from NusantaraScript";
        this.lineNumber = lineNumber;
    }
    
    public void addAction(Action action) {
        actions.add(action);
    }
    
    public String getName() {
        return name;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    @Override
    public String toString() {
        return "CustomCommand{name='" + name + "', permission='" + permission + 
               "', actions=" + actions.size() + "}";
    }
}
