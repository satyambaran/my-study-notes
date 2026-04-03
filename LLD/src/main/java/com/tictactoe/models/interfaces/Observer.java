package com.tictactoe.models.interfaces;

import com.tictactoe.models.entities.Game;

public interface Observer {
    void notify(Game game);
}
