class OddEven {
    private final Object lock = new Object();
    private boolean evenTurn = true; // Flag to track whose turn it is (even or odd)

    void printEven() {
        synchronized (lock) {
            for (int i = 0; i < 20; i += 2) {
                while (!evenTurn) { // Wait if it's not even's turn
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.printf("%d ", i);
                evenTurn = false; // After printing even number, set flag for odd turn
                lock.notify(); // Notify the odd thread to print
            }
        }
    }

    void printOdd() {
        synchronized (lock) {
            for (int i = 1; i < 20; i += 2) {
                while (evenTurn) { // Wait if it's not odd's turn
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.printf("%d ", i);
                evenTurn = true; // After printing odd number, set flag for even turn
                lock.notify(); // Notify the even thread to print
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        OddEven oddEven = new OddEven();

        // Create two threads, one for printing even numbers and one for odd numbers
        Thread evenThread = new Thread(oddEven::printEven);
        Thread oddThread = new Thread(oddEven::printOdd);

        // Start both threads
        evenThread.start();
        oddThread.start();

        try {
            // Wait for both threads to finish
            evenThread.join();
            oddThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}