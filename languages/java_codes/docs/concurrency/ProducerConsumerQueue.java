import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * PRODUCER-CONSUMER PATTERN WITH MESSAGE QUEUE
 * =============================================
 *
 * CONCEPT: A bounded queue sits between producers and consumers.
 * - Producers add messages; if the queue is full, they block (wait).
 * - Consumers take messages; if the queue is empty, they block (wait).
 *
 * KEY MECHANISM:
 * - synchronized + wait/notifyAll for thread-safe bounded buffer.
 * - wait() releases the monitor lock so the other side can make progress.
 * - notifyAll() wakes all waiting threads (both producers and consumers).
 *
 * COMPONENTS:
 * - Message: Immutable message object.
 * - MessageQueue: Thread-safe bounded queue (the core of the pattern).
 * - MessageBroker: Registry of named queues (like a mini Kafka/RabbitMQ).
 * - Producer/Consumer: Client code that sends/receives messages.
 *
 * REAL-WORLD ALTERNATIVE: Use java.util.concurrent.BlockingQueue (e.g.,
 * ArrayBlockingQueue, LinkedBlockingQueue) which handles all this internally.
 *
 * WHY BUILD IT MANUALLY?
 * - Understanding the underlying mechanism is a common interview question.
 * - Teaches wait/notify semantics and bounded buffer design.
 */
public class ProducerConsumerQueue {

    public static void main(String[] args) throws InterruptedException {
        MessageBroker broker = new MessageBroker();
        broker.createQueue("orders", 5);

        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    broker.getQueue("orders").enqueue(new Message("Order #" + i));
                    System.out.println("Produced: Order #" + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    Message msg = broker.getQueue("orders").dequeue();
                    System.out.println("Consumed: " + msg.getContent());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.println("All messages processed!");
    }
}

class Message {
    private final String content;

    public Message(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

class MessageQueue {
    private final String name;
    private final Queue<Message> queue;
    private final int capacity;

    public MessageQueue(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.queue = new LinkedList<>(); // LinkedList implements Queue
    }

    public String getName() {
        return name;
    }

    /**
     * Blocks if the queue is full (producer waits until consumer takes something).
     */
    public synchronized void enqueue(Message msg) throws InterruptedException {
        while (queue.size() == capacity) {
            wait(); // Queue full -> producer waits
        }
        queue.offer(msg);
        notifyAll(); // Wake consumers that might be waiting for data
    }

    /**
     * Blocks if the queue is empty (consumer waits until producer adds something).
     */
    public synchronized Message dequeue() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // Queue empty -> consumer waits
        }
        Message msg = queue.poll();
        notifyAll(); // Wake producers that might be waiting for space
        return msg;
    }
}

class MessageBroker {
    private final HashMap<String, MessageQueue> queues = new HashMap<>();

    public void createQueue(String name, int capacity) {
        queues.put(name, new MessageQueue(name, capacity));
    }

    public MessageQueue getQueue(String queueName) {
        MessageQueue q = queues.get(queueName);
        if (q == null) throw new RuntimeException("Queue '" + queueName + "' not found");
        return q;
    }
}
