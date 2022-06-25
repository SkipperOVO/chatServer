import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestACKSingleQueue {

    public static void main(String[] args) {

        ThreadPoolExecutor threadPool =
                new ThreadPoolExecutor(4, 4, 60,
                        TimeUnit.SECONDS, new LinkedBlockingDeque<>(100000),  new ThreadPoolExecutor.DiscardOldestPolicy());;

        LinkedBlockingQueue<ACKTask> ackQueue = new LinkedBlockingQueue<>(16);
        ACKTask[] ackTasks = new ACKTask[]{
                new ACKTask(1, 1, threadPool, ackQueue),
                new ACKTask(1, 1, threadPool, ackQueue),
                new ACKTask(1, 1, threadPool, ackQueue)};
        new Thread(()->{
            for (int i = 0; i < 50000; ++i) {
                try {
                    ackQueue.put(ackTasks[i%3]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


        // 时间空间 trade off

        // 单线程reactor模式，单线程进行分发(每秒几十万ops），不是当前线程绑定任务的话，就将该任务放到线程池的执行队列，所以线程池的执行队列有可能很大，还需要设置
        // 拒绝策略。这种方式的好处是对于锁的竞争比较小，甚至可以尝试用自旋锁替代重量级锁。缺点是内存消耗会大一些，因为有较大的任务队列。
        // 另外一点好处是这种方式使用了线程池作为调度器，可以用来完成调度任务。
//        new Thread(()->{
//            while (true) {
//                try {
//                    ACKTask ackTask = ackQueue.take();
//                    threadPool.execute(ackTask);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        // 多个线程 竞争同一个锁比较频繁，这是缺点。优点是几乎不需要线程池的执行任务队列，直接将任务队列数设置为线程数即可，因为每个任务执行时候都被阻塞了。
        // 每个线程都不会再返回任务队列。很节省空间。模拟的java线程池中线程任务的执行过程。
//        for (int i = 0; i < 4; ++i) {
//            threadPool.execute(()->{
//                while (true) {
//                    try {
//                        ACKTask ackTask = ackQueue.take();
//                        ackTask.run();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//        EventLoopGroup eventGroup = new NioEventLoopGroup(4);
    }


    public static class ACKTask  implements Runnable {

        ACKTask outerTask;

        static volatile int count = 0;

        int ackId = 0;

        volatile int  userId;

        volatile long tid = -1;

        final ThreadPoolExecutor threadPool;

        final LinkedBlockingQueue<ACKTask> ackQueue;

        public ACKTask(int ackId, int userId, ThreadPoolExecutor threadPool, LinkedBlockingQueue<ACKTask> ackQueue) {
            this.ackId = ackId;
            this.userId = userId;
            this.outerTask = this;
            this.threadPool = threadPool;
            this.ackQueue = ackQueue;
        }


        public void setTid(long tid) {
            this.tid = tid;
        }

        public long getTid( ){
            return this.tid;
        }

        @Override
        public void run() {
            long ctid = Thread.currentThread().getId();
            if (this.getTid() == -1) {
                this.setTid(ctid);
            }
            if (ctid != this.getTid()) {
                this.threadPool.submit(this.outerTask);
//                this.ackQueue.offer(this);
                System.out.println("opps! 这不是我的线程，我要被放回！");
                return ;
            } else {
                System.out.println("线程: " + ctid + " 处理了ACK任务：" + this.toString());
                System.out.println(++count);
            }
        }


        @Override
        public String toString() {
            return "{ userId: " + this.userId + ", tid: " + this.tid + "}";
        }

    }
}
