package com.tictactoe.models.interfaces;

import com.tictactoe.models.entities.Board;
import com.tictactoe.models.enums.Symbol;

public interface WinningStrategy {
    boolean checkWin(Board board, int row, int col, Symbol symbol);
}
