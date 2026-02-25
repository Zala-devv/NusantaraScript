package com.crow6980.nusantarascript.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * STEP 3: Basic Lexer (Tokenizer)
 * 
 * The lexer breaks down Indonesian script text into tokens.
 * This is the first phase of parsing: converting raw text into meaningful pieces.
 * 
 * Token Types:
 * - EVENT_TRIGGER: Lines ending with ':' (e.g., "saat pemain masuk:")
 * - ACTION: Indented lines that represent actions
 * - TEXT: String literals in quotes
 * 
 * @author crow6980
 */
public class ScriptLexer {
    
    /**
     * Token class represents a single meaningful unit of script text
     */
    public static class Token {
        public enum Type {
            EVENT_TRIGGER,  // Event declaration: "saat pemain masuk:"
            ACTION,         // Action to execute: "kirim \"text\" ke pemain"
            UNKNOWN
        }
        
        private final Type type;
        private final String value;
        private final int lineNumber;
        private final int indentation;
        
        public Token(Type type, String value, int lineNumber, int indentation) {
            this.type = type;
            this.value = value;
            this.lineNumber = lineNumber;
            this.indentation = indentation;
        }
        
        public Type getType() { return type; }
        public String getValue() { return value; }
        public int getLineNumber() { return lineNumber; }
        public int getIndentation() { return indentation; }
        
        @Override
        public String toString() {
            return "Token{" + type + ", '" + value + "', line=" + lineNumber + ", indent=" + indentation + "}";
        }
    }
    
    /**
     * Tokenizes a list of script lines into tokens
     * 
     * @param lines The raw script lines
     * @return List of tokens ready for parsing
     */
    public List<Token> tokenize(List<String> lines) {
        List<Token> tokens = new ArrayList<>();
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1; // Human-readable line numbers start at 1
            
            // Calculate indentation (number of leading spaces)
            int indentation = getIndentation(line);
            String trimmed = line.trim();
            
            // Determine token type
            Token.Type type = determineTokenType(trimmed, indentation);
            
            // Create token
            Token token = new Token(type, trimmed, lineNumber, indentation);
            tokens.add(token);
        }
        
        return tokens;
    }
    
    /**
     * Determines the type of token based on the line content
     * 
     * Rules:
     * - Lines ending with ':' are EVENT_TRIGGER
     * - Indented lines are ACTION
     * - Everything else is UNKNOWN
     */
    private Token.Type determineTokenType(String trimmed, int indentation) {
        // Event triggers end with a colon
        if (trimmed.endsWith(":")) {
            return Token.Type.EVENT_TRIGGER;
        }
        
        // Actions are indented lines
        if (indentation > 0) {
            return Token.Type.ACTION;
        }
        
        return Token.Type.UNKNOWN;
    }
    
    /**
     * Counts the number of leading spaces in a line
     * Used to determine code blocks and hierarchy
     */
    private int getIndentation(String line) {
        int spaces = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                spaces++;
            } else if (c == '	') {
                spaces += 4; // Treat tab as 4 spaces
            } else {
                break;
            }
        }
        return spaces;
    }
    
    /**
     * Extracts string literals from an action line
     * Example: kirim "Selamat datang!" ke pemain
     * Returns: ["Selamat datang!"]
     */
    public static List<String> extractStrings(String line) {
        List<String> strings = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(line);
        
        while (matcher.find()) {
            strings.add(matcher.group(1));
        }
        
        return strings;
    }
    
    /**
     * Removes string literals from a line for easier parsing
     * Example: kirim "Selamat datang!" ke pemain
     * Returns: kirim  ke pemain
     */
    public static String removeStrings(String line) {
        return line.replaceAll("\"[^\"]*\"", "");
    }
}
