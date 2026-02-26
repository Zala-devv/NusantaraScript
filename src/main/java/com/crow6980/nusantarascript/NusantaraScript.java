package com.crow6980.nusantarascript;

import com.crow6980.nusantarascript.command.CustomCommandRegistry;
import com.crow6980.nusantarascript.commands.NusantaraCommand;
import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.manager.ScriptManager;
import com.crow6980.nusantarascript.manager.VariableManager;
import com.crow6980.nusantarascript.registry.EventRegistry;
import com.crow6980.nusantarascript.registry.ScriptEventListener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class NusantaraScript extends JavaPlugin {
    
    private static NusantaraScript instance;
    
    private ScriptManager scriptManager;
    private EventRegistry eventRegistry;
    private VariableManager variableManager;
    private EnhancedScriptExecutor scriptExecutor;
    private CustomCommandRegistry customCommandRegistry;
    private boolean debugMode = false;
    private File scriptsFolder;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 1. First, setup the folders (This fixes the 'never used' warning)
        initializeDirectories();
        
        // 2. Then, setup the logic managers
        initializeManagers();
        
        // 3. Register Global Listeners
        // Main class onEnable
        getServer().getPluginManager().registerEvents(new ScriptEventListener(this, scriptExecutor), this);
        
        // 4. Register Commands
        getCommand("nusantara").setExecutor(new NusantaraCommand(this, scriptManager));
        
        // 5. Load everything
        reloadScripts();
        
        getLogger().info("NusantaraScript has been enabled successfully!");
    }

    /**
     * Creates the necessary directory structure and sample files
     */
    private void initializeDirectories() {
        // Define the folder
        scriptsFolder = new File(getDataFolder(), "scripts");
        
        // Create if missing
        if (!scriptsFolder.exists()) {
            if (scriptsFolder.mkdirs()) {
                getLogger().info("Created scripts directory: " + scriptsFolder.getPath());
            }
        }
        
        // CALL THE METHOD HERE - This removes the 'never used' warning
        createSampleScript();
    }

    private void initializeManagers() {
        this.variableManager = new VariableManager(this);
        this.scriptExecutor = new EnhancedScriptExecutor(this, variableManager);
        this.customCommandRegistry = new CustomCommandRegistry(this, scriptExecutor);
        this.eventRegistry = new EventRegistry(this, scriptExecutor);
        
        // Use the scriptsFolder we initialized in step 1
        this.scriptManager = new ScriptManager(this, scriptsFolder, eventRegistry, customCommandRegistry);
    }

    public void reloadScripts() {
        if (eventRegistry != null) eventRegistry.clear();
        if (customCommandRegistry != null) customCommandRegistry.unregisterAll();
        if (variableManager != null) {
            variableManager.saveVariables();
            variableManager.clearAll();
        }
        
        int loaded = scriptManager.loadAllScripts();
        getLogger().info("Berhasil memuat " + loaded + " skrip!");
    }

    @Override
    public void onDisable() {
        if (eventRegistry != null) eventRegistry.unregisterAll();
        if (customCommandRegistry != null) customCommandRegistry.unregisterAll();
        if (variableManager != null) variableManager.saveVariables();
    }

    // Getters
    public static NusantaraScript getInstance() { return instance; }
    public EventRegistry getEventRegistry() { return eventRegistry; }
    public VariableManager getVariableManager() { return variableManager; }
    public EnhancedScriptExecutor getExecutor() { return scriptExecutor; }
    public CustomCommandRegistry getCustomCommandRegistry() { return customCommandRegistry; }
    public boolean isDebugEnabled() { return debugMode; }
    public boolean toggleDebug() { return debugMode = !debugMode; }
    public File getScriptsFolder() { return scriptsFolder; }

/**
     * Creates sample .ns script files to help users get started
     */
    private void createSampleScript() {
        File sampleFile = new File(scriptsFolder, "contoh.ns");
        File stressTestFile = new File(scriptsFolder, "stress_test.ns");
        File welcomeFile = new File(scriptsFolder, "welcome_counter.ns");

        // 1. Basic Example
        if (!sampleFile.exists()) {
            try {
                java.nio.file.Files.write(sampleFile.toPath(), java.util.Arrays.asList(
                    "# File Contoh NusantaraScript",
                    "# Edit file ini untuk mencoba fitur dasar",
                    "",
                    "saat pemain masuk:",
                    "    kirim \"&aSelamat datang di server!\" ke pemain",
                    "    kirim \"&fHalo &b%player%&f, selamat bermain!\" ke pemain",
                    "",
                    "saat blok dihancurkan:",
                    "    jika blok adalah \"DIAMOND_ORE\":",
                    "        broadcast \"&b✦ %player% menemukan diamond!\"",
                    "        tambah 1 ke variabel {diamond.%player%}",
                    "    jika pemain punya izin \"nusantara.vip\":",
                    "        kirim \"&6[VIP] &aBonus XP diberikan!\" ke pemain"
                ), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                getLogger().warning("Failed to create contoh.ns");
            }
        }

        // 2. Full Stress Test (Indentation & Logic Check)
        if (!stressTestFile.exists()) {
            try {
                java.nio.file.Files.write(stressTestFile.toPath(), java.util.Arrays.asList(
                    "# NusantaraScript - Stress Test System",
                    "# Menguji: Event, Tool Check, Variabel, dan Indentasi",
                    "",
                    "saat blok dihancurkan:",
                    "    jika alat benar:",
                    "        tambah 1 ke variabel {skor.%pemain%}",
                    "        kirim \"&a[NS] Alat benar! Skor kamu: {skor.%pemain%}\" ke pemain",
                    "        ",
                    "        # Menguji Nested If (If di dalam If)",
                    "        jika {skor.%pemain%} lebih dari 10:",
                    "            kirim \"&6&l[NS] HEBAT! Kamu penambang ahli!\" ke pemain",
                    "            suara \"entity.player.levelup\" ke pemain",
                    "            setel {skor.%pemain%} = 0",
                    "            ",
                    "    jika tidak:",
                    "        kirim \"&c[NS] Gunakan alat yang benar!\" ke pemain",
                    "        batalkan event",
                    "        ",
                    "        # Menguji pengecekan status",
                    "        jika pemain sedang menyelinap:",
                    "            kirim \"&7(Psst, kamu menghancurkan blok sambil jongkok!)\" ke pemain"
                ), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                getLogger().warning("Failed to create stress_test.ns");
            }
        }

        // 3. Variables & Persistence Example
        if (!welcomeFile.exists()) {
            try {
                java.nio.file.Files.write(welcomeFile.toPath(), java.util.Arrays.asList(
                    "# Welcome Counter Example",
                    "# Menghitung berapa kali pemain sudah login",
                    "",
                    "saat pemain masuk:",
                    "    tambah 1 ke variabel {kunjungan.%player%}",
                    "    kirim \"&aSelamat datang kembali, %player%!\" ke pemain",
                    "    kirim \"&7Ini adalah kunjungan ke-&e{kunjungan.%player%}&7 kamu!\" ke pemain",
                    "    ",
                    "    jika {kunjungan.%player%} adalah 1:",
                    "        broadcast \"&d&l[!] &fSambut member baru kita: &b%player%!\""
                ), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                getLogger().warning("Failed to create welcome_counter.ns");
            }
        }
    }
}