package docs.concurrency;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * ============================================================
 * Part 4: BLOCKING QUEUE, LATCH, SEMAPHORE, CONCURRENT MAP
 * ============================================================
 *
 * These are common coordination utilities built on top of the raw thread tools.
 *
 * USE THEM WHEN:
 * - BlockingQueue: producer-consumer pipelines
 * - CountDownLatch: wait until N tasks finish or a start gate opens
 * - Semaphore: limit concurrency to N permits
 * - ConcurrentHashMap: shared map with thread-safe concurrent access
 *
 * PRACTICAL RULE:
 * Prefer these higher-level building blocks over hand-rolled wait/notify logic.
 */
public class Part4_CoordinationUtilities {

    public static void main(String[] args) throws Exception {
        example1_BlockingQueueProducerConsumer();
        example2_CountDownLatch();
        example3_Semaphore();
        example4_ConcurrentHashMap();
    }

    // =========================================================
    // 4A: BlockingQueue -> producer-consumer
    // =========================================================
    static void example1_BlockingQueueProducerConsumer() throws InterruptedException {
        System.out.println("=== 4A: BlockingQueue producer-consumer ===");

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        Thread producer = new Thread(() -> {
            try {
                queue.put("job-1");
                queue.put("job-2");
                queue.put("STOP");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "producer");

        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String item = queue.take();
                    if ("STOP".equals(item)) {
                        break;
                    }
                    System.out.println("processed " + item + " on " + Thread.currentThread().getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "consumer");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }

    // =========================================================
    // 4B: CountDownLatch -> wait until N events happen
    // =========================================================
    static void example2_CountDownLatch() throws InterruptedException {
        System.out.println("\n=== 4B: CountDownLatch ===");

        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 1; i <= 3; i++) {
            final int workerId = i;
            new Thread(() -> {
                System.out.println("worker " + workerId + " done");
                latch.countDown();
            }, "latch-worker-" + i).start();
        }

        latch.await();
        System.out.println("all workers completed");
    }

    // =========================================================
    // 4C: Semaphore -> limit concurrent access
    // =========================================================
    static void example3_Semaphore() throws InterruptedException {
        System.out.println("\n=== 4C: Semaphore ===");

        Semaphore semaphore = new Semaphore(2);

        Runnable task = () -> {
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " entered limited section");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        };

        Thread t1 = new Thread(task, "sem-1");
        Thread t2 = new Thread(task, "sem-2");
        Thread t3 = new Thread(task, "sem-3");
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

    // =========================================================
    // 4D: ConcurrentHashMap -> safe shared map operations
    // =========================================================
    static void example4_ConcurrentHashMap() throws InterruptedException {
        System.out.println("\n=== 4D: ConcurrentHashMap ===");

        Map<String, Integer> counts = new ConcurrentHashMap<>();

        // Scanner sc = new Scanner(System.in);
        // sc.nextLine();
        // sc.close();

        Runnable countTask = () -> {
            for (int i = 0; i < 1_000; i++) {
                counts.merge("hits", 1, Integer::sum);
            }
        };

        Thread t1 = new Thread(countTask, "map-1");
        Thread t2 = new Thread(countTask, "map-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("hits=" + counts.get("hits"));
    }
}