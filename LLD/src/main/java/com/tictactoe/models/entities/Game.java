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
    private final int id;
    private final Board board;
    private final List<Player> players;
    private final List<Symbol> symbols;
    private int currentPlayerIndex = 0;
    private GameStatus status = GameStatus.IN_PROGRESS;
    private Player winner;
    private final List<Move> moves;
    private final List<WinningStrategy> winningStrategies;
    private final List<Observer> observers;

    private static final Symbol[] PLAYER_SYMBOLS = { Symbol.X, Symbol.O };

    public Game(int boardSize) {
        this.id = idCounter++;
        this.board = new Board(boardSize);
        this.players = new ArrayList<>();
        this.symbols = new ArrayList<>();
        this.moves = new ArrayList<>();
        this.winningStrategies = initWinningStrategies();
        this.observers = new CopyOnWriteArrayList<>();
    }

    private List<WinningStrategy> initWinningStrategies() {
        List<WinningStrategy> strategies = new ArrayList<>();
        strategies.add(new RowLevelWinningStrategy());
        strategies.add(new ColumnLevelWinningStrategy());
        strategies.add(new DiagonalLevelWinningStrategy());
        return strategies;
    }

    /** Add a player before the game starts. Max 2 players supported. */
    public void addPlayer(Player player) {
        if (players.size() >= 2)
            throw new IllegalStateException("Game already has the maximum number of players.");
        symbols.add(PLAYER_SYMBOLS[players.size()]);
        players.add(player);
    }

    public synchronized void makeMove(Move move) throws InvalidMoveException {
        validateMove(move);
        int row = move.getRow();
        int col = move.getCol();
        Symbol currentSymbol = symbols.get(currentPlayerIndex);
        move.setSymbol(currentSymbol);
        moves.add(move);
        board.setCell(row, col, currentSymbol);

        if (checkWin(row, col)) {
            status = GameStatus.WINNER;
            winner = players.get(currentPlayerIndex);
            notifyObservers();
            return;
        }
        if (board.isFull()) {
            status = GameStatus.DRAW;
            notifyObservers();
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /** Undo the last move. Reverts board, turn, and game status. */
    public synchronized void undoMove() {
        if (moves.isEmpty())
            throw new InvalidMoveException("No moves to undo.");

        Move last = moves.get(moves.size() - 1);
        board.setCell(last.getRow(), last.getCol(), Symbol.EMPTY);

        if (status != GameStatus.IN_PROGRESS) {
            status = GameStatus.IN_PROGRESS;
            winner = null;
        }

        currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        moves.remove(moves.size() - 1);
    }

    private void validateMove(Move move) {
        if (status != GameStatus.IN_PROGRESS)
            throw new InvalidMoveException("Game is already over. No more moves allowed.");
        if (move.getPlayerIdx() != currentPlayerIndex)
            throw new InvalidMoveException("It's not the current player's turn.");
        if (!board.isCellEmpty(move.getRow(), move.getCol()))
            throw new InvalidMoveException("Cell is already occupied. Try a different move.");
    }

    private boolean checkWin(int row, int col) {
        for (WinningStrategy strategy : winningStrategies)
            if (strategy.checkWin(board, row, col, symbols.get(currentPlayerIndex)))
                return true;
        return false;
    }

    private void notifyObservers() {
        for (Observer observer : observers)
            observer.notify(this);
    }

    public int getId() { return id; }

    public Player getWinner() { return winner; }

    public GameStatus getStatus() { return status; }

    public boolean isOver() { return status != GameStatus.IN_PROGRESS; }

    public Board getBoard() { return board; }

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }

    public void printBoard() {
        board.printBoard();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }
}
