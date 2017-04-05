import java.util.concurrent.CountDownLatch;

/**
 * Created by Victoria on 05.04.2017.
 */
public class Thread5 extends Thread {
    private final CountDownLatch latch;

    public Thread5(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        System.out.println("Thread5 begins now");
        try {
            System.out.println("Thread 5 awaits!");
            latch.await();
            Thread.sleep(400);
            System.out.println("Thread 5 has finished!");

        } catch (InterruptedException ex) {
            return;
        }
    }
}