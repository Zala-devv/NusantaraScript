package com.crow6980.nusantarascript.condition;

import com.crow6980.nusantarascript.script.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * PHASE 2 - STEP 1: Conditional Block
 * 
 * Represents a "jika" (if) block containing:
 * - A condition to check
 * - A list of actions to execute if condition is true
 * 
 * Example:
 *     jika blok adalah "DIAMOND_ORE":
 *         broadcast "Diamond found!"
 *         kirim "Congratulations!" ke pemain
 * 
 * @author crow6980
 */
public class ConditionalBlock {
    
    private final Condition condition;
    private final List<Action> actions;
    private final List<Action> elseActions; // phase3
    private final int lineNumber;
    
    public ConditionalBlock(Condition condition, int lineNumber) {
        this.condition = condition;
        this.actions = new ArrayList<>();
        this.elseActions = new ArrayList<>();
        this.lineNumber = lineNumber;
    }
    
    public void addAction(Action action) {
        actions.add(action);
    }
    
    public void addElseAction(Action action) {
        elseActions.add(action);
    }
    
    public Condition getCondition() {
        return condition;
    }
    
    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }
    
    public List<Action> getElseActions() {
        return new ArrayList<>(elseActions);
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    @Override
    public String toString() {
        return "ConditionalBlock{condition=" + condition.getClass().getSimpleName() + 
               ", actions=" + actions.size() + "}";
    }
}
