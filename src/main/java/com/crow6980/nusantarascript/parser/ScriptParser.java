package com.crow6980.nusantarascript.parser;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.command.CustomCommand;
import com.crow6980.nusantarascript.condition.Condition;
import com.crow6980.nusantarascript.condition.ConditionalBlock;
import com.crow6980.nusantarascript.script.Action;
import com.crow6980.nusantarascript.script.EventHandler;
import com.crow6980.nusantarascript.script.Script;


import java.util.ArrayList;
import java.util.List;

public class ScriptParser {

    
    private static final int INDENT_SIZE = 4;

    public ScriptParser(NusantaraScript plugin) {
        // We can pass the plugin instance if we need to access registries or utilities during parsing
    }

    public Script parse(String filename, List<String> rawLines) {
        List<IndentedLine> lines = parseIndentation(rawLines);
        if (lines.isEmpty()) return null;

        List<EventHandler> eventHandlers = new ArrayList<>();
        List<CustomCommand> customCommands = new ArrayList<>();

        int i = 0;
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            if (line.content.isEmpty() || line.content.startsWith("#")) {
                i++; continue;
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
            result.add(new IndentedLine(line.trim(), spaces / INDENT_SIZE, i + 1));
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

            if (line.indentLevel == 1) {
                if (isCondition(line.content)) {
                    ConditionalParseResult result = parseConditionalBlock(lines, i, filename);
                    if (result.block != null) handler.addConditionalBlock(result.block);
                    i = result.nextIndex;
                    continue;
                } else {
                    Action action = parseAction(line, filename);
                    if (action != null) handler.addAction(action);
                }
            }
            i++;
        }
        return handler;
    }

