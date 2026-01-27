package lexer;

public enum TokenType {

    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    EQUAL, PLUS, MINUS, STAR, SLASH,
    GREATER, LESS,

    // One or two character tokens
    EQUAL_EQUAL,
    GREATER_EQUAL,
    LESS_EQUAL,

    // Literals
    IDENTIFIER, NUMBER, STRING,

    // Keywords
    PUT, SAY, SHOW,
    WHEN, OR,
    REPEAT,
    STOP,

    // End of file
    EOF
}
