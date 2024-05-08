import java.util.HashMap;
import java.util.List;

public class Syntax_Analyzer {
    private List<Token> tokens;
    private int currentTokenIndex;
    private Token currentToken;
    private HashMap<String , String > symbolType;
    public Syntax_Analyzer(List <Token> tokens){
        this.tokens = tokens;
        currentTokenIndex = 0;
        currentToken= tokens.get(0);
    }


    public void parse() {
        parseStatement();
        if (currentToken.getType().equals("EOF")) {
            System.out.println("Parsing successful!");
        } else {
            System.out.println("Parsing failed. Unexpected token: " + currentToken.getValue());
        }
    }

    private void parseStatement() {
        if (currentToken.getValue().equals("if")) {
            parseIfStatement();
        }
        else if(currentToken.getValue().equals("switch")){
            // Parse other types of statements
            ParseSwitchCaseDeclaration();
        }
        else if(currentToken.getValue().equals("for")) {
            parseForLoop();
        }
        else if(currentToken.getValue().equals("while")){
            parseWhileLoop();
        }
    }
    private void parseWhileLoop() {
        matchByValue("while");  // Expects the "while" keyword
        matchByType("LEFT_PAREN");  // Expects a left parenthesis token
        parseCondition();                                         // Parses the condition expression
        matchByType("RIGHT_PAREN");  // Expects a right parenthesis token
        matchByType("LEFT_BRACE");  // Expects a left brace token
        while(! currentToken.getValue().equals("}") && !currentToken.getValue().equals("EOF") ) {
            parseStatement();
        }
        // Parse the loop body
//        while (currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("KEYWORD")) {
//            // Call relevant parsing methods based on the current token
//            // You may need to handle different statements or declarations within the loop body
//        }
        matchByType("RIGHT_BRACE");  // Expects a right brace token
    }

    private void parseForLoop() {
        matchByValue("for");
        matchByValue("(");
        parseInitialization();
        matchByValue(";");
        parseCondition();
        matchByValue(";");
        parseUpdate();
        matchByValue(")");
        matchByValue("{");
        while(! currentToken.getValue().equals("}") && !currentToken.getValue().equals("EOF")) {
            parseStatement();
        }
        matchByValue("}");
    }

    private void parseUpdate() {
        // Parse update part of the for loop
        if (currentToken.getType().equals("IDENTIFIER")) {
            matchByValue(currentToken.getValue());
            if(currentToken.getValue().equals("++") || currentToken.getValue().equals("--")){
                matchByValue(currentToken.getValue());
            } else if (currentToken.getValue().equals("+=") || currentToken.getValue().equals("-=")
                    || currentToken.getValue().equals("*=") || currentToken.getValue().equals("/=")) {
                parseSignIncrement();
            }
        } else if (currentToken.getValue().equals("++") || currentToken.getValue().equals("--")) {
            parseIncrementDecrement();
        } else {
            throw new RuntimeException("Expected a valid update expression at index " + currentTokenIndex);
        }
    }

    private void parseSignIncrement() {
        matchByValue(currentToken.getValue());
        matchByType("NUMBER");
    }

    private void parseIncrementDecrement() {
        // Parse increment or decrement
        matchByValue(currentToken.getValue());
        matchByType("IDENTIFIER"); // Match variable name
    }

    private void parseInitialization() {
        // Parse initialization part of the for loop
        if (currentToken.getValue().equals("int")) {
            //parseDeclaration();
        } else if(currentToken.getType().equals("IDENTIFIER")) {
            // yehia hyzwdha w zawed li nfsak fyha sa3etha eno y tchedk eno numeric literal
            String type = symbolType.get(currentToken.getValue());
            if(type.isEmpty()){
                throw new RuntimeException("variable is not initialized");
            }
            else if(type.equals("int")) {
                //parseExpression();
            }
            else{
                throw new RuntimeException("Expected int type at index " + currentTokenIndex);
            }
        }
        else{
            throw new RuntimeException("Expected int type at index " + currentTokenIndex);
        }
    }

    private void parseIfStatement() {
        matchByValue("if");
        matchByValue("(");
        parseCondition();
        matchByValue(")");
        parseBlock();
        if (currentToken.getValue().equals("else")) {
            matchByValue("else");
            if (currentToken.getValue().equals("if")) {
                // Parse else if
                parseIfStatement();
            } else {
                // Parse else block
                parseBlock();
            }
        }
    }

    private void parseCondition() {
        parseExpression();
        if (currentToken.getType().equals("GREATER_THAN_OR_EQUALS") ||
                currentToken.getType().equals("EQUALS") || currentToken.getType().equals("NOT_EQUALS") ||
                currentToken.getType().equals("LESS_THAN_OR_EQUALS") ||
                currentToken.getType().equals("LESS_THAN") || currentToken.getType().equals("GREATER_THAN")) {
            matchByValue(currentToken.getValue());
        } else {
            throw new RuntimeException("Expected relational operator at index " + currentTokenIndex);
        }
        parseExpression();
    }

