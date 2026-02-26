package com.crow6980.nusantarascript.script;

import com.crow6980.nusantarascript.condition.ConditionalBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ActionExecutor {

    public static void execute(Action action, Map<String, Object> context) {
        Object playerObj = context.get("player");
        Player player = (playerObj instanceof Player) ? (Player) playerObj : null;
        
        switch (action.getActionType()) {
            case SEND_MESSAGE:
                if (player != null && action.getParameter() != null) {
                    // Modern replacement for player.sendMessage(String)
                    player.sendMessage(action.getParameter());
                }
                break;

            case BROADCAST:
                if (action.getParameter() != null) {
                    // Modern replacement for Bukkit.broadcastMessage(String)
                    // Instead of Component.text(parameter)
                    Bukkit.broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(action.getParameter()));
                }
                break;

            case PLAY_SOUND:
                if (player != null && action.getParameter() != null) {
                    try {
                        // 1. Convert input to a NamespacedKey format (e.g., entity_player_level_up)
                        // This handles both "ENTITY_PLAYER_LEVELUP" and "entity.player.levelup"
                        String soundKey = action.getParameter().toLowerCase()
                                            .replace("_", ".")
                                            .trim();
                        
                        // 2. Use the Registry to find the sound without using deprecated .valueOf()
                        org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(soundKey);
                        Sound sound = org.bukkit.Registry.SOUND_EVENT.get(key);

                        if (sound != null) {
                            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                        } else {
                            // Fallback: Try the old Enum way if Registry fails, 
                            // but suppress the warning specifically here.
                            @SuppressWarnings("deprecation")
                            Sound fallback = Sound.valueOf(action.getParameter().toUpperCase());
                            player.playSound(player.getLocation(), fallback, 1.0f, 1.0f);
                        }
                    } catch (Exception ignored) {
                        // Sound not found or invalid format
                    }
                }
                break;

            case GIVE_ITEM:
                if (player != null && action.getParameter() != null) {
                    Material mat = Material.matchMaterial(action.getParameter().toUpperCase());
                    int amount = 1;
                    if (action.getAdditionalParams() != null && action.getAdditionalParams().length > 0) {
                        try {
                            amount = Integer.parseInt(action.getAdditionalParams()[0]);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (mat != null) {
                        player.getInventory().addItem(new ItemStack(mat, amount));
                    }
                }
                break;

            case CANCEL_EVENT:
                context.put("cancelled", true);
                break;

            case HEAL_PLAYER:
                if (player != null) {
                    // Basic heal logic
                    player.setHealth(Math.min(player.getHealth() + 20, 20.0));
                }
                break;

            case STOP:
                context.put("stop_execution", true);
                break;

            case NESTED_CONDITION:
                ConditionalBlock block = action.getNestedBlock();
                if (block != null && block.getCondition() != null) {
                    if (block.getCondition().evaluate(context)) {
                        for (Action subAction : block.getActions()) {
                            execute(subAction, context);
                        }
                    } else {
                        for (Action elseAction : block.getElseActions()) {
                            execute(elseAction, context);
                        }
                    }
                }
                break;

            default:
                break;
        }
    }
}