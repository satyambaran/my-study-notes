package com.tictactoe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tictactoe.exceptions.GameNotFoundException;
import com.tictactoe.models.entities.Game;

/**
 * Owns the active/completed game maps.
 * Separated from TicTacToeInstance so the facade doesn't mix lookup logic
 * with game-lifecycle concerns.
 */
public class GameRegistry {
    private final Map<Integer, Game> activeGames = new ConcurrentHashMap<>();
    private final Map<Integer, Game> completedGames = new ConcurrentHashMap<>();

    public void registerGame(Game game) {
        activeGames.put(game.getId(), game);
    }

    /** Returns the active game or throws GameNotFoundException. */
    public Game findActiveGame(int gameId) {
        Game game = activeGames.get(gameId);
        if (game == null) throw new GameNotFoundException(gameId);
        return game;
    }

    public void completeGame(int gameId) {
        Game game = activeGames.remove(gameId);
        if (game != null) completedGames.put(gameId, game);
    }
}
