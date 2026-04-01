package com.tictactoe;

import com.tictactoe.models.entities.*;

public class TicTacToe {
    public static void main(String[] args) {
        TicTacToeInstance instance = TicTacToeInstance.getInstance();
        Player player1 = new Player("Alice");
        Player player2 = new Player("Bob");
        int gameId = instance.createGame(3, player1, player2);
        instance.makeMove(gameId, 0, 0, 0); // Alice moves
        instance.makeMove(gameId, 1, 0, 1); // Bob moves
        instance.makeMove(gameId, 0, 1, 1); // Alice moves
        instance.makeMove(gameId, 0, 1, 1); // Alice moves
        instance.makeMove(gameId, 1, 1, 1); // Bob moves
        // instance.makeMove(gameId, 1, -1, 12); // Bob moves
        instance.makeMove(gameId, 1, 1, 0); // Bob moves
        instance.makeMove(gameId, 0, 2, 2); // Alice moves and wins
    }
}
