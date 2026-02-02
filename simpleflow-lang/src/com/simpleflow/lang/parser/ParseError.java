package com.simpleflow.lang.parser;

public class ParseError extends RuntimeException {
    public final int line;
    public final int column;

    public ParseError(int line, int column, String message) {
        super(message);
        this.line = line;
        this.column = column;
    }
}
