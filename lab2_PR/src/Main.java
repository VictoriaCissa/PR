import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Victoria on 05.04.2017.
 */
public class Main {
    private final Object thread2Waiter = new Object();

    private void run_thread() {
        final CyclicBarrier barrier = new CyclicBarrier(2, () -> {
            System.out.println("BARRIER!");
            synchronized (thread2Waiter) {
                thread2Waiter.notify();
            }
        }
        );
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch5_6 = new CountDownLatch(1);


        Thread1 thread1 = new Thread1(barrier, latch3);
        Thread2 thread2 = new Thread2(thread2Waiter, latch5_6);
        Thread3 thread3 = new Thread3(barrier, latch3);
        Thread5 thread5 = new Thread5(latch5_6);
        Thread6 thread6 = new Thread6(latch5_6);
        thread1.start();
        thread2.start();
        thread3.start();
        thread5.start();
        thread6.start();
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.run_thread();
    }
}

