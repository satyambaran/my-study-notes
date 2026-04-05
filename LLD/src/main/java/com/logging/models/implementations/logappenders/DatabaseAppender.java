package com.logging.models.implementations.logappenders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import com.logging.models.entities.LogMessage;
import com.logging.models.interfaces.LogAppender;
import com.logging.models.interfaces.LogFormatter;

/**
 * Persists log messages to a relational database table.
 *
 * Expected table schema:
 *   CREATE TABLE logs (
 *       id         BIGINT AUTO_INCREMENT PRIMARY KEY,
 *       timestamp  TIMESTAMP     NOT NULL,
 *       log_level  VARCHAR(10)   NOT NULL,
 *       logger     VARCHAR(255)  NOT NULL,
 *       thread     VARCHAR(255)  NOT NULL,
 *       message    TEXT          NOT NULL
 *   );
 *
 * Uses a DataSource (connection pool) so connections are reused efficiently.
 */
public class DatabaseAppender implements LogAppender {
    private static final String INSERT_SQL =
        "INSERT INTO logs (timestamp, log_level, logger, thread, message) VALUES (?, ?, ?, ?, ?)";

    private final DataSource dataSource;
    private LogFormatter logFormatter;

    public DatabaseAppender(DataSource dataSource, LogFormatter logFormatter) {
        this.dataSource = dataSource;
        this.logFormatter = logFormatter;
    }

    @Override
    public void append(LogMessage logMessage) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setTimestamp(1, Timestamp.valueOf(logMessage.getTimestamp()));
            ps.setString(2, logMessage.getLogLevel().name());
            ps.setString(3, logMessage.getLoggerName());
            ps.setString(4, logMessage.getThreadName());
            ps.setString(5, logFormatter.format(logMessage));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DatabaseAppender failed to write log: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        // DataSource lifecycle is managed externally by the application
    }

    @Override
    public LogFormatter getLogFormatter() { return logFormatter; }

    @Override
    public void setLogFormatter(LogFormatter logFormatter) { this.logFormatter = logFormatter; }
}
