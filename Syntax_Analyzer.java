import java.util.HashMap;
import java.util.List;


public class Syntax_Analyzer {
    private List<Token> tokens;
    private HashMap<Integer,String> symbolTable;
    private HashMap<String,String> symbolType = new HashMap<>();
    private int currentTokenIndex;
    private Token currentToken;

    public Syntax_Analyzer(List<Token> tokens,HashMap<Integer,String> symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
        currentTokenIndex = 0;
        currentToken = tokens.get(0);
    }

    public void parse() {
 //       declaration();
//        operation();
        //function();
        Preprocessor();

        if (currentToken.getType().equals("EOF")) {
            System.out.println("Parsing successful!");
//            System.out.println("<----------SYMBOL Type---------->");
//            for(String i : symbolType.keySet()){
//                String type = symbolType.get(i);
//                System.out.println("Variable:  " + i+"   Type:  " + type);
//            }
        } else {
            System.out.println("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
    }

    private void declaration() {
        String tokenType = type();
        String identifier = identifier();
        symbolType.put(identifier,tokenType);
        if (currentToken.getType().equals("SEMICOLON")) {
            match("SEMICOLON");
        } else if (currentToken.getType().equals("ASSIGN_OP")) {
            match("ASSIGN_OP");

            // Check the authorized token type based on the declared type
            if (tokenType.equalsIgnoreCase("int")) {
                match("NUMBER");
            } else if (tokenType.equalsIgnoreCase("float")) {
                match("FLOAT_NUMBER");
            } else if (tokenType.equalsIgnoreCase("char")) {
                match("CHAR");
            } else if (tokenType.equalsIgnoreCase("double")) {
                match("FLOAT_NUMBER");
            } else if (tokenType.equalsIgnoreCase("long")) {
                match("NUMBER");
            } else if (tokenType.equalsIgnoreCase("short")) {
                match("NUMBER");
            }
            match("SEMICOLON");
        }
    }

    private void operation() {
        if (currentToken.getType().equals("IDENTIFIER")) {
            assignment();
        } else if (currentToken.getType().equals("integer") ||
                currentToken.getType().equals("float") ||
                currentToken.getType().equals("char")) {
            arithmeticOperation();
        } else {
            // Handle error: unexpected token
        }
    }

    private String type() {
        String tokenType = currentToken.getValue();
        if (currentToken.getValue().equalsIgnoreCase("int") ||
                currentToken.getValue().equalsIgnoreCase("float") ||
                currentToken.getValue().equalsIgnoreCase("char") ||
                currentToken.getValue().equalsIgnoreCase("double") ||
                currentToken.getValue().equalsIgnoreCase("long")||
                currentToken.getValue().equalsIgnoreCase("short")){
            advance();
            return tokenType;
        }
        return null;
    }
//    b(char|const|double|enum|float|int|long|short|static|struct|typedef|unsigned)

    private String identifier() {
        String identifier = currentToken.getValue();
        if (currentToken.getType().equals("IDENTIFIER")) {
            advance();
            return identifier;
        }
        return null;
    }

    private void assignment() {
        identifier();
        match("=");
        expression();
        match(";");
    }

    private void arithmeticOperation() {
        expression();
    }

    private void expression() {
        term();
        while (currentToken.getType().equals("+") || currentToken.getType().equals("-")) {
            match(currentToken.getType());
            term();
        }
    }

    private void term() {
        factor();
        while (currentToken.getType().equals("*") || currentToken.getType().equals("/")) {
            match(currentToken.getType());
            factor();
        }
    }

    private void factor() {
        if (currentToken.getType().equals("identifier") || currentToken.getType().equals("integer") ||
                currentToken.getType().equals("float") || currentToken.getType().equals("char")) {
            advance();
        } else if (currentToken.getType().equals("(")) {
            match("(");
            expression();
            match(")");
        }
    }

    private void match(String expectedType) {
        if (currentToken.getType().equals(expectedType)) {
            advance();
        }
    }

    private void advance() {
        currentTokenIndex++;
        currentToken = tokens.get(currentTokenIndex);
        if (currentToken.getType().equalsIgnoreCase("WHITESPACE"))
            currentTokenIndex++;

        if (currentTokenIndex < tokens.size()) {
            currentToken = tokens.get(currentTokenIndex);

        }
    }

    private void function()
    {
        String returnType = type();
        String functionName = identifier();
        match("LEFT_PAREN");
        parseParameters();
        match("RIGHT_PAREN");
        match("{");
        //parseFunctionBody();
        match("}");

    }

    private void parseParameters() {
        if (currentToken.getType().equals("RIGHT_PAREN")) {
            return;
        }
        String paramType = type();
        if (paramType == null) {
            System.out.println("Expected a type for parameter, but got: " + currentToken.getType());
            return;
        }
        String paramName = identifier();
        if (paramName == null) {
            System.out.println("Expected a parameter name, but got: " + currentToken.getType());
            return;
        }
        if (currentToken.getType().equals("COMMA")) {
            match("COMMA");
            if (currentToken.getType().equals("RIGHT_PAREN")) {
                System.out.println("Syntax error: Trailing comma in parameter list");
                return;
            }
            parseParameters();
        }
    }
    private void Preprocessor(){
        match("PRE_PROCESSOR_PATTERN");
        match("LESS_THAN");
        match("IDENTIFIER");
        match("GREATER_THAN");
    }
}

