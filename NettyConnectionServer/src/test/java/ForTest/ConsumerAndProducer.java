package ForTest;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConsumerAndProducer {

    public static void main(String[] args) {

        ConsumerAndProducer cap = new ConsumerAndProducer();
        BlockingQueue<Integer> blockingQueue = cap.new BlockingQueue<>();
        new Thread(()->{
            System.out.println("consumer started");
            Long start = System.currentTimeMillis();
                for (int i = 0; i < 5000; ++i) {
                    System.out.println("consumer get : " + blockingQueue.get() + "; ");
                }
            System.out.println("time: " + (System.currentTimeMillis() - start));

        }).start();

            new Thread(()->{
            System.out.println("producer started");
                for (int i = 0; i < 5000; i ++) {
                    blockingQueue.put(i);
                    System.out.println("producer put : " + i + "; ");
                }
        }).start();
    }

    /*** version 1.0 ***/
    // 1w 用时 663
    // 5w 用时 2040（单生产者单消费者）

    // Java BlockingQueue 实现：
    // 5w 用时 1733 （单生产者单消费者）
    // 50w 多生产者多消费者(10) 17596

//    private class Consumer {
//
//        private final LinkedList<Integer> queue;
//
//        private ReentrantLock lock = new ReentrantLock();
//
//        private Condition notEmpty = lock.newCondition();
//
//        public Consumer(LinkedList<Integer> q) {
//            queue = q;
//        }
//
//        public Integer get() {
//            synchronized (queue) {
//                int t = queue.getLast();
//                queue.remove(queue.size()-1);
//                return t;
//            }
//        }
//    }
//
//
//    private class Producer<T> {
//
//        private final BlockingQueue<T> queue;
//
//
//        public Producer(BlockingQueue q) {
//            queue = q;
//        }
//
//        public void put
//
//    }

//    private class BlockingQueue<T> {
//
//        private final LinkedList<T> queue;
//
//        private volatile int size = 0;
//
//        private static final int LIMIT = 8;
//
//        public BlockingQueue() {
//            queue = new LinkedList<T>();
//        }
//
//        public void put(T t) {
//            synchronized (this) {
//                try {
//                    while (size >= LIMIT) {
//                        this.wait();
//                    }
//                    queue.addLast(t);
//                    size++;
//                    this.notifyAll();
//                } catch (InterruptedException ine) {
//                    ine.printStackTrace();
//                }
//            }
//        }
//
//        public T get() {
//            synchronized (this) {
//                try {
//                    while (size <= 0) {
//                        this.wait();
//                    }
//                    T t = queue.getFirst();
//                    size--;
//                    queue.remove(0);
//                    this.notifyAll();
//                    return  t;
//                } catch (InterruptedException ine) {
//                    ine.printStackTrace();
//                }
//            }
//            return null;
//        }
//    }

    /** version 2.1 **/
    // 不应该一次锁整个链表，put 和 take 二者不应当互斥
    class BlockingQueue<T> {

        private final ReentrantLock putLock = new ReentrantLock();

        private final ReentrantLock takeLock = new ReentrantLock();

        // put 操作将等待 不满 这个条件
        private final Condition notFull = putLock.newCondition();

        // take 操作将等待 不空 这个条件
        private final Condition notEmpty = takeLock.newCondition();

        private final LinkedList<T> queue;

        private static final int CAPACITY = 8;

        private final AtomicInteger count = new AtomicInteger(0);

        public BlockingQueue() {
            queue = new LinkedList<>();
        }

        private void signalNotEmpty() {
            try {
                takeLock.lock();
                notEmpty.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                takeLock.unlock();
            }
        }

        private void signalNotFull() {
            try {
                putLock.lock();
                notFull.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                putLock.unlock();
            }
        }

        public void put(T t) {
            int c = -1;
            try {

                putLock.lockInterruptibly();
                while (count.get() == CAPACITY) {
                    notFull.await();
                }
                queue.addLast(t);
                c = count.incrementAndGet();
//                notEmpty.signal();
//                if (c + 1 < CAPACITY)
//                    notFull.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                putLock.unlock();
            }
            // 如果之前一直是空的，那么现在 put 后不为空了，通知一下get线程
            if (c == 0) {
                signalNotEmpty();
            }
        }


        public T get() {
            T res = null;
            int c = -1;
            try {

                takeLock.lockInterruptibly();
                while (count.get() == 0) {
                    notEmpty.await();
                }
                res = queue.removeFirst();
                c = count.decrementAndGet();
//                if (c > 1) {
//                    notEmpty.signal();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                takeLock.unlock();
            }
            // 如果之前一直是满的，现在通知 put 不满了
            if (c == CAPACITY) {
                signalNotFull();
            }
            return res;
        }

    }

    /** version 2.0 **/
//    class BlockingQueue<T> {
//
//        class Node<T> {
//            volatile Node pre;
//            volatile Node
//        }
//        private final LinkedList<T> queue;
//
//        private static final int LIMIT = 8;
//
//        private volatile int size = 0;
//
//        public BlockingQueue() {
//            queue = new LinkedList<>();
//        }
//
//
//    }
}
