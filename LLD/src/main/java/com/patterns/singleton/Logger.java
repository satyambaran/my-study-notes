package com.patterns.singleton;

public class Logger {
    private Logger() {}

    private volatile static Logger instance;

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    private Level minLevel = Level.INFO;

    public void setLevel(Level level) { minLevel = level; }

    private void log(Level level, String message) {
        if (level.ordinal() >= minLevel.ordinal()) {
            System.out.printf("[%s] %s\n", level, message);
        }
    }

    public void debug(String msg) { log(Level.DEBUG, msg); }

    public void info(String msg) { log(Level.INFO, msg); }

    public void warn(String msg) { log(Level.WARN, msg); }

    public void error(String msg) { log(Level.ERROR, msg); }

    public static void main(String[] args) {
        // After implementing, usage should look like:
        Logger l1 = Logger.getInstance();
        Logger l2 = Logger.getInstance();
        System.out.println("Same instance: " + (l1 == l2));
        l1.setLevel(Logger.Level.WARN);
        l1.debug("Starting up");
        l1.info("Server listening on port 8080");
        l1.warn("Connection pool running low");
        l1.error("Failed to connect to database");
    }
}
