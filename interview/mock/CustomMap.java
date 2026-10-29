public class CustomMap {
    public static void main(String[] args) {

    }

    public int josephus(int n, int k) {
        if (n == 1) {
            return 0;
        }
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
        // 4
        // 1 2 3 5 6 7 8 9 10 11 12 13 14 15
        // return (josephus(n-1,k)+k-1) % n + 1;
        return (k + josephus(n - 1, k)) % n;
    }

    <K, V> Node<K, V> reverse(Node<K, V> head) {
        Node<K, V> prev = null;
        Node<K, V> curr = head;
        while (curr != null) {
            Node<K, V> temp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = temp;
        }
        return prev;
    }

}

/**
 * Node
 */
class Node<K, V> {
    K key;
    V val;
    Node<K, V> next;

    Node(K key, V val) {
        this.key = key;
        this.val = val;
        this.next = null;
    }

    Node(K key, V val, Node<K, V> next) {
        this.key = key;
        this.val = val;
        this.next = next;
    }
}

class CustomHashMap<K, V> {
    private Node<K, V>[] keys;
    private int size;
    private static final int INITIAL_CAPACITY = 10;
    // Load factor = number of entries / number of buckets.
    // At 1.0, resize before an insertion would exceed the bucket count.
    private static final double LOAD_FACTOR = 1.0;

    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        keys = new Node[INITIAL_CAPACITY];
        size = 0;
    }

    public void put(K key, V value) {
        int hash = getBucketIndex(key);
        Node<K, V> current = keys[hash];

        while (current != null) {
            if (current.key.equals(key)) {
                current.val = value;
                return;
            }
            current = current.next;
        }

        if (size + 1 > keys.length * LOAD_FACTOR) {
            resize();
            hash = getBucketIndex(key);
        }

        keys[hash] = new Node<>(key, value, keys[hash]);
        size++;
    }

    public V get(K key) {
        int hash = getBucketIndex(key);
        Node<K, V> current = keys[hash];
        while (current != null) {
            if (current.key.equals(key)) {
                return current.val;
            }
            current = current.next;
        }
        return null;
    }

    private int getBucketIndex(K key) {
        return Math.floorMod(key.hashCode(), keys.length);
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldKeys = keys;
        keys = new Node[oldKeys.length * 2];

        for (Node<K, V> node : oldKeys) {
            while (node != null) {
                Node<K, V> next = node.next;
                int hash = getBucketIndex(node.key);
                node.next = keys[hash];
                keys[hash] = node;
                node = next;
            }
        }
    }
}

class CustomDeque<K> {
    int size;
    K[] array;
    // head is the first element; tail is the next free slot.
    int head, tail;
    static final int INITIAL_CAPACITY = 10;

    @SuppressWarnings("unchecked")
    CustomDeque() {
        array = (K[]) (new Object[INITIAL_CAPACITY]);
        size = 0;
        head = 0;
        tail = 0;
    }

    public void addFirst(K item) {
        if (size == array.length) {
            resize();
        }
        array[head] = item;
        head = (head - 1 + array.length) % array.length;
        size++;
    }

    public void addLast(K item) {
        if (size == array.length) {
            resize();
        }
        array[tail] = item;
        tail = (tail + 1) % array.length;
        size++;
    }

    public K pollFirst() {
        if (size == 0) {
            throw new IllegalStateException("Deque is empty");
        }
        K item = array[head];
        head = (head + 1) % array.length;
        array[head] = null;
        size--;
        return item;
    }

    public K pollLast() {
        if (size == 0) {
            throw new IllegalStateException("Deque is empty");
        }
        tail = (tail - 1 + array.length) % array.length;
        K item = array[tail];
        array[tail] = null;
        size--;
        return item;
    }

    public K peekFirst() {
        if (size == 0) {
            throw new IllegalStateException("Deque is empty");
        }
        int cur = (head + 1) % array.length;
        return array[cur];
    }

    public K peekLast() {
        if (size == 0) {
            throw new IllegalStateException("Deque is empty");
        }
        return array[(tail - 1 + array.length) % array.length];
    }

    public int size() {
        return size;
    }

    private void resize() {
        @SuppressWarnings("unchecked")
        K[] newArray = (K[]) (new Object[array.length * 2]);
        for (int i = 0; i < size; i++) {
            newArray[i] = array[(head + i) % array.length];
        }
        array = newArray;
        head = array.length - 1;
        tail = size;
    }

}
