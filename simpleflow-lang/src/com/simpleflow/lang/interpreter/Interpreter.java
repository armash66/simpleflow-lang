package com.simpleflow.lang.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import com.simpleflow.lang.ast.Expr;
import com.simpleflow.lang.ast.Stmt;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();
    private boolean inLoop = false;
    private static final int MAX_STEPS = 100_000;
    private int steps = 0;

    private void checkExecution() {
        steps++;
        if (steps > MAX_STEPS) {
            throw new RuntimeException("Execution limit exceeded");
        }
    }

    // ---------------- ENTRY ----------------

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

        return buffer.toString().trim();
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

        if (environment.exists(stmt.name.lexeme)) {
            environment.assign(stmt.name.lexeme, value);
        } else {
            environment.define(stmt.name.lexeme, value);
        }

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
        while (isTruthy(evaluate(stmt.condition))) {
            checkExecution();      // 🔥 THIS LINE FIXES EVERYTHING
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitStopStmt(Stmt.Stop stmt) {
        throw new ExitSignal();
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        environment.define(stmt.name.lexeme, stmt);
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
            case EQUAL_EQUAL -> left.equals(right);

            default -> throw new RuntimeException("Unknown operator.");
        };
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {

        Object callee = evaluate(expr.callee);

        if (!(callee instanceof Stmt.Function function)) {
            throw new RuntimeException("Can only call functions.");
        }

        if (expr.arguments.size() != function.params.size()) {
            throw new RuntimeException(
                "Expected " + function.params.size() +
                " arguments but got " + expr.arguments.size()
            );
        }

        Environment previous = environment;
        environment = new Environment(previous);

        for (int i = 0; i < function.params.size(); i++) {
            String name = function.params.get(i).lexeme;
            Object value = evaluate(expr.arguments.get(i));
            environment.define(name, value);
        }

        try {
            executeBlock(function.body, environment);
        } catch (ReturnSignal returnValue) {
            environment = previous;
            return returnValue.value;
        }

        environment = previous;
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

    // ---------------- CONTROL FLOW ----------------

    private static class ExitSignal extends RuntimeException {}

    private static class ReturnSignal extends RuntimeException {
        final Object value;
        ReturnSignal(Object value) {
            this.value = value;
        }
    }
}