    private ConditionalParseResult parseConditionalBlock(List<IndentedLine> lines, int startIndex, String filename) {
        IndentedLine conditionLine = lines.get(startIndex);
        Condition condition = parseCondition(conditionLine, filename);
        if (condition == null) return new ConditionalParseResult(null, startIndex + 1);

        ConditionalBlock block = new ConditionalBlock(condition, conditionLine.lineNumber);
        int i = startIndex + 1;

        // Parse standard "if" actions
        while (i < lines.size()) {
            IndentedLine line = lines.get(i);
            if (line.indentLevel <= conditionLine.indentLevel && !line.content.isEmpty()) break;
            if (line.indentLevel == conditionLine.indentLevel + 1 && !line.content.isEmpty()) {
                Action action = parseAction(line, filename);
                if (action != null) block.addAction(action);
            }
            i++;
        }

        // Parse "jika tidak" (else) or "jika..." (elseif)
        while (i < lines.size()) {
            IndentedLine next = lines.get(i);
            String lc = next.content.toLowerCase();
            
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
                if (elseifResult.block != null) {
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
            
            if (line.indentLevel == 1) {
                String lc = line.content.toLowerCase();
                if (lc.startsWith("izin:")) permission = extractQuotedString(line.content);
                else if (lc.startsWith("aksi:")) inActionBlock = true;
            } else if (line.indentLevel >= 2 && inActionBlock) {
                if (isCondition(line.content)) {
                    ConditionalParseResult result = parseConditionalBlock(lines, i, filename);
                    if (result.block != null) {
                        actions.add(new Action(Action.ActionType.NESTED_CONDITION, result.block, line.lineNumber));
                        i = result.nextIndex;
                        continue;
                    }
                } else {
                    Action action = parseAction(line, filename);
                    if (action != null) actions.add(action);
                }
            }
            i++;
        }
        CustomCommand cmd = new CustomCommand(commandName, argsDefs, permission, "Custom", commandLine.lineNumber);
        actions.forEach(cmd::addAction);
        return cmd;
    }

    private EventHandler parseEventTrigger(IndentedLine line, String filename) {
        String trigger = line.content.toLowerCase();
        if (trigger.endsWith(":")) trigger = trigger.substring(0, trigger.length() - 1).trim();
        
        EventHandler.EventType type = switch (trigger) {
            case "saat pemain masuk" -> EventHandler.EventType.PLAYER_JOIN;
            case "saat pemain chat" -> EventHandler.EventType.PLAYER_CHAT;
            case "saat pemain keluar" -> EventHandler.EventType.PLAYER_QUIT;
            case "saat blok dihancurkan" -> EventHandler.EventType.BLOCK_BREAK;
            default -> null;
        };
        return type != null ? new EventHandler(type, line.lineNumber) : null;
    }

    private Action parseAction(IndentedLine line, String filename) {
        String raw = line.content;
        String lower = raw.toLowerCase();
        List<String> strings = ScriptLexer.extractStrings(raw);

        if (lower.startsWith("kirim") && lower.contains("ke pemain")) 
            return new Action(Action.ActionType.SEND_MESSAGE, strings.isEmpty() ? "" : strings.get(0), line.lineNumber);
        
        if (lower.equals("berhenti")) {
            // Explicitly cast null to String to resolve ambiguity
            return new Action(Action.ActionType.STOP, (String) null, line.lineNumber);
        }
        if (lower.startsWith("beri_item")) {
            String data = raw.substring(9).trim();
            String[] parts = data.split(",");
            String mat = parts[0].trim();
            String amt = parts.length > 1 ? parts[1].trim() : "1";
            return new Action(Action.ActionType.GIVE_ITEM, mat, new String[]{amt}, line.lineNumber);
        }
        // Variables
        if (lower.startsWith("setel") || lower.startsWith("atur variabel")) {
            // Make sure this line is EXACTLY like this:
            String varName = extractVariableName(raw); 
            return new Action(Action.ActionType.SET_VARIABLE, varName, new String[]{strings.isEmpty() ? "0" : strings.get(0)}, line.lineNumber);
        }
        // 1. Broadcast / Umumkan
        if (lower.startsWith("broadcast") || lower.startsWith("umumkan")) {
            return new Action(Action.ActionType.BROADCAST, strings.isEmpty() ? "" : strings.get(0), line.lineNumber);
        }

        // 2. Play Sound / Suara
        if (lower.startsWith("suara")) {
            String sound = raw.substring(5).trim();
            return new Action(Action.ActionType.PLAY_SOUND, sound, line.lineNumber);
        }

        // 3. Cancel Event / Batalkan
        if (lower.equals("batalkan event") || lower.equals("cancel event")) {
            return new Action(Action.ActionType.CANCEL_EVENT, (String) null, line.lineNumber);
        }

        // 4. Heal & Feed
        if (lower.contains("pulihkan pemain") || lower.contains("heal pemain")) {
            return new Action(Action.ActionType.HEAL_PLAYER, (String) null, line.lineNumber);
        }
        if (lower.contains("beri makan pemain") || lower.contains("feed pemain")) {
            return new Action(Action.ActionType.FEED_PLAYER, (String) null, line.lineNumber);
        }

        // 5. Variable Math (Tambah/Kurangi)
        if (lower.startsWith("tambah")) {
            String varName = extractVariableName(raw);
            return new Action(Action.ActionType.ADD_VARIABLE, varName, new String[]{extractNumber(lower)}, line.lineNumber);
        }
        if (lower.startsWith("kurangi")) {
            String varName = extractVariableName(raw);
            return new Action(Action.ActionType.SUBTRACT_VARIABLE, varName, new String[]{extractNumber(lower)}, line.lineNumber);
        }
        // Add other actions (suara, setel, etc) here following the same pattern
        return null;
    }

    private Condition parseCondition(IndentedLine line, String filename) {
        String text = line.content;
        if (text.toLowerCase().startsWith("jika ")) text = text.substring(5).trim();
        if (text.endsWith(":")) text = text.substring(0, text.length() - 1).trim();

        if (text.contains(">") || text.contains("<") || text.contains("==")) {
            return new Condition.ExpressionCondition(text, line.lineNumber);
        }
        return null;
    }

    // --- UTILS ---

    private int findNextBlockStart(List<IndentedLine> lines, int currentIndex) {
        int baseLevel = lines.get(currentIndex).indentLevel;
        for (int i = currentIndex + 1; i < lines.size(); i++) {
            if (lines.get(i).indentLevel <= baseLevel && !lines.get(i).content.isEmpty()) return i;
        }
        return lines.size();
    }

    private boolean isEventTrigger(String line) { return line.toLowerCase().startsWith("saat "); }
    private boolean isCommandDeclaration(String line) { return line.toLowerCase().startsWith("perintah "); }
    private boolean isCondition(String line) { return line.toLowerCase().trim().startsWith("jika "); }

    private String extractCommandName(String line) {
        String name = line.substring(8).trim().split("\\s+|:")[0];
        return name.startsWith("/") ? name.substring(1).toLowerCase() : name.toLowerCase();
    }

    private List<String> extractCommandArguments(String line) {
        List<String> args = new ArrayList<>();
        String[] parts = line.split("\\s+");
        for(int i=2; i<parts.length; i++) args.add(parts[i].replace(":", ""));
        return args;
    }

    private String extractVariableName(String line) {
        int s = line.indexOf("{"), e = line.indexOf("}");
        return (s != -1 && e > s) ? line.substring(s+1, e) : "";
    }

    private String extractQuotedString(String t) {
        List<String> s = ScriptLexer.extractStrings(t);
        return s.isEmpty() ? "" : s.get(0);
    }

    private static class IndentedLine {
        final String content; final int indentLevel; final int lineNumber;
        IndentedLine(String c, int i, int l) { this.content = c; this.indentLevel = i; this.lineNumber = l; }
    }

    private static class ConditionalParseResult {
        final ConditionalBlock block; final int nextIndex;
        ConditionalParseResult(ConditionalBlock b, int n) { this.block = b; this.nextIndex = n; }
    }
    private String extractNumber(String text) {
    // Menghapus semua karakter kecuali angka dan titik desimal
    // Regex: [^0-9.] mencari apapun yang BUKAN angka atau titik dan menghapusnya
        String numeric = text.replaceAll("[^0-9.]", "").trim();
        return numeric.isEmpty() ? "0" : numeric;
    }
    // Add this to the bottom of ScriptParser.java if you don't have ScriptLexer
    
}