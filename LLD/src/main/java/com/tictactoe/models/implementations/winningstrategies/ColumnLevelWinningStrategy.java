package com.tictactoe.models.implementations.winningstrategies;

import com.tictactoe.models.entities.Board;
import com.tictactoe.models.enums.Symbol;
import com.tictactoe.models.interfaces.WinningStrategy;

public class ColumnLevelWinningStrategy implements WinningStrategy {

    @Override
    public boolean checkWin(Board board, int row, int col, Symbol symbol) {
        int size = board.getSize();
        for (int r = 0; r < size; r++) {
            if (board.getCell(r, col).getSymbol() != symbol) {
                return false;
            }
        }
        return true;
    }

}
