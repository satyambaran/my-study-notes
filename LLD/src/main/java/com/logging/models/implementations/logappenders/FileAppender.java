package com.logging.models.implementations.logappenders;

import java.io.FileWriter;

import com.logging.models.entities.LogMessage;
import com.logging.models.interfaces.LogAppender;
import com.logging.models.interfaces.LogFormatter;

public class FileAppender implements LogAppender {
    private LogFormatter logFormatter;
    private FileWriter fileWriter;

    public FileAppender(LogFormatter logFormatter, String filePath) {
        this.logFormatter = logFormatter;
        try {
            // create file if not exists, else append to existing file
            this.fileWriter = new FileWriter(filePath, true);
        } catch (Exception e) {
            // System.out.println("Failed to create writer for file logs, exception: " +
            // e.getMessage());
            throw new RuntimeException("Failed to create writer for file logs, exception: " + e.getMessage());
        }
    }

    @Override
    public synchronized void append(LogMessage logMessage) {
        try {
            this.fileWriter.write(this.logFormatter.format(logMessage) + "\n");
            this.fileWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write log message to file, exception: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            this.fileWriter.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close file writer, exception: " + e.getMessage());
        }
    }

    @Override
    public LogFormatter getLogFormatter() { return this.logFormatter; }

    @Override
    public void setLogFormatter(LogFormatter logFormatter) { this.logFormatter = logFormatter; }
}
