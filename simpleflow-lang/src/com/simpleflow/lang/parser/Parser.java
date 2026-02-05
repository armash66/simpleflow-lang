package com.simpleflow.lang.parser;

import java.util.ArrayList;
import java.util.List;

import com.simpleflow.lang.ast.Expr;
import com.simpleflow.lang.ast.Stmt;
import com.simpleflow.lang.lexer.Token;
import com.simpleflow.lang.lexer.TokenType;

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
        // variable declaration
        if (match(TokenType.SET)) return setStatement();

        // assignment (x = expr)
        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.EQUAL)) {
            return assignmentStatement();
        }

        // output
        if (match(TokenType.SAY)) return printStatement();
        if (match(TokenType.SHOW)) return printStatement();

        // control flow
        if (match(TokenType.WHEN)) return whenStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.LEAVE)) return new Stmt.Leave();
        if (match(TokenType.NEXT)) return new Stmt.Next();

        // program end
        if (match(TokenType.EXIT)) return new Stmt.Exit();

        if (match(TokenType.DEFINE)) return functionStatement();
        if (match(TokenType.RETURN)) return returnStatement();

        // block
        if (match(TokenType.LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        throw error(peek(), "Expected statement.");
    }

    private Stmt setStatement() {
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

    private Stmt whenStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'when'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(TokenType.OTHERWISE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after condition.");

        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'.");

        Stmt initializer = null;
        if (match(TokenType.SEMICOLON)) {
            // no initializer
        } else if (match(TokenType.SET)) {
            initializer = setStatement();
            consume(TokenType.SEMICOLON, "Expected ';' after initializer.");
        } else if (check(TokenType.IDENTIFIER) && checkNext(TokenType.EQUAL)) {
            initializer = assignmentStatement();
            consume(TokenType.SEMICOLON, "Expected ';' after initializer.");
        } else {
            throw error(peek(), "Expected initializer in for loop.");
        }

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after loop condition.");

        Stmt increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            if (match(TokenType.SET)) {
                increment = setStatement();
            } else if (check(TokenType.IDENTIFIER) && checkNext(TokenType.EQUAL)) {
                increment = assignmentStatement();
            } else {
                throw error(peek(), "Expected increment in for loop.");
            }
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after for clauses.");

        Stmt body = statement();

        if (increment != null) {
            body = replaceNext(body, increment);
            List<Stmt> statements = new ArrayList<>();
            statements.add(body);
            statements.add(increment);
            body = new Stmt.Block(statements);
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }

        body = new Stmt.While(condition, body);

        if (initializer != null) {
            List<Stmt> statements = new ArrayList<>();
            statements.add(initializer);
            statements.add(body);
            body = new Stmt.Block(statements);
        }

        return body;
    }

    private Stmt replaceNext(Stmt stmt, Stmt increment) {
        if (stmt instanceof Stmt.Next) {
            List<Stmt> statements = new ArrayList<>();
            statements.add(increment);
            statements.add(stmt);
            return new Stmt.Block(statements);
        }

        if (stmt instanceof Stmt.Block block) {
            List<Stmt> statements = new ArrayList<>();
            for (Stmt s : block.statements) {
                statements.add(replaceNext(s, increment));
            }
            return new Stmt.Block(statements);
        }

        if (stmt instanceof Stmt.If ifStmt) {
            Stmt thenBranch = replaceNext(ifStmt.thenBranch, increment);
            Stmt elseBranch = ifStmt.elseBranch == null
                ? null
                : replaceNext(ifStmt.elseBranch, increment);
            return new Stmt.If(ifStmt.condition, thenBranch, elseBranch);
        }

        if (stmt instanceof Stmt.While || stmt instanceof Stmt.Function) {
            return stmt;
        }

        return stmt;
    }

    private Stmt functionStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expected function name.");
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name.");

        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                parameters.add(
                    consume(TokenType.IDENTIFIER, "Expected parameter name.")
                );
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters.");
        consume(TokenType.LEFT_BRACE, "Expected '{' before function body.");

        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;

        if (!check(TokenType.SEMICOLON) && !check(TokenType.RIGHT_BRACE)) {
            value = expression();
        }

        return new Stmt.Return(keyword, value);
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
        return or();
    }

    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
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
        if (match(TokenType.NOT)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
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
            Expr expr = new Expr.Variable(previous());
            return finishCall(expr);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')'.");
            return expr;
        }

        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }

        throw error(peek(), "Expected expression.");
    }

    private Expr finishCall(Expr callee) {
    if (!match(TokenType.LEFT_PAREN)) return callee;

    List<Expr> arguments = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
        do {
            arguments.add(expression());
        } while (match(TokenType.COMMA));
    }

    Token paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments.");
    return new Expr.Call(callee, paren, arguments);
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

    private ParseError error(Token token, String message) {
        throw new ParseError(token.line, token.column, message);
    }
}
