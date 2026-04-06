package com.patterns.singleton;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
    private final BlockingQueue<Connection> pool;
    private final int maxConnections;

    private ConnectionPool(int maxSize) {
        this.maxConnections = maxSize;
        this.pool = new ArrayBlockingQueue<>(maxSize);
        for (int i = 1; i <= maxSize; i++) {
            pool.add(new Connection(i));
        }
    }

    public static ConnectionPool getInstance() { return Holder.instance; }

    public static class Connection {
        int id;

        public Connection(int id) { this.id = id; }

        public String toString() { return "Connection-" + id; }
    }

    public static class Holder {
        private static final ConnectionPool instance = new ConnectionPool(10);
    }

    public Connection acquireConnection() throws InterruptedException { return pool.take(); }

    public void releaseConnection(Connection conn) throws InterruptedException { pool.offer(conn); }

    public int getAvailableConnections() { return pool.size(); }

    public static void main(String[] args) throws InterruptedException {
        // After implementing, usage should look like:
        ConnectionPool p1 = ConnectionPool.getInstance();
        ConnectionPool p2 = ConnectionPool.getInstance();
        System.out.println("Same instance: " + (p1 == p2));
        System.out.println("Available connections: " + p1.getAvailableConnections());
        ConnectionPool.Connection c1 = p1.acquireConnection();
        System.out.println("Acquired: " + c1);
        ConnectionPool.Connection c2 = p1.acquireConnection();
        System.out.println("Acquired: " + c2);
        System.out.println("Available after acquiring 2: " + p1.getAvailableConnections());
        p1.releaseConnection(c1);
        System.out.println("Released: " + c1);
        System.out.println("Available after release: " + p1.getAvailableConnections());
    }
}
