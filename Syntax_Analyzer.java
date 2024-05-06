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
        declaration();
//        operation();

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
            matchByType("SEMICOLON");
        } else if (currentToken.getType().equals("ASSIGN_OP")) {
            matchByType("ASSIGN_OP");

            if (tokenType.equalsIgnoreCase("char")) {
                matchByType("CHAR");
            }
            else{
                parseExpression(tokenType);
            }
            matchByType("SEMICOLON");
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
    private void parseExpression(String tokentype) {
        parseTerm(tokentype);
        while (currentToken.getType().equals("ADD_OP") || currentToken.getType().equals("MOD_OP") || currentToken.getType().equals("MUL_OP") ||
                currentToken.getType().equals("DIV_OP") || currentToken.getType().equals("SUB_OP") ) {
            String operator = currentToken.getValue();
            matchByValue(operator);
            parseTerm(tokentype);
        }
    }

    private void parseTerm(String tokentype) {
        parseFactor(tokentype);
        while ((currentToken.getType().equals("ADD_OP") || currentToken.getType().equals("MOD_OP") || currentToken.getType().equals("MUL_OP") ||
                currentToken.getType().equals("DIV_OP") || currentToken.getType().equals("SUB_OP") ) && !(currentToken.getType().equals("SEMICOLON"))) {
            String operator = currentToken.getValue();
            if (operator.equals("+")) {
                matchByType("ADD_OP");
                parseFactor(tokentype);
            }
            else if (operator.equals("-")) {
                matchByType("SUB_OP");
                parseFactor(tokentype);
            }
            else if (operator.equals("*")) {
                matchByType("MUL_OP");
                parseFactor(tokentype);
            }
            else if (operator.equals("/")) {
                matchByType("DIV_OP");
                parseFactor(tokentype);
            }
            else if (operator.equals("%")) {
                matchByType("MOD_OP");
                parseFactor(tokentype);
            }
            else {
                break;
            }
        }
    }

    private void parseFactor(String tokentype) {
        if (currentToken.getType().equals("LEFT_PAREN")) {
            matchByType("LEFT_PAREN");
            parseExpression(tokentype);
            matchByType("RIGHT_PAREN");
        } else {
            parseNumber(tokentype);
        }
    }


    private void parseNumber(String tokentype) {
        if(currentToken.getType().equals("IDENTIFIER")) {
            String tokenType = symbolType.get(currentToken.getValue());
            if (tokenType.equalsIgnoreCase("int")) {
                advance();
            } else if (tokenType.equalsIgnoreCase("float") || tokenType.equalsIgnoreCase("double")) {
                advance();
            } else if (tokenType.equalsIgnoreCase("long") || tokenType.equalsIgnoreCase("short")) {
                advance();
            }
        }
        else{
            if (tokentype.equalsIgnoreCase("int") || tokentype.equalsIgnoreCase("long") || tokentype.equalsIgnoreCase("short")) {
                matchByType("NUMBER");
            } else if (tokentype.equalsIgnoreCase("float") || tokentype.equalsIgnoreCase("double")) {
                matchByType("FLOAT_NUMBER");
            }
        }
    }


    private void matchByType(String expectedType) {
        if (currentToken.getType().equals(expectedType)) {
            advance();
        }
    }
    private void matchByValue(String value) {
        if (currentToken.getValue().equals(value)) {
            advance();
        }
    }


    private void advance() {
        currentTokenIndex++;

        if (currentTokenIndex < tokens.size()) {
            currentToken = tokens.get(currentTokenIndex);
            if (currentToken.getType().equalsIgnoreCase("WHITESPACE")) {
                currentTokenIndex++;
                currentToken = tokens.get(currentTokenIndex);
            }
        }
    }
}