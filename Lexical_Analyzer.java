import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Lexical_Analyzer {
    private static final List<String> KEYWORDS = Arrays.asList(
            "auto", "break", "case", "char", "const", "continue", "default", "do",
            "double", "else", "enum", "extern", "float", "for", "goto", "if",
            "int", "long", "register", "return", "short", "signed", "sizeof", "static",
            "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while"
    );

    private static final List<String> OPERATORS = Arrays.asList(
            "+", "-", "*", "/", "%", "=", "==", "!=", "<", ">", "<=", ">=", "&&", "||",
            "!", "&", "|", "^", "~", "<<", ">>", "++", "--", "+=", "-=", "*=", "/=", "%=",
            "&=", "|=", "^=", "<<=", ">>="
    );

    private static final List<Character> SPECIAL_SYMBOLS = Arrays.asList(
            '(', ')', '{', '}', '[', ']', ';', ',', '.'
    );

    private static final List<String> TYPES = Arrays.asList(
            "int", "char", "float", "double", "void"
    );
    public static void main(String[] args){
        String sourceCodeFile = " ";
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

    private static List<Token> tokenizeLine(String Line,int lineNumber){
        List<Token> tokens = new ArrayList<>();
//        yehia
    }
}
