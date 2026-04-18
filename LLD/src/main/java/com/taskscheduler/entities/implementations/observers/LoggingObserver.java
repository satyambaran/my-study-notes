package taskscheduler.entities.implementations.observers;

import java.time.LocalTime;

import com.taskscheduler.models.entities.ScheduledTask;
import com.taskscheduler.models.interfaces.Observer;

public class LoggingObserver implements Observer {
    @Override
    public void onStart(ScheduledTask task) {
        System.out.printf("[LOG - %s] [%s] Task %s started.\n", LocalTime.now(), Thread.currentThread().getName(),
                task.getId());
    }

    @Override
    public void onComplete(ScheduledTask task) {
        System.out.printf("[LOG - %s] [%s] Task %s completed successfully.\n", LocalTime.now(),
                Thread.currentThread().getName(), task.getId());
    }

    @Override
    public void onFailure(ScheduledTask task, Exception e) {
        System.err.printf("[LOG - %s] [%s] Task %s failed: %s\n", LocalTime.now(), Thread.currentThread().getName(),
                task.getId(), e.getMessage());
    }
}
