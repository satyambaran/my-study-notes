package com.tictactoe.models.entities;

import com.tictactoe.models.enums.Symbol;

public class Cell {
    private final int row;
    private final int col;
    private Symbol symbol;

    Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.symbol = Symbol.EMPTY;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Symbol getSymbol() { return symbol; }
    public void setSymbol(Symbol symbol) { this.symbol = symbol; }
    public boolean isEmpty() { return this.symbol == Symbol.EMPTY; }
}
