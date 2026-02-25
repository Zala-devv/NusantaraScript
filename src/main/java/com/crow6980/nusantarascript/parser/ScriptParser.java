package com.crow6980.nusantarascript.parser;

import com.crow6980.nusantarascript.NusantaraScript;
import com.crow6980.nusantarascript.script.Script;
import com.crow6980.nusantarascript.script.EventHandler;
import com.crow6980.nusantarascript.script.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * STEP 3: Script Parser
 * 
 * The parser converts tokens into executable Script objects.
 * It understands the structure and meaning of the Indonesian syntax.
 * 
 * Parsing Process:
 * 1. Tokenize the raw lines using ScriptLexer
 * 2. Group tokens into EventHandlers (trigger + actions)
 * 3. Convert action tokens into Action objects
 * 4. Build a complete Script object
 * 
 * @author crow6980
 */
public class ScriptParser {
    
    private final NusantaraScript plugin;
    private final ScriptLexer lexer;
    
    public ScriptParser(NusantaraScript plugin) {
        this.plugin = plugin;
        this.lexer = new ScriptLexer();
    }
    
    /**
     * Parses a script file into an executable Script object
     * 
     * @param filename The name of the script file
     * @param lines The raw lines from the file
     * @return Parsed Script object, or null if parsing fails
     */
    public Script parse(String filename, List<String> lines) {
        // Step 1: Tokenize the lines
        List<ScriptLexer.Token> tokens = lexer.tokenize(lines);
        
        if (tokens.isEmpty()) {
            plugin.getLogger().warning("No valid tokens found in " + filename);
            return null;
        }
        
        // Step 2: Parse tokens into event handlers
        List<EventHandler> eventHandlers = parseEventHandlers(tokens, filename);
        
        if (eventHandlers.isEmpty()) {
            plugin.getLogger().warning("No event handlers found in " + filename);
            return null;
        }
        
        // Step 3: Create the Script object
        return new Script(filename, eventHandlers);
    }
    
    /**
     * Parses tokens into a list of EventHandlers
     * Each EventHandler represents one event trigger and its associated actions
     */
    private List<EventHandler> parseEventHandlers(List<ScriptLexer.Token> tokens, String filename) {
        List<EventHandler> handlers = new ArrayList<>();
        
        EventHandler currentHandler = null;
        
        for (ScriptLexer.Token token : tokens) {
            if (token.getType() == ScriptLexer.Token.Type.EVENT_TRIGGER) {
                // Save previous handler if exists
                if (currentHandler != null) {
                    handlers.add(currentHandler);
                }
                
                // Start new event handler
                currentHandler = parseEventTrigger(token, filename);
                
            } else if (token.getType() == ScriptLexer.Token.Type.ACTION) {
                // Add action to current handler
                if (currentHandler != null) {
                    Action action = parseAction(token, filename);
                    if (action != null) {
                        currentHandler.addAction(action);
                    }
                } else {
                    plugin.getLogger().warning(filename + " line " + token.getLineNumber() + 
                                             ": Action found without event trigger");
                }
            }
        }
        
        // Add the last handler
        if (currentHandler != null) {
            handlers.add(currentHandler);
        }
        
        return handlers;
    }
    
    /**
     * Parses an event trigger token into an EventHandler
     * 
     * Supported triggers:
     * - "saat pemain masuk:" -> PlayerJoinEvent
     * - "saat blok dihancurkan:" -> BlockBreakEvent
     * 
     * Easy to extend with more event types!
     */
    private EventHandler parseEventTrigger(ScriptLexer.Token token, String filename) {
        String trigger = token.getValue().toLowerCase();
        
        // Remove the trailing colon
        if (trigger.endsWith(":")) {
            trigger = trigger.substring(0, trigger.length() - 1).trim();
        }
        
        // Map Indonesian triggers to Bukkit event types
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
            plugin.getLogger().warning(filename + " line " + token.getLineNumber() + 
                                     ": Unknown event trigger: " + trigger);
            return null;
        }
        
        return new EventHandler(eventType, token.getLineNumber());
    }
    
    /**
     * Parses an action token into an Action object
     * 
     * Supported actions:
     * - kirim "text" ke pemain -> Send message to player
     * - broadcast "text" -> Broadcast to all players
     * 
     * Easy to extend with more action types!
     */
    private Action parseAction(ScriptLexer.Token token, String filename) {
        String actionLine = token.getValue();
        
        // Extract string literals from the action
        List<String> strings = ScriptLexer.extractStrings(actionLine);
        
        // Remove strings to get the command structure
        String command = ScriptLexer.removeStrings(actionLine).trim().toLowerCase();
        
        // Parse "kirim ... ke pemain" (send message to player)
        if (command.startsWith("kirim") && command.contains("ke pemain")) {
            if (strings.isEmpty()) {
                plugin.getLogger().warning(filename + " line " + token.getLineNumber() + 
                                         ": 'kirim' action requires a message in quotes");
                return null;
            }
            return new Action(Action.ActionType.SEND_MESSAGE, strings.get(0), token.getLineNumber());
        }
        
        // Parse "broadcast ..." (broadcast message)
        if (command.startsWith("broadcast") || command.startsWith("umumkan")) {
            if (strings.isEmpty()) {
                plugin.getLogger().warning(filename + " line " + token.getLineNumber() + 
                                         ": 'broadcast' action requires a message in quotes");
                return null;
            }
            return new Action(Action.ActionType.BROADCAST, strings.get(0), token.getLineNumber());
        }
        
        // Parse "batalkan event" (cancel event)
        if (command.equals("batalkan event") || command.equals("cancel event")) {
            return new Action(Action.ActionType.CANCEL_EVENT, "", token.getLineNumber());
        }
        
        plugin.getLogger().warning(filename + " line " + token.getLineNumber() + 
                                 ": Unknown action: " + actionLine);
        return null;
    }
}
