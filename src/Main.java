import lexer.*;
import parser.*;
import ast.*;
import interpreter.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        String code = """
            put x = 3

            repeat (x > 0) {
                show x
                x = x - 1
            }

            when (x == 0) {
                say "finished"
            } or {
                say "error"
            }

            stop
        """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);
    }
}
