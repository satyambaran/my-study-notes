# Threading
## Process
An instance of program that is being executed.

Compilation := javac Test.java

Execution:= java -Xms256m -Xmx2g Test

- A new JVM instance is created and allocated to it
- At this point JVM starts a process
    * JVM will compile byte code to machine code at runtime. Called Interpreting or JIT compiling
    * Happens at runtime
    * Save this machine code to code segment
    1. CPU(have registers) <==> Cache <==> Main memory
    2. Each thread have access to code segment and a counter
    3. Now, JVM scheduler(OS scheduler) will ask CPU to run this thread
    4. All the data will be stored in CPU register
    5. Once, thread run time finishes, CPU will store all the data from their register to thread's register
    6. CPU will get busy with other thread (called context switching)
    7. Once this thread's turn comes, data will get loaded from thread's register to CPU's register
    8. We do it just by changing the reference
    9. Keeping all the data in thread's register, then it helps in case multi-processor environment
- Multi-tasking vs Multi-threading
    * Different process each with multiple thread
- Process dont share their resources
- Process can have multiple threads
    * Each process has their own JVM
    * JVM will have heap, stack, code segment(compiled byte code), data segment, registers, program counters
    * stack, register, counters are local to each thread and they do not share with each other
- Code Segment:
    - Compiled byte code
- Data Segment:
    - Global and static variables
    - Each thread can read and modify these data, so proper synchronization is needed
- Heap:
    - Objects created by 'new' at runtime
    - SHared by threads(of same process) but not by process
- Stack:
    - each thread have their own stack
- Register:
    - thread wise unique
    - helps in context switching
    - 
- COunter:
    - Points to the instruction that is being used
## Thread
- Smallest sequence of instruction, that are executed by CPU independently
- First thread is always main thread ```Thread.currentThread().getName()```

1. Synchronization and Thread Safety
    a. Synchronized Methods
    ```java
    public synchronized void synchronizedMethod() {}
    ```
    b. Synchronized Blocks
    ```java
    public void exampleMethod() {
        synchronized(this) {}
    }
    ```
2. Inter-Thread Communication
    * wait(), notify(), and notifyAll()
        - wait(): Causes the current thread to wait until another thread invokes notify() or notifyAll() on the same object.
	    - notify(): Wakes up a single thread that is waiting on the object’s monitor.
	    - notifyAll(): Wakes up all threads that are waiting on the object’s monitor.
    

---

-  Introduction of Multithreading:
     * Definition of Multithreading
     * Benefits and Challenges of Multithreading
     * Processes v/s Threads
     * Multithreading in Java

-  Java Memory Model of Process and thread (Covered in this Video)

-  Basics of Threads - Part1: 
     * Creating Threads
         * Extending the Thread Class
         * Implementing the Runnable Interface
     * Thread Lifecycle
         * New
         * Runnable
         * Blocked
         * Waiting
         * Timed Waiting
         * Terminated

-  Basics of Thread - Part2 : Inter Thread Communication and Synchronization
  * Synchronization and Thread Safety
         * Synchronized Methods
             * Synchronized Blocks
 * Inter-Thread Communication
         * wait(), notify(), and notifyAll() methods
        * Producer-Consumer Problem - Assingment
         
 
- Basics of Threads - Part3 
     * Producer-Consumer Problem - Solution discuss
    * Stop, Resume, Suspended method is deprecated, understand why and its solution
     * Thread Joining
     * Volatile Keyword
     * Thread Priority and Daemon Threads
 
-  Some Advanced Topics
     * Thread Pools
         * Executor Framework
         * ThreadPoolExecutor
     * Callable and Future
     * Fork/Join Framework
     * ThreadLocal in Multithreading

-  Concurrency Utilities
     * java.util.concurrent Package
     * Executors and ExecutorService
     * Callable and Future
     * CompletableFuture
     * ScheduledExecutorService
     * CountDownLatch, CyclicBarrier, Phaser, and Exchanger

-  Concurrent Collections (already discussed during Collections topic, will provide working example for this)
     * ConcurrentHashMap
     * ConcurrentLinkedQueue and ConcurrentLinkedDeque
     * CopyOnWriteArrayList
     * BlockingQueue Interface
         * ArrayBlockingQueue
         * LinkedBlockingQueue
         * PriorityBlockingQueue

-  Atomic Variables
     * AtomicInteger, AtomicLong, and AtomicBoolean
     * AtomicReference and AtomicReferenceArray
     * Compare-and-Swap Operations

-  Locks and Semaphores
     * ReentrantLock
     * ReadWriteLock
     * StampedLock
     * Semaphores
     * Lock and Condition Interface

-  Parallel Streams (already discussed during Stream topic, will provide working example for this)

 -  Best Practices and Patterns
     * Thread Safety Best Practices
     * Immutable Objects
     * ThreadLocal Usage
     * Double-Checked Locking and its Issues
     * Concurrency Design Patterns

-  Common Concurrency Issues and Solutions
     * Deadlocks
     * Starvation
     * Livelocks
     * Race Conditions
     * Strategies for Avoiding Concurrency Issues

-  Java 9+ Features
     * Reactive Programming with Flow API
     * CompletableFuture Enhancements
     * Process API Updates

-  Java 11+ Features
     * Local-Variable Type Inference (var keyword)
     * Enhancements in Optional class
     * New Methods in the String class relevant to concurrency