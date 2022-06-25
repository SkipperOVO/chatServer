package personal.fields.util.concurrent;

import java.util.concurrent.TimeUnit;

public class DelayedTask implements Runnable {

    public long deadlineNano = 0;

    public long delay = 0;

    Runnable task;

    public DelayedTask(Runnable task, long delay) {
        this.delay = TimeUnit.MILLISECONDS.toNanos(delay);
        this.task = task;
        long now = System.nanoTime();
        this.deadlineNano = now + this.delay;
    }


    public long getDelay() {
        return Math.max(0L, deadlineNano - System.nanoTime());
//            return fakeDelay;
    }

    private long getDelay0() {
        return deadlineNano - System.nanoTime();
    }

    public void run() {

        this.task.run();
//        System.out.println("thread id: " + Thread.currentThread().getId());
//        System.out.println("task delay: [" + TimeUnit.NANOSECONDS.toSeconds(delay) + " finished ] 超时：" + TimeUnit.NANOSECONDS.toMillis(this.getDelay0())/1000.0);
//        task.run();
    }

}
