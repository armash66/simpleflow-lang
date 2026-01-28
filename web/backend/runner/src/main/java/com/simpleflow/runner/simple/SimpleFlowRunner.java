package com.simpleflow.runner.simple;

import com.simpleflow.lang.lexer.Lexer;
import com.simpleflow.lang.lexer.Token;
import com.simpleflow.lang.parser.Parser;
import com.simpleflow.lang.interpreter.Interpreter;
import com.simpleflow.lang.ast.Stmt;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class SimpleFlowRunner {

    public static String run(String source) {
        // Capture System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            Interpreter interpreter = new Interpreter();
            interpreter.interpret(statements);

        } catch (RuntimeException e) {
            return "Error: " + e.getMessage();
        } finally {
            System.setOut(originalOut);
        }

        return outputStream.toString().trim();
    }
}
