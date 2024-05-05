import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;


public class Lexical_Analyzer {
    private static final String PRE_PROCESSOR_PATTERN = "#\\s*\\w+";
    private static final String KEYWORD_PATTERN = "\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while)\\b";
    private static final String IDENTIFIER_PATTERN = "[a-zA-Z_]\\w*";
    private static final String NUMBER_PATTERN = "[-+]?0[bB][01]+|0[xX][0-9a-fA-F]+|0[0-7]*|[1-9]\\d*\\.?\\d*([eE][-+]?\\d+)?|\\.\\d+([eE][-+]?\\d+)?";
    private static final String OPERATOR_PATTERN = "[-+*/%<>=!&|~^]+";
    private static final String PUNCTUATION_PATTERN = "[(){}\\[\\];,\\.]";
    private static final String STRING_PATTERN = "\"([^\"]*)\"";
    private static final String CHAR_PATTERN = "'.'";
    private static final String COMMENT_PATTERN = "//.*|/\\*.*?\\*/";
    private static final String WHITESPACE_PATTERN = "\\s+";

    private static final Map<String, String> OPERATOR_TYPES = new HashMap<>();
    private static final Map<String, String> SYMBOL_TYPES = new HashMap<>();

    private  HashMap<Integer, String> symbolTable = new HashMap<>();
    private  List <Token> Tokens = new ArrayList<>();

    static {
        OPERATOR_TYPES.put("+", "ADD_OP");OPERATOR_TYPES.put("-", "SUB_OP");OPERATOR_TYPES.put("*", "MUL_OP");OPERATOR_TYPES.put("/", "DIV_OP");
        OPERATOR_TYPES.put("%", "MOD_OP");OPERATOR_TYPES.put("<", "LESS_THAN");OPERATOR_TYPES.put(">", "GREATER_THAN");OPERATOR_TYPES.put("=", "ASSIGN_OP");
        OPERATOR_TYPES.put("!", "NOT_OP");OPERATOR_TYPES.put("&", "BITWISE_AND");OPERATOR_TYPES.put("|", "BITWISE_OR");OPERATOR_TYPES.put("~", "BITWISE_NOT");
        OPERATOR_TYPES.put("^", "BITWISE_XOR");OPERATOR_TYPES.put("==", "EQUALS");OPERATOR_TYPES.put("!=", "NOT_EQUALS");OPERATOR_TYPES.put("<=", "LESS_THAN_OR_EQUALS");
        OPERATOR_TYPES.put(">=", "GREATER_THAN_OR_EQUALS");OPERATOR_TYPES.put("&&", "LOGICAL_AND");OPERATOR_TYPES.put("||", "LOGICAL_OR");OPERATOR_TYPES.put("++", "INCREMENT");
        OPERATOR_TYPES.put("--", "DECREMENT");OPERATOR_TYPES.put("*=", "MULTIPLY_ASSIGN");OPERATOR_TYPES.put("+=", "ADD_ASSIGN");OPERATOR_TYPES.put("-=", "SUBTRACT_ASSIGN");
        OPERATOR_TYPES.put("/=", "DIVIDE_ASSIGN");OPERATOR_TYPES.put("%=", "MOD_ASSIGN");

        SYMBOL_TYPES.put("(", "LEFT_PAREN");SYMBOL_TYPES.put(")", "RIGHT_PAREN");SYMBOL_TYPES.put("{", "LEFT_BRACE");
        SYMBOL_TYPES.put("}", "RIGHT_BRACE");SYMBOL_TYPES.put("[", "LEFT_BRACKET");SYMBOL_TYPES.put("]", "RIGHT_BRACKET");
        SYMBOL_TYPES.put(";", "SEMICOLON");SYMBOL_TYPES.put(",", "COMMA");SYMBOL_TYPES.put(".", "DOT");
    }


    public void lexicalAnalyze(String sourceCodeFile){
        Tokens = tokenizeSourceCode(sourceCodeFile);
        System.out.println("<------------------------------Tokens------------------------------>");
        for (Token token : Tokens) {
            System.out.println(token);
        }
        System.out.println("<----------SYMBOL TABLE---------->");
        for(Integer i : symbolTable.keySet()){
            String var = symbolTable.get(i);
            System.out.println("index:  " + i+"   variable:  "+var);
        }
    }

    private List<Token> tokenizeSourceCode(String sourceCodeFile){
        List<Token> tokens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceCodeFile))){
            String Line;
            int lineNumber = 1 ;
            while((Line = reader.readLine())!= null){
                tokens.addAll(tokenizeLine(Line,lineNumber));
                lineNumber++;
            }
            tokens.add(new Token("EOF", lineNumber, "EOF"));
        }
        catch (IOException e){
            System.out.println("File cannot be read");
        }
    return tokens;
    }

    private List<Token> tokenizeLine(String line,int lineNumber){
        List<Token> tokens = new ArrayList<>();

        Pattern commentPattern = Pattern.compile(COMMENT_PATTERN);
        Matcher commentMatcher = commentPattern.matcher(line);

        if (commentMatcher.find()) {
            tokens.add(new Token("COMMENT", lineNumber, line.substring(commentMatcher.start())));
            return tokens;
        }

        Pattern pattern = Pattern.compile(
                  PRE_PROCESSOR_PATTERN + "|"+
                        KEYWORD_PATTERN + "|" +
                        IDENTIFIER_PATTERN + "|" +
                        NUMBER_PATTERN + "|" +
                        OPERATOR_PATTERN + "|" +
                        PUNCTUATION_PATTERN + "|" +
                        STRING_PATTERN + "|" +
                        CHAR_PATTERN + "|" +
                        WHITESPACE_PATTERN
        );
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String tokenValue = matcher.group();
            String tokenType;
            if (tokenValue.matches(PRE_PROCESSOR_PATTERN)) {
                tokenType = "PRE_PROCESSOR_PATTERN";
            } else if (tokenValue.matches(KEYWORD_PATTERN)) {
                tokenType = "KEYWORD";
            } else if (tokenValue.matches(IDENTIFIER_PATTERN)) {
                 tokenType = "IDENTIFIER";
            }else if (tokenValue.matches(NUMBER_PATTERN)) {
                 tokenType = "NUMBER";
            } else if (tokenValue.matches(OPERATOR_PATTERN)) {
                 tokenType = OPERATOR_TYPES.get(tokenValue);
            } else if (tokenValue.matches(PUNCTUATION_PATTERN)) {
                 tokenType = SYMBOL_TYPES.get(tokenValue);
            } else if (tokenValue.matches(STRING_PATTERN)) {
                tokenType = "STRING";
            } else if (tokenValue.matches(CHAR_PATTERN)) {
                 tokenType = "CHAR";
            } else if (tokenValue.matches(WHITESPACE_PATTERN)) {
                tokenType = "WHITESPACE";
            } else {
                tokenType = "ERROR";
            }

            if(tokenType.equals("IDENTIFIER")){
                if(symbolTable.containsValue(tokenValue)) {
                    Token token = new Token(tokenType,lineNumber,tokenValue);
                    tokens.add(token);
                    continue;
                }
                Token token = new Token(tokenType,lineNumber,tokenValue,true);
                tokens.add(token);
                symbolTable.put(token.getHashIndex(),tokenValue);
            }
            else{
            tokens.add(new Token(tokenType,lineNumber,tokenValue));
            }
        }

        return tokens;
    }

    public HashMap<Integer, String> getSymbolTable() {
        return symbolTable;
    }

    public List<Token> getTokens() {
        return Tokens;
    }
}
