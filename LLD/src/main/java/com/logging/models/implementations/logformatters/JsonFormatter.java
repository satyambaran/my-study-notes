package com.logging.models.implementations.logformatters;

import java.time.format.DateTimeFormatter;

// import com.google.gson.Gson;
import com.logging.models.entities.LogMessage;
import com.logging.models.interfaces.LogFormatter;

public class JsonFormatter implements LogFormatter {
    // private static final Gson gson = new Gson();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(LogMessage logMessage) {
        // return gson.toJson(logMessage);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\n\t\"timestamp\": \"").append(logMessage.getTimestamp().format(DATE_TIME_FORMATTER)).append("\", ");
        sb.append("\n\t\"loggerName\": \"").append(logMessage.getLoggerName()).append("\", ");
        sb.append("\n\t\"threadName\": \"").append(logMessage.getThreadName()).append("\", ");
        sb.append("\n\t\"logLevel\": \"").append(logMessage.getLogLevel().getLevel()).append("\", ");
        sb.append("\n\t\"message\": \"").append(logMessage.getMessage()).append("\"");
        sb.append("\n}");
        return sb.toString();
    }
}
