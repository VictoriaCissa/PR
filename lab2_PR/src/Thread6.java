import java.util.concurrent.CountDownLatch;


/**
 * Created by Victoria on 05.04.2017.
 */
public class Thread6 extends Thread {

    private final CountDownLatch latch;
    public Thread6(CountDownLatch latch){
        this.latch = latch;
    }
    @Override
    public void run() {
        System.out.println("Thread6 begins now");
        try {
            System.out.println("Thread 6 awaits!");
            latch.await();
            Thread.sleep(400);
            System.out.println("Thread 6 has finished!");

        } catch (InterruptedException ex) {
            return;
        }
    }
}