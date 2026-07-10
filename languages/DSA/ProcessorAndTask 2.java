import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;

class Node {
    public int count; // Number of processors with the same capacity
    public int numberOfTasks; // Number of tasks assigned to this capacity

    // Constructor to initialize the Node object
    public Node() {
        this.count = 1; // Default to 1 processor when instantiated
        this.numberOfTasks = 0; // Initially no tasks assigned
        // this(1, 0);
    }

    // Method to increment the count of processors
    public void incrementCount() {
        this.count++;
    }

    // Method to increment the number of tasks assigned
    public void incrementTask() {
        this.numberOfTasks++;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("count: ");
        sb.append(count);
        sb.append(", numberOfTasks: ");
        sb.append(numberOfTasks);
        return sb.toString();
    }
}

public class ProcessorAndTask {
    public static void main(String[] args) {
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt(); // Number of tasks
        int m = sc.nextInt(); // Number of processors
        // 15 4
        // 8 8 5 5 5 4 4 4 4 4 4 4 4 3 1
        // 4 4 5 8
        int[] task = new int[n]; // Task sizes

        // Reading the task sizes
        for (int i = 0; i < n; i++) {
            task[i] = sc.nextInt();
        }

        TreeMap<Integer, Node> map = new TreeMap<>();

        // Reading processor capacities
        for (int i = 0; i < m; i++) {
            int capacity = sc.nextInt();
            if (map.containsKey(capacity)) {
                Node node = map.get(capacity);
                node.incrementCount(); // Increment processor count for this capacity
            } else {
                Node node = new Node();
                map.put(capacity, node); // Add a new processor group
            }
        }

        // Assigning tasks to processors
        for (int i = 0; i < n; i++) {
            Integer suitableProcessor = map.ceilingKey(task[i]); // Find the smallest suitable processor
            if (suitableProcessor == null) {
                System.out.println(-1); // No suitable processor found for this task
                return;
            }
            map.get(suitableProcessor).incrementTask(); // Assign task to processor group
        }

        // Calculating the minimum time required
        Iterator<Node> it = map.descendingMap().values().iterator();
        int maxUsed = 0, totalTasks = 0, totalProcessors = 0;

        while (it.hasNext()) {
            Node node = it.next();

            System.out.printf("1. %d %d %d\n", totalProcessors, totalTasks, maxUsed);
            // Calculate remaining tasks based on maxUsed processors handling tasks at this
            // level
            totalTasks += node.numberOfTasks; // Total tasks for this processor capacity

            totalProcessors += node.count;

            // Update the max number of tasks any processor group needs to handle
            maxUsed = Math.max(maxUsed, (int) Math.ceil((double) totalTasks / (double) totalProcessors));
            System.out.println(node.toString());
            System.out.printf("2. %d %d %d\n\n\n", totalProcessors, totalTasks, maxUsed);
        }

        System.out.println(maxUsed); // Output the result
    }
}