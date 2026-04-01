package com.tictactoe.models.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tictactoe.exceptions.InvalidMoveException;
import com.tictactoe.models.enums.*;
import com.tictactoe.models.implementations.winningstrategies.*;
import com.tictactoe.models.interfaces.*;

public class Game {
    private static int idCounter = 1;
    private final Board board;
    private final Player[] players;
    private final Symbol[] symbols;
    private int currentPlayerIndex;

    private GameStatus status;
    private Player winner;

    private final List<Move> moves;
    private final List<WinningStrategy> winningStrategies;
    private final List<Observer> observers;
    private final int id;

    public Game(int boardSize, Player player1, Player player2) {
        this.id = idCounter++;
        this.board = new Board(boardSize);
        this.players = new Player[] { player1, player2 };
        this.symbols = new Symbol[] { Symbol.X, Symbol.O };
        this.currentPlayerIndex = 0;
        this.status = GameStatus.IN_PROGRESS;
        this.winningStrategies = initWinningStrategies();
        this.observers = new CopyOnWriteArrayList<>();
        this.moves = new ArrayList<>();
    }

    private List<WinningStrategy> initWinningStrategies() {
        List<WinningStrategy> strategies = new ArrayList<>();
        strategies.add(new RowLevelWinningStrategy());
        strategies.add(new ColumnLevelWinningStrategy());
        strategies.add(new DiagonalLevelWinningStrategy());
        return strategies;
    }

    public synchronized void makeMove(Move move) throws InvalidMoveException {
        if (status != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("Game is already over. No more moves allowed.");
        }

        if (move.getPlayerIdx() != currentPlayerIndex) {
            throw new InvalidMoveException("It's not the current player's turn.");
        }

        int row = move.getRow();
        int col = move.getCol();

        if (!board.isCellEmpty(row, col)) {
            throw new InvalidMoveException("Cell is already occupied. Try a different move.");
        }

        Player currentPlayer = players[currentPlayerIndex];
        Symbol currentSymbol = symbols[currentPlayerIndex];

        move.setSymbol(currentSymbol);
        moves.add(move);
        board.setCell(row, col, currentSymbol);

        if (checkWin(row, col)) {
            status = GameStatus.WINNER;
            winner = currentPlayer;
            notifyObservers();
            return;
        }

        // Draw condition met
        if (board.isFull()) {
            status = GameStatus.DRAW;
            notifyObservers();
            return;
        }

        // Switch player for next turn
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length; // Toggle between 0 and 1
    }

    private boolean checkWin(int row, int col) {
        // Check for win
        for (WinningStrategy strategy : winningStrategies) {
            if (strategy.checkWin(board, row, col, symbols[currentPlayerIndex])) {
                return true;
            }
        }
        return false;
    }

    public int getId() { return id; }

    public Player getWinner() { return winner; }

    public GameStatus getStatus() { return status; }

    public boolean isOver() { return status != GameStatus.IN_PROGRESS; }

    public Board getBoard() { return board; }

    public Player getCurrentPlayer() { return players[currentPlayerIndex]; }

    public void printBoard() {
        board.printBoard();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.notify(this);
        }
    }
}
