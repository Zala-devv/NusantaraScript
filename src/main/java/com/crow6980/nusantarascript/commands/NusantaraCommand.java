package com.crow6980.nusantarascript.commands;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.manager.ScriptManager;
import com.crow6980.nusantarascript.script.EventHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main command handler for /nusantara
 * 
 * Commands:
 * - /nusantara reload - Reload all scripts
 * - /nusantara list - List loaded scripts
 * - /nusantara info - Show plugin information
 * 
 * @author crow6980
 */
public class NusantaraCommand implements CommandExecutor, TabCompleter {
    
    private final NusantaraScript plugin;
    private final ScriptManager scriptManager;
    
    public NusantaraCommand(NusantaraScript plugin, ScriptManager scriptManager) {
        this.plugin = plugin;
        this.scriptManager = scriptManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nusantarascript.admin")) {
            sender.sendMessage("§cKamu tidak memiliki izin untuk menggunakan perintah ini!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
                
            case "list":
                handleList(sender);
                break;
                
            case "info":
                handleInfo(sender);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        sender.sendMessage("§eMemuat ulang semua skrip...");
        
        long startTime = System.currentTimeMillis();
        plugin.reloadScripts();
        long endTime = System.currentTimeMillis();
        
        sender.sendMessage("§aSelesai! §7(" + (endTime - startTime) + "ms)");
        sender.sendMessage("§aDimuat: §f" + scriptManager.getLoadedScriptCount() + " skrip");
    }
    
    private void handleList(CommandSender sender) {
        Map<String, com.crow6980.nusantarascript.script.Script> scripts = scriptManager.getLoadedScripts();
        
        if (scripts.isEmpty()) {
            sender.sendMessage("§cTidak ada skrip yang dimuat.");
            sender.sendMessage("§7Letakkan file .ns di folder: §f" + plugin.getScriptsFolder().getPath());
            return;
        }
        
        sender.sendMessage("§e§l=== Daftar Skrip ===");
        for (var entry : scripts.entrySet()) {
            var script = entry.getValue();
            sender.sendMessage("§7- §f" + entry.getKey() + " §7(" + script.getEventHandlers().size() + " event handlers)");
        }
    }
    
    private void handleInfo(CommandSender sender) {
        sender.sendMessage("§e§l=== NusantaraScript ===");
        sender.sendMessage("§7Plugin scripting untuk Minecraft dalam Bahasa Indonesia");
        sender.sendMessage("");
        sender.sendMessage("§eVersi: §f" + plugin.getPluginMeta().getVersion());
        sender.sendMessage("§eAuthor: §f" + plugin.getPluginMeta().getAuthors().get(0));
        sender.sendMessage("§eSkript Dimuat: §f" + scriptManager.getLoadedScriptCount());
        
        // Show event statistics
        var stats = plugin.getEventRegistry().getEventStatistics();
        if (!stats.isEmpty()) {
            sender.sendMessage("");
            sender.sendMessage("§eEvent Terdaftar:");
            for (Map.Entry<EventHandler.EventType, Integer> entry : stats.entrySet()) {
                sender.sendMessage("  §7- §f" + entry.getKey() + " §7(x" + entry.getValue() + ")");
            }
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e§l=== NusantaraScript Commands ===");
        sender.sendMessage("§7/nusantara reload §f- Muat ulang semua skrip");
        sender.sendMessage("§7/nusantara list §f- Daftar skrip yang dimuat");
        sender.sendMessage("§7/nusantara info §f- Informasi plugin");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "list", "info");
        }
        return new ArrayList<>();
    }
}
