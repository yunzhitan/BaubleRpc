package com.yunzhitan.monitor;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class ThreadPoolStatus {
    private int currentSize; //当前线程池中线程数
    private int activeCount;  //执行任务的线程数
    private int corePoolSize; //线程池中的核心线程数
    private int maximumPoolSize; //线程池中允许的最大线程
    private long taskCount;      //计划执行的任务数
    private long completedTaskCount;  //已完成的任务数

    @ManagedOperation
    public int getCurrentSize() {
        return currentSize;
    }

    @ManagedOperation
    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    @ManagedOperation
    public int getActiveCount() {
        return activeCount;
    }

    @ManagedOperation
    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    @ManagedOperation
    public int getCorePoolSize() {
        return corePoolSize;
    }

    @ManagedOperation
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    @ManagedOperation
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    @ManagedOperation
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    @ManagedOperation
    public long getTaskCount() {
        return taskCount;
    }

    @ManagedOperation
    public void setTaskCount(long taskCount) {
        this.taskCount = taskCount;
    }

    @ManagedOperation
    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    @ManagedOperation
    public void setCompletedTaskCount(long completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

}
