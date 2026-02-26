package com.crow6980.nusantarascript.parser;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.command.CustomCommand;
import com.crow6980.nusantarascript.condition.Condition;
import com.crow6980.nusantarascript.condition.ConditionalBlock;
import com.crow6980.nusantarascript.script.Action;
import com.crow6980.nusantarascript.script.EventHandler;
import com.crow6980.nusantarascript.script.Script;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * PHASE 2 - STEP 1: Enhanced Script Parser with Indentation Support
 * 
 * The enhanced parser now supports:
 * 1. Nested conditional blocks (jika statements)
 * 2. Variable operations
 * 3. Custom command definitions
 * 
 * INDENTATION PARSING LOGIC:
 * - Base level (0 spaces): Event triggers, custom command declarations
 * - Level 1 (4 spaces): Actions directly under events, or conditions
 * - Level 2 (8 spaces): Actions inside conditional blocks
 * 
 * Example:
 * saat pemain masuk:           <- 0 spaces (event trigger)
 *     kirim "Hello" ke pemain  <- 4 spaces (direct action)
 *     jika pemain punya izin "vip": <- 4 spaces (condition)
 *         kirim "VIP!" ke pemain    <- 8 spaces (conditional action)
 * 
 * @author crow6980
 */
public class EnhancedScriptParser {
    
    private final NusantaraScript plugin;
    private final ScriptLexer lexer;
    
    // Indentation constants
    private static final int INDENT_SIZE = 4; // 4 spaces per indent level
    
    public EnhancedScriptParser(NusantaraScript plugin) {
        this.plugin = plugin;
        this.lexer = new ScriptLexer();
    }
    
    /**
     * Parses a script file with support for:
     * - Event handlers
     * - Conditional blocks (jika)
     * - Custom commands (perintah)
     * - Variable operations
     */
    public Script parse(String filename, List<String> rawLines) {
        // Step 1: Create IndentedLine objects to track indentation
        List<IndentedLine> lines = parseIndentation(rawLines);
        
        if (lines.isEmpty()) {
            plugin.getLogger().warning("No valid lines found in " + filename);
            return null;
        }
        
        // Step 2: Parse event handlers and custom commands
        List<EventHandler> eventHandlers = new ArrayList<>();
        List<CustomCommand> customCommands = new ArrayList<>();
        
        int i = 0;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            
            // Skip empty lines and comments
            if (line.content.trim().isEmpty() || line.content.trim().startsWith("#")) {
                i++;
                continue;
            }
            
            // Check for event trigger
            if (line.indentLevel == 0 && isEventTrigger(line.content)) {
                EventHandler handler = parseEventHandler(lines, i, filename);
                if (handler != null) {
                    eventHandlers.add(handler);
                }
                // Skip past the event block
                i = findNextBlockStart(lines, i);
                
            }
            // Check for custom command
            else if (line.indentLevel == 0 && isCommandDeclaration(line.content)) {
                CustomCommand command = parseCustomCommand(lines, i, filename);
                if (command != null) {
                    customCommands.add(command);
                }
                // Skip past the command block
                i = findNextBlockStart(lines, i);
            }
            else {
                i++;
            }
        }
        
        if (eventHandlers.isEmpty() && customCommands.isEmpty()) {
            plugin.getLogger().warning("No event handlers or commands found in " + filename);
            return null;
        }
        
