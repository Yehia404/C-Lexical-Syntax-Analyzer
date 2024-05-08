import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Syntax_Analyzer {
    private List<Token> tokens;
    private HashMap<Integer,String> symbolTable;
    private HashMap<String,String> symbolType = new HashMap<>();
    private HashMap<String,List<String>> funcParameters = new HashMap<>();
    private int currentTokenIndex;
    private Token currentToken;

    public Syntax_Analyzer(List<Token> tokens,HashMap<Integer,String> symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
        currentTokenIndex = 0;
        currentToken = tokens.get(0);
    }

    public void parse() {
        while (!currentToken.getType().equals("EOF")) {
            if (currentToken.getType().equals("EOF")) {
                break;
            }
            String tokenType = currentToken.getType();
            if (tokenType.equals("KEYWORD")){
                String dataType = type();
                String variable = identifier();
                symbolType.put(variable,dataType);
                if (currentToken.getType().equals("LEFT_PAREN")) {
                    function(variable);
                }
                else {
                    declaration(dataType);
                }
            }
        }


        if (currentToken.getType().equals("EOF")) {
            System.out.println("<----------SYMBOL Type---------->");
            for(String i : symbolType.keySet()){
                String type = symbolType.get(i);
                System.out.println("Variable:  " + i+"   Type:  " + type);
            }
            System.out.println("<---------- Function Parameters ---------->");
            for (String functionName : funcParameters.keySet()) {
                List<String> parameters = funcParameters.get(functionName);
                System.out.println("Function: " + functionName + "  Parameters: " + parameters + "  No of Parameters: " + parameters.size());
            }
            System.out.println("Parsing successful!");
        } else {
            System.out.println("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
    }

    private void declaration(String tokenType) {
        if (currentToken.getType().equals("SEMICOLON")) { //Declaration only
            matchByType("SEMICOLON");
            return;
        } else if (currentToken.getType().equals("ASSIGN_OP")) { // Declaration with initialization
            matchByType("ASSIGN_OP");

            if (tokenType.equalsIgnoreCase("char")) { // Declaration with char initialization
                matchByType("CHAR");
            }
            else if (currentToken.getType().equals("IDENTIFIER")){ // Declaration with function call
                String functionName = currentToken.getValue() ;
                String functionType = symbolType.get(functionName);
                if (functionType == null){
                    throw new RuntimeException("Parsing failed. Unexpected token (Function is not defined): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
                matchByType("IDENTIFIER");
                if (tokenType.equals(functionType)){
                    matchByType("LEFT_PAREN");
                    List<String> parameters;
                    parameters = funcParameters.get(functionName);
                    int paramLength = parameters.size();
                    int argumentCounter = 0;
                    int typeCounter = 0;

                    while(!(currentToken.getType().equals("RIGHT_PAREN"))) {

                        String paramName = identifier();
                        if (paramName == null) {
                            throw new RuntimeException("Parsing failed. Unexpected token (Expected an argument name): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                        }
                        String paramType = symbolType.get(paramName);

                        if(argumentCounter == parameters.size())
                            throw new RuntimeException("Parsing failed. Unexpected token (Invalid Number of arguments), " + "Line Number: " + currentToken.getLineNumber());

                        if(!(paramType.equals(parameters.get(typeCounter))))
                            throw new RuntimeException("Parsing failed. Unexpected token (Invalid argument Type): " + paramName + " Line Number: " + currentToken.getLineNumber());

                        typeCounter++;
                        argumentCounter++;

                        if (currentToken.getType().equals("COMMA")) {
                            matchByType("COMMA");
                            if (currentToken.getType().equals("RIGHT_PAREN")) {
                                throw new RuntimeException("Parsing failed. Unexpected token (Trailing comma in argument list): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                            }

                        }
                    }
                    if(argumentCounter < paramLength || argumentCounter > paramLength){
                        throw new RuntimeException("Parsing failed. Unexpected token (Invalid Number of arguments), " + "Line Number: " + currentToken.getLineNumber());
                    }

                    matchByType("RIGHT_PAREN");
                }
                else {
                    throw new RuntimeException("Parsing failed. Unexpected token (Function data type is different): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }

            }
            else if (currentToken.getType().equals("NUMBER") || currentToken.getType().equals("FLOAT_NUMBER")){ // Declaration with Number (Operations)
                parseExpression(tokenType);
            }
            else {
                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }

        } else if (currentToken.getType().equals("LEFT_BRACKET")) { // Array Declaration
            matchByType("LEFT_BRACKET");
            int size = Integer.parseInt(currentToken.getValue());
            if (currentToken.getType().equals("NUMBER")) {


                if (size < 0) {
                    throw new RuntimeException("Parsing failed. Unexpected token (Negative number in array declaration): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }

                matchByType("NUMBER");
            }
            matchByType("RIGHT_BRACKET");
            if (currentToken.getType().equals("SEMICOLON")){
                matchByType("SEMICOLON");
                return;
            }
            else if(currentToken.getType().equals("ASSIGN_OP")){
                matchByType("ASSIGN_OP");
                matchByType("LEFT_BRACE");
                matchArrayListContents(tokenType,size);
                matchByType("RIGHT_BRACE");
            }

        } else{
            throw new RuntimeException("Parsing failed. Unexpected token (Missing Semicolon): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
        matchByType("SEMICOLON");
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

    private void matchArrayListContents(String tokentype, int size) {
        int count = 0;

        // Match the initial element
        parseExpression(tokentype);

        count++;

        // Match the remaining elements
        while (currentToken.getType().equals("COMMA")) {
            matchByType("COMMA");

            parseExpression(tokentype);

            count++;
        }

        // Check if the count matches the expected size
        if (count != size) {
            throw new RuntimeException("Parsing failed. Array size does not match the number of elements. Expected size: " + size + ", Actual count: " + count + " Line Number: " + currentToken.getLineNumber());
        }
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


    private void function(String functionName)
    {
        matchByType("LEFT_PAREN");
        parseParameters(functionName);
        matchByType("RIGHT_PAREN");
        matchByType("LEFT_BRACE");
//        parseFunctionBody();
        matchByType("RIGHT_BRACE");

    }

    private void parseParameters(String functionName) {
        List<String> paramTypes = new ArrayList<>();
        while(!(currentToken.getType().equals("RIGHT_PAREN"))) {

            String paramType = type();
            if (paramType == null) {
                throw new RuntimeException("Parsing failed. Unexpected token (Expected a type for parameter): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
            String paramName = identifier();
            if (paramName == null) {
                throw new RuntimeException("Parsing failed. Unexpected token (Expected a parameter name): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
            paramTypes.add(paramType);
            if (currentToken.getType().equals("COMMA")) {
                matchByType("COMMA");
                if (currentToken.getType().equals("RIGHT_PAREN")) {
                    throw new RuntimeException("Parsing failed. Unexpected token (Trailing comma in parameter list): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }

            }
        }
        funcParameters.put(functionName,paramTypes);

    }

    private void matchByType(String expectedType) {
        if (currentToken.getType().equals(expectedType)) {
            advance();
        }
        else{
            throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
    }
    private void matchByValue(String value) {
        if (currentToken.getValue().equals(value)) {
            advance();
        }
        else{
            throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
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

    private void retract() {
        while (currentTokenIndex > 0) {
            currentTokenIndex--;

            if (!tokens.get(currentTokenIndex).getType().equalsIgnoreCase("WHITESPACE")) {
                currentToken = tokens.get(currentTokenIndex);
                break;
            }
        }
    }

}

