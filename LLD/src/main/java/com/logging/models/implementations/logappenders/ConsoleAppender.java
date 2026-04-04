package com.logging.models.implementations.logappenders;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.logging.models.entities.LogMessage;
import com.logging.models.interfaces.LogAppender;
import com.logging.models.interfaces.LogFormatter;

public class ConsoleAppender implements LogAppender {
    private LogFormatter logFormatter;
    private PrintWriter out;

    public ConsoleAppender(LogFormatter logFormatter) {
        this.logFormatter = logFormatter;
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
    }

    @Override
    public void append(LogMessage logMessage) {
        this.out.println(this.logFormatter.format(logMessage));
    }

    @Override
    public void close() {
        this.out.close();
    }

    @Override
    public LogFormatter getLogFormatter() { return this.logFormatter; }

    @Override
    public void setLogFormatter(LogFormatter logFormatter) { this.logFormatter = logFormatter; }

}
