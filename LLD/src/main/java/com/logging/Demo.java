package com.logging;

import com.logging.models.enums.LogLevel;
import com.logging.models.implementations.logappenders.*;
import com.logging.models.implementations.logformatters.*;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        // --- Configure root logger (console, text format, INFO+) ---
        Logger rootLogger = LogManager.getInstance().getRootLogger();
        rootLogger.setLogLevel(LogLevel.INFO);
        rootLogger.addAppender(new ConsoleAppender(new TextFormatter()));

        // --- Named logger for "com.myapp" (file, JSON format, DEBUG+) ---
        Logger appLogger = LoggerFactory.getLogger("com.myapp");
        // Root provides a global default
        appLogger.setLogLevel(LogLevel.WARN); // Only log WARN and above for this logger
        appLogger.addAppender(new FileAppender(new JsonFormatter(), "app.log"));

        // --- Log from a named logger; its appenders handle it ---
        appLogger.debug("Application starting up");
        appLogger.info("Server listening on port 8080");
        appLogger.warn("Connection pool nearing capacity");
        appLogger.error("Failed to reach database — retrying");

        // --- Log from a class-named logger (no own appenders → propagates to root) ---
        Logger serviceLogger = LoggerFactory.getLogger(Demo.class);
        serviceLogger.info("Service layer initialized");  // goes to root → console
        serviceLogger.debug("Debug: below root's INFO threshold — filtered out");

        // Give async processor time to drain before JVM exits
        Thread.sleep(500);
        LogManager.getInstance().shutdown();
        System.out.println("Shutdown complete.");
    }
}
