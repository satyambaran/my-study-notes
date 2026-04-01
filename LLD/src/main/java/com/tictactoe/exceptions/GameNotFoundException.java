package com.tictactoe.exceptions;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(int idx) {
        super("Game with ID " + idx + " not found.");
    }
}
