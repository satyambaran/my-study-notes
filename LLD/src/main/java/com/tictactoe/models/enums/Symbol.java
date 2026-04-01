package com.tictactoe.models.enums;

public enum Symbol {
    X('X'), O('O'), EMPTY('_');

    private final char displayChar;

    Symbol(char displayChar) {
        this.displayChar = displayChar;
    }

    public char getChar() {
        return this.displayChar;
    }
}
