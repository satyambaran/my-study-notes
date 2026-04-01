package com.tictactoe.models.entities;

import com.tictactoe.models.enums.Symbol;
import com.tictactoe.utils.AppLogger;

public class Board {
    private static final AppLogger logger = AppLogger.getInstance();
    private int size;
    private Cell[][] cells;

    public Board(int size) {
        this.size = size;
        this.cells = new Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    public int getSize() { return size; }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    public void printBoard() {
        StringBuilder sb = new StringBuilder("\n");
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sb.append(cells[i][j].getSymbol().getChar());
                if (j < size - 1) sb.append(" | ");
            }
            sb.append("\n");
            if (i < size - 1) sb.append("-".repeat(size * 4 - 3)).append("\n");
        }
        logger.info(sb.toString());
    }

    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setCell(int row, int col, Symbol currentSymbol) {
        cells[row][col].setSymbol(currentSymbol);
    }

    public boolean isCellEmpty(int row, int col) {
        Cell cell = getCell(row, col);
        return cell.isEmpty();
    }
}
