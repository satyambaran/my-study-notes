public class ScalableSequentialThreads {

    private static final Object lock = new Object();
    private static int count = 1;
    private static int n = 5;  // Number of threads (can be changed)

    public static void main(String[] args) {
        // Create and start n threads
        for (int i = 0; i < n; i++) {
            Thread t = new Thread(new Printer(i + 1, n));
            t.start();
        }
    }

    static class Printer implements Runnable {
        private int threadId;
        private int totalThreads;

        Printer(int threadId, int totalThreads) {
            this.threadId = threadId;
            this.totalThreads = totalThreads;
        }

        @Override
        public void run() {
            synchronized (lock) {
                while (count <= 10) {  // Change this to a dynamic value if needed
                    // Check if it's this thread's turn to print
                    if (count % totalThreads == threadId - 1) {
                        System.out.println("T" + threadId + ":" + count++);
                        lock.notify();  // Notify the next thread to run
                    } else {
                        try {
                            lock.wait();  // Wait until it's this thread's turn
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}