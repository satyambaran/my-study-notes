package com.tictactoe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tictactoe.exceptions.*;
import com.tictactoe.models.entities.*;
import com.tictactoe.models.enums.GameStatus;
import com.tictactoe.models.implementations.observers.Scoreboard;
import com.tictactoe.utils.AppLogger;

public class TicTacToeInstance {
    private static final AppLogger logger = AppLogger.getInstance();
    private static TicTacToeInstance instance;
    private final Scoreboard scoreboard;

    private final Map<Integer, Game> activeGames;
    private final Map<Integer, Game> completedGames;

    private TicTacToeInstance() {
        this.scoreboard = new Scoreboard();
        this.activeGames = new ConcurrentHashMap<>();
        this.completedGames = new ConcurrentHashMap<>();
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

    public int createGame(int boardSize, Player player1, Player player2) {
        Game game = new Game(boardSize, player1, player2);
        logger.info("Created new game with board size " + boardSize + " for players: " + player1.getName() + " and "
                + player2.getName());
        game.addObserver(scoreboard);
        activeGames.put(game.getId(), game);
        return game.getId();
    }

    public void makeMove(int gameId, int playerIdx, int row, int col) {
        Game game = activeGames.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }

        try {
            game.makeMove(new Move(playerIdx, row, col));
        } catch (InvalidMoveException e) {
            logger.warning("Invalid move in game " + gameId + ": " + e.getMessage());
            return;
        }

        if (game.isOver()) {
            activeGames.remove(gameId);

            completedGames.put(gameId, game);
        }

        game.printBoard();
    }

    public GameStatus getGameStatus(int gameId) {
        Game game = activeGames.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        return game.getStatus();
    }

    public void printScoreboard() {
        scoreboard.printScoreboard();
    }

}
