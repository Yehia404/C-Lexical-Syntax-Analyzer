import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;


public class Lexical_Analyzer {
    private static final String KEYWORD_PATTERN = "\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while)\\b";
    private static final String OPERATOR_PATTERN = "[-+*/%<>=!&|~^]+";
    private static final String IDENTIFIER_PATTERN = "[a-zA-Z_]\\w*";
    private static final String NUMBER_PATTERN = "\\d+";
    private static final String STRING_PATTERN = "\"([^\"]*)\"";
    private static final String SYMBOL_PATTERN = "[(){}\\[\\];,\\.]";
    public static void main(String[] args){
        String sourceCodeFile = "E:\\ASU\\Semester 6\\Compilers\\Project\\test.c";
        List <Token> tokens = tokenizeSourceCode(sourceCodeFile);
        for (Token token : tokens) {
            System.out.println(token);
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

        Pattern pattern = Pattern.compile(
                KEYWORD_PATTERN + "|" +
                        OPERATOR_PATTERN + "|" +
                        IDENTIFIER_PATTERN + "|" +
                        NUMBER_PATTERN + "|" +
                        STRING_PATTERN + "|" +
                        SYMBOL_PATTERN
        );
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String tokenValue = matcher.group();
            String tokenType;

            if (tokenValue.matches(KEYWORD_PATTERN)) {
                tokenType = "KEYWORD";
            } else if (tokenValue.matches(OPERATOR_PATTERN)) {
                tokenType = "OPERATOR";
            } else if (tokenValue.matches(IDENTIFIER_PATTERN)) {
                tokenType = "IDENTIFIER";
            } else if (tokenValue.matches(NUMBER_PATTERN)) {
                tokenType = "NUMBER";
            } else if (tokenValue.matches(STRING_PATTERN)) {
                tokenType = "STRING";
            } else if (tokenValue.matches(SYMBOL_PATTERN)) {
                tokenType = "SYMBOL";
            } else {
                tokenType = "UNKNOWN";
            }

            tokens.add(new Token(tokenType,lineNumber,tokenValue));
        }

        return tokens;
    }
}
