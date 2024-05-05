public class Compiler {
    private Lexical_Analyzer scanner = new Lexical_Analyzer();

    public Compiler(){
    }


    public static void main(String [] args){
        Compiler compiler = new Compiler();
        compiler.Compile("test.c");
    }


    public void Compile(String sourceCodeFile){
        scanner.lexicalAnalyze(sourceCodeFile);
    }
}
