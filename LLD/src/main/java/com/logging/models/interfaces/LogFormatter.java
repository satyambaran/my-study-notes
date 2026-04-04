package com.logging.models.interfaces;

import com.logging.models.entities.LogMessage;

public interface LogFormatter {

    String format(LogMessage logMessage);
}
