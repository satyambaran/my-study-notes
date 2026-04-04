package com.logging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// One instance of it always exactly one thread regardless of how many tasks are submitted.
public class AsyncWorker {
    private final String threadName;
    private ExecutorService executor;

    public AsyncWorker(String threadName) {
        this.threadName = threadName;
        this.executor = newExecutor();
    }

    private ExecutorService newExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(true); // todo : make it configurable
            return thread;
        });
    }

    public void submit(Runnable task) {
        if (executor.isShutdown()) {
            throw new IllegalStateException("AsyncWorker " + threadName + " is shutdown, cannot accept new tasks");
        }
        executor.submit(task);
    }

    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                System.err.println("AsyncWorker " + threadName + " did not terminate in the specified time.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void restart() {
        if (!executor.isShutdown()) {
            throw new IllegalStateException("AsyncWorker " + threadName + " is not shutdown, cannot restart");
        }
        this.executor = newExecutor();
    }
}
