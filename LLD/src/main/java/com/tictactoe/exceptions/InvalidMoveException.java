package com.tictactoe.exceptions;

public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String messageString) {
        super(messageString);
    }
}
