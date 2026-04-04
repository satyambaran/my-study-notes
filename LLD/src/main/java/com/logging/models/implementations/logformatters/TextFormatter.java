package com.logging.models.implementations.logformatters;

import com.logging.models.entities.LogMessage;
import com.logging.models.interfaces.LogFormatter;

public class TextFormatter implements LogFormatter {
    @Override
    public String format(LogMessage logMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(logMessage.getTimestamp()).append(" ");
        sb.append("[").append(logMessage.getLogLevel().getLevel()).append("] ");
        sb.append("[").append(logMessage.getThreadName()).append("] ");
        sb.append(logMessage.getLoggerName()).append(" - ");
        sb.append(logMessage.getMessage());
        return sb.toString();
    }
}
