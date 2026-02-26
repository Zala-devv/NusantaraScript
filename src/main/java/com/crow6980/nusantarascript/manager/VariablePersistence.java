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

    public void load(Map<String, Object> global, Map<String, Map<String, Object>> player) {
        global.clear();
        player.clear();
        Map<String, Object> loadedGlobal = (Map<String, Object>) config.getConfigurationSection("global").getValues(false);
        if (loadedGlobal != null) global.putAll(loadedGlobal);
        if (config.isConfigurationSection("player")) {
            for (String pname : config.getConfigurationSection("player").getKeys(false)) {
                Map<String, Object> vars = (Map<String, Object>) config.getConfigurationSection("player." + pname).getValues(false);
                player.put(pname, new ConcurrentHashMap<>(vars));
            }
        }
    }
}