        return new Script(filename, eventHandlers, customCommands);
    }
    
    /**
     * Parses raw lines into IndentedLine objects
     * Calculates the indentation level for each line
     */
    private List<IndentedLine> parseIndentation(List<String> rawLines) {
        List<IndentedLine> result = new ArrayList<>();
        
        for (int i = 0; i < rawLines.size(); i++) {
            String line = rawLines.get(i);
            
            // Count leading spaces
            int spaces = 0;
            for (char c : line.toCharArray()) {
                if (c == ' ') spaces++;
                else if (c == '	') spaces += 4; // Tab = 4 spaces
                else break;
            }
            
            // Calculate indent level
            int indentLevel = spaces / INDENT_SIZE;
            String content = line.trim();
            
            result.add(new IndentedLine(content, indentLevel, i + 1));
        }
        
        return result;
    }
    
    /**
     * Parses an event handler block including conditions and actions
     */
    private EventHandler parseEventHandler(List<IndentedLine> lines, int startIndex, String filename) {
        IndentedLine triggerLine = lines.get(startIndex);
        EventHandler handler = parseEventTrigger(triggerLine, filename);
        
        if (handler == null) return null;
        
        // Parse the block content (actions and conditions)
        int i = startIndex + 1;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            
            // Stop if we hit another block at same level
            if (line.indentLevel <= triggerLine.indentLevel && !line.content.isEmpty()) {
                break;
            }
            
            // Skip empty lines
            if (line.content.isEmpty()) {
                i++;
                continue;
            }
            
            // Level 1: Direct actions or conditions
            if (line.indentLevel == 1) {
                if (isCondition(line.content)) {
                    // Parse conditional block
                    ConditionalBlock condBlock = parseConditionalBlock(lines, i, filename);
                    if (condBlock != null) {
                        handler.addConditionalBlock(condBlock);
                    }
                    // Skip past conditional block
                    i = findNextSameLevelLine(lines, i);
                } else {
                    // Parse direct action
                    Action action = parseAction(line, filename);
                    if (action != null) {
                        handler.addAction(action);
                    }
                    i++;
                }
            } else {
                i++;
            }
        }
        
        return handler;
    }
    
    /**
     * Parses a conditional block (jika statement)
     */
    private ConditionalBlock parseConditionalBlock(List<IndentedLine> lines, int startIndex, String filename) {
        IndentedLine conditionLine = lines.get(startIndex);
        Condition condition = parseCondition(conditionLine, filename);
        
        if (condition == null) return null;
        
        ConditionalBlock block = new ConditionalBlock(condition, conditionLine.lineNumber);
        
        // Parse actions inside the conditional block (indentLevel = 2)
        int i = startIndex + 1;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            
            // Stop if we're back to level 1 or 0
            if (line.indentLevel <= conditionLine.indentLevel && !line.content.isEmpty()) {
                break;
            }
            
            // Only process level 2 lines (actions inside condition)
            if (line.indentLevel == 2 && !line.content.isEmpty()) {
                Action action = parseAction(line, filename);
                if (action != null) {
                    block.addAction(action);
                }
            }
            
            i++;
        }
        
        return block;
    }
    
    /**
     * Parses a custom command definition
     */
    private CustomCommand parseCustomCommand(List<IndentedLine> lines, int startIndex, String filename) {
        IndentedLine commandLine = lines.get(startIndex);
        String commandName = extractCommandName(commandLine.content);
        
        if (commandName == null) return null;
        
        CustomCommand command = new CustomCommand(commandName, commandLine.lineNumber);
        String permission = null;
        
        // Parse command properties and actions
        int i = startIndex + 1;
        boolean inActionBlock = false;
        
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            
            // Stop if we hit another top-level block
            if (line.indentLevel == 0 && !line.content.isEmpty()) {
                break;
            }
            
            if (line.content.isEmpty()) {
                i++;
                continue;
            }
            
            // Level 1: Properties or "aksi:" marker
            if (line.indentLevel == 1) {
                if (line.content.toLowerCase().startsWith("izin:")) {
                    permission = extractQuotedString(line.content);
                } else if (line.content.toLowerCase().equals("aksi:")) {
                    inActionBlock = true;
                }
                i++;
            }
            // Level 2: Actions inside aksi: block
            else if (line.indentLevel == 2 && inActionBlock) {
                Action action = parseAction(line, filename);
                if (action != null) {
                    command.addAction(action);
                }
                i++;
            } else {
                i++;
            }
        }
        
        // Set permission if found
        if (permission != null) {
            return new CustomCommand(commandName, permission, "Custom command", commandLine.lineNumber);
        }
        
        return command;
    }
    
    /**
     * Parses an event trigger line
     */
    private EventHandler parseEventTrigger(IndentedLine line, String filename) {
        String trigger = line.content.toLowerCase();
        
        if (trigger.endsWith(":")) {
            trigger = trigger.substring(0, trigger.length() - 1).trim();
        }
        
        EventHandler.EventType eventType = null;
        
        if (trigger.equals("saat pemain masuk") || trigger.equals("ketika pemain masuk")) {
            eventType = EventHandler.EventType.PLAYER_JOIN;
        } else if (trigger.equals("saat blok dihancurkan") || trigger.equals("ketika blok dihancurkan")) {
            eventType = EventHandler.EventType.BLOCK_BREAK;
        } else if (trigger.equals("saat pemain keluar") || trigger.equals("ketika pemain keluar")) {
            eventType = EventHandler.EventType.PLAYER_QUIT;
        } else if (trigger.equals("saat pemain chat") || trigger.equals("ketika pemain chat")) {
            eventType = EventHandler.EventType.PLAYER_CHAT;
        }
        
        if (eventType == null) {
            plugin.getLogger().warning(filename + " line " + line.lineNumber + 
                                     ": Unknown event trigger: " + trigger);
            return null;
        }
        
        return new EventHandler(eventType, line.lineNumber);
    }
    
    /**
     * Parses a condition from a "jika" line
     */
    private Condition parseCondition(IndentedLine line, String filename) {
        String conditionText = line.content.toLowerCase();
        
        // Remove "jika" and trailing colon
        if (conditionText.startsWith("jika ")) {
            conditionText = conditionText.substring(5).trim();
        }
        if (conditionText.endsWith(":")) {
            conditionText = conditionText.substring(0, conditionText.length() - 1).trim();
        }
        
        // Extract quoted strings
        List<String> strings = ScriptLexer.extractStrings(line.content);
        
        // Parse different condition types
        if (conditionText.contains("blok adalah") && !strings.isEmpty()) {
            Material material = parseMaterial(strings.get(0));
            if (material != null) {
                return new Condition.BlockTypeCondition(material, line.lineNumber);
            }
        }
        else if (conditionText.contains("pemain memegang") && !strings.isEmpty()) {
            Material material = parseMaterial(strings.get(0));
            if (material != null) {
                return new Condition.HoldingItemCondition(material, line.lineNumber);
            }
        }
        else if (conditionText.contains("pemain punya izin") && !strings.isEmpty()) {
            return new Condition.PermissionCondition(strings.get(0), line.lineNumber);
        }
        else if (conditionText.contains("pemain adalah") && !strings.isEmpty()) {
            return new Condition.PlayerNameCondition(strings.get(0), line.lineNumber);
        }
        else if (conditionText.contains("pemain sedang terbang")) {
            return new Condition.PlayerFlyingCondition(line.lineNumber);
        }
        else if (conditionText.contains("pemain sedang menyelinap")) {
            return new Condition.PlayerSneakingCondition(line.lineNumber);
        }
        
        plugin.getLogger().warning(filename + " line " + line.lineNumber + 
                                 ": Unknown condition: " + line.content);
        return null;
    }
    
    /**
     * Parses an action line
     */
    private Action parseAction(IndentedLine line, String filename) {
        String actionLine = line.content;
        List<String> strings = ScriptLexer.extractStrings(actionLine);
        String command = ScriptLexer.removeStrings(actionLine).trim().toLowerCase();
        
        // Parse "kirim ... ke pemain"
        if (command.startsWith("kirim") && command.contains("ke pemain")) {
            if (strings.isEmpty()) return null;
            return new Action(Action.ActionType.SEND_MESSAGE, strings.get(0), line.lineNumber);
        }
        
        // Parse "broadcast ..."
        if (command.startsWith("broadcast") || command.startsWith("umumkan")) {
            if (strings.isEmpty()) return null;
            return new Action(Action.ActionType.BROADCAST, strings.get(0), line.lineNumber);
        }
        
        // Parse "batalkan event"
        if (command.equals("batalkan event") || command.equals("cancel event")) {
            return new Action(Action.ActionType.CANCEL_EVENT, "", line.lineNumber);
        }
        
        // Parse "pulihkan pemain"
        if (command.equals("pulihkan pemain") || command.equals("heal pemain")) {
            return new Action(Action.ActionType.HEAL_PLAYER, "", line.lineNumber);
        }
        
        // Parse "beri makan pemain"
        if (command.equals("beri makan pemain") || command.equals("feed pemain")) {
            return new Action(Action.ActionType.FEED_PLAYER, "", line.lineNumber);
        }
        
        // Parse variable operations
        if (command.startsWith("atur variabel") || command.startsWith("set variabel")) {
            String varName = extractVariableName(actionLine);
            String value = strings.isEmpty() ? "" : strings.get(0);
            return new Action(Action.ActionType.SET_VARIABLE, varName, new String[]{value}, line.lineNumber);
        }
        
        if (command.startsWith("tambah") && command.contains("ke variabel")) {
            String varName = extractVariableName(actionLine);
            String amount = extractNumber(command);
            return new Action(Action.ActionType.ADD_VARIABLE, varName, new String[]{amount}, line.lineNumber);
        }
        
        if (command.startsWith("kurangi") && command.contains("dari variabel")) {
            String varName = extractVariableName(actionLine);
            String amount = extractNumber(command);
            return new Action(Action.ActionType.SUBTRACT_VARIABLE, varName, new String[]{amount}, line.lineNumber);
        }
        
        if (command.startsWith("hapus variabel") || command.startsWith("delete variabel")) {
            String varName = extractVariableName(actionLine);
            return new Action(Action.ActionType.DELETE_VARIABLE, varName, line.lineNumber);
        }
        
        plugin.getLogger().warning(filename + " line " + line.lineNumber + 
                                 ": Unknown action: " + actionLine);
        return null;
    }
    
    // Helper methods
    
    private boolean isEventTrigger(String line) {
        String lower = line.toLowerCase();
        return lower.startsWith("saat ") || lower.startsWith("ketika ");
    }
    
    private boolean isCommandDeclaration(String line) {
        return line.toLowerCase().startsWith("perintah /");
    }
    
    private boolean isCondition(String line) {
        return line.toLowerCase().startsWith("jika ");
    }
    
    private int findNextBlockStart(List<IndentedLine> lines, int currentIndex) {
        int baseLevel = lines.get(currentIndex).indentLevel;
        for (int i = currentIndex + 1; i < lines.size(); i++) {
            if (lines.get(i).indentLevel <= baseLevel && !lines.get(i).content.isEmpty()) {
                return i;
            }
        }
        return lines.size();
    }
    
    private int findNextSameLevelLine(List<IndentedLine> lines, int currentIndex) {
        int level = lines.get(currentIndex).indentLevel;
        for (int i = currentIndex + 1; i < lines.size(); i++) {
            IndentedLine line = lines.get(i);
            if (line.indentLevel <= level && !line.content.isEmpty()) {
                return i;
            }
        }
        return lines.size();
    }
    
    private String extractCommandName(String line) {
        if (line.toLowerCase().startsWith("perintah ")) {
            String name = line.substring(9).trim();
            if (name.endsWith(":")) {
                name = name.substring(0, name.length() - 1);
            }
            return name;
        }
        return null;
    }
    
    private String extractQuotedString(String line) {
        List<String> strings = ScriptLexer.extractStrings(line);
        return strings.isEmpty() ? null : strings.get(0);
    }
    
    private String extractVariableName(String line) {
        int start = line.indexOf("{");
        int end = line.indexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return line.substring(start + 1, end);
        }
        return "";
    }
    
    private String extractNumber(String text) {
        String[] parts = text.split("\\s+");
        for (String part : parts) {
            try {
                Double.parseDouble(part);
                return part;
            } catch (NumberFormatException ignored) {}
        }
        return "1";
    }
    
    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Helper class to track line content with indentation level
     */
    private static class IndentedLine {
        final String content;
        final int indentLevel;
        final int lineNumber;
        
        IndentedLine(String content, int indentLevel, int lineNumber) {
            this.content = content;
            this.indentLevel = indentLevel;
            this.lineNumber = lineNumber;
        }
    }
}
