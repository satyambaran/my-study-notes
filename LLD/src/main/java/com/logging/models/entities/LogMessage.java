package com.logging.models.entities;

import java.time.LocalDateTime;

import com.logging.models.enums.LogLevel;

public class LogMessage {
    private final LogLevel logLevel;
    private final String message;
    private final LocalDateTime timestamp;
    private final String loggerName;
    private final String threadName;

    public LogMessage(LogLevel logLevel, String message, LocalDateTime timestamp, String loggerName) {
        this.logLevel = logLevel;
        this.message = message;
        this.timestamp = timestamp;
        this.loggerName = loggerName;
        this.threadName = Thread.currentThread().getName();
    }

    public LogLevel getLogLevel() { return logLevel; }

    public String getMessage() { return message; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public String getLoggerName() { return loggerName; }

    public String getThreadName() { return threadName; }
}
