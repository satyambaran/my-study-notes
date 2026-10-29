package new_notes.shrayansh.lld.src.com.ratelimitter;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

class RateLimiter {
    private final int maxRequests;
    private final int windowSizeInSeconds;
    private final ConcurrentMap<String, SlidingWindowCounter> clientWindows;
    private final ReentrantLock lock = new ReentrantLock();

    public RateLimiter(int maxRequests, int windowSizeInSeconds) {
        this.maxRequests = maxRequests;
        this.windowSizeInSeconds = windowSizeInSeconds;
        this.clientWindows = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String clientId) {
        long currentTime = System.currentTimeMillis();
        clientWindows.putIfAbsent(clientId, new SlidingWindowCounter(currentTime, windowSizeInSeconds));
        SlidingWindowCounter windowCounter = clientWindows.get(clientId);

        lock.lock();
        try {
            int currentCount = windowCounter.getTotalCount(currentTime);
            System.out.println(currentCount+","+currentTime);
            if (currentCount < maxRequests) {
                windowCounter.addRequest(currentTime);
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
}

class SlidingWindowCounter {
    private final int windowSizeInSeconds;
    private final TimeBucket bucket;

    public SlidingWindowCounter(long currentTime, int windowSizeInSeconds) {
        this.windowSizeInSeconds = windowSizeInSeconds;
        this.bucket = new TimeBucket(currentTime);
    }

    public void addRequest(long timestamp) {
        bucket.increment();
    }

    public int getTotalCount(long currentTime) {
        return bucket.getCount(currentTime, windowSizeInSeconds);
    }
}

class TimeBucket {
    private long timestamp;
    private int count, prevCount;

    public TimeBucket(long timestamp) {
        this.timestamp = timestamp;
        this.count = 1;
        prevCount = 0;
    }

    public void increment() {
        count++;
    }

    public int getCount(long currentTime, int windowSizeInSeconds) {
        if (currentTime - timestamp >= windowSizeInSeconds * 2000) {
            prevCount = 0;
            count = 0;
            timestamp = 1000 * (currentTime / 1000);
        } else if (currentTime - timestamp >= windowSizeInSeconds * 1000) {
            prevCount = count;
            count = 0;
            timestamp += windowSizeInSeconds * 1000;
        }
        double elapsedTime = (double) (currentTime - timestamp);
        double virtualCount = count + (elapsedTime * prevCount) / (windowSizeInSeconds * 1000.0);
        return (int) Math.ceil(virtualCount);
    }
}

class APIGateway {
    private final RateLimiter rateLimiter;
    private final Map<String, APIService> services;

    public APIGateway(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        this.services = new HashMap<>();
    }

    public void registerService(String path, APIService service) {
        services.put(path, service);
    }

    public String handleRequest(String clientId, String path) {
        if (rateLimiter.allowRequest(clientId)) {
            APIService service = services.get(path);
            if (service != null) {
                return service.processRequest();
            } else {
                return "404 - Service Not Found";
            }
        } else {
            return "429 - Too Many Requests";
        }
    }
}

class UserService implements APIService {
    @Override
    public String processRequest() {
        return "User Service Response";
    }
}

class ProductService implements APIService {
    @Override
    public String processRequest() {
        return "Product Service Response";
    }
}

interface APIService {
    String processRequest();
}

public class Main {
    public static void main(String[] args) {
        RateLimiter rateLimiter = new RateLimiter(5, 3); // 5 requests per 3 seconds
        APIGateway apiGateway = new APIGateway(rateLimiter);

        apiGateway.registerService("/user", new UserService());
        apiGateway.registerService("/product", new ProductService());

        String clientId = "client1";
        @SuppressWarnings("unused")
        String clientId2 = "client2";

        for (int i = 0; i < 10; i++) {
            String response = apiGateway.handleRequest(clientId, "/user");
            System.out.println(response);
            // String response2 = apiGateway.handleRequest(clientId2, "/user");
            // System.out.println(response2);
            try {
                Thread.sleep(500); // Simulate time between requests
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}