package com.logging;

/**
 * Thin facade over LogManager — mirrors the SLF4J / Log4j entry point. Clients
 * call LoggerFactory.getLogger(...) rather than reaching into LogManager
 * directly.
 */
public class LoggerFactory {
    private LoggerFactory() {}

    public static Logger getLogger(String name) { return LogManager.getInstance().getLogger(name); }

    public static Logger getLogger(Class<?> _class) { return getLogger(_class.getName()); }
}
