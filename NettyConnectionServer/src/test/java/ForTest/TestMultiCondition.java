package ForTest;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TestMultiCondition {

    static boolean state = false;
    public static void main(String[] args) {

        ReentrantLock lock = new ReentrantLock();
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();
        new Thread(()->{
            try {
                lock.lockInterruptibly();
                while (!state) {
                    condition1.await();
                }
                condition2.signal();
                System.out.println("notifyed");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }).start();


        new Thread(()->{
            state = true;
            lock.lock();
            try {
                condition1.signal();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }).start();

    }
}
