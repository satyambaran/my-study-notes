package com.tictactoe.models.implementations.winningstrategies;

import com.tictactoe.models.entities.Board;
import com.tictactoe.models.enums.Symbol;
import com.tictactoe.models.interfaces.WinningStrategy;

public class RowLevelWinningStrategy implements WinningStrategy {

    @Override
    public boolean checkWin(Board board, int row, int col, Symbol symbol) {
        int size = board.getSize();
        for (int c = 0; c < size; c++) {
            if (board.getCell(row, c).getSymbol() != symbol) {
                return false;
            }
        }
        return true;
    }

}
