package com.simpleflow.lang.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.simpleflow.lang.ast.Expr;
import com.simpleflow.lang.ast.Stmt;
import com.simpleflow.lang.lexer.Lexer;
import com.simpleflow.lang.lexer.TokenType;
import com.simpleflow.lang.parser.ParseError;
import com.simpleflow.lang.parser.Parser;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();
    private boolean inLoop = false;

    // ---------------- ENTRY ----------------

    public Interpreter() {
        environment.define("length", new LengthFunction());
        environment.define("input", new InputFunction());
        environment.define("random", new RandomFunction());
        environment.define("clock", new ClockFunction());
        environment.define("type", new TypeFunction());
        environment.define("toNumber", new ToNumberFunction());
        environment.define("toString", new ToStringFunction());
        environment.define("push", new PushFunction());
        environment.define("pop", new PopFunction());
        environment.define("shift", new ShiftFunction());
        environment.define("unshift", new UnshiftFunction());
        environment.define("keys", new KeysFunction());
        environment.define("values", new ValuesFunction());
        environment.define("has", new HasFunction());
        environment.define("slice", new SliceFunction());
        environment.define("merge", new MergeFunction());
        environment.define("assert", new AssertFunction());
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (ExitSignal ignored) {
            // program stopped
        }
    }

    public String interpretAndReturn(List<Stmt> statements) {

        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        try {
            interpret(statements);
        } finally {
            System.setOut(originalOut);
        }

        return buffer
                .toString()
                .replace("\r\n", "\n")
                .trim();
    }

    public void interpretSource(String source) {
        try {
            Lexer lexer = new Lexer(source);
            Parser parser = new Parser(lexer.scanTokens());
            List<Stmt> statements = parser.parse();
            interpret(statements);
        } catch (ParseError e) {
            throw new RuntimeException(
                "Parse error at line " + e.line + ", column " + e.column + ": " + e.getMessage()
            );
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // ---------------- STATEMENTS ----------------

    @Override
    public Void visitPutStmt(Stmt.Put stmt) {
        Object value = evaluate(stmt.initializer);
        environment.define(stmt.name.lexeme, value);

        return null;
    }

    @Override
    public Void visitAssignStmt(Stmt.Assign stmt) {
        Object value = evaluate(stmt.value);
        environment.assign(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitPrintInlineStmt(Stmt.PrintInline stmt) {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {

        if (inLoop) {
            // SAME environment → allow mutation
            for (Stmt s : stmt.statements) {
                execute(s);
            }
        } else {
            // normal lexical scope
            executeBlock(stmt.statements, new Environment(environment));
        }

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {

        boolean previous = inLoop;
        inLoop = true;

        try {
            while (isTruthy(evaluate(stmt.condition))) {
                try {
                    execute(stmt.body);
                } catch (NextSignal n) {
                    continue;
                } catch (LeaveSignal l) {
                    break;
                }
            }
        } finally {
            inLoop = previous;
        }

        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        environment.define(stmt.name.lexeme, new UserFunction(stmt, environment));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }
        throw new ReturnSignal(value);
    }

    @Override
    public Void visitIncludeStmt(Stmt.Include stmt) {
        try {
            String source = Files.readString(Path.of(stmt.path));
            interpretSource(source);
        } catch (Exception e) {
            throw new RuntimeException("include failed: " + e.getMessage());
        }
        return null;
    }

    // ---------------- EXPRESSIONS ----------------

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitCellLiteralExpr(Expr.CellLiteral expr) {
        List<Object> values = new java.util.ArrayList<>();
        for (Expr element : expr.elements) {
            values.add(evaluate(element));
        }
        return new Cell(values);
    }

    @Override
    public Object visitIndexExpr(Expr.Index expr) {
        Object target = evaluate(expr.target);
        Object index = evaluate(expr.index);

        if (target instanceof Cell cell) {
            return cell.get(index);
        }

        throw new RuntimeException("Can only index a cell.");
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name.lexeme);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case PLUS -> (int) left + (int) right;
            case MINUS -> (int) left - (int) right;
            case STAR -> (int) left * (int) right;
            case SLASH -> (int) left / (int) right;

            case GREATER -> (int) left > (int) right;
            case GREATER_EQUAL -> (int) left >= (int) right;
            case LESS -> (int) left < (int) right;
            case LESS_EQUAL -> (int) left <= (int) right;
            case EQUAL_EQUAL -> java.util.Objects.equals(left, right);
            case BANG_EQUAL -> !java.util.Objects.equals(left, right);

            default -> throw new RuntimeException("Unknown operator.");
        };
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        if (expr.operator.type == TokenType.NOT) {
            return !isTruthy(right);
        }
        
        if (expr.operator.type == TokenType.MINUS) {
            if (right instanceof Integer i) {
                return -i;
            }
            throw new RuntimeException("Unary minus expects a number.");
        }

        throw new RuntimeException("Unknown operator.");
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object condition = evaluate(expr.condition);
        if (isTruthy(condition)) {
            return evaluate(expr.thenBranch);
        }
        return evaluate(expr.elseBranch);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {

        Object callee = evaluate(expr.callee);

        if (!(callee instanceof Callable function)) {
            throw new RuntimeException("Can only call functions.");
        }

        if (expr.arguments.size() != function.arity()) {
            throw new RuntimeException(
                "Expected " + function.arity() +
                " arguments but got " + expr.arguments.size()
            );
        }

        List<Object> arguments = new java.util.ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visitExitStmt(Stmt.Exit stmt) {
        throw new ExitSignal();
    }

    @Override
    public Void visitLeaveStmt(Stmt.Leave stmt) {
        if (!inLoop) {
            throw new RuntimeException("leave used outside loop");
        }
        throw new LeaveSignal();
    }

    @Override
    public Void visitNextStmt(Stmt.Next stmt) {
        if (!inLoop) {
            throw new RuntimeException("next used outside loop");
        }
        throw new NextSignal();
    }

    @Override
    public Void visitIndexAssignStmt(Stmt.IndexAssign stmt) {
        Object target = evaluate(stmt.target);
        if (!(target instanceof Cell cell)) {
            throw new RuntimeException("Can only index-assign into a cell.");
        }

        Object index = evaluate(stmt.index);
        Object value = evaluate(stmt.value);
        cell.set(index, value);
        return null;
    }

    @Override
    public Void visitIncDecStmt(Stmt.IncDec stmt) {
        Object value = environment.get(stmt.name.lexeme);
        if (!(value instanceof Integer i)) {
            throw new RuntimeException("Can only apply ++/-- to numbers.");
        }

        int updated = (stmt.operator.type == TokenType.PLUS_PLUS) ? i + 1 : i - 1;
        environment.assign(stmt.name.lexeme, updated);
        return null;
    }

    // ---------------- HELPERS ----------------

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Integer i) return i != 0;
        return true;
    }

    private String stringify(Object value) {
        if (value == null) return "null";
        return value.toString();
    }

    private void executeBlock(List<Stmt> statements, Environment newEnv) {
        Environment previous = environment;
        try {
            environment = newEnv;
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } finally {
            environment = previous;
        }
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return true;
        } else { // AND
            if (!isTruthy(left)) return false;
        }

        Object right = evaluate(expr.right);
        return isTruthy(right);
    }

    // ---------------- CONTROL FLOW ----------------

    private static class ExitSignal extends RuntimeException {}

    private static class ReturnSignal extends RuntimeException {
        final Object value;
        ReturnSignal(Object value) {
            this.value = value;
        }
    }

    private interface Callable {
        int arity();
        Object call(Interpreter interpreter, List<Object> arguments);
    }

    private static class UserFunction implements Callable {
        private final Stmt.Function declaration;
        private final Environment closure;

        UserFunction(Stmt.Function declaration, Environment closure) {
            this.declaration = declaration;
            this.closure = closure;
        }

        @Override
        public int arity() {
            return declaration.params.size();
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Environment previous = interpreter.environment;
            interpreter.environment = new Environment(closure);

            for (int i = 0; i < declaration.params.size(); i++) {
                String name = declaration.params.get(i).lexeme;
                interpreter.environment.define(name, arguments.get(i));
            }

            try {
                interpreter.executeBlock(declaration.body, interpreter.environment);
            } catch (ReturnSignal returnValue) {
                interpreter.environment = previous;
                return returnValue.value;
            }

            interpreter.environment = previous;
            return null;
        }
    }

    private static class LengthFunction implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object value = arguments.get(0);
            if (value instanceof Cell cell) {
                return cell.length();
            }
            throw new RuntimeException("length() expects a cell.");
        }
    }

    private static class InputFunction implements Callable {
        private final Scanner scanner = new Scanner(System.in);

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            try {
                if (scanner.hasNextLine()) {
                    return scanner.nextLine();
                }
            } catch (Exception ignored) {
            }
            return "";
        }
    }

    private static class RandomFunction implements Callable {
        private final Random random = new Random();

        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object a = arguments.get(0);
            Object b = arguments.get(1);
            if (!(a instanceof Integer) || !(b instanceof Integer)) {
                throw new RuntimeException("random(min, max) expects numbers.");
            }
            int min = (Integer) a;
            int max = (Integer) b;
            if (max < min) {
                int tmp = min;
                min = max;
                max = tmp;
            }
            return min + random.nextInt(max - min + 1);
        }
    }

    private static class ClockFunction implements Callable {
        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (int) (System.currentTimeMillis() / 1000);
        }
    }

    private static class TypeFunction implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object value = arguments.get(0);
            if (value == null) return "null";
            if (value instanceof Integer) return "number";
            if (value instanceof String) return "string";
            if (value instanceof Boolean) return "boolean";
            if (value instanceof Cell) return "cell";
            if (value instanceof Callable) return "function";
            return "unknown";
        }
    }

    private static class ToNumberFunction implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object value = arguments.get(0);
            if (value instanceof Integer) return value;
            if (value instanceof Boolean b) return b ? 1 : 0;
            if (value instanceof String s) {
                try {
                    return Integer.parseInt(s.trim());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("toNumber() invalid string.");
                }
            }
            throw new RuntimeException("toNumber() expects number/string/bool.");
        }
    }

    private static class ToStringFunction implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return interpreter.stringify(arguments.get(0));
        }
    }

    private static class PushFunction implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object target = arguments.get(0);
            if (!(target instanceof Cell cell)) {
                throw new RuntimeException("push() expects a cell.");
            }
            cell.push(arguments.get(1));
            return cell;
        }
    }

    private static class PopFunction implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object target = arguments.get(0);
            if (!(target instanceof Cell cell)) {
                throw new RuntimeException("pop() expects a cell.");
            }
            return cell.pop();
        }
    }

    private static class ShiftFunction implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object target = arguments.get(0);
            if (!(target instanceof Cell cell)) {
                throw new RuntimeException("shift() expects a cell.");
            }
            return cell.shift();
        }
    }

    private static class UnshiftFunction implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object target = arguments.get(0);
            if (!(target instanceof Cell cell)) {
                throw new RuntimeException("unshift() expects a cell.");
            }
            cell.unshift(arguments.get(1));
            return cell;
        }
    }

    private static class KeysFunction implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object target = arguments.get(0);
            if (!(target instanceof Cell cell)) {
                throw new RuntimeException("keys() expects a cell.");
            }
            return cell.keys();
        }
    }

    private static class ValuesFunction implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object target = arguments.get(0);
            if (!(target instanceof Cell cell)) {
                throw new RuntimeException("values() expects a cell.");
            }
            return cell.values();
        }
    }

    private static class HasFunction implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object target = arguments.get(0);
            if (!(target instanceof Cell cell)) {
                throw new RuntimeException("has() expects a cell.");
            }
            return cell.has(arguments.get(1));
        }
    }

    private static class SliceFunction implements Callable {
        @Override
        public int arity() {
            return 3;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object target = arguments.get(0);
            if (!(target instanceof Cell cell)) {
                throw new RuntimeException("slice() expects a cell.");
            }
            if (!(arguments.get(1) instanceof Integer start) ||
                !(arguments.get(2) instanceof Integer end)) {
                throw new RuntimeException("slice() expects start/end numbers.");
            }
            return cell.slice(start, end);
        }
    }

    private static class MergeFunction implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            if (!(arguments.get(0) instanceof Cell a) || !(arguments.get(1) instanceof Cell b)) {
                throw new RuntimeException("merge() expects two cells.");
            }
            return a.merge(b);
        }
    }

    private static class AssertFunction implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object condition = arguments.get(0);
            Object message = arguments.get(1);
            if (!interpreter.isTruthy(condition)) {
                throw new RuntimeException("assert failed: " + interpreter.stringify(message));
            }
            return null;
        }
    }
}
