package com.example.compiler;

import java.io.IOException;

public class Compiler {
    private Lexical_Analyzer scanner = new Lexical_Analyzer();
    private Syntax_Analyzer parser;
    public Compiler(){
    }

    public static void main(String[] args) {
        Compiler compiler = new Compiler();
        compiler.compile("src/main/java/com/example/compiler/test.c");
    }

    public void compile(String sourceCodeFile) {
        scanner.lexicalAnalyze(sourceCodeFile);
        parser = new Syntax_Analyzer(scanner.getTokens(), scanner.getSymbolTable());
        ParseTreeNode parseTree = parser.parse();

        if (parseTree != null) {
            parseTree.visualizeTree();
            parseTree.generateDotFile("src/main/java/com/example/compiler/parse_tree.dot");
            previewParseTree("src/main/java/com/example/compiler/parse_tree.dot");
        }
    }

    private void previewParseTree(String dotFilePath) {
        try {
            String command = "dot -Tpng " + dotFilePath + " -o src/main/java/com/example/compiler/parse_tree.png";
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Open the generated image file
                String imageFilePath = "src/main/java/com/example/compiler/parse_tree.png";
                Process openProcess = Runtime.getRuntime().exec("cmd.exe /c start " + imageFilePath);
                openProcess.waitFor();
            } else {
                System.out.println("Failed to generate the parse tree image.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
