import java.util.HashMap;
import java.util.Queue;

public class queue {

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
    String name;
    Queue<Message> queue;
    int capacity;

    Message(String _name, int _capacity){
        name=_name;capacity=_capacity;
    }

    public String getName() {
        return name;
    }

    public void enqueue(Message msg) throws InterruptedException {
        synchronized (this) {
            while (queue.size() == capacity) {
                wait();
            }
            queue.offer(msg);
            notifyAll();
        }
    }

    public Message dequeue() throws InterruptedException {
        synchronized (this) {
            while (queue.size() == 0) {
                wait();
            }
            Message msg = queue.poll();
            notifyAll();
            return msg;
        }
    }
}

class MessageBroker {
    private HashMap<String, MessageQueue> queues;

    MessageBroker() {
        queues = new HashMap<>();
    }

    public void createQueue(String name, int capacity) {
        queues.put(name, new MessageQueue(name, capacity));
    }

    public MessageQueue getQueue(String queueName) {
        return queues.get(queueName);
    }
}

class Producer {
    private final MessageBroker broker;

    Producer(MessageBroker broker) {
        this.broker = broker;
    }

    public void produce(String content, String queueName) {
        MessageQueue queue = broker.getQueue(queueName);
        if (queue == null) {
            throw new RuntimeException("queue is not there");
        }
        queue.enqueue(new Message(content));
    }
}
/*
 * Two gamblers are playing a coin toss game. Gambler
 * 
 * A has (n+1) fair coins;
 * 
 * B has n fair coins. What is the probability that
 * 
 * A will have more heads than
 * 
 * B if both flip all their coins?
 * 
 * 
 * B has
 * exactly 0 head = nc0 (1/2)^n
 * exactly 1 head = nc1 (1/2)^n
 * exactly 2 head = nc2 (1/2)^n
 * 
 * exactly n head = ncn (1/2)^n
 * 
 * at most k heads = sum(ncr) * (1/2)^n where r=0,1,...k
 * 
 * 
 * 
 * 
 * exaclty k heads:((n+1 c k) * (1/2)^(n+1) )
 * 
 * probablity of A winning when A gets exaclty k heads: ((n+1 c k) * (1/2)^(n+1)
 * ) *
 * (sum(ncr) * (1/2)^n where r=0,1,...k-1) = f(k)
 * 
 * 
 * probability of A winning = sum(f(k)) where k=0,1, ...N+1
 * 
 * 
 * 
 * a=n+1
 * b=N
 * 
 * a=n b=N
 * p(a>b)=p(b>a)=x
 * e1 e2
 * p(a==b)=y
 * e3
 * 2x + y = 1
 * 
 * 
 * 
 * now a has one coin
 * 
 * e3 = y/2
 * e2 = 0
 * e1 = x
 * x + (y/2) = 1/2
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * p(a/b) = p(a^b)/ p(b)
 * = p(b/a)/p(b)
 * 
 * 
 * 
 * 25 horse, 5 tracks
 * find number of attempts to get the top 3 horse who runs fastest
 * 
 * a b c d e
 * f g h i j
 * k l m n o
 * p q r s t
 * u v w x y
 * 
 * 5 race
 * 
 * a f k p u
 * 1 race
 * 
 * a b c
 * f g
 * k
 * 
 * A
 * 
 * b c f g k
 * 
 * 
 * 
 * 
 * a b c f g
 * h k l m p
 * q r u v w
 * 
 * 3 race
 * 
 * a b c
 * h k l
 * q r u
 * 
 * a b c h k
 * l q r u
 * 
 * 
 * 3 3 8 8
 * + - x %
 * 24
 * 
 * 8/(3-(8/3))
 * 8/(1/3)
 * 24
 * 
 * 
 * 
 * 
 * ((3/3)+8)*8 =72
 * 
 * 
 * door1 door2
 * A     B
 * 
 * A & B both know each other properties
 * 
 * 
 * always truth
 * always false
 * 
 * 
 * b truth a false
 * b false a truth
 * 
 * que to B:  whether A is saying truth or false?
 * 
 * B truth ->
 * 
 * que to B: what will A say, if I ask door1 is correct?
 *             door1 is correct
 * B truth:
 * A truth:
 */