package com.tictactoe.utils;

import java.util.logging.Logger;

public class AppLogger {
    private static final Logger logger = Logger.getLogger("TicTacToe");
    private static AppLogger instance;

    private AppLogger() {}

    public static AppLogger getInstance() {
        if (instance == null) {
            synchronized (AppLogger.class) {
                if (instance == null) {
                    instance = new AppLogger();
                }
            }
        }
        return instance;
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warning(String message) {
        logger.warning(message);
    }
}
