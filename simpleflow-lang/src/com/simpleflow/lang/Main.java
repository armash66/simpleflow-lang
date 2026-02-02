package com.simpleflow.lang;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.simpleflow.lang.ast.Stmt;
import com.simpleflow.lang.interpreter.Interpreter;
import com.simpleflow.lang.lexer.Lexer;
import com.simpleflow.lang.parser.Parser;

public class Main {

    // ======================
    // CLI ENTRY POINT
    // ======================
    public static void main(String[] args) throws Exception {
        System.out.println("MAIN STARTED");

        if (args.length != 1) {
            System.out.println("Usage: java Main <source-file>");
            System.exit(1);
        }

        String source = Files.readString(Path.of(args[0]));

        System.out.println("----- SOURCE START -----");
        System.out.println(source);
        System.out.println("----- SOURCE END -----");

        String output = run(source);

        if (!output.isEmpty()) {
            System.out.println(output);
        }
    }

    // ======================
    // REUSABLE ENGINE API
    // ======================
    public static String run(String source) {
        try {
            Lexer lexer = new Lexer(source);
            Parser parser = new Parser(lexer.scanTokens());
            List<Stmt> statements = parser.parse();

            Interpreter interpreter = new Interpreter();
            return interpreter.interpretAndReturn(statements);

        } catch (com.simpleflow.lang.parser.ParseError e) {
            return "Parse error at line " + e.line + ", col " + e.column + ": " + e.getMessage();            
        }
    }
}
