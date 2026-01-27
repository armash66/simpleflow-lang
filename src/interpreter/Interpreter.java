package interpreter;

import ast.*;
import lexer.Token;
import lexer.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private final Map<String, Object> environment = new HashMap<>();

    // ---------------- ENTRY ----------------

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (StopSignal ignored) {
            // program stopped
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
        environment.put(stmt.name.lexeme, value);
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
        for (Stmt statement : stmt.statements) {
            execute(statement);
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
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitStopStmt(Stmt.Stop stmt) {
        throw new StopSignal();
    }

    // ---------------- EXPRESSIONS ----------------

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        if (!environment.containsKey(expr.name.lexeme)) {
            throw new RuntimeException(
                "Undefined variable '" + expr.name.lexeme + "' at line " + expr.name.line
            );
        }
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

    // ---------------- HELPERS ----------------

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean) value;
        if (value instanceof Integer) return (int) value != 0;
        return true;
    }

    private String stringify(Object value) {
        if (value == null) return "null";
        return value.toString();
    }

    // ---------------- STOP SIGNAL ----------------

    private static class StopSignal extends RuntimeException {}

    @Override
        public Void visitAssignStmt(Stmt.Assign stmt) {
    if (!environment.containsKey(stmt.name.lexeme)) {
        throw new RuntimeException(
            "Undefined variable '" + stmt.name.lexeme + "' at line " + stmt.name.line
        );
    }
    Object value = evaluate(stmt.value);
    environment.put(stmt.name.lexeme, value);
    return null;
    }
}
