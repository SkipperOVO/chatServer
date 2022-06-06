package ForTest;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestSchedule {

    public static void main(String[] args) {

        ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1);

        threadPool.scheduleAtFixedRate(()->{
            System.out.println("1");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
