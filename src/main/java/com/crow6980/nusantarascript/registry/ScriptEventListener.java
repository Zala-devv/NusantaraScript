package com.crow6980.nusantarascript.registry;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import com.crow6980.nusantarascript.script.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;

public class ScriptEventListener implements Listener {
    private final EnhancedScriptExecutor executor;
    private final NusantaraScript plugin;

    public ScriptEventListener(NusantaraScript plugin, EnhancedScriptExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
    }

    @org.bukkit.event.EventHandler
    public void onJoin(PlayerJoinEvent event) {
        execute(EventHandler.EventType.PLAYER_JOIN, event.getPlayer(), event, null);
    }

    @org.bukkit.event.EventHandler
    public void onQuit(PlayerQuitEvent event) {
        execute(EventHandler.EventType.PLAYER_QUIT, event.getPlayer(), event, null);
    }

    @org.bukkit.event.EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("block", event.getBlock());
        // Context for 'jika alat benar'
        extra.put("alat_benar", event.getBlock().isPreferredTool(event.getPlayer().getInventory().getItemInMainHand()));
        
        execute(EventHandler.EventType.BLOCK_BREAK, event.getPlayer(), event, extra);
    }

    @org.bukkit.event.EventHandler
    public void onChat(AsyncChatEvent event) {
        Map<String, Object> extra = new HashMap<>();
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        extra.put("message", rawMessage);
        
        Player player = event.getPlayer();
        // Return to Main Thread for script execution safety
        Bukkit.getScheduler().runTask(plugin, () -> {
            execute(EventHandler.EventType.PLAYER_CHAT, player, event, extra);
        });
    }

    @org.bukkit.event.EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Map<String, Object> extra = new HashMap<>();
        String message = event.deathMessage() != null ? 
            PlainTextComponentSerializer.plainText().serialize(event.deathMessage()) : "";
        extra.put("message", message);
        
        execute(EventHandler.EventType.PLAYER_DEATH, event.getEntity(), event, extra);
    }

    @org.bukkit.event.EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        execute(EventHandler.EventType.PLAYER_RESPAWN, event.getPlayer(), event, null);
    }

    @org.bukkit.event.EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Map<String, Object> extra = new HashMap<>();
        extra.put("damage", event.getDamage());
        extra.put("cause", event.getCause().name());
        
        execute(EventHandler.EventType.PLAYER_DAMAGE, player, event, extra);
    }

    /**
     * Centralized execution bridge
     */
    private void execute(EventHandler.EventType type, Player player, org.bukkit.event.Event event, Map<String, Object> extra) {
        Map<String, Object> context = new HashMap<>();
        context.put("player", player);
        context.put("event", event);
        if (extra != null) context.putAll(extra);
        
        // This method must exist in EnhancedScriptExecutor
        executor.executeEvent(type, context);
    }
}