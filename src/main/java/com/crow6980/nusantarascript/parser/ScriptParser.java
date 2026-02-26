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
public class ScriptParser {
    
    private final NusantaraScript plugin;
    
    // Indentation constants
    private static final int INDENT_SIZE = 4; // 4 spaces per indent level
    
    public ScriptParser(NusantaraScript plugin) {
        this.plugin = plugin;
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
                    ConditionalParseResult result = parseConditionalBlock(lines, i, filename);
                    if (result != null) {
                        if (result.block != null) {
                            handler.addConditionalBlock(result.block);
                        }
                        i = result.nextIndex;
                        continue;
                    }
                    i++;
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
    /**
     * Helper used by event handler and command parser.
     * Returns both the parsed block and the index to continue from.
     */
    private static class ConditionalParseResult {
        final ConditionalBlock block;
        final int nextIndex;
        ConditionalParseResult(ConditionalBlock block, int nextIndex) {
            this.block = block;
            this.nextIndex = nextIndex;
        }
    }

    private ConditionalParseResult parseConditionalBlock(List<IndentedLine> lines, int startIndex, String filename) {
        IndentedLine conditionLine = lines.get(startIndex);
        Condition condition = parseCondition(conditionLine, filename);
        if (condition == null) {
            return new ConditionalParseResult(null, startIndex + 1);
        }

        ConditionalBlock block = new ConditionalBlock(condition, conditionLine.lineNumber);
        int i = startIndex + 1;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            if (line.indentLevel <= conditionLine.indentLevel && !line.content.isEmpty()) {
                break;
            }
            if (line.indentLevel == conditionLine.indentLevel + 1 && !line.content.isEmpty()) {
                Action action = parseAction(line, filename);
                if (action != null) block.addAction(action);
            }
            i++;
        }

        // else/elseif branch
        while (i < lines.size()) {
            IndentedLine next = lines.get(i);
            String lc = next.content.toLowerCase().trim();
            if (lc.equals("jika tidak:") || lc.equals("jika tidak")) {
                i++;
                while (i < lines.size()) {
                    IndentedLine line = lines.get(i);
                    if (line.indentLevel <= conditionLine.indentLevel && !line.content.isEmpty()) {
                        break;
                    }
                    if (line.indentLevel == conditionLine.indentLevel + 1 && !line.content.isEmpty()) {
                        Action action = parseAction(line, filename);
                        if (action != null) block.addElseAction(action);
                    }
                    i++;
                }
            } else if (lc.startsWith("jika ") && lc.endsWith(":")) { // elseif
                ConditionalParseResult elseifResult = parseConditionalBlock(lines, i, filename);
                if (elseifResult != null && elseifResult.block != null) {
                    // Store as elseAction: wrap as a special Action type or handle in executor
                    // For now, add a special Action that holds the ConditionalBlock
                    block.addElseAction(new Action(Action.ActionType.CUSTOM, "ELSEIF", new String[]{}, next.lineNumber) {
                        public ConditionalBlock getConditionalBlock() { return elseifResult.block; }
                    });
                    i = elseifResult.nextIndex;
                }
                break;
            } else {
                break;
            }
        }

        return new ConditionalParseResult(block, i);
    }
    
    /**
     * Parses a custom command definition
     */
    private CustomCommand parseCustomCommand(List<IndentedLine> lines, int startIndex, String filename) {
        IndentedLine commandLine = lines.get(startIndex);
        String commandName = extractCommandName(commandLine.content);
        if (commandName == null) return null;

        List<String> argsDefs = extractCommandArguments(commandLine.content);
        String permission = null;
        List<Action> actions = new ArrayList<>();
        boolean inActionBlock = false;

        int i = startIndex + 1;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);

            if (line.indentLevel == 0 && !line.content.isEmpty()) {
                break;
            }

            if (line.content.isEmpty()) {
                i++;
                continue;
            }

