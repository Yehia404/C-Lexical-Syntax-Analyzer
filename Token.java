public class Token {
    public enum DataType {
        IDENTIFIER,STRING,INTEGER,FLOAT,PLUS,MINUS,TIMES,DIVIDE,KEYWORD,INVALID,
        ASSIGNMENT_OPERATOR,SEMICOLON, LEFT_PARENTHESIS, RIGHT_PARENTHESIS,COMMA,DOT,POINTER_DOT,COLON,EQUAL, LOWER_OR_EQUALS, GREATER_OR_EQUALS, NOT_EQUALS, GREATER_THAN, LOWER_THAN, AT_SIGN,
        LEFT_BRACE, RIGHT_BRACE
    }
    private DataType type;
    private int lineNumber;
    private String value;
    public Token(DataType t,int lineNumber, String val) {
        type = t;
        this.lineNumber = lineNumber;
        value = val;
    }



    public DataType getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getValue() {
        return value;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    public void setType(DataType type) {
        this.type = type;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
