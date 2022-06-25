import io.netty.util.concurrent.DefaultEventExecutor;

import java.util.concurrent.TimeUnit;

public class TestScheduleJDK {
    public static void main(String[] args) {
        DefaultEventExecutor defaultEventExecutor = new DefaultEventExecutor();
        defaultEventExecutor.schedule(()->{
            System.out.println(10);
        }, 10000, TimeUnit.MILLISECONDS);

        defaultEventExecutor.schedule(()->{
            System.out.println(5);
        }, 5000, TimeUnit.MILLISECONDS);

        defaultEventExecutor.schedule(()->{
            System.out.println(0.1);
        }, 100, TimeUnit.MILLISECONDS);

        defaultEventExecutor.schedule(()->{
            System.out.println(0.2);
        }, 200, TimeUnit.MILLISECONDS);
    }
}
