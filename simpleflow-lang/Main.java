package com.simpleflow.lang;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.simpleflow.lang.ast.Stmt;
import com.simpleflow.lang.interpreter.Interpreter;
import com.simpleflow.lang.lexer.Lexer;
import com.simpleflow.lang.parser.ParseError;
import com.simpleflow.lang.parser.Parser;

public class Main {

    // ======================
    // CLI ENTRY POINT
    // ======================
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Usage: java Main <file.sf>");
            return;
        }

        String source = Files.readString(Path.of(args[0]));

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

        } catch (ParseError e) {
            return "Parse error at line " + e.line +
                   ", column " + e.column +
                   ": " + e.getMessage();
        } catch (RuntimeException e) {
            return "Runtime error: " + e.getMessage();
        }
    }
}
