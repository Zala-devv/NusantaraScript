package com.crow6980.nusantarascript.manager;

import com.crow6980.nusantarascript.NusantaraScript;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.crow6980.nusantarascript.manager.VariablePersistence;

/**
 * PHASE 2 - STEP 3: Variable Manager
 * 
 * Manages global and player-specific variables for scripts.
 * Variables are stored in-memory using HashMap structures.
 * 
 * Variable Syntax:
 * - {variableName} for global variables
 * - {variableName.%player%} for player-specific variables
 * 
 * Examples:
 * - {serverStartTime}
 * - {kunjungan.%player%}
 * - {saldo.%player%}
 * 
 * @author crow6980
 */
public class VariableManager {
    
    private final NusantaraScript plugin;
    private final Map<String, Object> globalVariables;
    private final Map<String, Map<String, Object>> playerVariables;
    private final VariablePersistence persistence;
    
    public VariableManager(NusantaraScript plugin) {
        this.plugin = plugin;
        this.globalVariables = new ConcurrentHashMap<>();
        this.playerVariables = new ConcurrentHashMap<>();
        this.persistence = new VariablePersistence(plugin);
        loadVariables();
    }
        /**
         * Saves all variables to disk (variables.yml)
         */
        public void saveVariables() {
            persistence.save(globalVariables, playerVariables);
            plugin.getLogger().info("Variables saved to variables.yml");
        }

        /**
         * Loads all variables from disk (variables.yml)
         */
        public void loadVariables() {
            persistence.load(globalVariables, playerVariables);
            plugin.getLogger().info("Variables loaded from variables.yml");
        }
    
    /**
     * Sets a global variable
     * 
     * @param name Variable name
     * @param value Variable value
     */
    public void setGlobal(String name, Object value) {
        globalVariables.put(name, value);
        plugin.getLogger().fine("Set global variable: " + name + " = " + value);
    }
    
    /**
     * Gets a global variable
     * 
     * @param name Variable name
     * @return Variable value, or null if not found
     */
    public Object getGlobal(String name) {
        return globalVariables.get(name);
    }
    
    /**
     * Sets a player-specific variable
     * 
     * @param playerName Player name
     * @param variableName Variable name
     * @param value Variable value
     */
    public void setPlayer(String playerName, String variableName, Object value) {
        playerVariables.computeIfAbsent(playerName, k -> new ConcurrentHashMap<>())
                       .put(variableName, value);
        plugin.getLogger().fine("Set player variable: " + playerName + "." + variableName + " = " + value);
    }
    
    /**
     * Gets a player-specific variable
     * 
     * @param playerName Player name
     * @param variableName Variable name
     * @return Variable value, or null if not found
     */
    public Object getPlayer(String playerName, String variableName) {
        Map<String, Object> playerVars = playerVariables.get(playerName);
        if (playerVars == null) {
            return null;
        }
        return playerVars.get(variableName);
    }
    
    /**
     * Deletes a global variable
     * 
     * @param name Variable name
     */
    public void deleteGlobal(String name) {
        globalVariables.remove(name);
    }
    
    /**
     * Deletes a player-specific variable
     * 
     * @param playerName Player name
     * @param variableName Variable name
     */
    public void deletePlayer(String playerName, String variableName) {
        Map<String, Object> playerVars = playerVariables.get(playerName);
        if (playerVars != null) {
            playerVars.remove(variableName);
        }
    }
    
    /**
     * Deletes all variables for a player
     * 
     * @param playerName Player name
     */
    public void deleteAllPlayer(String playerName) {
        playerVariables.remove(playerName);
    }
    
    /**
     * Adds a numeric value to a variable (creates it if doesn't exist)
     * Useful for counters and statistics
     * 
     * @param playerName Player name (null for global)
     * @param variableName Variable name
     * @param amount Amount to add
     */
    public void add(String playerName, String variableName, double amount) {
        if (playerName == null) {
            // Global variable
            Object current = globalVariables.get(variableName);
            double newValue = (current instanceof Number) ? ((Number) current).doubleValue() + amount : amount;
            globalVariables.put(variableName, newValue);
        } else {
            // Player variable
            Map<String, Object> playerVars = playerVariables.computeIfAbsent(playerName, k -> new ConcurrentHashMap<>());
            Object current = playerVars.get(variableName);
            double newValue = (current instanceof Number) ? ((Number) current).doubleValue() + amount : amount;
            playerVars.put(variableName, newValue);
        }
    }
    
    /**
     * Subtracts a numeric value from a variable
     * 
     * @param playerName Player name (null for global)
     * @param variableName Variable name
     * @param amount Amount to subtract
     */
    public void subtract(String playerName, String variableName, double amount) {
        add(playerName, variableName, -amount);
    }
    
    /**
     * Gets all global variables (for debugging/info commands)
     */
    public Map<String, Object> getAllGlobal() {
        return new HashMap<>(globalVariables);
    }
    
    /**
     * Gets all variables for a specific player
     */
    public Map<String, Object> getAllPlayer(String playerName) {
        Map<String, Object> playerVars = playerVariables.get(playerName);
        return playerVars == null ? new HashMap<>() : new HashMap<>(playerVars);
    }
    
    /**
     * Clears all variables (useful for reload)
     */
    public void clearAll() {
        saveVariables();
        globalVariables.clear();
        playerVariables.clear();
        plugin.getLogger().info("All variables cleared and saved");
    }
    
    /**
     * Gets the number of stored variables
     */
    public int getVariableCount() {
        int count = globalVariables.size();
        for (Map<String, Object> playerVars : playerVariables.values()) {
            count += playerVars.size();
        }
        return count;
    }
}
