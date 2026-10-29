import java.util.LinkedList;
import java.util.Queue;

public class ThreadNotes {
    public static void main(String[] args) {
        // Creating a runnable to demonstrate the use of Runnable interface
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("Runnable is running");
            }
        };

        /*
         * Thread implements Runnable
         * The Thread class has a Runnable target that is executed when the thread starts.
         * The implementation of start() in the Thread class:
         * if(target != null) {
         *     target.run();
         * }
         */

        // Creating and starting a thread using Runnable
        Thread myThread = new Thread(r);
        myThread.start();

        // Ensuring the main thread waits for myThread to finish
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Main thread finished.");

        // Creating instances of MyClass for two counter threads
        MyClass counter1 = new MyClass("Counter Thread 1");
        MyClass counter2 = new MyClass("Counter Thread 2");

        // Creating and starting the counter threads
        Thread thread1 = new Thread(counter1);
        Thread thread2 = new Thread(counter2);
        thread1.start();
        thread2.start();

        // Joining the counter threads to ensure the main thread waits for their completion
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted.");
        }
        System.out.println("Both threads have finished execution.");

        // Creating and starting a thread using MyClass2 which extends Thread
        MyClass2 obj = new MyClass2();
        obj.start();
        try {
            obj.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted.");
        }

        // Creating a shared buffer for producer-consumer example
        SharedBuffer sharedBuffer = new SharedBuffer(5); // Buffer capacity of 5

        // Creating and starting producer and consumer threads
        Thread producerThread = new Thread(new Producer(sharedBuffer), "Producer");
        Thread consumerThread = new Thread(new Consumer(sharedBuffer), "Consumer");
        producerThread.start();
        consumerThread.start();
    }
}

class MyClass implements Runnable {
    private final String threadName;

    MyClass(String name) {
        this.threadName = name;
    }

    @Override
    public void run() {
        System.out.println(threadName + " - " + Thread.currentThread().getName());
    }
}

class MyClass2 extends Thread {
    @Override
    public void run() {
        System.out.println("Thread Name: " + Thread.currentThread().getName());
    }
}

class SharedBuffer {
    private final Queue<Integer> buffer = new LinkedList<>();
    private final int capacity;

    public SharedBuffer(int capacity) {
        this.capacity = capacity;
    }

    // Producer method
    public void produce(int value) throws InterruptedException {
        synchronized (this) {
            while (buffer.size() == capacity) {
                System.out.println("Buffer is full. Producer is waiting...");
                wait(); // Wait until space is available
                // it will until someone calls notify/ notifyAll on the same object
            }
            buffer.add(value);
            System.out.println("Produced: " + value);
            notifyAll(); // Notify consumers that a new item has been added
        }
    }

    // Consumer method
    public int consume() throws InterruptedException {
        synchronized (this) {
            while (buffer.isEmpty()) {
                System.out.println("Buffer is empty. Consumer is waiting...");
                wait(); // Wait until an item is available
            }
            int value = buffer.remove();
            System.out.println("Consumed: " + value);
            notifyAll(); // Notify producers that space is available
            return value;
        }
    }
}

class Producer implements Runnable {
    private final SharedBuffer sharedBuffer;

    public Producer(SharedBuffer sharedBuffer) {
        this.sharedBuffer = sharedBuffer;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                sharedBuffer.produce(i);
                Thread.sleep(100); // Simulate time taken to produce
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class Consumer implements Runnable {
    private final SharedBuffer sharedBuffer;

    public Consumer(SharedBuffer sharedBuffer) {
        this.sharedBuffer = sharedBuffer;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                sharedBuffer.consume();
                Thread.sleep(150); // Simulate time taken to consume
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}


/*
 * // Threads, Executors, Callable Future, Fork/Join
 * // Synchronized Blocks/Methods, Reentrant Locks, Read/Write Locks, Atomic
 * Variables
 * Concurrency Patterns:
 * Producer-Consumer Pattern, Thread Pool Pattern(using executer), Future and
 * Callable Pattern, Read-Write Lock Pattern, ReentrantLock,
 * 
 * Minimizing Synchronization, Thread Pools, Avoiding Deadlocks, Lock
 * Contention, Non-Blocking Algorithms, Thread Local Variables
 * 
 * 1. Threads:
 * • Java supports multi-threading through the Thread class and the Runnable
 * interface.
 * • You can create a new thread by extending the Thread class or implementing
 * the Runnable interface and passing it to a Thread object.
 * 2. Executors: higher-level API for managing threads using the
 * ExecutorService, ScheduledExecutorService, and ThreadPoolExecutor.
 * Executors manage a pool of threads and handle tasks asynchronously.
 * 3. Callable and Future:
 * • Callable is a functional interface similar to Runnable, but it can return a
 * result and throw a checked exception.
 * • Future represents the result of an asynchronous computation. It provides
 * methods to check if the computation is complete and to retrieve the result.
 * 4. Fork/Join Framework:
 */

/*
 * 
 * void updateTime(){
 * write ut for updateTime
 * updateSErvice.updateTimestamp()
 * }
 * 
 * concurrency in hashmap
 * 
 * List<node>[]bucket
 * 
 * getValue(key)
 * 
 * setValue
 * 
 * 
 * 
 * class Employee{
 * int empId;
 * String name;
 * String dep;
 * 
 * }
 * 
 * Set<Employee>
 * hashcode, equals
 * 
 * 
 * 
 * /get
 * /updatedMap
 * /delete
 * 
 * static Integer
 * 
 * /metrics -> count of request
 * spread across multiple controller on a single instance server
 * 
 * 
 * single instance
 * 
 * 
 */