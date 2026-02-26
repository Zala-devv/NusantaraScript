package com.crow6980.nusantarascript.condition;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.crow6980.nusantarascript.manager.VariableManager;
import java.util.Map;

/**
 * PHASE 2 - STEP 2: Condition Interface
 * Represents a conditional check in a script (jika statement).
 */
public abstract class Condition {

    /**
     * NEW: Condition for expressions like {arg1} == "emas" or {arg2} > 10
     */
    public static class ExpressionCondition extends Condition {
        private final String expression;

        public ExpressionCondition(String expression, int lineNumber) {
            super(lineNumber);
            this.expression = expression;
        }

        @Override
        public boolean evaluate(Map<String, Object> context) {
            String processed = expression;

            // Replace placeholders with values from context (like {arg1}, {player}, etc.)
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                if (entry.getValue() != null) {
                    processed = processed.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                }
            }

            try {
                // Handle Math: Greater Than
                if (processed.contains(">")) {
                    String[] parts = processed.split(">");
                    return Double.parseDouble(parts[0].trim()) > Double.parseDouble(parts[1].trim());
                } 
                // Handle Math: Less Than
                else if (processed.contains("<")) {
                    String[] parts = processed.split("<");
                    return Double.parseDouble(parts[0].trim()) < Double.parseDouble(parts[1].trim());
                } 
                // Handle Equality: ==
                else if (processed.contains("==")) {
                    String[] parts = processed.split("==");
                    String left = parts[0].trim().replace("\"", "");
                    String right = parts[1].trim().replace("\"", "");
                    return left.equalsIgnoreCase(right);
                }
            } catch (Exception e) {
                return false; // Return false if math parsing fails
            }
            return false;
        }
    }

    // --- EXISTING INNER CLASSES ---

    public static class PlayerHealthCondition extends Condition {
        private final double threshold;
        public PlayerHealthCondition(double threshold, int lineNumber) {
            super(lineNumber);
            this.threshold = threshold;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            return player != null && player.getHealth() < threshold;
        }
    }

    public static class WorldCondition extends Condition {
        private final String worldName;
        public WorldCondition(String worldName, int lineNumber) {
            super(lineNumber);
            this.worldName = worldName;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            return player != null && player.getWorld().getName().equalsIgnoreCase(worldName);
        }
    }

    public static class BlockTypeCondition extends Condition {
        private final Material material;
        public BlockTypeCondition(Material material, int lineNumber) {
            super(lineNumber);
            this.material = material;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Block block = getBlock(context);
            return block != null && block.getType() == material;
        }
    }

    public static class HoldingItemCondition extends Condition {
        private final Material material;
        public HoldingItemCondition(Material material, int lineNumber) {
            super(lineNumber);
            this.material = material;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            if (player == null) return false;
            ItemStack item = player.getInventory().getItemInMainHand();
            return item != null && item.getType() == material;
        }
    }

    public static class PermissionCondition extends Condition {
        private final String permission;
        public PermissionCondition(String permission, int lineNumber) {
            super(lineNumber);
            this.permission = permission;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            return player != null && player.hasPermission(permission);
        }
    }

    public static class PlayerNameCondition extends Condition {
        private final String playerName;
        public PlayerNameCondition(String playerName, int lineNumber) {
            super(lineNumber);
            this.playerName = playerName;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            return player != null && player.getName().equalsIgnoreCase(playerName);
        }
    }

    public static class VariableLessThanCondition extends Condition {
        private final String variableName;
        private final double threshold;
        public VariableLessThanCondition(String variableName, double threshold, int lineNumber) {
            super(lineNumber);
            this.variableName = variableName;
            this.threshold = threshold;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Object val = getVariable(context, variableName);
            if (val == null) return false;
            try { return Double.parseDouble(val.toString()) < threshold; } 
            catch (NumberFormatException e) { return false; }
        }
    }

    public static class VariableGreaterThanCondition extends Condition {
        private final String variableName;
        private final double threshold;
        public VariableGreaterThanCondition(String variableName, double threshold, int lineNumber) {
            super(lineNumber);
            this.variableName = variableName;
            this.threshold = threshold;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Object val = getVariable(context, variableName);
            if (val == null) return false;
            try { return Double.parseDouble(val.toString()) > threshold; } 
            catch (NumberFormatException e) { return false; }
        }
    }

    public static class VariableEqualsCondition extends Condition {
        private final String variableName;
        private final String expectedValue;
        public VariableEqualsCondition(String variableName, String expectedValue, int lineNumber) {
            super(lineNumber);
            this.variableName = variableName;
            this.expectedValue = expectedValue;
        }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Object val = getVariable(context, variableName);
            return val != null && val.toString().equalsIgnoreCase(expectedValue);
        }
    }

    public static class PlayerFlyingCondition extends Condition {
        public PlayerFlyingCondition(int lineNumber) { super(lineNumber); }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            return player != null && player.isFlying();
        }
    }

    public static class PlayerSneakingCondition extends Condition {
        public PlayerSneakingCondition(int lineNumber) { super(lineNumber); }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            return player != null && player.isSneaking();
        }
    }

    public static class ToolMatchCondition extends Condition {
        public ToolMatchCondition(int lineNumber) { super(lineNumber); }
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            Block block = getBlock(context);
            if (player == null || block == null) return false;
            Material toolType = player.getInventory().getItemInMainHand().getType();
            String blockName = block.getType().name();
            String toolName = toolType.name();
            if (blockName.contains("LOG") || blockName.contains("WOOD")) return toolName.contains("AXE") && !toolName.contains("PICKAXE");
            if (blockName.contains("STONE") || blockName.contains("ORE")) return toolName.contains("PICKAXE");
            if (blockName.contains("DIRT") || blockName.contains("SAND")) return toolName.contains("SHOVEL");
            return true;
        }
    }

    // --- BASE CLASS LOGIC ---

    protected final int lineNumber;
    public Condition(int lineNumber) { this.lineNumber = lineNumber; }
    public abstract boolean evaluate(Map<String, Object> context);
    public int getLineNumber() { return lineNumber; }

    protected Player getPlayer(Map<String, Object> context) {
        Object obj = context.get("player");
        return obj instanceof Player ? (Player) obj : null;
    }

    protected Block getBlock(Map<String, Object> context) {
        Object obj = context.get("block");
        return obj instanceof Block ? (Block) obj : null;
    }

    protected Object getVariable(Map<String, Object> context, String variableName) {
        Object vmObj = context.get("variableManager");
        if (!(vmObj instanceof VariableManager)) return null;
        VariableManager vm = (VariableManager) vmObj;
        if (variableName.contains("%player%")) {
            Player player = getPlayer(context);
            if (player == null) return null;
            String actual = variableName.replace(".%player%", "").replace("%player%.", "");
            return vm.getPlayer(player.getName(), actual);
        }
        return vm.getGlobal(variableName);
    }
}