    private void parseExpression() {
        // Parse the first simple expression
        parseSimpleExpression();
        // LESA BA2Y OPERATORSS
        // While there are more tokens to process and the current token is an operator
        while ((currentToken.getValue().equals("+") || currentToken.getValue().equals("-") ||currentToken.getValue().equals("*") ||
                currentToken.getValue().equals("/") || currentToken.getValue().equals("!") || currentToken.getValue().equals("||")
                || currentToken.getValue().equals("&&") )) {
            if(currentToken.getValue().equals("||") || currentToken.getValue().equals("&&")){
                matchByValue(currentToken.getValue());
                parseCondition();
                break;
            }
            // Match the current operator token
            matchByValue(currentToken.getValue());

            // Parse the next simple expression
            parseSimpleExpression();
        }
    }

    private void parseSimpleExpression() {
        if (currentToken.getType().equals("IDENTIFIER")) {
            matchByValue(currentToken.getValue());
        } else if (currentToken.getType().equals("NUMBER")) {
            matchByValue(currentToken.getValue());
        } else if (currentToken.getType().equals("STRING")) {
            matchByValue(currentToken.getValue());
        } else if (currentToken.getType().equals("CHAR")) {
                matchByValue(currentToken.getValue());
        } else if (currentToken.getValue().equals("(")) {
            matchByValue("(");
            parseExpression();
            matchByValue(")");
        } else {
            throw new RuntimeException("Invalid expression at index " + currentTokenIndex);
        }
    }
    private void parseBlock() {
        // Parse block of statements
        matchByValue("{");

        while (!currentToken.getValue().equals("}") && !currentToken.getValue().equals("EOF")) {
            if (currentToken.getValue().equals("if")) {
                parseIfStatement(); // Parse nested if statement
            } else {
                parseStatement(); // Parse other statements
            }
        }
        matchByValue("}");

    }
    private void ParseSwitchCaseDeclaration() {
                // Match the 'switch' keyword
                matchByValue("switch");

                // Match the opening parenthesis after 'switch'
                matchByValue("(");

                // Now, expect an expression after '('
                String varType = symbolType.get(currentToken.getValue());
                matchByType("IDENTIFIER"); // Assuming IDENTIFIER represents the expression

                // Match the closing parenthesis after the expression
                matchByValue(")");

                // Match the opening curly brace after the expression
                matchByValue("{");

                // Detect the switch-case statements inside the switch block
                detectSwitchCaseInsideBlock(varType);

                // Match the closing curly brace after the switch block
                matchByValue("}");
        }
    private void detectSwitchCaseInsideBlock(String type) {
                // Match the 'case' keyword
        while(!(currentToken.getValue().equals("}") || currentToken.getValue().equals("default") || currentToken.getValue().equals("EOF "))) {
            matchByValue("case");


            // Now, expect a constant after 'case' *if int u need to make it in analyzer*
            if (type.equals("int")) {
                matchByType("NUMBER");
            } else if (type.equals("float")) {
                matchByType("FLOAT_NUMBER");
            } else if (type.equals("char")) {
                matchByType("CHAR");
            } else if (type.equals("string")) {
                matchByType("STRING");
            }
            // Match the colon after the constant
            matchByType("COLON");
            parseStatement();
            if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("break")) {
                // Match the 'break' keyword
                matchByValue( "break");
                // Match the semicolon after 'break'
                matchByType("SEMICOLON");
            }
        }
            if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("default")) {
                // Match the 'default' keyword
                matchByValue("default");
                // Match the colon after 'default'
                matchByType("COLON");
                parseStatement();
            }
        }



    private void matchByValue(String expectedValue) {
        if (currentToken.getValue().equals(expectedValue)) {
            advance();
        } else {
            throw new RuntimeException("Expected '" + expectedValue + "' at index " + currentTokenIndex);
        }
    }
    private void matchByType(String expectedValue) {
        if (currentToken.getType().equals(expectedValue)) {
            advance();
        } else {
            throw new RuntimeException("Expected '" + expectedValue + "' at index " + currentTokenIndex);
        }
    }

    private void advance() {
        currentTokenIndex++;
        currentToken = tokens.get(currentTokenIndex);
        if (currentToken.getType().equalsIgnoreCase("WHITESPACE"))
            currentTokenIndex++;

        if (currentTokenIndex < tokens.size()) {
            currentToken = tokens.get(currentTokenIndex);

        } else {
            // Handle error: reached end of tokens
            throw new RuntimeException("You reached the end of file");
        }
    }

}
