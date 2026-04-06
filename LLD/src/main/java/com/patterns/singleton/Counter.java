package com.patterns.singleton;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private AtomicInteger count = new AtomicInteger(0);

    private Counter() {}

    private static volatile Counter instance;

    public static Counter getInstance() {
        if (instance == null) {
            synchronized (Counter.class) {
                if (instance == null) {
                    instance = new Counter();
                }
            }
        }
        return instance;
    }

    public void increment() { count.incrementAndGet(); }

    public int getCount() { return count.get(); }

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
        Counter c1 = Counter.getInstance();
        Counter c2 = Counter.getInstance();
        System.out.println("Same instance: " + (c1 == c2));
        for (int i = 0; i < 5; i++) {
            c1.increment();
        }
        System.out.println("Count after 5 increments: " + c2.getCount());
    }
}