package com.crow6980.nusantarascript.manager;

import com.crow6980.nusantarascript.NusantaraScript;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
// import java.util.HashMap; // Unused, remove
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles persistence for VariableManager (load/save variables to variables.yml)
 */
public class VariablePersistence {
    private final NusantaraScript plugin;
    private final File file;
    private final YamlConfiguration config;

    public VariablePersistence(NusantaraScript plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "variables.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void save(Map<String, Object> global, Map<String, Map<String, Object>> player) {
        config.set("global", global);
        config.set("player", player);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save variables.yml: " + e.getMessage());
        }
    }

    // Inside your load method in VariablePersistence.java

    public void load(Map<String, Object> global, Map<String, Map<String, Object>> player) {
        if (config.contains("global")) {
            Map<String, Object> loadedGlobal = config.getConfigurationSection("global").getValues(false);
            global.clear();
            global.putAll(loadedGlobal);
        }
        if (config.contains("player")) {
            Map<String, Map<String, Object>> loadedPlayer = new ConcurrentHashMap<>();
            for (String playerId : config.getConfigurationSection("player").getKeys(false)) {
                Map<String, Object> vars = config.getConfigurationSection("player." + playerId).getValues(false);
                loadedPlayer.put(playerId, vars);
            }
            player.clear();
            player.putAll(loadedPlayer);
        }
    }
}