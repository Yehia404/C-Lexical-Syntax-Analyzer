package com.example.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Syntax_Analyzer {
    private List<Token> tokens;
    private ParseTreeNode currentParseNode;
    private HashMap<Integer,String> symbolTable;
    private static HashMap<String,String> symbolType = new HashMap<>();
    private HashMap<String,List<String>> funcParameters = new HashMap<>();
    private int currentTokenIndex;
    private Token currentToken;
    int nestingLevel = 0; // Start with 1 to account for the outermost block
    static{
        symbolType.put("printf","void");
        symbolType.put("scanf","void");
    }
    public Syntax_Analyzer(List<Token> tokens,HashMap<Integer,String> symbolTable) {
        this.tokens = tokens;
        this.currentParseNode = null;
        this.symbolTable = symbolTable;
        currentTokenIndex = 0;
        currentToken = tokens.get(0);
    }

    private void createParseNode(String type, String value) {
        ParseTreeNode node = new ParseTreeNode(type, value);
        if (currentParseNode != null) {
            currentParseNode.addChild(node);
        }
        currentParseNode = node;
    }

    private void moveUpInParseTree() {
        currentParseNode = currentParseNode.getParent();
    }
//    const/static/enum/typedef/struct
    public ParseTreeNode parse() {
        createParseNode("Program", "");
        while (!currentToken.getType().equals("EOF")) {
            if (currentToken.getType().equals("EOF")) {
                break;
            }
            if(currentToken.getType().equals("PRE_PROCESSOR_PATTERN")){
                createParseNode("PREPROCESSOR","");
                Preprocessor();
                while (!currentParseNode.getType().equals("Program")){
                    moveUpInParseTree();
                }
            }

            if (currentToken.getType().equals("KEYWORD")){
                createParseNode("Declaration","");
                String dataType;
                if (currentToken.getValue().equalsIgnoreCase("void")){
                     dataType = "void";
                     matchByType("KEYWORD");
                }
                else{
                     dataType = type();
                }

                String variable = identifier();
                if(dataType.equals("int") && variable.equals("main")){
                    matchByType("LEFT_PAREN");
                    matchByType("RIGHT_PAREN");
                    parseMain();
                    while (!currentParseNode.getType().equals("Program")){
                        moveUpInParseTree();
                    }
                }
                else {
                    symbolType.put(variable, dataType);
                    if (currentToken.getType().equals("LEFT_PAREN")) {
                        parseFunction(variable,dataType);
                    } else {
                        declaration(dataType);
                    }
                    while (!currentParseNode.getType().equals("Program")){
                        moveUpInParseTree();
                    }
                }
            }
            else if(currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("INCREMENT") || currentToken.getType().equals("DECREMENT")){
                createParseNode("Assignment","");
                parseAssignment();
                while (!currentParseNode.getType().equals("Program")){
                    moveUpInParseTree();
                }
            }
//            else{
//                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
//            }
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
            return currentParseNode;
        } else {
            System.out.println("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
        return null;
    }

    private void declaration(String tokenType) {
        if (currentToken.getType().equals("SEMICOLON")) { //Declaration only
            matchByType("SEMICOLON");
            return;
        } else if (currentToken.getType().equals("ASSIGN_OP") || currentToken.getType().equals("ADD_ASSIGN") || currentToken.getType().equals("SUB_ASSIGN") || currentToken.getType().equals("MULTIPLY_ASSIGN") || currentToken.getType().equals("DIVIDE_ASSIGN")) { // Declaration with initialization
            if(currentToken.getType().equals("ASSIGN_OP"))
                matchByType("ASSIGN_OP");
            if(currentToken.getType().equals("ADD_ASSIGN"))
                matchByType("ADD_ASSIGN");
            if(currentToken.getType().equals("SUB_ASSIGN"))
                matchByType("SUB_ASSIGN");
            if(currentToken.getType().equals("DIVIDE_ASSIGN"))
                matchByType("DIVIDE_ASSIGN");
            if(currentToken.getType().equals("ASSIGN_ASSIGN"))
                matchByType("MULTIPLY_ASSIGN");


            if (tokenType.equalsIgnoreCase("char")) { // Declaration with char initialization
                matchByType("CHAR");
            }
            else if (currentToken.getType().equals("IDENTIFIER")){ // Declaration with function call
                String functionName = currentToken.getValue() ;
                String functionType = symbolType.get(functionName);
                if (functionType == null){
                    throw new RuntimeException("Parsing failed. Unexpected token (Variable or function is not defined): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
                advance();
                if (currentToken.getType().equals("LEFT_PAREN")){
                    retract();
                    matchByType("IDENTIFIER");
                    if (tokenType.equals(functionType)) {
                        matchByType("LEFT_PAREN");
                        List<String> parameters;
                        parameters = funcParameters.get(functionName);
                        int paramLength = parameters.size();
                        int argumentCounter = 0;
                        int typeCounter = 0;

                        while (!(currentToken.getType().equals("RIGHT_PAREN"))) {
                            String paramName;
                            String paramType;
                            if(currentToken.getType().equals("IDENTIFIER")){
                                paramName = identifier();
                                if (paramName == null) {
                                    throw new RuntimeException("Parsing failed. Unexpected token (Expected an argument name): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                                }
                                paramType = symbolType.get(paramName);
                            }
                            else if (currentToken.getType().equals("NUMBER")){
                                paramType = "int";
                                matchByType("NUMBER");
                            }
                            else if(currentToken.getType().equals("FLOAT_NUMBER")){
                                paramType = "float";
                                matchByType("FLOAT_NUMBER");
                            }
                            else if (currentToken.getType().equals("CHAR")){
                                paramType = "char";
                                matchByType("CHAR");
                            }
                            else{
                                throw new RuntimeException("Parsing failed. Unexpected token (Invalid Argument Datatype), " + "Line Number: " + currentToken.getLineNumber());
                            }

                            if (argumentCounter == parameters.size())
                                throw new RuntimeException("Parsing failed. Unexpected token (Invalid Number of arguments), " + "Line Number: " + currentToken.getLineNumber());

                            if (!(paramType.equals(parameters.get(typeCounter))))
                                throw new RuntimeException("Parsing failed. Unexpected token (Invalid argument Type) " + " Line Number: " + currentToken.getLineNumber());

                            typeCounter++;
                            argumentCounter++;

                            if (currentToken.getType().equals("COMMA")) {
                                matchByType("COMMA");
                                if (currentToken.getType().equals("RIGHT_PAREN")) {
                                    throw new RuntimeException("Parsing failed. Unexpected token (Trailing comma in argument list): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                                }

                            }
                        }
                        if (argumentCounter < paramLength || argumentCounter > paramLength) {
                            throw new RuntimeException("Parsing failed. Unexpected token (Invalid Number of arguments), " + "Line Number: " + currentToken.getLineNumber());
                        }

                        matchByType("RIGHT_PAREN");
                    }
                    else {
                        throw new RuntimeException("Parsing failed. Unexpected token (Function data type is different): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                    }
                }
                else{
                    retract();
                    parseNumOperation(tokenType);
                }


            }
            else if (currentToken.getType().equals("NUMBER") || currentToken.getType().equals("FLOAT_NUMBER")){ // Declaration with Number (Operations)
                parseNumOperation(tokenType);
            }
            else {
                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }

        }else if (currentToken.getType().equals("IDENTIFIER")){
            String functionName = currentToken.getValue();
            matchByType("IDENTIFIER");
            if(currentToken.getType().equals("LEFT_PAREN")){
                matchByType("LEFT_PAREN");
                List<String> parameters;
                parameters = funcParameters.get(functionName);
                int paramLength = parameters.size();
                int argumentCounter = 0;
                int typeCounter = 0;

                while (!(currentToken.getType().equals("RIGHT_PAREN"))) {
                    String paramName;
                    String paramType;
                    if(currentToken.getType().equals("IDENTIFIER")){
                        paramName = identifier();
                        if (paramName == null) {
                            throw new RuntimeException("Parsing failed. Unexpected token (Expected an argument name): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                        }
                        paramType = symbolType.get(paramName);
                        if (paramType == null) {
                            throw new RuntimeException("Parsing failed. Unexpected token (Invalid Argument): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                        }
                    }
                    else if (currentToken.getType().equals("NUMBER")){
                        paramType = "int";
                        matchByType("NUMBER");
                    }
                    else if(currentToken.getType().equals("FLOAT_NUMBER")){
                        paramType = "float";
                        matchByType("FLOAT_NUMBER");
                    }
                    else if (currentToken.getType().equals("CHAR")){
                        paramType = "char";
                        matchByType("CHAR");
                    }
                    else{
                        throw new RuntimeException("Parsing failed. Unexpected token (Invalid Argument Datatype), " + "Line Number: " + currentToken.getLineNumber());
                    }



                    if (argumentCounter == parameters.size())
                        throw new RuntimeException("Parsing failed. Unexpected token (Invalid Number of arguments), " + "Line Number: " + currentToken.getLineNumber());

                    if (!(paramType.equals(parameters.get(typeCounter))))
                        throw new RuntimeException("Parsing failed. Unexpected token (Invalid argument Type) " + " Line Number: " + currentToken.getLineNumber());

                    typeCounter++;
                    argumentCounter++;

                    if (currentToken.getType().equals("COMMA")) {
                        matchByType("COMMA");
                        if (currentToken.getType().equals("RIGHT_PAREN")) {
                            throw new RuntimeException("Parsing failed. Unexpected token (Trailing comma in argument list): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                        }

                    }
                }
                if (argumentCounter < paramLength || argumentCounter > paramLength) {
                    throw new RuntimeException("Parsing failed. Unexpected token (Invalid Number of arguments), " + "Line Number: " + currentToken.getLineNumber());
                }

                matchByType("RIGHT_PAREN");
            }
            else {
                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }

        }

        else if (currentToken.getType().equals("LEFT_BRACKET")) { // Array Declaration
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

    private void parseMain(){
            matchByType("LEFT_BRACE");
            createParseNode("Body","");

            while(nestingLevel>0){

                if (currentToken.getType().equals("KEYWORD")){
                    if(currentToken.getValue().equals("return")){
                        moveUpInParseTree();
                        createParseNode("Return","");
                        matchByValue("return");
                        if(currentToken.getType().equals("NUMBER")) {
                            matchByType("NUMBER");
                        }
                        matchByType("SEMICOLON");
                    }
                    else if (currentToken.getValue().equalsIgnoreCase("int") || currentToken.getValue().equalsIgnoreCase("float") || currentToken.getValue().equalsIgnoreCase("char") ||
                            currentToken.getValue().equalsIgnoreCase("double") || currentToken.getValue().equalsIgnoreCase("long")|| currentToken.getValue().equalsIgnoreCase("short")){
                        createParseNode("Declaration","");
                        String dataType = type();
                        String variable = identifier();
                        symbolType.put(variable, dataType);
                        declaration(dataType);
                        while (!currentParseNode.getType().equals("Body"))
                            moveUpInParseTree();
                    }
                    else if (currentToken.getValue().equals("if") || currentToken.getValue().equals("for") || currentToken.getValue().equals("while") || currentToken.getValue().equals("switch")){
                        createParseNode("Statement","");
                        parseStatement();
                        while (!currentParseNode.getType().equals("Body"))
                            moveUpInParseTree();
                    }
                }
                else if(currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("INCREMENT") || currentToken.getType().equals("DECREMENT")){
                    createParseNode("Assignment","");
                    parseAssignment();
                    while (!currentParseNode.getType().equals("Body"))
                        moveUpInParseTree();
                }
                else{
                    throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }

                if(currentToken.getType().equals("RIGHT_BRACE")){
                    moveUpInParseTree();
                    matchByType("RIGHT_BRACE");
                }
                else if (currentToken.getType().equals("EOF")){
                    throw new RuntimeException("Parsing failed. Unexpected token (Missing Brace): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
            }
    }

    private void parseAssignment(){
        boolean pre = false;
        if(currentToken.getType().equals("INCREMENT")){
            matchByType("INCREMENT");
            pre = true;
        }
        if(currentToken.getType().equals("DECREMENT")){
            matchByType("DECREMENT");
            pre=true;
        }
        String datatype;
        if (currentToken.getType().equals("IDENTIFIER")) {
            datatype = symbolType.get(currentToken.getValue());
            if (datatype == null){
                throw new RuntimeException("Parsing failed. Unexpected token (Variable not defined): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }

            if(datatype.equals("void")){
                if(currentToken.getValue().equals("scanf") || currentToken.getValue().equals("printf")){
                    matchByType("IDENTIFIER");
                    matchByType("LEFT_PAREN");
                    while(!currentToken.getType().equals("RIGHT_PAREN")){
                        matchByType("STRING_LITERAL");
                        if (currentToken.getType().equals("COMMA")){
                            matchByType("COMMA");
                        }
                        if(currentToken.getType().equals("IDENTIFIER")){
                            String dataType = symbolType.get(currentToken.getValue());
                            if(dataType == null){
                                throw new RuntimeException("Parsing failed. Unexpected token (Variable not defined): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                            }
                            matchByType("IDENTIFIER");
                            if (currentToken.getType().equals("COMMA")){
                                matchByType("COMMA");
                            }
                        }
                    }
                    matchByType("RIGHT_PAREN");
                    matchByType("SEMICOLON");

                }
                else{
                    declaration(datatype);
                }

            }

            else {
                matchByType("IDENTIFIER");
                if (pre) {
                    matchByType("SEMICOLON");
                } else if (currentToken.getType().equals("INCREMENT")) {
                    matchByType("INCREMENT");
                    matchByType("SEMICOLON");
                } else if (currentToken.getType().equals("DECREMENT")) {
                    matchByType("DECREMENT");
                    matchByType("SEMICOLON");
                } else if (currentToken.getType().equals("ASSIGN_OP") || currentToken.getType().equals("ADD_ASSIGN") || currentToken.getType().equals("SUB_ASSIGN") || currentToken.getType().equals("MULTIPLY_ASSIGN") || currentToken.getType().equals("DIVIDE_ASSIGN")) {
                    declaration(datatype);
                }
            }

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
            matchByType("KEYWORD");
            return tokenType;
        }
        return null;
    }


    private String identifier() {
        String identifier = currentToken.getValue();
        if (currentToken.getType().equals("IDENTIFIER")) {
            matchByType("IDENTIFIER");
            return identifier;
        }
        return null;
    }

    private void matchArrayListContents(String tokentype, int size) {
        int count = 0;


        parseNumOperation(tokentype);

        count++;


        while (currentToken.getType().equals("COMMA")) {
            matchByType("COMMA");

            parseNumOperation(tokentype);

            count++;
        }


        if (count != size) {
            throw new RuntimeException("Parsing failed. Array size does not match the number of elements. Expected size: " + size + ", Actual count: " + count + " Line Number: " + currentToken.getLineNumber());
        }
    }

    private void parseNumOperation(String tokentype) {
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
            parseNumOperation(tokentype);
            matchByType("RIGHT_PAREN");
        } else {
            parseNumber(tokentype);
        }
    }


    private void parseNumber(String tokentype) {
        if(currentToken.getType().equals("IDENTIFIER")) {
            String tokenType = symbolType.get(currentToken.getValue());
            if (tokenType.equalsIgnoreCase("int")) {
                matchByType("IDENTIFIER");
            } else if (tokenType.equalsIgnoreCase("float") || tokenType.equalsIgnoreCase("double")) {
                matchByType("IDENTIFIER");
            } else if (tokenType.equalsIgnoreCase("long") || tokenType.equalsIgnoreCase("short")) {
                matchByType("IDENTIFIER");
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


    private void parseFunction(String functionName,String functionType)
    {
        matchByType("LEFT_PAREN");
        parseParameters(functionName);
        matchByType("RIGHT_PAREN");
        matchByType("LEFT_BRACE");
        parseBody();
        while (!currentParseNode.getType().equals("Declaration")){
            moveUpInParseTree();
        }
        if(currentToken.getValue().equals("return")){
            createParseNode("Return","");
            matchByValue("return");
            if(!functionType.equals("void")){
                if(currentToken.getType().equals("IDENTIFIER")){
                    if(symbolType.get(currentToken.getValue()).equals(functionType)) {
                        matchByType("IDENTIFIER");
                    }
                    else {
                        throw new RuntimeException("Parsing failed. Unexpected token (Invalid return datatype): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                    }
                }
                else{
                    if(currentToken.getType().equals("NUMBER")) {
                        matchByType("NUMBER");
                    }
                }
            }
            moveUpInParseTree();
            matchByType("SEMICOLON");
        }
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
            symbolType.put(paramName,paramType);
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
    private void Preprocessor(){
        matchByType("PRE_PROCESSOR_PATTERN");  //matching
        matchByType("LESS_THAN");
        matchByType("IDENTIFIER");
        if(currentToken.getValue().equals(".")) {
            matchByType("DOT");
            matchByType("IDENTIFIER");
        }
        matchByType("GREATER_THAN");
    }

    private void parseBody() {
        createParseNode("Body","");
        ParseTreeNode parent = currentParseNode.getParent();
        while(!currentToken.getType().equals("RIGHT_BRACE") && !currentToken.getType().equals("EOF")){
            if (currentToken.getType().equals("KEYWORD")){
                if(currentToken.getValue().equals("return")){
                    break;
                }
                else if (currentToken.getValue().equalsIgnoreCase("int") || currentToken.getValue().equalsIgnoreCase("float") || currentToken.getValue().equalsIgnoreCase("char") ||
                        currentToken.getValue().equalsIgnoreCase("double") || currentToken.getValue().equalsIgnoreCase("long")|| currentToken.getValue().equalsIgnoreCase("short")){
                    createParseNode("Declaration","");
                    String dataType = type();
                    String variable = identifier();
                    symbolType.put(variable, dataType);
                    declaration(dataType);
                    while (!currentParseNode.getType().equals("Body"))
                        moveUpInParseTree();
                }
                else if (currentToken.getValue().equals("if") || currentToken.getValue().equals("for") || currentToken.getValue().equals("while") || currentToken.getValue().equals("switch")){
                    createParseNode("Statement","");
                    parseStatement();
                    while (!currentParseNode.getType().equals("Body"))
                        moveUpInParseTree();
                }
                else if (currentToken.getValue().equalsIgnoreCase("break")) {
                    matchByValue( "break");
                    matchByType("SEMICOLON");
                }
                else if (currentToken.getValue().equalsIgnoreCase("continue")) {
                    matchByValue( "continue");
                    matchByType("SEMICOLON");
                }
                else{
                    throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
            }
            else if(currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("INCREMENT") || currentToken.getType().equals("DECREMENT")){
                createParseNode("Assignment", "");
                parseAssignment();
                while (!currentParseNode.getType().equals("Body"))
                    moveUpInParseTree();
            }
            else {
                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
        }
        while (currentParseNode != parent)
            moveUpInParseTree();
    }
    private void parseCaseBody(){
        createParseNode("Body","");
        ParseTreeNode parent = currentParseNode.getParent();
        while((!currentToken.getValue().equals("case") || !currentToken.getValue().equals("default") || !currentToken.getType().equals("RIGHT_BRACE"))  && !currentToken.getType().equals("EOF")){
            if(currentToken.getValue().equals("case") || currentToken.getValue().equals("default") || currentToken.getType().equals("RIGHT_BRACE"))
                break;
            else if (currentToken.getType().equals("KEYWORD")){
                if(currentToken.getValue().equals("return")){
                    matchByValue("return");
                    if(currentToken.getType().equals("NUMBER")) {
                        matchByType("NUMBER");
                    }
                    matchByType("SEMICOLON");
                }
                else if (currentToken.getValue().equalsIgnoreCase("int") || currentToken.getValue().equalsIgnoreCase("float") || currentToken.getValue().equalsIgnoreCase("char") ||
                        currentToken.getValue().equalsIgnoreCase("double") || currentToken.getValue().equalsIgnoreCase("long")|| currentToken.getValue().equalsIgnoreCase("short")){
                    createParseNode("Declaration","");
                    String dataType = type();
                    String variable = identifier();
                    symbolType.put(variable, dataType);
                    declaration(dataType);
                    while (!currentParseNode.getType().equals("Body"))
                        moveUpInParseTree();
                }
                else if (currentToken.getValue().equals("if") || currentToken.getValue().equals("for") || currentToken.getValue().equals("while") || currentToken.getValue().equals("switch")){
                    createParseNode("Statement","");
                    parseStatement();
                    while (!currentParseNode.getType().equals("Body"))
                        moveUpInParseTree();
                }
                else if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("break")) {
                    matchByValue( "break");
                    matchByType("SEMICOLON");
                }
                else{
                    throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
            }
            else if(currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("INCREMENT") || currentToken.getType().equals("DECREMENT")){
                createParseNode("Assignment", "");
                parseAssignment();
                while (!currentParseNode.getType().equals("Body"))
                    moveUpInParseTree();
            }
            else {
                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
        }
        while (currentParseNode != parent)
            moveUpInParseTree();
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
        parseBody();
        if (currentToken.getValue().equals("return")){
            matchByValue("return");
            if(currentToken.getType().equals("NUMBER")) {
                matchByType("NUMBER");
            }
            matchByType("SEMICOLON");
        }
        matchByType("RIGHT_BRACE");  // Expects a right brace token
    }

    private void parseForLoop() {
        matchByValue("for");
        matchByValue("(");
        parseForInit();
        matchByValue(";");
        parseCondition();
        matchByValue(";");
        parseUpdate();
        matchByValue(")");
        matchByValue("{");
        parseBody();
        if (currentToken.getValue().equals("return")){
            matchByValue("return");
            if(currentToken.getType().equals("NUMBER")) {
                matchByType("NUMBER");
            }
            matchByType("SEMICOLON");
        }
        matchByValue("}");
    }

    private void parseUpdate() {
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
            throw new RuntimeException("Parsing Failed. Expected a valid update expression at line number: " + currentToken.getLineNumber());
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

    private void parseForInit() {
        if (currentToken.getValue().equals("int")) {
            matchByValue("int");
            symbolType.put(currentToken.getValue(),"int");
            matchByType("IDENTIFIER");
            matchByType("ASSIGN_OP");
            matchByType("NUMBER");
        }
        else if(currentToken.getType().equals("IDENTIFIER")) {
            String type = symbolType.get(currentToken.getValue());
            if(type == null){
                throw new RuntimeException("Parsing Failed. variable is not initialized: (" +currentToken.getValue()+ ") at line number: "+currentToken.getLineNumber());
            }
            else if(type.equals("int")) {
                matchByType("IDENTIFIER");
                if(currentToken.getType().equals("ASSIGN_OP")){
                    matchByType("ASSIGN_OP");
                    matchByType("NUMBER");
                }
            }
            else{
                throw new RuntimeException("Parsing Failed. Invalid variable datatype "+currentToken.getValue()+" at line number: " + currentToken.getLineNumber());
            }
        }
        else{
            throw new RuntimeException("Parsing Failed. Expected int type at line number: " + currentToken.getLineNumber());
        }
    }

    private void parseIfStatement() {
        matchByValue("if");
        matchByValue("(");
        parseCondition();
        matchByValue(")");
        matchByType("LEFT_BRACE");
        parseBody();
        if (currentToken.getValue().equals("return")){
            matchByValue("return");
            if(currentToken.getType().equals("NUMBER")) {
                matchByType("NUMBER");
            }
            matchByType("SEMICOLON");
        }
        matchByType("RIGHT_BRACE");
        if (currentToken.getValue().equals("else")) {
            matchByValue("else");
            if (currentToken.getValue().equals("if")) {
                // Parse else if
                parseIfStatement();
            } else {
                matchByType("LEFT_BRACE");
                parseBody();
                if (currentToken.getValue().equals("return")){
                    matchByValue("return");
                    if(currentToken.getType().equals("NUMBER")) {
                        matchByType("NUMBER");
                    }
                    matchByType("SEMICOLON");
                }
                matchByType("RIGHT_BRACE");
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
            throw new RuntimeException("Parsing Failed. Expected relational operator at line number: " + currentToken.getLineNumber());
        }
        parseExpression();
    }

    private void parseExpression() {
        parseSimpleExpression();
        while ((currentToken.getValue().equals("+") || currentToken.getValue().equals("-") ||currentToken.getValue().equals("*") ||
                currentToken.getValue().equals("/") || currentToken.getValue().equals("!") || currentToken.getValue().equals("||")
                || currentToken.getValue().equals("&&") || currentToken.getValue().equals("%"))) {
            if(currentToken.getValue().equals("||") || currentToken.getValue().equals("&&")){
                matchByValue(currentToken.getValue());
                parseCondition();
                break;
            }
            matchByValue(currentToken.getValue());

            parseSimpleExpression();
        }
    }

    private void parseSimpleExpression() {
        if (currentToken.getType().equals("IDENTIFIER")) {
            String varType = symbolType.get(currentToken.getValue());
            if(varType == null){
                throw new RuntimeException("Parsing Failed. variable is not initialized: (" +currentToken.getValue()+ ") at line number: "+currentToken.getLineNumber());
            }
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
            throw new RuntimeException("Parsing Failed. Invalid expression at line number: " + currentToken.getLineNumber());
        }
    }

    private void ParseSwitchCaseDeclaration() {
        // Match the 'switch' keyword
        matchByValue("switch");

        // Match the opening parenthesis after 'switch'
        matchByValue("(");

        // Now, expect an expression after '('
        String varType = symbolType.get(currentToken.getValue());
        if(varType == null){
            throw new RuntimeException("Parsing Failed. variable is not initialized: (" +currentToken.getValue()+ ") at line number: "+currentToken.getLineNumber());
        }
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
            parseCaseBody();
//            if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("break")) {
//                // Match the 'break' keyword
//                matchByValue( "break");
//                // Match the semicolon after 'break'
//                matchByType("SEMICOLON");
//            }
        }
        if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("default")) {
            // Match the 'default' keyword
            matchByValue("default");
            // Match the colon after 'default'
            matchByType("COLON");
            parseBody();
            if (currentToken.getValue().equals("return")){
                matchByValue("return");
                if(currentToken.getType().equals("NUMBER")) {
                    matchByType("NUMBER");
                }
                matchByType("SEMICOLON");
            }
        }
    }

    private void matchByType(String expectedType) {
        if (currentToken.getType().equals(expectedType)) {
            if(expectedType.equals("LEFT_BRACE"))
                nestingLevel++;
            if(expectedType.equals("RIGHT_BRACE"))
                nestingLevel--;
            createParseNode(currentToken.getType(),currentToken.getValue());
            moveUpInParseTree();
            advance();
        }
        else{
            throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
    }
    private void matchByValue(String value) {
        if (currentToken.getValue().equals(value)) {
            createParseNode(currentToken.getType(),currentToken.getValue());
            moveUpInParseTree();
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

