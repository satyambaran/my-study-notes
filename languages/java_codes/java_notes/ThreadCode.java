import java.sql.Time;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class MyThread extends Thread {
    @Override
    public void run() {
        System.out.printf("%d", 1);
    }
}

class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.printf("%d", 2);
    }
}

class ThreadPoolExecutorImpl {
    public static void main(String[] args) {

        Random rand = new Random();
        System.out.printf("%d", rand.nextInt(5));

        Object obj = new Object();
        ThreadPoolExecutor ex = new ThreadPoolExecutor(2, 4, 50, TimeUnit.SECONDS, new LinkedBlockingDeque<>(10));
        for (int i = 1; i < 100; i++) {
            int task = i;
            ex.submit(() -> {
                System.out.printf("%d\n", task);
            });
        }
    }
}

class OddEven {
    private final Object obj = new Object();
    private boolean evenTurn;

    OddEven() {
        evenTurn = true;
    }

    void printEven() {
        synchronized (obj) {
            for (int i = 0; i < 20; i += 2) {
                while (!evenTurn) {
                    try {
                        obj.wait();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                System.out.printf("%d ", i);
                evenTurn = false;
                obj.notify();
            }
        }
    }

    void printOdd() {
        synchronized (obj) {
            for (int i = 1; i < 20; i += 2) {
                while (evenTurn) {
                    try {
                        obj.wait();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                System.out.printf("%d ", i);
                evenTurn = true;
                obj.notify();
            }
        }
    }
}

public class ThreadCode {
    public static void main(String[] args) {
        OddEven oddEven = new OddEven();
        Thread evenThread = new Thread(oddEven::printEven);
        Thread oddThread = new Thread(oddEven::printOdd);
        evenThread.start();
        oddThread.start();

        try {
            // Wait for both threads to finish
            evenThread.join();
            oddThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Object obj = new Object();
        // ThreadPoolExecutor ex = new ThreadPoolExecutor(2, 4, 50, TimeUnit.SECONDS,
        // new LinkedBlockingDeque<>(10));
        // try {
        // for (int i = 1; i < 100; i++) {
        // int task = i;
        // ex.submit(() -> {
        // System.out.printf("%d\n", task);
        // });
        // }
        // } catch (Exception e) {
        // // TODO: handle exception
        // } finally {

        // ex.shutdown();
        // }

        // Thread t1 = new MyThread();
        // Thread t2 = new MyThread();
        // Thread t3 = new Thread(new MyRunnable());
        // Thread t4 = new Thread(() -> {
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // System.out.printf("%d", 3);
        // });
        // Thread t5 = new Thread(() -> {
        // try {
        // Thread.sleep(4000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // System.out.printf("%d", 4);
        // });
        // Thread t6 = new Thread(() -> {
        // try {
        // Thread.sleep(6000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // System.out.printf("%d", 5);
        // });
        // t5.setDaemon(true);
        // t6.setDaemon(true);
        // t1.start();
        // t2.start();
        // t3.start();
        // t4.start();
        // t5.start();
        // t6.start();
        // try {
        // t1.join();
        // t2.join();
        // t3.join();
        // // t4.join();
        // t5.join();
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }
}