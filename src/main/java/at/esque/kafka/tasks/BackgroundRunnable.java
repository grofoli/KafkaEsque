package at.esque.kafka.tasks;

import at.esque.kafka.BackGroundTaskHolder;

public abstract class BackgroundRunnable implements Runnable {

    protected BackGroundTaskHolder backGroundTaskHolder;

    public void setBackGroundTaskHolder(BackGroundTaskHolder backGroundTaskHolder) {
        this.backGroundTaskHolder = backGroundTaskHolder;
    }
}
