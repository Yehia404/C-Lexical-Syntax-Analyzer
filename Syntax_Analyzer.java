import java.util.List;

public class Syntax_Analyzer {
    private List<Token> tokens;
    private int currentIndexToken;
    private Token currentToken;

    public Syntax_Analyzer(List <Token> tokens){
        this.tokens = tokens;
        currentIndexToken = 0;
        currentToken= tokens.get(0);
    }





}
