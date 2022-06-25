import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/*
    1. 为什么不用 jdk 的 scheduledExecutorService ？
        因为 scheduledExecutorService 为了实现延迟任务的低延迟，采用的 executor 策略是最大线程池数量为int最大值，即非常多的线程来实现的。
        我需要的是一个单线程的调度器。
    2.为什么不直接用 Netty 的 SingleThreadEventExecutor ？
        虽然该类实现了单线程下的延迟任务调度，但是和Netty的框架紧耦合，需要使用Netty的类层次结构，
        不容易剥离出来。
    3. JDK 或者 Netty 的任务队列都是默认为int最大值的，使用一个 available 条件变量来控制阻塞队列，任务大量堆积会导致oom。我实现的版本实现了定长的阻塞队列，使用三个条件变量
       对访问队列的线程进行控制。当队列满时put操作会阻塞，降低系统可用性从而限流。


 */
public class DelayExecutor {

    static double totalTimeout = 0;

    public static void main(String[] args) {
        DelayExecutor d = new DelayExecutor();
        DelayedExecutor executor = d.new DelayedExecutor();

        // 第一组测试，延迟降序排序
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 12000);
//        }).start();
//
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 10000);
//        }).start();
//
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 5200);
//            executor.scheduleWithFixedDelay(null, 1200);
//            executor.scheduleWithFixedDelay(null, 1800);
//            executor.scheduleWithFixedDelay(null, 10000);
//        }).start();
//
//
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 1000);
//        }).start();
//
//
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 10);
//            executor.scheduleWithFixedDelay(null, 10);
//            executor.scheduleWithFixedDelay(null, 10);
//        }).start();

        // 第二组，延迟升序排序

//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 10);
//            executor.scheduleWithFixedDelay(null, 10);
//            executor.scheduleWithFixedDelay(null, 10);
//        }).start();
//
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 1000);
//        }).start();
//
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 5200);
//            executor.scheduleWithFixedDelay(null, 1200);
//            executor.scheduleWithFixedDelay(null, 1800);
//            executor.scheduleWithFixedDelay(null, 10000);
//        }).start();
//
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 10000);
//        }).start();
//
//        new Thread(()->{
//            executor.scheduleWithFixedDelay(null, 12000);
//            executor.scheduleWithFixedDelay(null, 0);
//            executor.scheduleWithFixedDelay(null, 0);
//        }).start();

        // 1000 个延时任务平均超时：0.00978s,---> 9.78ms
        Random random = new Random();
        for (int i = 0; i < 1000; ++i) {
            executor.scheduleWithFixedDelay(null, random.nextInt(1000));
        }
        System.out.println(totalTimeout / 1000);
    }


    class DelayedExecutor {

//        // 对应 jdk 线程池中 workers
//        Thread[] workers;

        Thread thread;

        boolean isStarted = false;

        int capacity = 8;

        boolean isTimewaiting = false;

        PriorityQueue<DelayedTask> taskQueue = new PriorityQueue<>(new Comparator<DelayedTask>() {
            @Override
            public int compare(DelayedTask o1, DelayedTask o2) {
                long diff = o1.deadlineNano - o2.deadlineNano;
                if (diff < 0L) return -1;
                else  return 1;
            }
        });

        AtomicInteger queueSize = new AtomicInteger(0);

        ReentrantLock lock = new ReentrantLock();
        Condition notEmpty = lock.newCondition();
        Condition notFull = lock.newCondition();
        Condition avaliable = lock.newCondition();

        public void scheduleWithFixedDelay(Runnable task, long delay) {
            if (!isStarted) {
                this.start();
                isStarted = true;
            }
            putTask(new DelayedTask(task, delay));
        }


        public DelayedTask takeTask() {
            DelayedTask task = null;
            int c = -1;
            try {
                lock.lockInterruptibly();
                while (true) {
                    task = taskQueue.poll();
//                    if (task != null)
//                        System.out.println("从队列中取出任务：" + TimeUnit.NANOSECONDS.toSeconds(task.delay));
//                    else System.out.println("从队列中取出 null 任务");
                    // 当前任务队列为空，阻塞当前线程等待队列中有任务被添加进来
                    if (task == null) {
                        notEmpty.await();
                    } else {
                        long delay = task.getDelay();
                        // 如果队列头中延时任务已经超时，那么退出循环返回这个任务
                        if (delay <= 0L) {
//                            System.out.println("取出一个超时任务" + TimeUnit.NANOSECONDS.toSeconds(task.delay));
                            break;
                        // 否则等待这个任务 delay 结束
                        } else {
//                            System.out.println("等待任务：" + TimeUnit.NANOSECONDS.toSeconds(task.delay) + "超时, 放回队列");
                            isTimewaiting = true;
                            avaliable.awaitNanos(delay);
                            isTimewaiting = false;
                            taskQueue.offer(task);
//                            System.out.println("avaliable await waked");
                        }
                    }
                }
                c = queueSize.getAndDecrement();
            } catch (InterruptedException ine) {
                ine.printStackTrace();
            }
            finally {
                lock.unlock();
            }
            if (c == capacity) {
                signalNotFull();
            }
            return task;
        }


        public void putTask(DelayedTask task) {
            int c = -1;
            try {
                lock.lockInterruptibly();
                while (taskQueue.size() == capacity) {
                    notFull.await();
                }
                taskQueue.offer(task);
                c = queueSize.getAndIncrement();
                if (isTimewaiting) {
                    avaliable.signal();
//                    System.out.println("avaliable signaled");
                }
            } catch (InterruptedException ine) {
                ine.printStackTrace();
            } finally {
                lock.unlock();
            }
            if (c == 0) {
                signalNotEmpty();
            }
        }

        public void signalNotEmpty() {
            try {
                lock.lockInterruptibly();
                notEmpty.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public void signalNotFull() {
            try {
                lock.lockInterruptibly();
                notFull.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }



        public void start() {
                try {
                    thread = new Thread(()->{
                        while (true) {
                            try {
                                DelayedTask task = takeTask();
//                                while (task.getDelay() > 0L) {
//                                    Thread.sleep(TimeUnit.NANOSECONDS.toMillis(task.getDelay()));
//                                }
                                task.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }


    class DelayedTask {


        long deadlineNano = 0;

        long delay = 0;

        Runnable task;

        public DelayedTask(Runnable task, long delay) {
            this.delay = TimeUnit.MILLISECONDS.toNanos(delay);
            this.task = task;
            long now = System.nanoTime();
            this.deadlineNano = now + this.delay;
        }


        // 在队列中等的越久的任务会越先执行
        public long getDelay() {
            return Math.max(0L, deadlineNano - System.nanoTime());
//            return fakeDelay;
        }

        private long getDelay0() {
            return deadlineNano - System.nanoTime();
        }

        public void run() {
            // Todo
//            task.run();
            double timeout = TimeUnit.NANOSECONDS.toMillis(this.getDelay0())/1000.0;
            System.out.println("task delay: [" + TimeUnit.NANOSECONDS.toSeconds(delay) + " finished ] 超时：" + timeout);
            totalTimeout += timeout;

        }

    }
}
