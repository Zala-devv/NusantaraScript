package com.crow6980.nusantarascript.manager;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.command.CustomCommand;
import com.crow6980.nusantarascript.command.CustomCommandRegistry;
import com.crow6980.nusantarascript.parser.ScriptParser;
import com.crow6980.nusantarascript.registry.EventRegistry;
import com.crow6980.nusantarascript.script.Script;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * STEP 2: Script Manager & File Reader
 * 
 * Responsible for:
 * - Discovering .ns files in the scripts directory
 * - Reading script files line by line
 * - Passing file contents to the parser
 * - Managing loaded scripts
 * 
 * @author crow6980
 */
public class ScriptManager {
    
    private final NusantaraScript plugin;
    private final File scriptsFolder;
    private final EventRegistry eventRegistry;
    private final CustomCommandRegistry customCommandRegistry;
    private final ScriptParser parser;
    
    // Map to store loaded scripts: filename -> Script object
    private final Map<String, Script> loadedScripts;
    
    public ScriptManager(NusantaraScript plugin, File scriptsFolder, EventRegistry eventRegistry, CustomCommandRegistry customCommandRegistry) {
        this.plugin = plugin;
        this.scriptsFolder = scriptsFolder;
        this.eventRegistry = eventRegistry;
        this.customCommandRegistry = customCommandRegistry;
        this.parser = new ScriptParser(plugin);
        this.loadedScripts = new HashMap<>();
    }
    
    /**
     * Loads all .ns files from the scripts directory
     * @return Number of scripts successfully loaded
     */
    public int loadAllScripts() {
        // Clear previously loaded scripts
        loadedScripts.clear();
        
        // Find all .ns files
        File[] scriptFiles = scriptsFolder.listFiles((dir, name) -> name.endsWith(".ns"));
        
        if (scriptFiles == null || scriptFiles.length == 0) {
            plugin.getLogger().info("No .ns script files found in " + scriptsFolder.getPath());
            return 0;
        }
        
        int successCount = 0;
        
        // Load each script file
        for (File file : scriptFiles) {
            try {
                if (loadScript(file)) {
                    successCount++;
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading script " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return successCount;
    }
    
    /**
     * Loads a single script file
     * @param file The .ns file to load
     * @return true if loaded successfully
     */
    private boolean loadScript(File file) {
        plugin.getLogger().info("Loading script: " + file.getName());
        
        try {
            // Read all lines from the file
            List<String> lines = readScriptFile(file);
            
            if (lines.isEmpty()) {
                plugin.getLogger().warning("Script " + file.getName() + " is empty, skipping...");
                return false;
            }
            
            // Parse the script content
            Script script = parser.parse(file.getName(), lines);
            
            if (script == null) {
                plugin.getLogger().warning("Failed to parse script: " + file.getName());
                return false;
            }
            
            // Store the loaded script
            loadedScripts.put(file.getName(), script);
            
            // Register events used by this script
            eventRegistry.registerScript(script);
            
            // Register custom commands from this script
            for (CustomCommand command : script.getCustomCommands()) {
                customCommandRegistry.registerCommand(command);
            }
            
            plugin.getLogger().info("Successfully loaded script: " + file.getName() + 
                                   " (Events: " + script.getEventHandlers().size() + 
                                   ", Commands: " + script.getCustomCommands().size() + ")");
            
            return true;
            
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to read script file " + file.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reads a script file and returns its lines
     * Handles:
     * - UTF-8 encoding for Indonesian characters
     * - Preserves indentation for Phase 2 parser
     * - Keeps comments for parser to handle
     * 
     * @param file The file to read
     * @return List of all lines (parser will handle comments/empty lines)
     */
    private List<String> readScriptFile(File file) throws IOException {
        // Phase 2: Return all lines with original indentation
        // The enhanced parser will handle comments and empty lines
        return Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
    }
    
    /**
     * Gets a loaded script by filename
     */
    public Script getScript(String filename) {
        return loadedScripts.get(filename);
    }
    
    /**
     * Gets all loaded scripts
     */
    public Map<String, Script> getLoadedScripts() {
        return new HashMap<>(loadedScripts);
    }
    
    /**
     * Gets the number of loaded scripts
     */
    public int getLoadedScriptCount() {
        return loadedScripts.size();
    }
}
