package com.taskscheduler.models.implementations.tasks;

import com.taskscheduler.models.interfaces.Task;

public class DataBackupTask implements Task {
    String source;
    String destination;

    public DataBackupTask(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void execute() {
        System.out.printf("[%s] Backing up data from " + source + " to " + destination,
                Thread.currentThread().getName());
        System.out.printf("[%s] Data backup completed successfully.", Thread.currentThread().getName());
    }
}
