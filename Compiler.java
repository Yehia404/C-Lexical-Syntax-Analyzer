public class Compiler {
    private Lexical_Analyzer scanner = new Lexical_Analyzer();
    private Syntax_Analyzer parser;
    public Compiler(){
    }


    public static void main(String [] args){
        Compiler compiler = new Compiler();
        compiler.Compile("test6.c");
    }


    public void Compile(String sourceCodeFile){
        scanner.lexicalAnalyze(sourceCodeFile);
        parser = new Syntax_Analyzer(scanner.getTokens(),scanner.getSymbolTable());
        ParseTreeNode parseTree = parser.parse();

        if (parseTree != null) {
            parseTree.visualizeTree();
        }
    }
}
