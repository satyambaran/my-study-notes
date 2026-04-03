package com.tictactoe.models.implementations.observers;

import java.util.concurrent.ConcurrentHashMap;

import com.tictactoe.models.entities.Game;
import com.tictactoe.models.entities.Player;
import com.tictactoe.models.enums.GameStatus;
import com.tictactoe.models.interfaces.Observer;
import com.tictactoe.utils.AppLogger;

public class Scoreboard implements Observer {
    private static final AppLogger logger = AppLogger.getInstance();
    private final ConcurrentHashMap<String, Integer> playerScores;

    public Scoreboard() {
        this.playerScores = new ConcurrentHashMap<>();
    }

    @Override
    public void notify(Game game) {
        if (game.getStatus() == GameStatus.WINNER) {
            Player winner = game.getWinner();
            logger.info("Scoreboard updated: " + winner.getName() + " wins!");
            playerScores.merge(winner.getName(), 1, Integer::sum);
        }
    }

    public void recordWin(Player player) {
        playerScores.merge(player.getName(), 1, Integer::sum);
    }

    public int getScore(String playerName) {
        return playerScores.getOrDefault(playerName, 0);
    }

    public void printScoreboard() {
        StringBuilder sb = new StringBuilder("\n===== SCOREBOARD =====\n");
        if (playerScores.isEmpty()) {
            sb.append("No games played yet.\n");
        } else {
            playerScores.forEach((name, score) -> sb.append(name).append(": ").append(score).append(" wins\n"));
        }
        sb.append("======================");
        logger.info(sb.toString());
    }
}
