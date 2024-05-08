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
        match("while");  // Expects the "while" keyword
        matchType("LEFT_PAREN");  // Expects a left parenthesis token
        parseCondition();                                         // Parses the condition expression
        matchType("RIGHT_PAREN");  // Expects a right parenthesis token
        matchType("LEFT_BRACE");  // Expects a left brace token
        while(! currentToken.getValue().equals("}") && !currentToken.getValue().equals("EOF") ) {
            parseStatement();
        }
        // Parse the loop body
//        while (currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("KEYWORD")) {
//            // Call relevant parsing methods based on the current token
//            // You may need to handle different statements or declarations within the loop body
//        }
        matchType("RIGHT_BRACE");  // Expects a right brace token
    }

    private void parseForLoop() {
        match("for");
        match("(");
        parseInitialization();
        match(";");
        parseCondition();
        match(";");
        parseUpdate();
        match(")");
        match("{");
        while(! currentToken.getValue().equals("}") && !currentToken.getValue().equals("EOF")) {
            parseStatement();
        }
        match("}");
    }

    private void parseUpdate() {
        // Parse update part of the for loop
        if (currentToken.getType().equals("IDENTIFIER")) {
            match(currentToken.getValue());
            if(currentToken.getValue().equals("++") || currentToken.getValue().equals("--")){
                match(currentToken.getValue());
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
        match(currentToken.getValue());
        matchType("NUMBER");
    }

    private void parseIncrementDecrement() {
        // Parse increment or decrement
        match(currentToken.getValue());
        matchType("IDENTIFIER"); // Match variable name
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
        match("if");
        match("(");
        parseCondition();
        match(")");
        parseBlock();
        if (currentToken.getValue().equals("else")) {
            match("else");
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
            match(currentToken.getValue());
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
                match(currentToken.getValue());
                parseCondition();
                break;
            }
            // Match the current operator token
            match(currentToken.getValue());

            // Parse the next simple expression
            parseSimpleExpression();
        }
    }

    private void parseSimpleExpression() {
        if (currentToken.getType().equals("IDENTIFIER")) {
            match(currentToken.getValue());
        } else if (currentToken.getType().equals("NUMBER")) {
            match(currentToken.getValue());
        } else if (currentToken.getType().equals("STRING")) {
            match(currentToken.getValue());
        } else if (currentToken.getType().equals("CHAR")) {
                match(currentToken.getValue());
        } else if (currentToken.getValue().equals("(")) {
            match("(");
            parseExpression();
            match(")");
        } else {
            throw new RuntimeException("Invalid expression at index " + currentTokenIndex);
        }
    }
    private void parseBlock() {
        // Parse block of statements
        match("{");

        while (!currentToken.getValue().equals("}") && !currentToken.getValue().equals("EOF")) {
            if (currentToken.getValue().equals("if")) {
                parseIfStatement(); // Parse nested if statement
            } else {
                parseStatement(); // Parse other statements
            }
        }
        match("}");

    }
    private void ParseSwitchCaseDeclaration() {
                // Match the 'switch' keyword
                match("switch");

                // Match the opening parenthesis after 'switch'
                match("(");

                // Now, expect an expression after '('
                String varType = symbolType.get(currentToken.getValue());
                matchType("IDENTIFIER"); // Assuming IDENTIFIER represents the expression

                // Match the closing parenthesis after the expression
                match(")");

                // Match the opening curly brace after the expression
                match("{");

                // Detect the switch-case statements inside the switch block
                detectSwitchCaseInsideBlock(varType);

                // Match the closing curly brace after the switch block
                match("}");
        }
    private void detectSwitchCaseInsideBlock(String type) {
                // Match the 'case' keyword
        while(!(currentToken.getValue().equals("}") || currentToken.getValue().equals("default") || currentToken.getValue().equals("EOF "))) {
            match("case");


            // Now, expect a constant after 'case' *if int u need to make it in analyzer*
            if (type.equals("int")) {
                matchType("NUMBER");
            } else if (type.equals("float")) {
                matchType("FLOAT_NUMBER");
            } else if (type.equals("char")) {
                matchType("CHAR");
            } else if (type.equals("string")) {
                matchType("STRING");
            }
            // Match the colon after the constant
            matchType("COLON");
            parseStatement();
            if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("break")) {
                // Match the 'break' keyword
                match( "break");
                // Match the semicolon after 'break'
                matchType("SEMICOLON");
            }
        }
            if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("default")) {
                // Match the 'default' keyword
                match("default");
                // Match the colon after 'default'
                matchType("COLON");
                parseStatement();
            }
        }



    private void match(String expectedValue) {
        if (currentToken.getValue().equals(expectedValue)) {
            advance();
        } else {
            throw new RuntimeException("Expected '" + expectedValue + "' at index " + currentTokenIndex);
        }
    }
    private void matchType(String expectedValue) {
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
