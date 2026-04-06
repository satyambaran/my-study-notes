package com.tictactoe;

import java.util.List;

import com.tictactoe.models.entities.Player;
import com.tictactoe.utils.AppLogger;

public class TicTacToe {
    public static void main(String[] args) {
        TicTacToeInstance instance = TicTacToeInstance.getInstance();
        Player player1 = new Player("Alice");
        Player player2 = new Player("Bob");

        int gameId = instance.createGame(3, List.of(player1, player2));
        instance.makeMove(gameId, 0, 0, 0); // Alice → (0,0)
        instance.makeMove(gameId, 1, 0, 1); // Bob → (0,1)
        instance.makeMove(gameId, 0, 1, 1); // Alice → (1,1)
        instance.makeMove(gameId, 0, 1, 1); // duplicate — warned
        instance.makeMove(gameId, 1, 1, 1); // Bob tries occupied cell — warned
        instance.makeMove(gameId, 1, 1, 0); // Bob → (1,0)

        AppLogger.getInstance().info("--- Undoing Bob's last move ---");
        instance.undoMove(gameId); // undo Bob (1,0)

        instance.makeMove(gameId, 1, 2, 0); // Bob → (2,0)
        instance.makeMove(gameId, 0, 2, 2); // Alice → (2,2) — wins on diagonal
        instance.printScoreboard();
    }
}
