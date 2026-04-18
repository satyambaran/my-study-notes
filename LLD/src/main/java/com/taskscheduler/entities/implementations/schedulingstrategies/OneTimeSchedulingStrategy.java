package taskscheduler.entities.implementations.schedulingstrategies;

import java.time.LocalDateTime;
import java.util.Optional;

import com.taskscheduler.models.interfaces.SchedulingStrategy;

public class OneTimeSchedulingStrategy implements SchedulingStrategy {
    private final LocalDateTime executionTime;

    public OneTimeSchedulingStrategy(LocalDateTime executionTime) { this.executionTime = executionTime; }

    @Override
    public Optional<LocalDateTime> getNextExecutionTime(LocalDateTime lastExecutionTime) {
        // For one-time tasks, we only execute once, so return null after the first
        // execution
        return lastExecutionTime == null ? Optional.of(executionTime) : Optional.empty();
    }
}
