package com.taskscheduler.models.interfaces;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SchedulingStrategy {
    // returns next execution time or null if no more executions are needed
    public Optional<LocalDateTime> getNextExecutionTime(LocalDateTime lastExecutionTime);
}
