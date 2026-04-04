package com.logging.models.interfaces;

import com.logging.models.enums.LogLevel;

public interface ILogger {
    void log(LogLevel level, String message);

    default void debug(String message) { log(LogLevel.DEBUG, message); }

    default void info(String message) { log(LogLevel.INFO, message); }

    default void warn(String message) { log(LogLevel.WARN, message); }

    default void error(String message) { log(LogLevel.ERROR, message); }

    default void fatal(String message) { log(LogLevel.FATAL, message); }
}
