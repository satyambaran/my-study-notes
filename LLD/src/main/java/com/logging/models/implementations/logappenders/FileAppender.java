package com.logging.models.implementations.logappenders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.logging.models.entities.LogMessage;
import com.logging.models.interfaces.LogAppender;
import com.logging.models.interfaces.LogFormatter;

public class FileAppender implements LogAppender {
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final DateTimeFormatter ROTATION_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private LogFormatter logFormatter;
    private final String filePath;
    private final long maxFileSize;
    private FileWriter fileWriter;
    private long bytesWritten;

    public FileAppender(LogFormatter logFormatter, String filePath) {
        this(logFormatter, filePath, DEFAULT_MAX_FILE_SIZE);
    }

    public FileAppender(LogFormatter logFormatter, String filePath, long maxFileSize) {
        this.logFormatter = logFormatter;
        this.filePath = filePath;
        this.maxFileSize = maxFileSize;
        this.bytesWritten = existingFileSize(filePath);
        try {
            // create file if not exists, else append to existing file
            this.fileWriter = new FileWriter(filePath, true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create writer for file logs: " + e.getMessage());
        }
    }

    @Override
    public synchronized void append(LogMessage logMessage) {
        try {
            String formatted = logFormatter.format(logMessage) + "\n";
            byte[] bytes = formatted.getBytes();

            if (bytesWritten + bytes.length > maxFileSize) {
                rotate();
            }

            fileWriter.write(formatted);
            fileWriter.flush();
            bytesWritten += bytes.length;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write log message to file: " + e.getMessage());
        }
    }

    /**
     * Closes the current file, renames it with a timestamp suffix, and opens a fresh file at the original path.
     * e.g. app.log → app.log.20260404_153012
     */
    private void rotate() throws IOException {
        fileWriter.close();

        String rotatedName = filePath + "." + LocalDateTime.now().format(ROTATION_SUFFIX);
        File current = new File(filePath);
        File rotated = new File(rotatedName);
        if (!current.renameTo(rotated)) {
            System.err.println("Failed to rotate log file: " + filePath + " → " + rotatedName);
        }

        fileWriter = new FileWriter(filePath, true);
        bytesWritten = 0;
    }

    @Override
    public void close() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close file writer: " + e.getMessage());
        }
    }

    @Override
    public LogFormatter getLogFormatter() { return logFormatter; }

    @Override
    public void setLogFormatter(LogFormatter logFormatter) { this.logFormatter = logFormatter; }

    private static long existingFileSize(String path) {
        File f = new File(path);
        return f.exists() ? f.length() : 0;
    }
}
