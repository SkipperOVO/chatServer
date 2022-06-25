
package personal.fields.util.concurrent;

import personal.fields.util.concurrent.DelayedTask;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DelayExecutorGroup {

    public static void main(String[] args) {
        DelayExecutorGroup executorGroup = new DelayExecutorGroup(4, 8);
        new Thread(()->{
            executorGroup.scheduleWithFixedDelay(()->{ System.out.println("delay: 12000");}, 12000, TimeUnit.MILLISECONDS);
        }).start();
        new Thread(()->{
            executorGroup.scheduleWithFixedDelay(()->{ System.out.println("delay: 1200");}, 1200, TimeUnit.MILLISECONDS);
        }).start();
        new Thread(()->{
            executorGroup.scheduleWithFixedDelay(()->{ System.out.println("delay: 5000");}, 5000, TimeUnit.MILLISECONDS);
        }).start();
        new Thread(()->{
            executorGroup.scheduleWithFixedDelay(()->{ System.out.println("delay: 100");}, 100, TimeUnit.MILLISECONDS);
        }).start();

    }

    private DelayedExecutor[] workers;

    private int nthread = 0;

    public DelayExecutorGroup(int nthread, int queueSize)  {
        this.nthread = nthread;
        workers = new DelayedExecutor[nthread];
        for (int i = 0; i < nthread; ++i) {
            workers[i] = new DelayedExecutor(queueSize);
        }
    }

    public void execute(Runnable task, Integer bindId) {
        boolean hasBind = false;
        for (int i = 0; i < nthread; ++i) {
            if (workers[i].isBind(bindId)) {
                workers[i].scheduleWithFixedDelay(new DelayedTask(task, 0), bindId);
                hasBind = true;
            }
        }
        if (hasBind == false) {
            int ri = (int) Math.random() * (nthread);
            workers[ri].bindUser(bindId);
            workers[ri].scheduleWithFixedDelay(task, 0);
        }
    }

    public void execute(Runnable task) {
        int ri = (int) Math.random() * (nthread);
        workers[ri].scheduleWithFixedDelay(new DelayedTask(task, 0));
    }


    public void scheduleWithFixedDelay(Runnable task, long delay, TimeUnit unit) {
        DelayedTask delayedTask = new DelayedTask(task, delay);
        this.execute(delayedTask);
    }

    public void scheduleWithFixedDelay(Runnable task, long delay, TimeUnit unit, Integer bindId) {
        DelayedTask delayedTask = new DelayedTask(task, delay);
        this.execute(delayedTask, bindId);
    }


    class DelayedExecutor {

        Thread thread;

        boolean isStarted = false;

        int capacity = 32;

        boolean isTimewaiting = false;

        // 这里的数据结构应当使用缓存机制，不能一直保存所有用户，应该长期不在线的用户就删除掉，节省时空开销
        // 更好的实现应该是在线程池外部业务逻辑中保存一个 map，这样可以避免因为缓存失效导致多个线程出现都包含同一个用户的情况
        private HashSet<Integer> bindUserIds = new HashSet<>();


        private PriorityQueue<DelayedTask> taskQueue = new PriorityQueue<DelayedTask>(new Comparator<DelayedTask>() {
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


        public DelayedExecutor() {

        }

        public DelayedExecutor(int capacity) {
            this.capacity = capacity;
        }

        public DelayedExecutor(int capacity, PriorityQueue<DelayedTask> queue) {
            this.capacity = capacity;
            this.taskQueue = queue;
        }


        public void scheduleWithFixedDelay(Runnable task, long delay) {

            scheduleWithFixedDelay(new DelayedTask(task, delay));
        }

        public void scheduleWithFixedDelay(Runnable task) {
            if (!isStarted) {
                this.start();
                isStarted = true;
            }
            putTask((DelayedTask) task);
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

        public boolean isBind(Integer userId) {
            return this.bindUserIds.contains(userId);
        }

        public void bindUser(Integer userId) {
            this.bindUserIds.add(userId);
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
}