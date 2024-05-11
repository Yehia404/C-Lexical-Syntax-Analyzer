import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Syntax_Analyzer {
    private List<Token> tokens;
    private HashMap<Integer,String> symbolTable;
    private HashMap<String,String> symbolType = new HashMap<>();
    private HashMap<String,List<String>> funcParameters = new HashMap<>();
    private int currentTokenIndex;
    private StringTreeAsTreeForTreeLayout tree;
    StringTreeNode root;
    private Token currentToken;
    int nestingLevel = 0; // Start with 1 to account for the outermost block

    public Syntax_Analyzer(List<Token> tokens,HashMap<Integer,String> symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
        currentTokenIndex = 0;
        currentToken = tokens.get(0);
    }
//    const/static/enum/typedef/struct
    public void parse() {
        Token program = new Token("program node",-12,"program");
        root = new StringTreeNode(program);

        while (!currentToken.getType().equals("EOF")) {
            StringTreeNode sub = null;
            if (currentToken.getType().equals("EOF")) {
                break;
            }

            if(currentToken.getType().equals("PRE_PROCESSOR_PATTERN")){
                Token preProc = new Token("program node",-2,"pre processor");
                sub = new StringTreeNode(preProc);
                StringTreeNode temp = new StringTreeNode(new Token("dummy node", -1, "dummy"));;
                sub.child = temp;
                Preprocessor(temp);
                if (root.child == null) {
                    root.child = sub; // Assign sub to root.child if it's the first node
                }
                sub = sub.sibling;
            }
            if (currentToken.getType().equals("KEYWORD")){
                String dataType;
                if (currentToken.getValue().equalsIgnoreCase("void")){
                     dataType = "void";
                     matchByType("KEYWORD",sub);

                }
                else{
                    Token preProc = new Token("program node",-2,"type");
                    sub = new StringTreeNode(preProc);
                    sub = sub.child;
                    StringTreeNode temp = sub;
                    dataType = type(sub);
                    sub = temp.sibling;
                }
                Token preProc = new Token("program node",-2,"identifier");
                sub = new StringTreeNode(preProc);
                sub = sub.child;
                StringTreeNode temp = sub;
                String variable = identifier(sub);
                sub = temp.sibling;
                if(dataType.equals("int") && variable.equals("main")){
                    Token main = new Token("program node",-2,"main");
                    sub = new StringTreeNode(main);
                    sub = sub.child;
                    temp = sub;
                    parseMain(sub);
                    sub = temp.sibling;
                }
                else {
                    symbolType.put(variable, dataType);
                    if (currentToken.getType().equals("LEFT_PAREN")) {
                        Token main = new Token("program node",-2,"function body");
                        sub = new StringTreeNode(main);
                        sub = sub.child;
                        temp = sub;
                        parseFunction(variable,dataType, sub);
                        sub = temp.sibling;
                    } else {
                        Token main = new Token("program node",-2,"declaration");
                        sub = new StringTreeNode(main);
                        sub = sub.child;
                        temp = sub;
                        declaration(dataType, sub);
                        sub = temp.sibling;
                    }
                }
            }
            else if(currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("INCREMENT") || currentToken.getType().equals("DECREMENT")){
                Token main = new Token("program node",-2,"declaration");
                sub = new StringTreeNode(main);
                sub = sub.child;
                StringTreeNode temp = sub;
                parseInitialization(sub);
                sub = temp.sibling;
            }

            else{
                //throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
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

    private void declaration(String tokenType, StringTreeNode sub) {
        if (currentToken.getType().equals("SEMICOLON")) { //Declaration only
            matchByType("SEMICOLON", sub);
            return;
        } else if (currentToken.getType().equals("ASSIGN_OP")) { // Declaration with initialization
            matchByType("ASSIGN_OP", sub);

            if (tokenType.equalsIgnoreCase("char")) { // Declaration with char initialization
                matchByType("CHAR", sub);
            }
            else if (currentToken.getType().equals("IDENTIFIER")){ // Declaration with function call
                String functionName = currentToken.getValue() ;
                String functionType = symbolType.get(functionName);
                if (functionType == null){
                    throw new RuntimeException("Parsing failed. Unexpected token (Variable or function is not defined): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
                matchByType("IDENTIFIER", sub);
                if (currentToken.getType().equals("LEFT_PAREN")){
                    if (tokenType.equals(functionType)) {
                        matchByType("LEFT_PAREN", sub);
                        List<String> parameters;
                        parameters = funcParameters.get(functionName);
                        int paramLength = parameters.size();
                        int argumentCounter = 0;
                        int typeCounter = 0;

                        while (!(currentToken.getType().equals("RIGHT_PAREN"))) {

                            String paramName = identifier(sub);
                            if (paramName == null) {
                                throw new RuntimeException("Parsing failed. Unexpected token (Expected an argument name): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                            }
                            String paramType = symbolType.get(paramName);

                            if (argumentCounter == parameters.size())
                                throw new RuntimeException("Parsing failed. Unexpected token (Invalid Number of arguments), " + "Line Number: " + currentToken.getLineNumber());

                            if (!(paramType.equals(parameters.get(typeCounter))))
                                throw new RuntimeException("Parsing failed. Unexpected token (Invalid argument Type): " + paramName + " Line Number: " + currentToken.getLineNumber());

                            typeCounter++;
                            argumentCounter++;

                            if (currentToken.getType().equals("COMMA")) {
                                matchByType("COMMA", sub);
                                if (currentToken.getType().equals("RIGHT_PAREN")) {
                                    throw new RuntimeException("Parsing failed. Unexpected token (Trailing comma in argument list): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                                }

                            }
                        }
                        if (argumentCounter < paramLength || argumentCounter > paramLength) {
                            throw new RuntimeException("Parsing failed. Unexpected token (Invalid Number of arguments), " + "Line Number: " + currentToken.getLineNumber());
                        }

                        matchByType("RIGHT_PAREN", sub);
                    }
                    else {
                        throw new RuntimeException("Parsing failed. Unexpected token (Function data type is different): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                    }
                }
                else{
                    retract();
                    parseNumOperation(tokenType, sub);
                }


            }
            else if (currentToken.getType().equals("NUMBER") || currentToken.getType().equals("FLOAT_NUMBER")){ // Declaration with Number (Operations)
                parseNumOperation(tokenType, sub);
            }
            else {
                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }

        } else if (currentToken.getType().equals("LEFT_BRACKET")) { // Array Declaration
            matchByType("LEFT_BRACKET",sub);
            int size = Integer.parseInt(currentToken.getValue());
            if (currentToken.getType().equals("NUMBER")) {


                if (size < 0) {
                    throw new RuntimeException("Parsing failed. Unexpected token (Negative number in array declaration): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }

                matchByType("NUMBER",sub);
            }
            matchByType("RIGHT_BRACKET",sub);
            if (currentToken.getType().equals("SEMICOLON")){
                matchByType("SEMICOLON",sub);
                return;
            }
            else if(currentToken.getType().equals("ASSIGN_OP")){
                matchByType("ASSIGN_OP",sub);
                matchByType("LEFT_BRACE",sub);

                Token main = new Token("program node",-2,"Array contents");
                sub = new StringTreeNode(main);
                sub = sub.child;
                StringTreeNode temp = sub;
                matchArrayListContents(tokenType,size,sub);
                sub = temp.sibling;

                matchByType("RIGHT_BRACE",sub);
            }

        } else{
            throw new RuntimeException("Parsing failed. Unexpected token (Missing Semicolon): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
        matchByType("SEMICOLON",sub);
    }

    private void parseMain(StringTreeNode sub){
            matchByType("LEFT_BRACE",sub);

            while(nestingLevel>0){

                if (currentToken.getType().equals("KEYWORD")){
                    if(currentToken.getValue().equals("return")){
                        matchByValue("return",sub);
                        if(currentToken.getType().equals("NUMBER")) {
                            matchByType("NUMBER",sub);
                        }
                        matchByType("SEMICOLON",sub);
                    }
                    else if (currentToken.getValue().equalsIgnoreCase("int") || currentToken.getValue().equalsIgnoreCase("float") || currentToken.getValue().equalsIgnoreCase("char") ||
                            currentToken.getValue().equalsIgnoreCase("double") || currentToken.getValue().equalsIgnoreCase("long")|| currentToken.getValue().equalsIgnoreCase("short")){
                        String dataType = type(sub);
                        String variable = identifier(sub);
                        symbolType.put(variable, dataType);
                        Token main = new Token("program node",-2,"declaration");
                        sub = new StringTreeNode(main);
                        sub = sub.child;
                        StringTreeNode temp = sub;
                        declaration(dataType, sub);
                        sub = temp.sibling;
                    }
                    else if (currentToken.getValue().equals("if") || currentToken.getValue().equals("for") || currentToken.getValue().equals("while") || currentToken.getValue().equals("switch")){
                        Token main = new Token("program node",-2,"Statement");
                        sub = new StringTreeNode(main);
                        sub = sub.child;
                        StringTreeNode temp = sub;
                        parseStatement(sub);
                        sub = temp.sibling;
                    }
                }
                else if(currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("INCREMENT") || currentToken.getType().equals("DECREMENT")){
                    Token main = new Token("program node",-2,"initialization");
                    sub = new StringTreeNode(main);
                    sub = sub.child;
                    StringTreeNode temp = sub;
                    parseInitialization(sub);
                    sub = temp.sibling;
                }
                else{
                    throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }

                if(currentToken.getType().equals("RIGHT_BRACE")){
                    matchByType("RIGHT_BRACE",sub);
                }
                else if (currentToken.getType().equals("EOF")){
                    throw new RuntimeException("Parsing failed. Unexpected token (Missing Brace): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
            }
    }

    private void parseInitialization(StringTreeNode sub){
        boolean pre = false;
        if(currentToken.getType().equals("INCREMENT")){
            matchByType("INCREMENT",sub);
            pre = true;
        }
        if(currentToken.getType().equals("DECREMENT")){
            matchByType("DECREMENT",sub);
            pre=true;
        }
        String datatype;
        if (currentToken.getType().equals("IDENTIFIER")) {
            datatype = symbolType.get(currentToken.getValue());
            if (datatype == null){
                throw new RuntimeException("Parsing failed. Unexpected token (Variable not defined): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
            matchByType("IDENTIFIER",sub);
            if(pre){
                matchByType("SEMICOLON",sub);
            }
            else if(currentToken.getType().equals("INCREMENT")){
                matchByType("INCREMENT",sub);
                matchByType("SEMICOLON",sub);
            }
            else if(currentToken.getType().equals("DECREMENT")){
                matchByType("DECREMENT",sub);
                matchByType("SEMICOLON",sub);
            }
            else if (currentToken.getType().equals("ASSIGN_OP")) {
                Token main = new Token("program node",-2,"declaration");
                sub = new StringTreeNode(main);
                sub = sub.child;
                StringTreeNode temp = sub;
                declaration(datatype, sub);
                sub = temp.sibling;
            }

        }
    }

    private String type(StringTreeNode sub) {
        String tokenType = currentToken.getValue();
        if (currentToken.getValue().equalsIgnoreCase("int") ||
                currentToken.getValue().equalsIgnoreCase("float") ||
                currentToken.getValue().equalsIgnoreCase("char") ||
                currentToken.getValue().equalsIgnoreCase("double") ||
                currentToken.getValue().equalsIgnoreCase("long")||
                currentToken.getValue().equalsIgnoreCase("short")){
            matchByValue(currentToken.getValue(),sub);
            return tokenType;
        }
        return null;
    }


    private String identifier(StringTreeNode sub) {
        String identifier = currentToken.getValue();
        if (currentToken.getType().equals("IDENTIFIER")) {
            matchByValue(currentToken.getValue(),sub);
            return identifier;
        }
        return null;
    }

    private void matchArrayListContents(String tokentype, int size, StringTreeNode sub) {
        int count = 0;

        Token main = new Token("program node",-2,"Num operation");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseNumOperation(tokentype, sub);
        sub = temp.sibling;

        count++;


        while (currentToken.getType().equals("COMMA")) {
            matchByType("COMMA", sub);

            main = new Token("program node",-2,"Num operation");
            sub = new StringTreeNode(main);
            sub = sub.child;
            temp = sub;
            parseNumOperation(tokentype, sub);
            sub = temp.sibling;


            count++;
        }


        if (count != size) {
            throw new RuntimeException("Parsing failed. Array size does not match the number of elements. Expected size: " + size + ", Actual count: " + count + " Line Number: " + currentToken.getLineNumber());
        }
    }

    private void parseNumOperation(String tokentype, StringTreeNode sub) {
        Token main = new Token("program node",-2,"Term");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseTerm(tokentype, sub);
        sub = temp.sibling;

        while (currentToken.getType().equals("ADD_OP") || currentToken.getType().equals("MOD_OP") || currentToken.getType().equals("MUL_OP") ||
                currentToken.getType().equals("DIV_OP") || currentToken.getType().equals("SUB_OP") ) {
            String operator = currentToken.getValue();
            matchByValue(operator, sub);

            main = new Token("program node",-2,"Term");
            sub = new StringTreeNode(main);
            sub = sub.child;
            temp = sub;
            parseTerm(tokentype, sub);
            sub = temp.sibling;

        }
    }

    private void parseTerm(String tokentype, StringTreeNode sub) {
        Token main = new Token("program node",-2,"Term");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseFactor(tokentype, sub);
        sub = temp.sibling;

        while ((currentToken.getType().equals("ADD_OP") || currentToken.getType().equals("MOD_OP") || currentToken.getType().equals("MUL_OP") ||
                currentToken.getType().equals("DIV_OP") || currentToken.getType().equals("SUB_OP") ) && !(currentToken.getType().equals("SEMICOLON"))) {
            String operator = currentToken.getValue();
            if (operator.equals("+")) {
                matchByType("ADD_OP", sub);

                main = new Token("program node",-2,"Term");
                sub = new StringTreeNode(main);
                sub = sub.child;
                temp = sub;
                parseFactor(tokentype, sub);
                sub = temp.sibling;

            }
            else if (operator.equals("-")) {
                matchByType("SUB_OP", sub);

                main = new Token("program node",-2,"Term");
                sub = new StringTreeNode(main);
                sub = sub.child;
                temp = sub;
                parseFactor(tokentype, sub);
                sub = temp.sibling;

            }
            else if (operator.equals("*")) {
                matchByType("MUL_OP", sub);

                main = new Token("program node",-2,"Term");
                sub = new StringTreeNode(main);
                sub = sub.child;
                temp = sub;
                parseFactor(tokentype, sub);
                sub = temp.sibling;

            }
            else if (operator.equals("/")) {
                matchByType("DIV_OP", sub);

                main = new Token("program node",-2,"Term");
                sub = new StringTreeNode(main);
                sub = sub.child;
                temp = sub;
                parseFactor(tokentype, sub);
                sub = temp.sibling;

            }
            else if (operator.equals("%")) {
                matchByType("MOD_OP", sub);

                main = new Token("program node",-2,"Term");
                sub = new StringTreeNode(main);
                sub = sub.child;
                temp = sub;
                parseFactor(tokentype, sub);
                sub = temp.sibling;

            }
            else {
                break;
            }
        }
    }

    private void parseFactor(String tokentype, StringTreeNode sub) {
        if (currentToken.getType().equals("LEFT_PAREN")) {
            matchByType("LEFT_PAREN", sub);
            parseNumOperation(tokentype, sub);
            matchByType("RIGHT_PAREN", sub);
        } else {
            parseNumber(tokentype, sub);
        }
    }


    private void parseNumber(String tokentype, StringTreeNode sub) {
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
                matchByType("NUMBER", sub);
            } else if (tokentype.equalsIgnoreCase("float") || tokentype.equalsIgnoreCase("double")) {
                matchByType("FLOAT_NUMBER", sub);
            }
        }
    }


    private void parseFunction(String functionName, String functionType, StringTreeNode sub)
    {
        matchByType("LEFT_PAREN",sub);
        Token main = new Token("program node",-2,"Parameters");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseParameters(functionName, sub);
        sub = temp.sibling;
        matchByType("RIGHT_PAREN",sub);
        matchByType("LEFT_BRACE",sub);
        matchByType("LEFT_PAREN",sub);
        main = new Token("program node",-2,"Body");
        sub = new StringTreeNode(main);
        sub = sub.child;
        temp = sub;
        parseBody(sub);
        sub = temp.sibling;
        if(currentToken.getValue().equals("return")){
            matchByValue("return",sub);
            if(!functionType.equals("void")){
                if(currentToken.getType().equals("IDENTIFIER")){
                    if(symbolType.get(currentToken.getValue()).equals(functionType)) {
                        matchByType("IDENTIFIER",sub);
                    }
                    else {
                        throw new RuntimeException("Parsing failed. Unexpected token (Invalid return datatype): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                    }
                }
            }
            matchByType("SEMICOLON", sub);
        }
        matchByType("RIGHT_BRACE", sub);

    }

    private void parseParameters(String functionName, StringTreeNode sub) {
        List<String> paramTypes = new ArrayList<>();
        while(!(currentToken.getType().equals("RIGHT_PAREN"))) {

            String paramType = type(sub);
            if (paramType == null) {
                throw new RuntimeException("Parsing failed. Unexpected token (Expected a type for parameter): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
            String paramName = identifier(sub);
            if (paramName == null) {
                throw new RuntimeException("Parsing failed. Unexpected token (Expected a parameter name): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
            symbolType.put(paramName,paramType);
            paramTypes.add(paramType);
            if (currentToken.getType().equals("COMMA")) {
                matchByType("COMMA", sub);
                if (currentToken.getType().equals("RIGHT_PAREN")) {
                    throw new RuntimeException("Parsing failed. Unexpected token (Trailing comma in parameter list): " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }

            }
        }
        funcParameters.put(functionName,paramTypes);

    }
    private void Preprocessor(StringTreeNode sub){
        matchByType("PRE_PROCESSOR_PATTERN", sub);  //matching
        matchByType("LESS_THAN", sub);
        matchByType("IDENTIFIER", sub);

        if(currentToken.getValue().equals(".")) {
            matchByType("DOT",sub);
            matchByType("IDENTIFIER", sub);
        }
        matchByType("GREATER_THAN", sub);
    }
    private void parseBody(StringTreeNode sub) {

        while(!currentToken.getType().equals("RIGHT_BRACE") && !currentToken.getType().equals("EOF")){
            if (currentToken.getType().equals("KEYWORD")){
                if(currentToken.getValue().equals("return")){
                    break;
                }
                else if (currentToken.getValue().equalsIgnoreCase("int") || currentToken.getValue().equalsIgnoreCase("float") || currentToken.getValue().equalsIgnoreCase("char") ||
                        currentToken.getValue().equalsIgnoreCase("double") || currentToken.getValue().equalsIgnoreCase("long")|| currentToken.getValue().equalsIgnoreCase("short")){
                    String dataType = type(sub);
                    String variable = identifier(sub);
                    symbolType.put(variable, dataType);
                    declaration(dataType, sub);
                }
                else if (currentToken.getValue().equals("if") || currentToken.getValue().equals("for") || currentToken.getValue().equals("while") || currentToken.getValue().equals("switch")){
                    Token main = new Token("program node",-2,"Statement");
                    sub = new StringTreeNode(main);
                    sub = sub.child;
                    StringTreeNode temp = sub;
                    parseStatement(sub);
                    sub = temp.sibling;
                }
                else if (currentToken.getValue().equalsIgnoreCase("break")) {
                    matchByValue( "break",sub);
                    matchByType("SEMICOLON",sub);
                }
                else if (currentToken.getValue().equalsIgnoreCase("continue")) {
                    matchByValue( "continue",sub);
                    matchByType("SEMICOLON",sub);
                }
                else{
                    throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
            }
            else if(currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("INCREMENT") || currentToken.getType().equals("DECREMENT")){
                Token main = new Token("program node",-2,"Statement");
                sub = new StringTreeNode(main);
                sub = sub.child;
                StringTreeNode temp = sub;
                parseInitialization(sub);
                sub = temp.sibling;
            }
            else {
                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
        }
    }
    private void parseCaseBody(StringTreeNode sub){
        while((!currentToken.getValue().equals("case") || !currentToken.getValue().equals("default") || !currentToken.getType().equals("RIGHT_BRACE"))  && !currentToken.getType().equals("EOF")){
            if(currentToken.getValue().equals("case") || currentToken.getValue().equals("default") || currentToken.getType().equals("RIGHT_BRACE"))
                break;
            else if (currentToken.getType().equals("KEYWORD")){
                if(currentToken.getValue().equals("return")){
                    matchByValue("return", sub);
                    if(currentToken.getType().equals("NUMBER")) {
                        matchByType("NUMBER", sub);
                    }
                    matchByType("SEMICOLON", sub);
                }
                else if (currentToken.getValue().equalsIgnoreCase("int") || currentToken.getValue().equalsIgnoreCase("float") || currentToken.getValue().equalsIgnoreCase("char") ||
                        currentToken.getValue().equalsIgnoreCase("double") || currentToken.getValue().equalsIgnoreCase("long")|| currentToken.getValue().equalsIgnoreCase("short")){
                    String dataType = type(sub);
                    String variable = identifier(sub);
                    symbolType.put(variable, dataType);

                    Token main = new Token("program node",-2,"If Statement");
                    sub = new StringTreeNode(main);
                    sub = sub.child;
                    StringTreeNode temp = sub;
                    declaration(dataType, sub);
                    sub = temp.sibling;
                }
                else if (currentToken.getValue().equals("if") || currentToken.getValue().equals("for") || currentToken.getValue().equals("while") || currentToken.getValue().equals("switch")){
                    Token main = new Token("program node",-2,"If Statement");
                    sub = new StringTreeNode(main);
                    sub = sub.child;
                    StringTreeNode temp = sub;
                    parseStatement(sub);
                    sub = temp.sibling;
                }
                else if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("break")) {
                    matchByValue( "break", sub);
                    matchByType("SEMICOLON", sub);
                }
                else{
                    throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
                }
            }
            else if(currentToken.getType().equals("IDENTIFIER") || currentToken.getType().equals("INCREMENT") || currentToken.getType().equals("DECREMENT")){
                Token main = new Token("program node",-2,"If Statement");
                sub = new StringTreeNode(main);
                sub = sub.child;
                StringTreeNode temp = sub;
                parseInitialization(sub);
                sub = temp.sibling;
            }
            else {
                throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
            }
        }
    }

    private void parseStatement(StringTreeNode sub) {
        if (currentToken.getValue().equals("if")) {
            Token main = new Token("program node",-2,"If Statement");
            sub = new StringTreeNode(main);
            sub = sub.child;
            StringTreeNode temp = sub;
            parseIfStatement(sub);
            sub = temp.sibling;
        }
        else if(currentToken.getValue().equals("switch")){
            // Parse other types of statements
            Token main = new Token("program node",-2,"Switch Statement");
            sub = new StringTreeNode(main);
            sub = sub.child;
            StringTreeNode temp = sub;
            ParseSwitchCaseDeclaration(sub);
            sub = temp.sibling;
        }
        else if(currentToken.getValue().equals("for")) {
            Token main = new Token("program node",-2,"For loop");
            sub = new StringTreeNode(main);
            sub = sub.child;
            StringTreeNode temp = sub;
            parseForLoop(sub);
            sub = temp.sibling;
        }
        else if(currentToken.getValue().equals("while")){
            Token main = new Token("program node",-2,"While loop");
            sub = new StringTreeNode(main);
            sub = sub.child;
            StringTreeNode temp = sub;
            parseWhileLoop(sub);
            sub = temp.sibling;
        }
    }
    private void parseWhileLoop(StringTreeNode sub) {
        matchByValue("while",sub);  // Expects the "while" keyword
        matchByType("LEFT_PAREN",sub);
        Token main = new Token("program node",-2,"Condition");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseCondition(sub);                                         // Parses the condition expression
        sub = temp.sibling;
        matchByType("RIGHT_PAREN", sub);  // Expects a right parenthesis token
        matchByType("LEFT_BRACE", sub);  // Expects a left brace token
        main = new Token("program node",-2,"Body");
        sub = new StringTreeNode(main);
        sub = sub.child;
        temp = sub;
        parseBody(sub);
        sub = temp.sibling;
        if (currentToken.getValue().equals("return")){
            matchByValue("return",sub);
            matchByType("SEMICOLON",sub);
        }
        matchByType("RIGHT_BRACE",sub);  // Expects a right brace token
    }

    private void parseForLoop(StringTreeNode sub) {
        matchByValue("for",sub);
        matchByValue("(",sub);

        Token main = new Token("program node",-2,"For Initialization");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseForInit(sub);
        sub = temp.sibling;

        matchByValue(";",sub);

        main = new Token("program node",-2,"Condition");
        sub = new StringTreeNode(main);
        sub = sub.child;
        temp = sub;
        parseCondition(sub);
        sub = temp.sibling;

        matchByValue(";",sub);

        main = new Token("program node",-2,"Update");
        sub = new StringTreeNode(main);
        sub = sub.child;
        temp = sub;
        parseUpdate(sub);
        sub = temp.sibling;

        matchByValue(")",sub);
        matchByValue("{",sub);

        main = new Token("program node",-2,"Body");
        sub = new StringTreeNode(main);
        sub = sub.child;
        temp = sub;
        parseBody(sub);
        sub = temp.sibling;

        if (currentToken.getValue().equals("return")){
            matchByValue("return",sub);
            matchByType("SEMICOLON",sub);
        }
        matchByValue("}",sub);
    }

    private void parseUpdate(StringTreeNode sub) {
        if (currentToken.getType().equals("IDENTIFIER")) {
            matchByValue(currentToken.getValue(),sub);
            if(currentToken.getValue().equals("++") || currentToken.getValue().equals("--")){
                matchByValue(currentToken.getValue(),sub);
            } else if (currentToken.getValue().equals("+=") || currentToken.getValue().equals("-=")
                    || currentToken.getValue().equals("*=") || currentToken.getValue().equals("/=")) {
                Token main = new Token("program node",-2,"Increment");
                sub = new StringTreeNode(main);
                sub = sub.child;
                StringTreeNode temp = sub;
                parseSignIncrement(sub);
                sub = temp.sibling;

            }
        } else if (currentToken.getValue().equals("++") || currentToken.getValue().equals("--")) {
            Token main = new Token("program node",-2,"Increment decrement");
            sub = new StringTreeNode(main);
            sub = sub.child;
            StringTreeNode temp = sub;
            parseIncrementDecrement(sub);
            sub = temp.sibling;
        } else {
            throw new RuntimeException("Parsing Failed. Expected a valid update expression at line number: " + currentToken.getLineNumber());
        }
    }

    private void parseSignIncrement(StringTreeNode sub) {
        matchByValue(currentToken.getValue(),sub);
        matchByType("NUMBER",sub);
    }

    private void parseIncrementDecrement(StringTreeNode sub) {
        // Parse increment or decrement
        matchByValue(currentToken.getValue(),sub);
        matchByType("IDENTIFIER",sub); // Match variable name
    }

    private void parseForInit(StringTreeNode sub) {
        if (currentToken.getValue().equals("int")) {
            matchByValue("int",sub);
            symbolType.put(currentToken.getValue(),"int");
            matchByType("IDENTIFIER",sub);
            matchByType("ASSIGN_OP",sub);
            matchByType("NUMBER",sub);
        }
        else if(currentToken.getType().equals("IDENTIFIER")) {
            String type = symbolType.get(currentToken.getValue());
            if(type == null){
                throw new RuntimeException("Parsing Failed. variable is not initialized: (" +currentToken.getValue()+ ") at line number: "+currentToken.getLineNumber());
            }
            else if(type.equals("int")) {
                matchByType("IDENTIFIER",sub);
                if(currentToken.getType().equals("ASSIGN_OP")){
                    matchByType("ASSIGN_OP",sub);
                    matchByType("NUMBER",sub);
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

    private void parseIfStatement(StringTreeNode sub) {
        matchByValue("if",sub);
        matchByValue("(", sub);

        Token main = new Token("program node",-2,"Condition");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseCondition(sub);
        sub = temp.sibling;

        matchByValue(")", sub);
        matchByType("LEFT_BRACE", sub);

        main = new Token("program node",-2,"Body");
        sub = new StringTreeNode(main);
        sub = sub.child;
        temp = sub;
        parseBody(sub);
        sub = temp.sibling;

        if (currentToken.getValue().equals("return")){
            matchByValue("return", sub);
            matchByType("SEMICOLON" , sub);
        }
        matchByType("RIGHT_BRACE", sub);
        if (currentToken.getValue().equals("else")) {
            matchByValue("else", sub);
            if (currentToken.getValue().equals("if")) {
                // Parse else if
                main = new Token("program node",-2,"IF Statement");
                sub = new StringTreeNode(main);
                sub = sub.child;
                temp = sub;
                parseIfStatement(sub);
                sub = temp.sibling;

            } else {
                matchByType("LEFT_BRACE", sub);

                main = new Token("program node",-2,"Body");
                sub = new StringTreeNode(main);
                sub = sub.child;
                temp = sub;
                parseBody(sub);
                sub = temp.sibling;

                if (currentToken.getValue().equals("return")){
                    matchByValue("return", sub);
                    matchByType("SEMICOLON", sub);
                }
                matchByType("RIGHT_BRACE", sub);
            }
        }
    }

    private void parseCondition(StringTreeNode sub) {
        Token main = new Token("program node",-2,"Expression");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseExpression(sub);
        sub = temp.sibling;

        if (currentToken.getType().equals("GREATER_THAN_OR_EQUALS") ||
                currentToken.getType().equals("EQUALS") || currentToken.getType().equals("NOT_EQUALS") ||
                currentToken.getType().equals("LESS_THAN_OR_EQUALS") ||
                currentToken.getType().equals("LESS_THAN") || currentToken.getType().equals("GREATER_THAN")) {
            matchByValue(currentToken.getValue(), sub);
        } else {
            throw new RuntimeException("Parsing Failed. Expected relational operator at line number: " + currentToken.getLineNumber());
        }
        main = new Token("program node",-2,"Expression");
        sub = new StringTreeNode(main);
        sub = sub.child;
        temp = sub;
        parseExpression(sub);
        sub = temp.sibling;
    }

    private void parseExpression(StringTreeNode sub) {
        Token main = new Token("program node",-2,"Simple Expression");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        parseSimpleExpression(sub);
        sub = temp.sibling;

        while ((currentToken.getValue().equals("+") || currentToken.getValue().equals("-") ||currentToken.getValue().equals("*") ||
                currentToken.getValue().equals("/") || currentToken.getValue().equals("!") || currentToken.getValue().equals("||")
                || currentToken.getValue().equals("&&") || currentToken.getValue().equals("%"))) {
            if(currentToken.getValue().equals("||") || currentToken.getValue().equals("&&")){
                matchByValue(currentToken.getValue(), sub);
                parseCondition(sub);
                break;
            }
            matchByValue(currentToken.getValue(), sub);

            main = new Token("program node",-2,"Simple Expression");
            sub = new StringTreeNode(main);
            sub = sub.child;
            temp = sub;
            parseSimpleExpression(sub);
            sub = temp.sibling;
        }
    }

    private void parseSimpleExpression(StringTreeNode sub) {
        if (currentToken.getType().equals("IDENTIFIER")) {
            String varType = symbolType.get(currentToken.getValue());
            if(varType == null){
                throw new RuntimeException("Parsing Failed. variable is not initialized: (" +currentToken.getValue()+ ") at line number: "+currentToken.getLineNumber());
            }
            matchByValue(currentToken.getValue(), sub);
        } else if (currentToken.getType().equals("NUMBER")) {
            matchByValue(currentToken.getValue(), sub);
        } else if (currentToken.getType().equals("STRING")) {
            matchByValue(currentToken.getValue(), sub);
        } else if (currentToken.getType().equals("CHAR")) {
            matchByValue(currentToken.getValue(), sub);
        } else if (currentToken.getValue().equals("(")) {
            matchByValue("(", sub);

            Token main = new Token("program node",-2,"Expression");
            sub = new StringTreeNode(main);
            sub = sub.child;
            StringTreeNode temp = sub;
            parseExpression(sub);
            sub = temp.sibling;

            matchByValue(")", sub);
        } else {
            throw new RuntimeException("Parsing Failed. Invalid expression at line number: " + currentToken.getLineNumber());
        }
    }

    private void ParseSwitchCaseDeclaration(StringTreeNode sub) {
        // Match the 'switch' keyword
        matchByValue("switch", sub);

        // Match the opening parenthesis after 'switch'
        matchByValue("(", sub);

        // Now, expect an expression after '('
        String varType = symbolType.get(currentToken.getValue());
        if(varType == null){
            throw new RuntimeException("Parsing Failed. variable is not initialized: (" +currentToken.getValue()+ ") at line number: "+currentToken.getLineNumber());
        }
        matchByType("IDENTIFIER", sub); // Assuming IDENTIFIER represents the expression

        // Match the closing parenthesis after the expression
        matchByValue(")", sub);

        // Match the opening curly brace after the expression
        matchByValue("{", sub);

        // Detect the switch-case statements inside the switch block
        Token main = new Token("program node",-2,"Case Beginning");
        sub = new StringTreeNode(main);
        sub = sub.child;
        StringTreeNode temp = sub;
        detectSwitchCaseInsideBlock(varType, sub);
        sub = temp.sibling;


        // Match the closing curly brace after the switch block
        matchByValue("}", sub);
    }
    private void detectSwitchCaseInsideBlock(String type, StringTreeNode sub) {
        // Match the 'case' keyword
        while(!(currentToken.getValue().equals("}") || currentToken.getValue().equals("default") || currentToken.getValue().equals("EOF "))) {
            matchByValue("case", sub);


            // Now, expect a constant after 'case' *if int u need to make it in analyzer*
            if (type.equals("int")) {
                matchByType("NUMBER", sub);
            } else if (type.equals("float")) {
                matchByType("FLOAT_NUMBER", sub);
            } else if (type.equals("char")) {
                matchByType("CHAR", sub);
            } else if (type.equals("string")) {
                matchByType("STRING", sub);
            }
            // Match the colon after the constant
            matchByType("COLON", sub);
            Token main = new Token("program node",-2,"Case Body");
            sub = new StringTreeNode(main);
            sub = sub.child;
            StringTreeNode temp = sub;
            parseCaseBody(sub);
            sub = temp.sibling;
//            if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("break")) {
//                // Match the 'break' keyword
//                matchByValue( "break");
//                // Match the semicolon after 'break'
//                matchByType("SEMICOLON");
//            }
        }
        if (currentToken.getType().equalsIgnoreCase("KEYWORD") && currentToken.getValue().equalsIgnoreCase("default")) {
            // Match the 'default' keyword
            matchByValue("default", sub);
            // Match the colon after 'default'
            matchByType("COLON", sub);

            matchByType("COLON", sub);
            Token main = new Token("program node",-2,"Body");
            sub = new StringTreeNode(main);
            sub = sub.child;
            StringTreeNode temp = sub;
            parseBody(sub);
            sub = temp.sibling;

            if (currentToken.getValue().equals("return")){
                matchByValue("return", sub);
                matchByType("SEMICOLON", sub);
            }
        }
    }

    private void matchByType(String expectedType, StringTreeNode p) {
        if(root == null){
            root = new StringTreeNode(currentToken);
            tree = new StringTreeAsTreeForTreeLayout(root);
            p = root.child;
        } else {
            p = new StringTreeNode(currentToken);
            p = p.sibling;
        }
        if (currentToken.getType().equals(expectedType)) {
            if(expectedType.equals("LEFT_BRACE"))
                nestingLevel++;
            if(expectedType.equals("RIGHT_BRACE"))
                nestingLevel--;

            advance();
        }
        else{
            throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
    }
    private void matchByValue(String value , StringTreeNode p) {
        if(root == null){
            root = new StringTreeNode(currentToken);
            tree = new StringTreeAsTreeForTreeLayout(root);
            p = root.child;
        } else {
            p = new StringTreeNode(currentToken);
            p = p.sibling;
        }
        if (currentToken.getValue().equals(value)) {
            advance();
        }
        else{
            throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
    }
    private void matchByTypeChild(String expectedType, StringTreeNode p) {
        if(root == null){
            root = new StringTreeNode(currentToken);
            tree = new StringTreeAsTreeForTreeLayout(root);
            p = root.child;
        } else {
            p = new StringTreeNode(currentToken);
            p = p.child;
        }
        if (currentToken.getType().equals(expectedType)) {
            if(expectedType.equals("LEFT_BRACE"))
                nestingLevel++;
            if(expectedType.equals("RIGHT_BRACE"))
                nestingLevel--;

            advance();
        }
        else{
            throw new RuntimeException("Parsing failed. Unexpected token: " + currentToken.getValue() + " Token Type: " + currentToken.getType() + " Line Number: " + currentToken.getLineNumber());
        }
    }
    private void matchByValueChild(String value, StringTreeNode p) {
        if(root == null){
            root = new StringTreeNode(currentToken);
            tree = new StringTreeAsTreeForTreeLayout(root);
            p = root.child;
        } else {
            p = new StringTreeNode(currentToken);
            p = p.child;
        }
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

