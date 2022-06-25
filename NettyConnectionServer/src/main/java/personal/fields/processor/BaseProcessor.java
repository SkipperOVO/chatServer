package personal.fields.processor;

import personal.fields.util.concurrent.DelayExecutorGroup;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseProcessor implements Processor {

    private DelayExecutorGroup executor;

    protected ProcessorContainer container;

    public BaseProcessor() {

    }

    public BaseProcessor(DelayExecutorGroup executor, ProcessorContainer container) {
        this.executor = executor;
        this.container = container;
    }

    public void setThreadPool(DelayExecutorGroup executor) {
        this.executor = executor;
    }

    public void execute(Runnable task) {
        this.executor.execute(task);
    }

//    public void schedule(Runnable task, int delay, TimeUnit timeUnit) {
//        this.threadPool.schedule(task, delay, timeUnit);
//    }

//    public void scheduleWithFixedDelay(Runnable task, int initDelay, int delay, TimeUnit timeUnit) {
//        this.threadPool.scheduleWithFixedDelay(task, initDelay, delay, timeUnit);
//    }
}
