package taskscheduler.entities.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.taskscheduler.models.enums.TaskStatus;
import com.taskscheduler.models.interfaces.SchedulingStrategy;
import com.taskscheduler.models.interfaces.Task;

public class ScheduledTask implements Comparable<ScheduledTask> {
    private final String id;
    private final Task task;
    private SchedulingStrategy strategy;
    // FIX: volatile ensures status changes by worker threads are visible to other threads
    private volatile TaskStatus status;
    private LocalDateTime lastExecutionTime;
    private LocalDateTime nextExecutionTime;
    // FIX: FIFO tiebreaker — tasks with same execution time are ordered by insertion order
    private final int sequenceNumber;

    public ScheduledTask(Task task, SchedulingStrategy strategy, int sequenceNumber) {
        this.id = UUID.randomUUID().toString();
        this.task = task;
        this.strategy = strategy;
        this.status = TaskStatus.SCHEDULED;
        this.lastExecutionTime = null;
        this.nextExecutionTime = strategy.getNextExecutionTime(null).orElse(null);
        this.sequenceNumber = sequenceNumber;
    }

    // Used by rescheduleTask — resets timing state with a new strategy
    public void reschedule(SchedulingStrategy newStrategy) {
        this.strategy = newStrategy;
        this.lastExecutionTime = null;
        this.nextExecutionTime = newStrategy.getNextExecutionTime(null).orElse(null);
    }

    public void updateLastExecutionTime() { this.lastExecutionTime = this.nextExecutionTime; }

    public void updateNextExecutionTime() {
        this.nextExecutionTime = strategy.getNextExecutionTime(this.lastExecutionTime).orElse(null);
    }

    public String getId() { return id; }
    public Task getTask() { return task; }
    public LocalDateTime getNextExecutionTime() { return nextExecutionTime; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public boolean hasMoreExecutions() { return nextExecutionTime != null; }

    // FIX: tiebreak on sequenceNumber when execution times are equal
    @Override
    public int compareTo(ScheduledTask o) {
        int timeCompare = this.nextExecutionTime.compareTo(o.nextExecutionTime);
        return timeCompare != 0 ? timeCompare : Integer.compare(this.sequenceNumber, o.sequenceNumber);
    }
}
