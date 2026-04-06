package com.taskscheduler.models.interfaces;

import com.taskscheduler.models.entities.ScheduledTask;

public interface Observer {
    public void onStart(ScheduledTask task);

    public void onComplete(ScheduledTask task);

    public void onFailure(ScheduledTask task, Exception e);
}
