package com.logging;

import java.util.ArrayList;
import java.util.List;

import com.logging.models.entities.LogMessage;
import com.logging.models.enums.LogLevel;
import com.logging.models.interfaces.*;

public class Logger implements ILogger {
    private final String name;
    private LogLevel logLevel; // todo why not final
    private final List<LogAppender> appenders;
    private final Logger parent;

    public Logger(String name, Logger parent) {
        this.name = name;
        this.appenders = new ArrayList<>();
        this.parent = parent;
    }

    public void addAppender(LogAppender appender) { this.appenders.add(appender); }

    public List<LogAppender> getAppenders() { return appenders; }

    public LogLevel getLogLevel() { return logLevel; }

    public void setLogLevel(LogLevel logLevel) { this.logLevel = logLevel; }

    public LogLevel getEffectiveLogLevel() {
        for (Logger l = this; l != null; l = l.parent) {
            if (l.logLevel != null)
                return l.logLevel;
        }
        return LogLevel.DEBUG; // root has no level set, so default to lowest level
}

    @Override
    public void log(LogLevel level, String message) {
        // Check if log level is enabled for this logger, if not return immediately
        // We check whether we should log this message or not.
        if (!level.isGreaterOrEqual(getEffectiveLogLevel()))
            return;
        LogMessage logMessage = new LogMessage(level, message, java.time.LocalDateTime.now(), this.name);
        for (Logger l = this; l != null; l = l.parent) {
            if (!l.appenders.isEmpty()) {
                List<LogAppender> targets = l.appenders;
                LogManager.getInstance().getProcessor().submit(() -> targets.forEach(a -> a.append(logMessage)));
                return;
            }
        }
    }
}