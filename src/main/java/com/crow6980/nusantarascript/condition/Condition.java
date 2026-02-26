package com.crow6980.nusantarascript.condition;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.crow6980.nusantarascript.manager.VariableManager;

import java.util.Map;

/**
 * PHASE 2 - STEP 2: Condition Interface
 * 
 * Represents a conditional check in a script (jika statement).
 * Conditions evaluate to true or false based on the event context.
 * 
 * @author crow6980
 */
public abstract class Condition {
        /**
         * Condition: jika darah pemain kurang dari NUMBER
         */
        public static class PlayerHealthCondition extends Condition {
            private final double threshold;

            public PlayerHealthCondition(double threshold, int lineNumber) {
                super(lineNumber);
                this.threshold = threshold;
            }

            @Override
            public boolean evaluate(Map<String, Object> context) {
                Player player = getPlayer(context);
                // Spigot: player.getHealth() memberikan nilai darah saat ini (Max default 20.0)
                return player != null && player.getHealth() < threshold;
            }
        }

        /**
         * Condition: jika dunia adalah "world_name"
         */
        public static class WorldCondition extends Condition {
            private final String worldName;

            public WorldCondition(String worldName, int lineNumber) {
                super(lineNumber);
                this.worldName = worldName;
            }

            @Override
            public boolean evaluate(Map<String, Object> context) {
                Player player = getPlayer(context);
                if (player == null) return false;
                return player.getWorld().getName().equalsIgnoreCase(worldName);
            }
        }
    
    protected final int lineNumber;
    
    public Condition(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    /**
     * Evaluates the condition based on the event context
     * 
     * @param context Event context containing player, block, etc.
     * @return true if condition is met, false otherwise
     */
    public abstract boolean evaluate(Map<String, Object> context);
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Helper method to safely get a player from context
     */
    protected Player getPlayer(Map<String, Object> context) {
        Object obj = context.get("player");
        return obj instanceof Player ? (Player) obj : null;
    }
    
    /**
     * Helper method to safely get a block from context
     */
    protected Block getBlock(Map<String, Object> context) {
        Object obj = context.get("block");
        return obj instanceof Block ? (Block) obj : null;
    }

    /**
     * Retrieve variable value using the injected VariableManager.
     * Supports player-specific variables using %player% token.
     */
    protected Object getVariable(Map<String, Object> context, String variableName) {
        Object vmObj = context.get("variableManager");
        if (!(vmObj instanceof VariableManager)) return null;
        VariableManager vm = (VariableManager) vmObj;

        if (variableName.contains("%player%")) {
            Player player = getPlayer(context);
            if (player == null) return null;
            String actual = variableName.replace(".%player%", "").replace("%player%.", "");
            return vm.getPlayer(player.getName(), actual);
        } else {
            return vm.getGlobal(variableName);
        }
    }
    
    /**
     * Condition: jika blok adalah "MATERIAL_NAME"
     * Checks if the broken/placed block is of a specific type
     */
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
    
    /**
     * Condition: jika pemain memegang "MATERIAL_NAME"
     * Checks if the player is holding a specific item
     */
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
    
    /**
     * Condition: jika pemain punya izin "permission.node"
     * Checks if the player has a specific permission
     */
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
    
    /**
     * Condition: jika pemain adalah "PlayerName"
     * Checks if the player has a specific name
     */
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
    
    /**
     * Condition: jika variabel {name} kurang dari NUMBER
     */
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
            try {
                double current = Double.parseDouble(val.toString());
                return current < threshold;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    /**
     * Condition: jika variabel {name} lebih dari NUMBER
     */
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
            try {
                double current = Double.parseDouble(val.toString());
                return current > threshold;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    /**
     * Condition: jika variabel {name} sama dengan "value"
     */
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
            if (val == null) return false;
            return val.toString().equalsIgnoreCase(expectedValue);
        }
    }

    /**
     * Condition: jika pemain sedang terbang
     * Checks if the player is flying
     */
    public static class PlayerFlyingCondition extends Condition {
        
        public PlayerFlyingCondition(int lineNumber) {
            super(lineNumber);
        }
        
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            return player != null && player.isFlying();
        }
    }
    
    /**
     * Condition: jika pemain sedang menyelinap
     * Checks if the player is sneaking
     */
    public static class PlayerSneakingCondition extends Condition {
        
        public PlayerSneakingCondition(int lineNumber) {
            super(lineNumber);
        }
        
        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            return player != null && player.isSneaking();
        }
    }

    /**
     * Condition: jika alat benar
     * Logic: Checks if the held tool matches the broken block type.
     */
    public static class ToolMatchCondition extends Condition {
        public ToolMatchCondition(int lineNumber) {
            super(lineNumber);
        }

        @Override
        public boolean evaluate(Map<String, Object> context) {
            Player player = getPlayer(context);
            Block block = getBlock(context);
            if (player == null || block == null) return false;

            Material toolType = player.getInventory().getItemInMainHand().getType();
            String blockName = block.getType().name();
            String toolName = toolType.name();

            // Logic for matching tools to blocks
            if (blockName.contains("LOG") || blockName.contains("PLANKS") || blockName.contains("WOOD")) {
                return toolName.contains("AXE") && !toolName.contains("PICKAXE");
            } else if (blockName.contains("STONE") || blockName.contains("ORE") || blockName.contains("COAL") || blockName.contains("IRON")) {
                return toolName.contains("PICKAXE");
            } else if (blockName.contains("DIRT") || blockName.contains("SAND") || blockName.contains("GRAVEL")) {
                return toolName.contains("SHOVEL");
            }

            return true; // Default to true for blocks that don't need specific tools
        }
    }
}
