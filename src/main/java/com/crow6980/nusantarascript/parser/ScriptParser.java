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
 */
public class ScriptParser {

    private final NusantaraScript plugin;
    private static final int INDENT_SIZE = 4;

    public ScriptParser(NusantaraScript plugin) {
        this.plugin = plugin;
    }

    public Script parse(String filename, List<String> rawLines) {
        List<IndentedLine> lines = parseIndentation(rawLines);
        if (lines.isEmpty()) {
            plugin.getLogger().warning("No valid lines found in " + filename);
            return null;
        }

        List<EventHandler> eventHandlers = new ArrayList<>();
        List<CustomCommand> customCommands = new ArrayList<>();

        int i = 0;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            if (line.content.trim().isEmpty() || line.content.trim().startsWith("#")) {
                i++;
                continue;
            }

            if (line.indentLevel == 0 && isEventTrigger(line.content)) {
                EventHandler handler = parseEventHandler(lines, i, filename);
                if (handler != null) eventHandlers.add(handler);
                i = findNextBlockStart(lines, i);
            } else if (line.indentLevel == 0 && isCommandDeclaration(line.content)) {
                CustomCommand command = parseCustomCommand(lines, i, filename);
                if (command != null) customCommands.add(command);
                i = findNextBlockStart(lines, i);
            } else {
                i++;
            }
        }
        return new Script(filename, eventHandlers, customCommands);
    }

    private List<IndentedLine> parseIndentation(List<String> rawLines) {
        List<IndentedLine> result = new ArrayList<>();
        for (int i = 0; i < rawLines.size(); i++) {
            String line = rawLines.get(i);
            int spaces = 0;
            for (char c : line.toCharArray()) {
                if (c == ' ') spaces++;
                else if (c == '\t') spaces += 4;
                else break;
            }
            int indentLevel = spaces / INDENT_SIZE;
            result.add(new IndentedLine(line.trim(), indentLevel, i + 1));
        }
        return result;
    }

    private EventHandler parseEventHandler(List<IndentedLine> lines, int startIndex, String filename) {
        IndentedLine triggerLine = lines.get(startIndex);
        EventHandler handler = parseEventTrigger(triggerLine, filename);
        if (handler == null) return null;

        int i = startIndex + 1;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            if (line.indentLevel <= triggerLine.indentLevel && !line.content.isEmpty()) break;
            if (line.content.isEmpty()) { i++; continue; }

            if (line.indentLevel == 1) {
                if (isCondition(line.content)) {
                    ConditionalParseResult result = parseConditionalBlock(lines, i, filename);
                    if (result != null) {
                        if (result.block != null) handler.addConditionalBlock(result.block);
                        i = result.nextIndex;
                        continue;
                    }
                } else {
                    Action action = parseAction(line, filename);
                    if (action != null) handler.addAction(action);
                }
            }
            i++;
        }
        return handler;
    }

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
        if (condition == null) return new ConditionalParseResult(null, startIndex + 1);

        ConditionalBlock block = new ConditionalBlock(condition, conditionLine.lineNumber);
        int i = startIndex + 1;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            if (line.indentLevel <= conditionLine.indentLevel && !line.content.isEmpty()) break;
            if (line.indentLevel == conditionLine.indentLevel + 1 && !line.content.isEmpty()) {
                Action action = parseAction(line, filename);
                if (action != null) block.addAction(action);
            }
            i++;
        }

        while (i < lines.size()) {
            IndentedLine next = lines.get(i);
            String lc = next.content.toLowerCase().trim();
            if (lc.equals("jika tidak:") || lc.equals("jika tidak")) {
                i++;
                while (i < lines.size()) {
                    IndentedLine line = lines.get(i);
                    if (line.indentLevel <= conditionLine.indentLevel && !line.content.isEmpty()) break;
                    if (line.indentLevel == conditionLine.indentLevel + 1 && !line.content.isEmpty()) {
                        Action action = parseAction(line, filename);
                        if (action != null) block.addElseAction(action);
                    }
                    i++;
                }
            } else if (lc.startsWith("jika ") && lc.endsWith(":")) {
                ConditionalParseResult elseifResult = parseConditionalBlock(lines, i, filename);
                if (elseifResult != null && elseifResult.block != null) {
                    block.addElseAction(new Action(Action.ActionType.NESTED_CONDITION, elseifResult.block, next.lineNumber));
                    i = elseifResult.nextIndex;
                }
                break;
            } else {
                break;
            }
        }
        return new ConditionalParseResult(block, i);
    }

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
            if (line.indentLevel == 0 && !line.content.isEmpty()) break;
            if (line.content.isEmpty()) { i++; continue; }

            if (line.indentLevel == 1) {
                String lc = line.content.toLowerCase();
                if (lc.startsWith("izin:")) permission = extractQuotedString(line.content);
                else if (lc.equals("aksi:")) inActionBlock = true;
            } else if (line.indentLevel == 2 && inActionBlock) {
                Action action = parseAction(line, filename);
                if (action != null) actions.add(action);
            }
            i++;
        }

        CustomCommand command = (permission != null) ? 
            new CustomCommand(commandName, argsDefs, permission, "Custom", commandLine.lineNumber) :
            new CustomCommand(commandName, argsDefs, commandLine.lineNumber);
        
        for (Action a : actions) command.addAction(a);
        return command;
    }

    private EventHandler parseEventTrigger(IndentedLine line, String filename) {
        String trigger = line.content.toLowerCase();
        if (trigger.endsWith(":")) trigger = trigger.substring(0, trigger.length() - 1).trim();
        
        EventHandler.EventType eventType = switch (trigger) {
            case "saat pemain masuk", "ketika pemain masuk" -> EventHandler.EventType.PLAYER_JOIN;
            case "saat blok dihancurkan", "ketika blok dihancurkan" -> EventHandler.EventType.BLOCK_BREAK;
            case "saat pemain keluar", "ketika pemain keluar" -> EventHandler.EventType.PLAYER_QUIT;
            case "saat pemain chat", "ketika pemain chat" -> EventHandler.EventType.PLAYER_CHAT;
            case "saat pemain mati" -> EventHandler.EventType.PLAYER_DEATH;
            case "saat pemain hidup kembali" -> EventHandler.EventType.PLAYER_RESPAWN;
            case "saat pemain terluka" -> EventHandler.EventType.PLAYER_DAMAGE;
            case "saat entity terluka" -> EventHandler.EventType.ENTITY_DAMAGE;
            default -> null;
        };
        
        if (eventType == null) {
            plugin.getLogger().warning(filename + " line " + line.lineNumber + ": Unknown trigger: " + trigger);
            return null;
        }
        return new EventHandler(eventType, line.lineNumber);
    }

    private Condition parseCondition(IndentedLine line, String filename) {
        // DECLARE VARIABLES FIRST
        String conditionText = line.content.toLowerCase();
        List<String> strings = ScriptLexer.extractStrings(line.content);
        
        if (conditionText.startsWith("jika ")) conditionText = conditionText.substring(5).trim();
        if (conditionText.endsWith(":")) conditionText = conditionText.substring(0, conditionText.length() - 1).trim();

        // 1. Health Check
        if (conditionText.contains("darah pemain kurang dari")) {
            String num = extractNumber(conditionText);
            try {
                return new Condition.PlayerHealthCondition(Double.parseDouble(num), line.lineNumber);
            } catch (NumberFormatException ignored) {}
        }

        // 2. World Check
        if (conditionText.contains("dunia adalah") && !strings.isEmpty()) {
            return new Condition.WorldCondition(strings.get(0), line.lineNumber);
        }

        // 3. Block Type
        if (conditionText.contains("blok adalah") && !strings.isEmpty()) {
            Material m = parseMaterial(strings.get(0));
            if (m != null) return new Condition.BlockTypeCondition(m, line.lineNumber);
        }

        // 4. Holding Item
        if (conditionText.contains("pemain memegang") && !strings.isEmpty()) {
            Material m = parseMaterial(strings.get(0));
            if (m != null) return new Condition.HoldingItemCondition(m, line.lineNumber);
        }

        // 5. Basic Player Checks
        if (conditionText.contains("pemain punya izin") && !strings.isEmpty()) 
            return new Condition.PermissionCondition(strings.get(0), line.lineNumber);
        if (conditionText.contains("pemain adalah") && !strings.isEmpty()) 
            return new Condition.PlayerNameCondition(strings.get(0), line.lineNumber);
        if (conditionText.contains("pemain sedang terbang")) 
            return new Condition.PlayerFlyingCondition(line.lineNumber);
        if (conditionText.contains("pemain sedang menyelinap")) 
            return new Condition.PlayerSneakingCondition(line.lineNumber);
        if (conditionText.equals("alat benar")) 
            return new Condition.ToolMatchCondition(line.lineNumber);

        // 6. Variable Comparisons
        if (conditionText.contains("{")) {
            String var = extractVariableName(line.content);
            String num = extractNumber(conditionText);
            if (conditionText.contains("kurang dari")) {
                try { return new Condition.VariableLessThanCondition(var, Double.parseDouble(num), line.lineNumber); } catch (Exception ignored) {}
            } else if (conditionText.contains("lebih dari")) {
                try { return new Condition.VariableGreaterThanCondition(var, Double.parseDouble(num), line.lineNumber); } catch (Exception ignored) {}
            } else if (conditionText.contains("adalah") && !strings.isEmpty()) {
                return new Condition.VariableEqualsCondition(var, strings.get(0), line.lineNumber);
            }
        }

        plugin.getLogger().warning(filename + " line " + line.lineNumber + ": Unknown condition: " + line.content);
        return null;
    }

    private Action parseAction(IndentedLine line, String filename) {
        String actionLine = line.content;
        List<String> strings = ScriptLexer.extractStrings(actionLine);
        String command = ScriptLexer.removeStrings(actionLine).trim().toLowerCase();

        if (command.startsWith("kirim") && command.contains("ke pemain")) return new Action(Action.ActionType.SEND_MESSAGE, strings.isEmpty() ? "" : strings.get(0), line.lineNumber);
        if (command.startsWith("broadcast") || command.startsWith("umumkan")) return new Action(Action.ActionType.BROADCAST, strings.isEmpty() ? "" : strings.get(0), line.lineNumber);
        if (command.equals("batalkan event") || command.equals("cancel event")) return new Action(Action.ActionType.CANCEL_EVENT, "", line.lineNumber);
        if (command.equals("pulihkan pemain") || command.equals("heal pemain")) return new Action(Action.ActionType.HEAL_PLAYER, "", line.lineNumber);
        if (command.equals("beri makan pemain") || command.equals("feed pemain")) return new Action(Action.ActionType.FEED_PLAYER, "", line.lineNumber);

        if (command.startsWith("setel") || command.startsWith("atur variabel")) {
            String varName = extractVariableName(actionLine);
            String val = strings.isEmpty() ? "" : strings.get(0);
            return new Action(Action.ActionType.SET_VARIABLE, varName, new String[]{val}, line.lineNumber);
        }
        if (command.startsWith("tambah")) {
            return new Action(Action.ActionType.ADD_VARIABLE, extractVariableName(actionLine), new String[]{extractNumber(command)}, line.lineNumber);
        }
        if (command.startsWith("kurangi")) {
            return new Action(Action.ActionType.SUBTRACT_VARIABLE, extractVariableName(actionLine), new String[]{extractNumber(command)}, line.lineNumber);
        }

        plugin.getLogger().warning(filename + " line " + line.lineNumber + ": Unknown action: " + actionLine);
        return null;
    }

    private boolean isEventTrigger(String line) {
        String lower = line.toLowerCase();
        return lower.startsWith("saat ") || lower.startsWith("ketika ");
    }

    private boolean isCommandDeclaration(String line) {
        return line.toLowerCase().startsWith("perintah ");
    }

    private boolean isCondition(String line) {
        String lower = line.toLowerCase().trim();
        return lower.startsWith("jika ") && !lower.startsWith("jika tidak");
    }

    private int findNextBlockStart(List<IndentedLine> lines, int currentIndex) {
        int baseLevel = lines.get(currentIndex).indentLevel;
        for (int i = currentIndex + 1; i < lines.size(); i++) {
            if (lines.get(i).indentLevel <= baseLevel && !lines.get(i).content.isEmpty()) return i;
        }
        return lines.size();
    }

    private String extractCommandName(String line) {
        if (!line.toLowerCase().startsWith("perintah ")) return null;
        String remainder = line.substring(8).trim();
        if (remainder.endsWith(":")) remainder = remainder.substring(0, remainder.length() - 1).trim();
        String name = remainder.split("\\s+")[0];
        return name.startsWith("/") ? name.substring(1).toLowerCase() : name.toLowerCase();
    }

    private String extractQuotedString(String line) {
        List<String> strings = ScriptLexer.extractStrings(line);
        return strings.isEmpty() ? null : strings.get(0);
    }

    private String extractVariableName(String line) {
        int start = line.indexOf("{"), end = line.indexOf("}");
        return (start != -1 && end > start) ? line.substring(start + 1, end) : "";
    }

    private List<String> extractCommandArguments(String line) {
        List<String> args = new ArrayList<>();
        String remainder = line.substring(8).trim();
        if (remainder.endsWith(":")) remainder = remainder.substring(0, remainder.length() - 1).trim();
        String[] parts = remainder.split("\\s+");
        for (int i = 1; i < parts.length; i++) args.add(parts[i]);
        return args;
    }

    private String extractNumber(String text) {
        for (String part : text.split("\\s+")) {
            try { Double.parseDouble(part); return part; } catch (NumberFormatException ignored) {}
        }
        return "1";
    }

    private Material parseMaterial(String name) {
        try { return Material.valueOf(name.toUpperCase().replace(" ", "_")); } 
        catch (Exception e) { return null; }
    }

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