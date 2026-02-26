package com.crow6980.nusantarascript.registry;

import com.crow6980.nusantarascript.script.EventHandler;
import com.crow6980.nusantarascript.execution.EnhancedScriptExecutor;
import org.bukkit.entity.Player;
// Removed duplicate import, use fully qualified annotation below
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.HashMap;
import java.util.Map;

public class ScriptEventListener implements Listener {
    private final EnhancedScriptExecutor executor;

    public ScriptEventListener(EnhancedScriptExecutor executor) {
        this.executor = executor;
    }

    @org.bukkit.event.EventHandler
    @SuppressWarnings("deprecation")
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Map<String, Object> context = new HashMap<>();
        context.put("player", player);
        context.put("message", event.getDeathMessage()); // deprecated, but used for compatibility
        executor.executeEvent(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_DEATH, context);
    }

    @org.bukkit.event.EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Map<String, Object> context = new HashMap<>();
        context.put("player", player);
        executor.executeEvent(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_RESPAWN, context);
    }

    @org.bukkit.event.EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            Map<String, Object> context = new HashMap<>();
            context.put("player", player);
            context.put("damage", event.getDamage());
            context.put("cause", event.getCause().name());
            executor.executeEvent(com.crow6980.nusantarascript.script.EventHandler.EventType.PLAYER_DAMAGE, context);
        } else {
            Map<String, Object> context = new HashMap<>();
            context.put("entity", event.getEntity());
            context.put("damage", event.getDamage());
            context.put("cause", event.getCause().name());
            executor.executeEvent(com.crow6980.nusantarascript.script.EventHandler.EventType.ENTITY_DAMAGE, context);
        }
    }
}
