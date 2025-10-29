package new_notes.shrayansh.lld.src.com.hashMap;

public class MyHashMap<K, V> {
    //  there is one key named TreefyThresold, so when next.length becomes more than this thresold, hashmap will convert this next list to tree. So that worst case becomes O(log n) not O(n)
    
    private static final int INITIAL_SIZE = 1 << 4;
    private static final int MAX_SIZE = 1 << 30;
    public Entry[] hashTable;

    public MyHashMap() {
        hashTable = new Entry[INITIAL_SIZE];
    }

    public MyHashMap(int capacity) {
        int tableSize = getTableSize(capacity);
        hashTable = new Entry[tableSize];
    }

    final int getTableSize(int capacity) {
        int n = capacity - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        if (n < 0)
            return 1;
        else if (n >= MAX_SIZE) {
            return MAX_SIZE;
        }
        return n + 1;
    }

    class Entry<K, V> {
        public K key;
        public V value;
        Entry<K, V> next;

        Entry(K k, V v) {
            key = k;
            value = v;
        }
    }

    public void put(K key, V value) {
        int hashCode = key.hashCode() % hashTable.length;
        Entry<K, V> node = hashTable[hashCode];
        if (node == null) {
            hashTable[hashCode] = new Entry<K, V>(key, value);
        } else {
            Entry prevNode = node;
            while (node != null) {
                if (node.key == key) {
                    node.value = value;
                    return;
                }
                prevNode = node;
                node = node.next;
            }
            Entry<K, V> newNode = new Entry<K, V>(key, value);
            prevNode.next = newNode;
        }
    }

    public V get(K key) {
        int hashCode = key.hashCode() % hashTable.length;
        Entry<K, V> node = hashTable[hashCode];
        while (node != null) {
            if (node.key.equals(key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }
};
