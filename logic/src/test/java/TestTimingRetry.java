import io.netty.util.concurrent.DefaultEventExecutor;

import java.util.concurrent.*;

public class TestTimingRetry {


    public static void main(String[] args) {

        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);

        DefaultEventExecutor executor = new DefaultEventExecutor();
//
//        ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(4, 4, 60,
//                TimeUnit.SECONDS, new LinkedBlockingDeque<>(16), new ThreadPoolExecutor.DiscardOldestPolicy());

//        LinkedBlockingDeque<NotifyTask>

    }


    public static class NotifyTast implements Runnable {

        int retryTimes = 0;

        long sendTime = 0;

        static int idx = 0;

        int id = idx++;

        ThreadPoolExecutor threadPool;

        public NotifyTast(int retryTimes, ThreadPoolExecutor threadPool) {
            this.sendTime = System.currentTimeMillis();
            this.threadPool = threadPool;
        }


        @Override
        public void run() {
            if (retryTimes < 3) {
                System.out.println("notify id: " + id + " 超时重发");
//                threadPool.shc
            }
        }
    }



}
