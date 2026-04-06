package com.logging.models.interfaces;

import com.logging.models.entities.LogMessage;

public interface LogAppender {

    void append(LogMessage logMessage);

    void close();

    LogFormatter getLogFormatter();

    void setLogFormatter(LogFormatter logFormatter);
}
