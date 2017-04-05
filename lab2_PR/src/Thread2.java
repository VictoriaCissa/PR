import java.util.concurrent.CountDownLatch;

/**
 * Created by Victoria on 05.04.2017.
 */
public class Thread2 extends Thread {
    private final Object waiter;
    private final CountDownLatch latch;
    public Thread2(Object waiter, CountDownLatch latch){
        this.waiter = waiter;
        this.latch = latch;
    }

    @Override
    public void run(){
        System.out.println("Thread2 begins now");
        synchronized (waiter) {
            try {
                System.out.println("Thread 2 awaits");
                waiter.wait();
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Thread 2 has finished");
    }
}


