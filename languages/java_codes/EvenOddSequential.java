class EvenOddPrinter {
    private final int limit; // The maximum number to print
    private boolean isOdd = true; // Flag to control thread execution

    public EvenOddPrinter(int limit) {
        this.limit = limit;
    }

    public void printOdd() {
        synchronized (this) {
            for (int i = 1; i <= limit; i += 2) {
                while (!isOdd) { // Wait until it's the turn for odd numbers
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("Odd: " + i);
                isOdd = false; // Toggle the flag to even
                notify(); // Wake up the even thread
            }
        }
    }

    public void printEven() {
        synchronized (this) {
            for (int i = 2; i <= limit; i += 2) {
                while (isOdd) { // Wait until it's the turn for even numbers
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("Even: " + i);
                isOdd = true; // Toggle the flag to odd
                notify(); // Wake up the odd thread
            }
        }
    }
}

public class EvenOddSequential {
    public static void main(String[] args) {
        int limit = 10; // Change this to set the maximum number to print
        EvenOddPrinter printer = new EvenOddPrinter(limit);

        Thread oddThread = new Thread(printer::printOdd, "Odd-Thread");
        Thread evenThread = new Thread(printer::printEven, "Even-Thread");

        oddThread.start();
        evenThread.start();
    }
}