package personal.fields.processor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseProcessor implements Processor {

    private ThreadPoolExecutor threadPool;

    protected ProcessorContainer container;

    public BaseProcessor() {

    }

    public BaseProcessor(ThreadPoolExecutor threadPool, ProcessorContainer container) {
        this.threadPool = threadPool;
        this.container = container;
    }

    public void setThreadPool(ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public void execute(Runnable task) {
        this.threadPool.execute(task);
    }

//    public void schedule(Runnable task, int delay, TimeUnit timeUnit) {
//        this.threadPool.schedule(task, delay, timeUnit);
//    }

//    public void scheduleWithFixedDelay(Runnable task, int initDelay, int delay, TimeUnit timeUnit) {
//        this.threadPool.scheduleWithFixedDelay(task, initDelay, delay, timeUnit);
//    }
}
