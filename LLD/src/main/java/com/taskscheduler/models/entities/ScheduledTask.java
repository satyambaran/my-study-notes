package com.taskscheduler.models.entities;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.taskscheduler.models.enums.TaskStatus;
import com.taskscheduler.models.interfaces.SchedulingStrategy;
import com.taskscheduler.models.interfaces.Task;

public class ScheduledTask implements Comparable<ScheduledTask> {
    private String id; // UUID for identification
    private Task task;
    private SchedulingStrategy strategy;
    private TaskStatus status;
    private LocalDateTime lastExecutionTime;
    private LocalDateTime nextExecutionTime;

    public ScheduledTask(Task task, SchedulingStrategy strategy) {
        this.id = UUID.randomUUID().toString();
        this.task = task;
        this.strategy = strategy;
        this.status = TaskStatus.SCHEDULED;
        this.lastExecutionTime = null;
        this.nextExecutionTime = strategy.getNextExecutionTime(null).orElse(null);
    }

    public void updateLastExecutionTime() { this.lastExecutionTime = this.nextExecutionTime; }

    public void updateNextExecutionTime() {
        LocalDateTime nextTime = strategy.getNextExecutionTime(this.lastExecutionTime).orElse(null);
        this.nextExecutionTime = nextTime;
    }

    public String getId() { return id; }

    public Task getTask() { return task; }

    public LocalDateTime getNextExecutionTime() { return nextExecutionTime; }

    public TaskStatus getStatus() { return status; }

    public void setStatus(TaskStatus status) { this.status = status; }

    public boolean hasMoreExecutions() { return nextExecutionTime != null; }

    @Override
    public int compareTo(ScheduledTask o) { return this.nextExecutionTime.compareTo(o.nextExecutionTime); }
}
