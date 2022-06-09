package ForTest;

import lombok.Synchronized;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Subtask {

    private volatile  int totalSum = 0;

    static CountDownLatch cd = new CountDownLatch(10);

    private static final sun.misc.Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            // 无视权限
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
           throw new Error(e);
        }

    }

    public static void main(String[] args) {

        new Subtask().process();

    }

    public void process() {

        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 10, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
            executor.prestartAllCoreThreads();
            int n = 10000;
            int[] a = new int[n];
            Random random = new Random();
            int gt = 0;
            Long start = System.currentTimeMillis();
            for (int i = 0; i < n; ++i) {
                a[i] = random.nextInt(100);
                gt += a[i];
                int k = 10;
                while (k-- > 0) {
                    File file = new File("./Subtask.java");
                    file.exists();
                    file.getAbsoluteFile();
                }
            }
            System.out.println(gt);
            System.out.println(System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            int step = n / 10;
            for (int i = 0; i < 10; ++i) {
                int fi = i*step;
                executor.execute(() -> {
                    try {
                        int subSum = 0;
                        int upper = (fi+step > n) ? Math.max(fi+step, n) : fi+step;
//                        System.out.println("(" + fi + ", " + upper + ")");
                        for (int j = fi; j < upper; ++j) {
                            subSum += a[j];
                            int k = 10;
                            while (k-- > 0) {
                                File file = new File("./Subtask.java");
                                file.exists();
                                file.getAbsoluteFile();
                            }
                        }
                        for (; ; ) {
                            if (unsafe.compareAndSwapInt(
                                    this,
                                    unsafe.objectFieldOffset(Subtask.class.getDeclaredField("totalSum")),
                                    totalSum, totalSum + subSum)) {
                                break;
                            }
                        }
//                        synchronized(this) {
//                            totalSum += subSum;
//                        }
                        cd.countDown();
//                        System.out.println("cd: " + cd);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Error(e);
                    }

                });
            }

            cd.await();
            System.out.println(totalSum);
            System.out.println(System.currentTimeMillis() - start);
            executor.shutdown();
        } catch (InterruptedException ine) {
            ine.printStackTrace();
        }

    }
}
