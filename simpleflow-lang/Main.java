package com.simpleflow.lang;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

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

        if (args.length == 0) {
            repl();
            return;
        }

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
            return formatParseError(source, e);
        } catch (RuntimeException e) {
            return "Runtime error: " + e.getMessage();
        }
    }

    private static String formatParseError(String source, ParseError e) {
        String[] lines = source.split("\n", -1);
        String lineText = "";
        if (e.line > 0 && e.line <= lines.length) {
            lineText = lines[e.line - 1];
        }
        String caret = " ".repeat(Math.max(0, e.column - 1)) + "^";
        return "Parse error at line " + e.line +
               ", column " + e.column +
               ": " + e.getMessage() + "\n" +
               lineText + "\n" +
               caret;
    }

    private static void repl() {
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();
        StringBuilder buffer = new StringBuilder();
        int braceDepth = 0;

        while (true) {
            System.out.print(braceDepth > 0 ? "... " : "sf> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine();
            if (line.trim().equals("exit")) {
                break;
            }

            buffer.append(line).append("\n");
            braceDepth += countBraces(line);

            if (braceDepth > 0) {
                continue;
            }

            String source = buffer.toString();
            buffer.setLength(0);

            try {
                Lexer lexer = new Lexer(source);
                Parser parser = new Parser(lexer.scanTokens());
                List<Stmt> statements = parser.parse();
                interpreter.interpret(statements);
            } catch (ParseError e) {
                System.out.println(formatParseError(source, e));
            } catch (RuntimeException e) {
                System.out.println("Runtime error: " + e.getMessage());
            }
        }
    }

    private static int countBraces(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '{') count++;
            if (c == '}') count--;
        }
        return count;
    }
}
