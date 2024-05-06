import java.util.List;

public class Syntax_Analyzer {
    private List<Token> tokens;
    private int currentTokenIndex;
    private Token currentToken;

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
    }

    private void parseIfStatement() {
        match("if");
        parseCondition();
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
        match("(");
        parseExpression();
        if (currentToken.getType().equals("GREATER_THAN_OR_EQUALS") ||
                currentToken.getType().equals("EQUALS") || currentToken.getType().equals("NOT_EQUALS") ||
                currentToken.getType().equals("LESS_THAN_OR_EQUALS")  ) {
            match(currentToken.getValue());
        } else {
            throw new RuntimeException("Expected relational operator at index " + currentTokenIndex);
        }
        parseExpression();
        match(")");
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
    private boolean ParseSwitchCaseDeclaration() {
        // Reset token index to the beginning
//        currentTokenIndex = 0;
//        currentToken = tokens.get(currentTokenIndex);

        // Look for the 'switch' keyword
        while (currentTokenIndex < tokens.size()) {
            if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("switch")) {
                // Match the 'switch' keyword
                match("switch");

                // Match the opening parenthesis after 'switch'
                matchType("OPEN_PARENTHESIS");

                // Now, expect an expression after '('
                matchType("IDENTIFIER"); // Assuming IDENTIFIER represents the expression

                // Match the closing parenthesis after the expression
                matchType("CLOSE_PARENTHESIS");

                // Match the opening curly brace after the expression
                matchType("OPEN_BRACE");

                // Detect the switch-case statements inside the switch block
                boolean switchCaseDetected = detectSwitchCaseInsideBlock();

                // Match the closing curly brace after the switch block
                matchType("CLOSE_BRACE");

                // Return true if switch-case statements were detected inside the block
                return switchCaseDetected;
            } else {
                // Advance to the next token
                advance();
            }
        }

        // Return false if no switch-case declaration is found
        return false;
    }

    private boolean detectSwitchCaseInsideBlock() {
        // Track whether any switch-case statements were detected
        boolean switchCaseDetected = false;

        // Iterate through the tokens inside the switch block
        while (currentTokenIndex < tokens.size()) {
            if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("case")) {
                // Match the 'case' keyword
                matchType("KEYWORD");

                // Now, expect a constant after 'case'
                matchType("CONSTANT");

                // Match the colon after the constant
                matchType("COLON");

                // Set switchCaseDetected to true since we found at least one case
                switchCaseDetected = true;
            } else if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("default")) {
                // Match the 'default' keyword
                matchType("KEYWORD");

                // Match the colon after 'default'
                matchType("COLON");

                // Set switchCaseDetected to true since we found the default case
                switchCaseDetected = true;
            } else if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("break")) {
                // Match the 'break' keyword
                matchType("KEYWORD");

                // Match the semicolon after 'break'
                matchType("SEMICOLON");
            } else {
                // Advance to the next token
                advance();
            }
        }

        // Return whether switch-case statements were detected inside the block
        return switchCaseDetected;
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
