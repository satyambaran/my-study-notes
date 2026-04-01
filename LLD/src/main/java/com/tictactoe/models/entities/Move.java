package com.tictactoe.models.entities;

import com.tictactoe.models.enums.Symbol;

public class Move {
    private final int playerIdx;
    private final int row;
    private final int col;
    private long timestamp;
    private Symbol symbol;

    public Move(int playerIdx, int row, int col) {
        this.playerIdx = playerIdx;
        this.row = row;
        this.col = col;
    }

    public int getPlayerIdx() { return playerIdx; }

    public int getRow() { return row; }

    public int getCol() { return col; }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Symbol getSymbol() { return symbol; }

    public void setSymbol(Symbol symbol) { this.symbol = symbol; }
}
