package com.tictactoe;

import java.util.List;

import com.tictactoe.exceptions.InvalidMoveException;
import com.tictactoe.models.entities.*;
import com.tictactoe.models.enums.GameStatus;
import com.tictactoe.models.implementations.observers.Scoreboard;
import com.tictactoe.utils.AppLogger;

/**
 * Facade: single entry-point for the outside world.
 * Delegates session tracking to GameRegistry and score tracking to Scoreboard.
 */
public class TicTacToeInstance {
    private static final AppLogger logger = AppLogger.getInstance();
    private static TicTacToeInstance instance;
    private final Scoreboard scoreboard;
    private final GameRegistry registry;

    private TicTacToeInstance() {
        this.scoreboard = new Scoreboard();
        this.registry = new GameRegistry();
    }

    public static TicTacToeInstance getInstance() {
        if (instance == null) {
            synchronized (TicTacToeInstance.class) {
                if (instance == null) {
                    instance = new TicTacToeInstance();
                }
            }
        }
        return instance;
    }

    /** Creates a game and adds the provided players in order. */
    public int createGame(int boardSize, List<Player> players) {
        Game game = new Game(boardSize);
        for (Player p : players) game.addPlayer(p);

        StringBuilder names = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) names.append(" and ");
            names.append(players.get(i).getName());
        }
        logger.info("Created new game with board size " + boardSize + " for players: " + names);

        game.addObserver(scoreboard);
        registry.registerGame(game);
        return game.getId();
    }

    public void makeMove(int gameId, int playerIdx, int row, int col) {
        Game game = registry.findActiveGame(gameId);

        try {
            game.makeMove(new Move(playerIdx, row, col));
        } catch (InvalidMoveException e) {
            logger.warning("Invalid move in game " + gameId + ": " + e.getMessage());
            return;
        }

        if (game.isOver()) registry.completeGame(gameId);
        game.printBoard();
    }

    public void undoMove(int gameId) {
        Game game = registry.findActiveGame(gameId);
        try {
            game.undoMove();
        } catch (InvalidMoveException e) {
            logger.warning("Undo failed in game " + gameId + ": " + e.getMessage());
        }
        game.printBoard();
    }

    public GameStatus getGameStatus(int gameId) {
        return registry.findActiveGame(gameId).getStatus();
    }

    public void printScoreboard() {
        scoreboard.printScoreboard();
    }
}