            if (line.indentLevel == 1) {
                String lc = line.content.toLowerCase();
                if (lc.startsWith("izin:")) {
                    permission = extractQuotedString(line.content);
                } else if (lc.equals("aksi:")) {
                    inActionBlock = true;
                }
                i++;
            } else if (line.indentLevel == 2 && inActionBlock) {
                Action action = parseAction(line, filename);
                if (action != null) {
                    actions.add(action);
                }
                i++;
            } else {
                i++;
            }
        }

        CustomCommand command;
        if (permission != null) {
            command = new CustomCommand(commandName, argsDefs, permission, "Custom command", commandLine.lineNumber);
        } else {
            command = new CustomCommand(commandName, argsDefs, commandLine.lineNumber);
        }
        for (Action a : actions) {
            command.addAction(a);
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
        
        // variable comparisons added in Phase 3
        if (conditionText.contains("{") && conditionText.contains("kurang dari")) {
            String var = extractVariableName(line.content);
            String num = extractNumber(conditionText);
            try {
                double thresh = Double.parseDouble(num);
                return new Condition.VariableLessThanCondition(var, thresh, line.lineNumber);
            } catch (NumberFormatException ignored) {}
        }
        if (conditionText.contains("{") && conditionText.contains("lebih dari")) {
            String var = extractVariableName(line.content);
            String num = extractNumber(conditionText);
            try {
                double thresh = Double.parseDouble(num);
                return new Condition.VariableGreaterThanCondition(var, thresh, line.lineNumber);
            } catch (NumberFormatException ignored) {}
        }
        if (conditionText.contains("{") && conditionText.contains("adalah") && !strings.isEmpty()) {
            String var = extractVariableName(line.content);
            return new Condition.VariableEqualsCondition(var, strings.get(0), line.lineNumber);
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
        
        // new "setel" syntax (Phase 3)
        if (command.startsWith("setel")) {
            String varName = extractVariableName(actionLine);
            String value = "";
            int eq = actionLine.indexOf('=');
            if (eq != -1) {
                value = actionLine.substring(eq + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    value = value.substring(1, value.length() - 1);
                }
            } else if (!strings.isEmpty()) {
                value = strings.get(0);
            }
            return new Action(Action.ActionType.SET_VARIABLE, varName, new String[]{value}, line.lineNumber);
        }

        // Parse variable operations (legacy syntax)
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
        // Allow command declarations with or without leading slash and with arguments
        return line.toLowerCase().startsWith("perintah ");
    }
    
    private boolean isCondition(String line) {
        String lower = line.toLowerCase().trim();
        return lower.startsWith("jika ") && !lower.startsWith("jika tidak");
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
    
    /**
     * Extracts the base command name from a declaration line.
     *
     * Examples:
     *   "perintah /foo:"               -> "foo"
     *   "perintah foo bar baz:"        -> "foo"
     *   "perintah /say <text> [player]:" -> "say"
     */
    private String extractCommandName(String line) {
        if (!line.toLowerCase().startsWith("perintah ")) {
            return null;
        }
        // remove leading keyword and trailing colon
        String remainder = line.substring(8).trim(); // after "perintah"
        if (remainder.endsWith(":")) {
            remainder = remainder.substring(0, remainder.length() - 1).trim();
        }
        if (remainder.isEmpty()) {
            return null;
        }
        // first token is the command; strip leading slash if present
        String[] parts = remainder.split("\\s+");
        String name = parts[0];
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name.toLowerCase();
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

    /**
     * Extracts argument definitions from a command line.
     * Everything after the base command name and before the colon is
     * considered an argument token.
     *
     * Example:
     *   "perintah /foo <text> [player]:" -> ["<text>", "[player]"]
     */
    private List<String> extractCommandArguments(String line) {
        List<String> args = new ArrayList<>();
        if (!line.toLowerCase().startsWith("perintah ")) {
            return args;
        }
        String remainder = line.substring(8).trim();
        if (remainder.endsWith(":")) {
            remainder = remainder.substring(0, remainder.length() - 1).trim();
        }
        String[] parts = remainder.split("\\s+");
        // first part is command name, skip it
        for (int i = 1; i < parts.length; i++) {
            args.add(parts[i]);
        }
        return args;
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
