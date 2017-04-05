import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Victoria on 05.04.2017.
 */
public class Thread3 extends Thread {
    private final CyclicBarrier barrier;
    private final CountDownLatch latch;

    public Thread3(CyclicBarrier barrier, CountDownLatch latch) {
        this.barrier = barrier;
        this.latch = latch;
    }

    @Override
    public void run() {
        System.out.println("Thread3 begins now");
        try {
            System.out.println("Thread 3 awaits!");
            latch.await();
            Thread.sleep(500);
            System.out.println("Thread 3 has finished!");
            barrier.await();
        } catch (InterruptedException ex) {
            return;
        } catch (BrokenBarrierException ex) {
            return;
        }

    }
}