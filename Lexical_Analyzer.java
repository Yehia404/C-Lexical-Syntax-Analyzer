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

    private static HashMap<Integer, String> symbolTable;

    public static void main(String[] args){
        String sourceCodeFile = "test.c";
        symbolTable = new HashMap<>();
        List <Token> tokens = tokenizeSourceCode(sourceCodeFile);
        System.out.println("<------------------------------Tokens------------------------------>");
        for (Token token : tokens) {
            System.out.println(token);
        }
        System.out.println("<----------SYMBOL TABLE---------->");
        for(Integer i : symbolTable.keySet()){
            String var = symbolTable.get(i);
            System.out.println("index:  " + i+"   variable:  "+var);
        }
    }

    private static List<Token> tokenizeSourceCode(String sourceCodeFile){
        List<Token> tokens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceCodeFile))){
            String Line;
            int lineNumber = 1 ;
            while((Line = reader.readLine())!= null){
                tokens.addAll(tokenizeLine(Line,lineNumber));
                lineNumber++;
            }
        }
        catch (IOException e){
            System.out.println("File cannot be read");
        }
    return tokens;
    }

    private static List<Token> tokenizeLine(String line,int lineNumber){
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
                 tokenType = "OPERATOR";
            } else if (tokenValue.matches(PUNCTUATION_PATTERN)) {
                 tokenType = "SYMBOL";
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

}
