package com.taskscheduler;

import java.time.*;
import com.taskscheduler.models.implementations.observers.*;
import com.taskscheduler.models.implementations.schedulingstrategies.*;
import com.taskscheduler.models.implementations.tasks.*;
import com.taskscheduler.models.interfaces.*;

public class TaskSchedulerDemo {
    public static void main(String[] args) throws InterruptedException {
        // 1. Setup the facade and observers
        TaskSchedulerService scheduler = TaskSchedulerService.getInstance();
        scheduler.addObserver(new LoggingObserver());

        // 2. Initialize the scheduler with 3 worker threads
        scheduler.initialize(3);

        // 3. Schedule a one-time task (runs in 1 second)
        Task oneTimeTask = new PrintMessageTask("This is a one-time task.");
        SchedulingStrategy oneTimeStrategy = new OneTimeSchedulingStrategy(LocalDateTime.now().plusSeconds(1));
        String oneTimeId = scheduler.scheduleTask(oneTimeTask, oneTimeStrategy);

        // 4. Schedule a recurring task (every 2 seconds)
        Task recurringTask = new PrintMessageTask("This is a recurring task.");
        SchedulingStrategy recurringStrategy = new RecurringSchedulingStrategy(Duration.ofSeconds(2));
        String recurringId = scheduler.scheduleTask(recurringTask, recurringStrategy);

        // 5. Schedule a backup task (runs in 3 seconds)
        Task backupTask = new DataBackupTask("/data/source", "/data/backup");
        SchedulingStrategy backupStrategy = new OneTimeSchedulingStrategy(LocalDateTime.now().plusSeconds(3));
        scheduler.scheduleTask(backupTask, backupStrategy);

        System.out.println("Scheduler is running. Waiting for tasks to execute...");
        Thread.sleep(5000);

        // 6. Demonstrate cancel — stop the recurring task
        System.out.println("\n--- Cancelling recurring task ---");
        scheduler.cancelTask(recurringId);

        // 7. Demonstrate reschedule — reschedule the one-time task with a new strategy
        System.out.println("--- Rescheduling one-time task as recurring (every 2s) ---");
        scheduler.rescheduleTask(oneTimeId, new RecurringSchedulingStrategy(Duration.ofSeconds(2)));

        Thread.sleep(5000);

        // 8. Shutdown
        scheduler.shutdown();
    }
}
