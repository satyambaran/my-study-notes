package com.logging;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// One instance of it always exactly one thread regardless of how many tasks are submitted.
public class AsyncWorker {
    private static final int DEFAULT_QUEUE_CAPACITY = 1024;

    private final String threadName;
    private final int queueCapacity;
    private ExecutorService executor;

    public AsyncWorker(String threadName) {
        this(threadName, DEFAULT_QUEUE_CAPACITY);
    }

    public AsyncWorker(String threadName, int queueCapacity) {
        this.threadName = threadName;
        this.queueCapacity = queueCapacity;
        this.executor = newExecutor();
    }

    /**
     * Bounded queue backed by ArrayBlockingQueue.
     * CallerRunsPolicy: when the queue is full, the submitting thread executes the task itself.
     * This applies natural back-pressure — fast producers slow down instead of OOM-ing the JVM.
     */
    private ExecutorService newExecutor() {
        return new ThreadPoolExecutor(
            1, 1,                                           // single thread — preserves ordering
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(queueCapacity),        // bounded queue
            runnable -> {
                Thread thread = new Thread(runnable, threadName);
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()       // back-pressure: caller blocks & runs the task
        );
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
