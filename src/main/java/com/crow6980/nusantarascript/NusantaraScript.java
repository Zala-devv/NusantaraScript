package com.crow6980.nusantarascript;

import com.crow6980.nusantarascript.command.CustomCommandRegistry;
import com.crow6980.nusantarascript.commands.NusantaraCommand;
import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.manager.ScriptManager;
import com.crow6980.nusantarascript.manager.VariableManager;
import com.crow6980.nusantarascript.registry.EventRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * STEP 1: Main Plugin Class
 * 
 * NusantaraScript - Indonesian Language Scripting Plugin for Minecraft
 * 
 * This is the main entry point of the plugin. It handles:
 * - Plugin initialization and shutdown
 * - Creating the scripts directory structure
 * - Loading and managing script files
 * - Registering commands
 * 
 * @author crow6980
 */
public class NusantaraScript extends JavaPlugin {
    
    // Singleton instance for global access
    private static NusantaraScript instance;
    
    // Core managers
    private ScriptManager scriptManager;
    private EventRegistry eventRegistry;
    private VariableManager variableManager;
    private EnhancedScriptExecutor scriptExecutor;
    private CustomCommandRegistry customCommandRegistry;
    
    // Directory for script files
    private File scriptsFolder;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Step 1: Initialize the plugin directories
        initializeDirectories();
        
        // Step 2: Initialize core managers
        initializeManagers();
        
        // Step 3: Register commands
        registerCommands();
        
        // Step 4: Load all scripts from the scripts folder
        loadAllScripts();
        
        getLogger().info("===========================================");
        getLogger().info("NusantaraScript telah aktif!");
        getLogger().info("Indonesian Language Scripting is enabled!");
        getLogger().info("===========================================");
    }
    
    @Override
    public void onDisable() {
        // Unregister all dynamic event listeners
        if (eventRegistry != null) {
            eventRegistry.unregisterAll();
        }
        
        // Unregister all custom commands
        if (customCommandRegistry != null) {
            customCommandRegistry.unregisterAll();
        }
        
        getLogger().info("NusantaraScript telah dinonaktifkan!");
        getLogger().info("Thank you for using NusantaraScript!");
    }
    
    /**
     * Creates the necessary directory structure for the plugin
     * Creates: plugins/NusantaraScript/scripts/
     */
    private void initializeDirectories() {
        // Create main scripts folder
        scriptsFolder = new File(getDataFolder(), "scripts");
        
        if (!scriptsFolder.exists()) {
            if (scriptsFolder.mkdirs()) {
                getLogger().info("Created scripts directory: " + scriptsFolder.getPath());
                
                // Create a sample script file for users to learn from
                createSampleScript();
            }
        }
    }
    
    /**
     * Creates a sample .ns script file to help users get started
     */
    private void createSampleScript() {
        File sampleFile = new File(scriptsFolder, "contoh.ns");
        
        if (!sampleFile.exists()) {
            try {
                java.nio.file.Files.write(sampleFile.toPath(), java.util.Arrays.asList(
                    "# File Contoh NusantaraScript",
                    "# Hapus tanda # untuk mengaktifkan script",
                    "",
                    "# Event: Ketika pemain bergabung ke server",
                    "#saat pemain masuk:",
                    "#    kirim \"Selamat datang di server!\" ke pemain",
                    "#    kirim \"Halo %player%!\" ke pemain",
                    "",
                    "# Event: Ketika pemain menghancurkan blok",
                    "#saat blok dihancurkan:",
                    "#    kirim \"Kamu menghancurkan %block%!\" ke pemain",
                    "",
                    "# Lebih banyak fitur akan datang!",
                    ""
                ));
                getLogger().info("Created sample script: contoh.ns");
            } catch (Exception e) {
                getLogger().warning("Failed to create sample script: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initializes the core manager classes
     */
    private void initializeManagers() {
        variableManager = new VariableManager(this);
        scriptExecutor = new EnhancedScriptExecutor(this, variableManager);
        customCommandRegistry = new CustomCommandRegistry(this, scriptExecutor);
        eventRegistry = new EventRegistry(this, scriptExecutor);
        scriptManager = new ScriptManager(this, scriptsFolder, eventRegistry, customCommandRegistry);
    }
    
    /**
     * Registers plugin commands
     */
    private void registerCommands() {
        getCommand("nusantara").setExecutor(new NusantaraCommand(this, scriptManager));
    }
    
    /**
     * Loads all script files from the scripts directory
     */
    private void loadAllScripts() {
        int loaded = scriptManager.loadAllScripts();
        getLogger().info("Loaded " + loaded + " script(s) successfully!");
    }
    
    /**
     * Reloads all scripts (used by reload command)
     */
    public void reloadScripts() {
        // Unregister all existing listeners
        eventRegistry.unregisterAll();
        
        // Unregister all custom commands
        customCommandRegistry.unregisterAll();
        
        // Clear all variables
        variableManager.clearAll();
        
        // Reload scripts
        int loaded = scriptManager.loadAllScripts();
        getLogger().info("Reloaded " + loaded + " script(s) successfully!");
    }
    
    // Getters for managers
    public static NusantaraScript getInstance() {
        return instance;
    }
    
    public ScriptManager getScriptManager() {
        return scriptManager;
    }
    
    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }
    
    public VariableManager getVariableManager() {
        return variableManager;
    }
    
    public EnhancedScriptExecutor getScriptExecutor() {
        return scriptExecutor;
    }
    
    public CustomCommandRegistry getCustomCommandRegistry() {
        return customCommandRegistry;
    }
    
    public File getScriptsFolder() {
        return scriptsFolder;
    }
}
