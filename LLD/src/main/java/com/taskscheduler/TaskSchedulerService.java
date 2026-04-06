package com.taskscheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.taskscheduler.models.entities.ScheduledTask;
import com.taskscheduler.models.enums.TaskStatus;
import com.taskscheduler.models.interfaces.Observer;
import com.taskscheduler.models.interfaces.SchedulingStrategy;
import com.taskscheduler.models.interfaces.Task;

public class TaskSchedulerService {
    // FIX: volatile ensures visibility across threads for double-checked locking
    private static volatile TaskSchedulerService INSTANCE;

    private Thread[] workers;
    private CopyOnWriteArrayList<Observer> observers;
    // FIX: removed redundant volatile — AtomicInteger is already thread-safe
    private final AtomicInteger sequenceCounter = new AtomicInteger(0);
    private volatile boolean isRunning;
    private PriorityQueue<ScheduledTask> taskQueue;
    // FIX: added task lookup map to support cancel/reschedule by ID
    private Map<String, ScheduledTask> taskMap;
    private final int queueCapacity = 1000;
    // FIX: initialize lock and conditions here — old code initialized conditions
    // from a null queueLock (assigned in initialize()), causing NPE on class load
    private final ReentrantLock queueLock = new ReentrantLock();
    private final Condition notFull = queueLock.newCondition();
    private final Condition notEmpty = queueLock.newCondition();

    private TaskSchedulerService() {}

    // FIX: proper double-checked locking with volatile
    public static TaskSchedulerService getInstance() {
        if (INSTANCE == null) {
            synchronized (TaskSchedulerService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TaskSchedulerService();
                }
            }
        }
        return INSTANCE;
    }

    public void initialize(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Worker count must be greater than 0");
        }
        this.taskQueue = new PriorityQueue<>();
        this.taskMap = new ConcurrentHashMap<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.isRunning = true;
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

    public String scheduleTask(Task task, SchedulingStrategy strategy) throws InterruptedException {
        // FIX: assign sequence number for FIFO tiebreaking
        ScheduledTask scheduledTask = new ScheduledTask(task, strategy, sequenceCounter.getAndIncrement());
        taskMap.put(scheduledTask.getId(), scheduledTask);
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
            // FIX: signalAll instead of signal — a newly added task may be due sooner
            // than what sleeping workers are waiting on, so wake everyone to re-evaluate
            notEmpty.signalAll();
        } finally {
            queueLock.unlock();
        }
    }

    // FIX: implemented cancel — marks task cancelled, removes from queue
    public boolean cancelTask(String taskId) {
        ScheduledTask task = taskMap.remove(taskId);
        if (task == null || task.getStatus() == TaskStatus.CANCELLED) {
            return false;
        }
        task.setStatus(TaskStatus.CANCELLED);
        queueLock.lock();
        try {
            taskQueue.remove(task);
            notFull.signal();
        } finally {
            queueLock.unlock();
        }
        return true;
    }

    // FIX: implemented reschedule — removes from queue, applies new strategy, re-enqueues
    public boolean rescheduleTask(String taskId, SchedulingStrategy newStrategy) throws InterruptedException {
        ScheduledTask task = taskMap.get(taskId);
        if (task == null || task.getStatus() == TaskStatus.CANCELLED) {
            return false;
        }
        queueLock.lock();
        try {
            taskQueue.remove(task);
        } finally {
            queueLock.unlock();
        }
        task.reschedule(newStrategy);
        pushTaskToQueue(task);
        return true;
    }

    // FIX: complete rewrite of runWorker — old version had 3 critical bugs:
    // 1. Accessed taskQueue without holding queueLock → race conditions
    // 2. Called await()/signal() without lock → IllegalMonitorStateException
    // 3. Used Thread.sleep() after removing task → blocked worker from other tasks
    //
    // New approach: Timed Wait Pattern
    // - Workers peek (don't remove) the next task while holding the lock
    // - If not yet due, await(delay) releases the lock and sleeps efficiently
    // - On wake (timeout or signal from new task), re-evaluate from the top
    // - Only poll() the task when it's actually due for execution
    private void runWorker() {
        while (isRunning) {
            ScheduledTask task = null;
            try {
                queueLock.lock();
                try {
                    while (isRunning) {
                        if (taskQueue.isEmpty()) {
                            notEmpty.await();
                            continue;
                        }
                        ScheduledTask next = taskQueue.peek();
                        // Skip cancelled tasks still in the queue
                        if (next.getStatus() == TaskStatus.CANCELLED) {
                            taskQueue.poll();
                            continue;
                        }
                        long waitMs = Duration.between(LocalDateTime.now(), next.getNextExecutionTime()).toMillis();
                        if (waitMs <= 0) {
                            task = taskQueue.poll();
                            notFull.signal();
                            break; // exit lock to execute
                        }
                        // Timed wait — releases lock, wakes on timeout or signalAll
                        notEmpty.await(waitMs, TimeUnit.MILLISECONDS);
                    }
                } finally {
                    queueLock.unlock();
                }
                if (task != null) {
                    executeTask(task);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.printf("%s stopped.%n", Thread.currentThread().getName());
    }

    private void executeTask(ScheduledTask task) throws InterruptedException {
        if (task.getStatus() == TaskStatus.CANCELLED) return;
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
            } else {
                taskMap.remove(task.getId());
            }
        }
    }

    public void addObserver(Observer observer) { observers.add(observer); }

    // FIX: shutdown now wakes all workers via signalAll so they can exit the await loop
    public void shutdown() {
        isRunning = false;
        queueLock.lock();
        try {
            notEmpty.signalAll();
        } finally {
            queueLock.unlock();
        }
        for (Thread worker : workers) {
            worker.interrupt();
        }
        System.out.println("Task Scheduler Service has been shut down.");
    }
}
