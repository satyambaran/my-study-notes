package com.taskscheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.taskscheduler.models.entities.ScheduledTask;
import com.taskscheduler.models.enums.TaskStatus;
import com.taskscheduler.models.interfaces.Observer;
import com.taskscheduler.models.interfaces.SchedulingStrategy;
import com.taskscheduler.models.interfaces.Task;

public class TaskSchedulerService {
    private static TaskSchedulerService INSTANCE;
    private Thread[] workers;
    private CopyOnWriteArrayList<Observer> observers;
    private volatile AtomicInteger sequenceCounter; // to maintain FIFO order for tasks with same execution time
    private volatile boolean isRunning;
    private PriorityQueue<ScheduledTask> taskQueue;
    private int queueCapacity = 1000;
    private ReentrantLock queueLock;
    private final Condition notFull = queueLock.newCondition();
    private final Condition notEmpty = queueLock.newCondition();

    private TaskSchedulerService() {}

    public void initialize(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Worker count must be greater than 0");
        }
        this.taskQueue = new PriorityQueue<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.sequenceCounter = new AtomicInteger(0);
        this.isRunning = true;
        this.queueLock = new ReentrantLock();
        this.workers = new Thread[workerCount];
        startWorkers(workerCount);
    }

    private void startWorkers(int workerCount) {
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Thread(this::runWorker, "Worker-" + (i + 1));
            workers[i].setDaemon(true);
            workers[i].start();
        }
    }

    public static TaskSchedulerService getInstance() { return INSTANCE; }

    public String scheduleTask(Task task, SchedulingStrategy strategy) throws InterruptedException {
        ScheduledTask scheduledTask = new ScheduledTask(task, strategy);
        pushTaskToQueue(scheduledTask);
        return scheduledTask.getId();
    }

    private void pushTaskToQueue(ScheduledTask scheduledTask) throws InterruptedException {
        scheduledTask.setStatus(TaskStatus.SCHEDULED);
        queueLock.lock();
        try {
            while (taskQueue.size() >= queueCapacity) {
                notFull.await();
            }
            taskQueue.offer(scheduledTask);
            notEmpty.signal();
        } finally {
            queueLock.unlock();
        }
    }

    public void cancelTask(String taskId) {}

    public void rescheduleTask(String taskId, SchedulingStrategy newStrategy) {}

    private void runWorker() {
        while (isRunning) {
            try {
                while (taskQueue.isEmpty()) {
                    notEmpty.await();
                }
                ScheduledTask task = taskQueue.poll();
                notFull.signal();
                LocalDateTime now = LocalDateTime.now();
                long wait = Duration.between(now, task.getNextExecutionTime()).toMillis();
                if (wait > 0) {
                    Thread.sleep(wait);
                }
                executeTask(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.printf("%s stopped.%n", Thread.currentThread().getName());
    }

    private void executeTask(ScheduledTask task) throws InterruptedException {
        observers.forEach(observer -> observer.onStart(task));
        try {
            task.setStatus(TaskStatus.RUNNING);
            task.getTask().execute();
            task.setStatus(TaskStatus.COMPLETED);
            task.updateLastExecutionTime();
            observers.forEach(observer -> observer.onComplete(task));
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            observers.forEach(observer -> observer.onFailure(task, e));
        } finally {
            task.updateNextExecutionTime();
            if (task.hasMoreExecutions()) {
                pushTaskToQueue(task);
            }
        }
    }

    public void addObserver(Observer observer) { observers.add(observer); }

    public void shutdown() {
        isRunning = false;
        for (Thread worker : workers) {
            worker.interrupt();
        }
        System.out.println("Task Scheduler Service has been shut down.");
    }
}
