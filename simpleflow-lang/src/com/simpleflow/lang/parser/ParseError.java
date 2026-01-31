package com.simpleflow.lang.parser;

import com.simpleflow.lang.lexer.Token;

public class ParseError extends RuntimeException {

    public final int line;

    public ParseError(Token token, String message) {
        super(message);
        this.line = token.line;
    }
}
