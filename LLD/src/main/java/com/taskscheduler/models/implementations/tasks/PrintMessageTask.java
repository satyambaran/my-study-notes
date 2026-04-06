package com.taskscheduler.models.implementations.tasks;

import java.time.LocalDateTime;

import com.taskscheduler.models.interfaces.Task;

public class PrintMessageTask implements Task {
    String message;

    public PrintMessageTask(String message) { this.message = message; }

    @Override
    public void execute() { System.out.printf("[%s] Executing PrintMessageTask:%s\n", LocalDateTime.now(), message); }
}
