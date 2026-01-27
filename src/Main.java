import ast.Stmt;
import interpreter.Interpreter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lexer.Lexer;
import parser.Parser;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Usage: java Main <source-file>");
            System.exit(1);
        }

        String source = Files.readString(Path.of(args[0]));

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.scanTokens());
        List<Stmt> statements = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);
    }
}
