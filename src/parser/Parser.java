package parser;

import lexer.*;
import ast.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ---------------- ENTRY ----------------

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    // ---------------- STATEMENTS ----------------

    private Stmt statement() {
        if (match(TokenType.PUT)) return putStatement();

        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.EQUAL)) {
            return assignmentStatement();
        }

        if (match(TokenType.SAY)) return printStatement();
        if (match(TokenType.SHOW)) return printStatement();
        if (match(TokenType.WHEN)) return ifStatement();
        if (match(TokenType.REPEAT)) return repeatStatement();
        if (match(TokenType.STOP)) return new Stmt.Stop();
        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());

        throw error(peek(), "Expected statement.");
    }

    private Stmt putStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");
        consume(TokenType.EQUAL, "Expected '=' after variable name.");
        Expr initializer = expression();
        return new Stmt.Put(name, initializer);
    }

    private Stmt assignmentStatement() {
        Token name = advance(); // IDENTIFIER
        consume(TokenType.EQUAL, "Expected '=' in assignment.");
        Expr value = expression();
        return new Stmt.Assign(name, value);
    }

    private Stmt printStatement() {
        Expr value = expression();
        return new Stmt.Print(value);
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'when'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(TokenType.OR)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt repeatStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'repeat'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after condition.");

        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    // ---------------- EXPRESSIONS ----------------

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL)) {

            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.NUMBER)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')'.");
            return expr;
        }

        throw error(peek(), "Expected expression.");
    }

    // ---------------- HELPERS ----------------

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuntimeException error(Token token, String message) {
        return new RuntimeException(
                "[line " + token.line + "] Error at '" + token.lexeme + "': " + message
        );
    }
}
