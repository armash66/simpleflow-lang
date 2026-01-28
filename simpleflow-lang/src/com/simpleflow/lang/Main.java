package com.simpleflow.lang;

import com.simpleflow.lang.lexer.Lexer;
import com.simpleflow.lang.parser.Parser;
import com.simpleflow.lang.ast.Stmt;
import com.simpleflow.lang.interpreter.Interpreter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    // ======================
    // CLI ENTRY POINT
    // ======================
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Usage: java Main <source-file>");
            System.exit(1);
        }

        String source = Files.readString(Path.of(args[0]));
        run(source);
    }

    // ======================
    // REUSABLE ENGINE API
    // ======================
    public static String run(String source) {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.scanTokens());
        List<Stmt> statements = parser.parse();

        Interpreter interpreter = new Interpreter();
        return interpreter.interpretAndReturn(statements);
    }
}
