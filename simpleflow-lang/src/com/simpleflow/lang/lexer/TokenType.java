package com.simpleflow.lang.lexer;

public enum TokenType {

    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    LEFT_BRACKET, RIGHT_BRACKET,
    COMMA, DOT,
    MINUS, PLUS,
    SEMICOLON, SLASH, STAR,
    AT,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    PLUS_PLUS, MINUS_MINUS,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND,
    OR,
    NOT,
    STORE,
    PRINT,
    SAY,
    SHOW,
    WHEN,
    OTHERWISE,
    WHILE,
    FOR,
    EXIT,
    LEAVE,
    NEXT,
    TRUE,
    FALSE,
    NULL,
    DEFINE,
    RETURN,

    // End of file
    EOF
}
