import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Victoria on 05.04.2017.
 */
public class Thread1 extends Thread {
    private final CyclicBarrier barrier;
    private final CountDownLatch latch;
    public Thread1(CyclicBarrier barrier, CountDownLatch latch){
        this.barrier = barrier;
        this.latch = latch;
    }
    @Override
    public void run(){
        System.out.println("Thread1 begins now");
        try {
            Thread.sleep(300);
            latch.countDown();
            System.out.println("Thread 1 has finished");
            barrier.await();
        } catch (InterruptedException ex) {
            return;
        } catch (BrokenBarrierException ex) {
            return;
        }
    }
}

