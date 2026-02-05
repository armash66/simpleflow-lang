package com.simpleflow.lang.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import com.simpleflow.lang.ast.Expr;
import com.simpleflow.lang.ast.Stmt;
import com.simpleflow.lang.lexer.TokenType;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();
    private boolean inLoop = false;

    // ---------------- ENTRY ----------------

    public Interpreter() {
        environment.define("length", new LengthFunction());
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

            default -> throw new RuntimeException("Unknown operator.");
        };
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        if (expr.operator.type == TokenType.NOT) {
            return !isTruthy(right);
        }

        throw new RuntimeException("Unknown operator.");
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
        Object target = environment.get(stmt.name.lexeme);
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
}
