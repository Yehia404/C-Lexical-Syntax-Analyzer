public class Token {
//    public enum SymbolType {
//        IDENTIFIER,STRING,INTEGER,FLOAT,PLUS,MINUS,TIMES,DIVIDE,KEYWORD,INVALID,
//        ASSIGNMENT_OPERATOR,SEMICOLON, LEFT_PARENTHESIS, RIGHT_PARENTHESIS,COMMA,DOT,POINTER_DOT,COLON,EQUAL, LOWER_OR_EQUALS, GREATER_OR_EQUALS, NOT_EQUALS, GREATER_THAN, LOWER_THAN, AT_SIGN,
//        LEFT_BRACE, RIGHT_BRACE
//    }
    private String type;
    private int lineNumber;
    private java.lang.String value;
    public Token(String t, int lineNumber, java.lang.String val) {
        type = t;
        this.lineNumber = lineNumber;
        value = val;
    }



    public String getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public java.lang.String getValue() {
        return value;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setValue(java.lang.String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }

}
