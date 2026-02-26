package com.crow6980.nusantarascript.execution;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.condition.ConditionalBlock;
import com.crow6980.nusantarascript.manager.VariableManager;
import com.crow6980.nusantarascript.script.Action;



import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PHASE 2 - Enhanced Execution Logic
 * Supports: Variable replacement, Conditional logic, Sound, Teleport, and Effects.
 */
public class EnhancedScriptExecutor {

    private final NusantaraScript plugin;
    private final VariableManager variableManager;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)}");

    public EnhancedScriptExecutor(NusantaraScript plugin, VariableManager variableManager) {
        this.plugin = plugin;
        this.variableManager = variableManager;
    }

    /**
     * Entry point for Listeners to trigger script logic.
     */
    public void executeHandler(com.crow6980.nusantarascript.script.EventHandler handler, Map<String, Object> context) {
        context.put("variableManager", variableManager);
        try {
            for (Action action : handler.getActions()) {
                if (action.getActionType() == Action.ActionType.STOP) return;
                executeAction(action, context);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing script handler: " + e.getMessage());
        }
    }
    /**
     * Finds and executes all script handlers for a specific event type.
     * This fixes the "undefined" error in your listener/registry classes.
     */
    public void executeEvent(com.crow6980.nusantarascript.script.EventHandler.EventType eventType, Map<String, Object> context) {
        // 1. Get the list of handlers for this specific event from the registry
        var handlers = plugin.getEventRegistry().getHandlers(eventType);
        
        if (handlers == null || handlers.isEmpty()) {
            return;
        }

        // 2. Loop through every script that wants to run for this event
        for (com.crow6980.nusantarascript.script.EventHandler handler : handlers) {
            // 3. Use your existing executeHandler method to run the actions
            executeHandler(handler, context);
        }
    }
    public void executeAction(Action action, Map<String, Object> context) {
        switch (action.getActionType()) {
            case SEND_MESSAGE -> executeSendMessage(action, context);
            case BROADCAST -> executeBroadcast(action, context);
            case CANCEL_EVENT -> handleCancelAction(context); // FIXED: Linked to method
            case HEAL_PLAYER -> executeHealPlayer(action, context);
            case FEED_PLAYER -> executeFeedPlayer(action, context);
            case SET_VARIABLE -> executeSetVariable(action, context);
            case ADD_VARIABLE -> executeAddVariable(action, context);
            case SUBTRACT_VARIABLE -> executeSubtractVariable(action, context);
            case DELETE_VARIABLE -> executeDeleteVariable(action, context);
            case GIVE_ITEM -> executeGiveItem(action, context);
            case KICK_PLAYER -> executeKickPlayer(action, context);
            case TELEPORT -> executeTeleport(action, context);
            case PLAY_SOUND -> executePlaySound(action, context);
            case GIVE_EFFECT -> executeGiveEffect(action, context);
            case NESTED_CONDITION -> {
                if (action.getNestedBlock() != null) executeConditionalBlock(action.getNestedBlock(), context);
            }
            default -> {}
        }
    }

    private void executeConditionalBlock(ConditionalBlock block, Map<String, Object> context) {
        // We use .toString() to get the condition text (e.g., "alat benar")
        // and pass it to our evaluateCondition method.
        String conditionText = block.getCondition().toString(); 
        
        // This line FIXES BOTH ERRORS: 
        // 1. It uses evaluateCondition (removing the 'unused' warning)
        // 2. It avoids calling the undefined getRawCondition()
        boolean result = evaluateCondition(conditionText, context);
        
        if (result) {
            for (Action action : block.getActions()) {
                executeAction(action, context);
            }
        } else {
            for (Action elseAction : block.getElseActions()) {
                executeAction(elseAction, context);
            }
        }
    }

    /**
     * Logic for evaluating conditions, including Indonesian keywords.
     */
    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        if (condition == null) return false;
        
        // Clean the string (remove brackets or whitespace if your parser leaves them)
        String cleanCondition = condition.replace("{", "").replace("}", "").trim();
        
        // Check for our special Indonesian keyword
        if (cleanCondition.equalsIgnoreCase("alat benar")) {
            return isToolCorrect(context);
        }
        
        // Add more conditions here later (e.g., "pemain sedang menyelinap")
        if (cleanCondition.equalsIgnoreCase("pemain sedang menyelinap")) {
            Player p = (Player) context.get("player");
            return p != null && p.isSneaking();
        }

        return false;
    }

    // ==================== CORE UTILITIES ====================

    private void handleCancelAction(Map<String, Object> context) {
        Object eventObj = context.get("event");
        if (eventObj instanceof Cancellable cancellableEvent) {
            cancellableEvent.setCancelled(true);
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("[Debug] Event dibatalkan via skrip.");
            }
        }
    }

    private boolean isToolCorrect(Map<String, Object> context) {
        Object playerObj = context.get("player");
        Object blockObj = context.get("block");

        if (playerObj instanceof Player player && blockObj instanceof org.bukkit.block.Block block) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            return block.isPreferredTool(itemInHand);
        }
        return false;
    }

    // ==================== ACTION IMPLEMENTATIONS ====================

    private void executeSendMessage(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player != null) player.sendMessage(replacePlaceholders(action.getParameter(), context));
    }
    
    private void executeBroadcast(Action action, Map<String, Object> context) {
        String msg = replacePlaceholders(action.getParameter(), context);
        Bukkit.broadcast(net.kyori.adventure.text.Component.text(msg));
    }

    private void executeHealPlayer(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player != null) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
        }
    }

    private void executeFeedPlayer(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player != null) player.setFoodLevel(20);
    }

    private void executeTeleport(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null || action.getParameter() == null) return;
        String[] parts = action.getParameter().split(",");
        if (parts.length >= 4) {
            try {
                org.bukkit.World world = Bukkit.getWorld(parts[0].trim());
                double x = Double.parseDouble(parts[1].trim());
                double y = Double.parseDouble(parts[2].trim());
                double z = Double.parseDouble(parts[3].trim());
                if (world != null) player.teleport(new org.bukkit.Location(world, x, y, z));
            } catch (Exception e) {
                plugin.getLogger().warning("Format teleport salah: " + action.getParameter());
            }
        }
    }

    private void executePlaySound(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null || action.getParameter() == null) return;
        try {
            String soundName = action.getParameter().toLowerCase().replace(".", "_");
            org.bukkit.Sound sound = org.bukkit.Registry.SOUND_EVENT.get(org.bukkit.NamespacedKey.minecraft(soundName));
            if (sound != null) player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    private void executeGiveEffect(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null || action.getParameter() == null) return;
        String[] parts = action.getParameter().split(",");
        if (parts.length >= 3) {
            try {
                String effectName = parts[0].toUpperCase().trim();
                int duration = Integer.parseInt(parts[1].trim()) * 20;
                int amp = Integer.parseInt(parts[2].trim());
                org.bukkit.potion.PotionEffectType type = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft(effectName.toLowerCase()));
                if (type != null) player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amp));
            } catch (Exception ignored) {}
        }
    }

    // ==================== VARIABLE LOGIC ====================

    private void executeSetVariable(Action action, Map<String, Object> context) {
        String varName = action.getParameter();
        String value = action.getAdditionalParams().length > 0 ? action.getAdditionalParams()[0] : "";
        value = replacePlaceholders(value, context);
        if (varName.contains("%player%")) {
            Player p = (Player) context.get("player");
            if (p != null) variableManager.setPlayer(p.getName(), varName.replace("%player%", "").replace("..", "."), value);
        } else {
            variableManager.setGlobal(varName, value);
        }
    }

    private void executeAddVariable(Action action, Map<String, Object> context) { modifyVariable(action, context, 1); }
    private void executeSubtractVariable(Action action, Map<String, Object> context) { modifyVariable(action, context, -1); }

    private void modifyVariable(Action action, Map<String, Object> context, int mult) {
        String varName = action.getParameter();
        String amtStr = action.getAdditionalParams().length > 0 ? action.getAdditionalParams()[0] : "1";
        try {
            double val = Double.parseDouble(amtStr) * mult;
            if (varName.contains("%player%")) {
                Player p = (Player) context.get("player");
                if (p != null) variableManager.add(p.getName(), varName.replace("%player%", "").replace("..", "."), val);
            } else {
                variableManager.add(null, varName, val);
            }
        } catch (Exception ignored) {}
    }

    private void executeDeleteVariable(Action action, Map<String, Object> context) {
        String varName = action.getParameter();
        if (varName.contains("%player%")) {
            Player p = (Player) context.get("player");
            if (p != null) variableManager.deletePlayer(p.getName(), varName.replace("%player%", "").replace("..", "."));
        } else {
            variableManager.deleteGlobal(varName);
        }
    }

    private void executeGiveItem(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return;
        try {
            Material mat = Material.valueOf(action.getParameter().toUpperCase());
            int amt = action.getAdditionalParams().length > 0 ? Integer.parseInt(action.getAdditionalParams()[0]) : 1;
            player.getInventory().addItem(new ItemStack(mat, amt));
        } catch (Exception ignored) {}
    }

    private void executeKickPlayer(Action action, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player != null) {
            String reason = replacePlaceholders(action.getParameter(), context);
            player.kick(net.kyori.adventure.text.Component.text(reason));
        }
    }

    // ==================== PLACEHOLDERS ====================

    private String replacePlaceholders(String message, Map<String, Object> context) {
        if (message == null) return "";
        String result = message;
        if (context.containsKey("player")) {
            result = result.replace("%player%", ((Player) context.get("player")).getName());
        }
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String key = "{" + entry.getKey() + "}";
            if (result.contains(key) && entry.getValue() != null) {
                result = result.replace(key, entry.getValue().toString());
            }
        }
        result = replaceVariables(result, context);
        return result.replace("&", "§");
    }

    private String replaceVariables(String text, Map<String, Object> context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object val;
            if (varName.contains("%player%")) {
                Player p = (Player) context.get("player");
                val = (p != null) ? variableManager.getPlayer(p.getName(), varName.replace("%player%", "").replace("..", ".")) : null;
            } else {
                val = variableManager.getGlobal(varName);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(val != null ? val.toString() : "0"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}