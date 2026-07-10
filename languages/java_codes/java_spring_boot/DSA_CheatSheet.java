package java_spring_boot;

import java.util.*;
import java.util.stream.*;

public class DSA_CheatSheet {
    // A class can extend only ONE class (single inheritance)
    // A interface can extend only ONE interface (single inheritance)
    // A class can implement MULTIPLE interfaces

    // ─────────────────────────────────────────────
    // CUSTOM HASHING: wrap key in a class with hashCode + equals
    // Use when: default Object identity hash isn't what you want
    // e.g. HashMap<Point, Integer> where Point(1,2) == Point(1,2)
    // ─────────────────────────────────────────────
    static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y); // standard: combine fields
            // return 31 * x + y; // manual alternative
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point))
                return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }

        // Returns a reusable Comparator — use in TreeMap/TreeSet/sort without
        // implementing Comparable
        // Usage: new TreeMap<>(Point.comparator())
        // points.sort(Point.comparator())
        static Comparator<Point> comparator() {
            return Comparator.comparingInt((Point p) -> p.x).thenComparingInt(p -> p.y);
        }
    }

    // ─────────────────────────────────────────────
    // COMPARABLE: lets the object sort itself (natural order)
    // Use when: you always want the same default sort
    // ─────────────────────────────────────────────
    static class Student implements Comparable<Student> {
        String name;
        int age;

        Student(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public int compareTo(Student other) {
            if (this.age != other.age)
                return this.age - other.age; // sort by age asc
            return this.name.compareTo(other.name); // then name lex
        }
    }

    static Set<Integer> other_set = new HashSet<>();
    static int sink = 0;

    static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    static void touch(Object... values) {
        sink ^= Arrays.deepHashCode(values);
    }

    static int lowerBound(List<Integer> list, int target) {
        int l = 0, r = list.size() - 1;
        int ans = list.size();
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (list.get(mid) >= target) {
                ans = mid;
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return ans;
    }

    static int upperBound(List<Integer> list, int target) {
        int l = 0, r = list.size() - 1;
        int ans = list.size();
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (list.get(mid) > target) {
                ans = mid;
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        // ─────────────────────────────────────────────
        // SCANNER & PRINT
        // ─────────────────────────────────────────────
        try (Scanner sc = new Scanner(System.in)) {
            int n = sc.nextInt(); // read int
            long lv = sc.nextLong(); // read long
            double dv = sc.nextDouble(); // read double
            String s = sc.next(); // read token (no spaces)
            String ln = sc.nextLine(); // read full line
            System.out.println("hi"); // print + newline
            System.out.print("hi"); // print no newline
            System.out.printf("%d %s\n", n, s); // formatted
            touch(n, lv, dv, s, ln);
        }
        // ─────────────────────────────────────────────
        // ARRAY
        // ─────────────────────────────────────────────
        int[] arr = new int[10]; // default 0
        int[] arr2 = { 1, 2, 3 };
        Arrays.fill(arr, -1); // fill all with -1
        Arrays.sort(arr); // sort ascending O(n log n)
        Arrays.sort(arr, 1, 5); // sort subarray [1,5)
        int idx = Arrays.binarySearch(arr, 3); // binary search (must be sorted)
        int[] copy = Arrays.copyOf(arr, arr.length); // copy full
        int[] copy2 = Arrays.copyOfRange(arr, 1, 4); // copy [1,4)
        int total = Arrays.stream(arr).sum(); // sum of elements
        System.out.println(Arrays.toString(arr)); // print 1D
        System.out.println(Arrays.deepToString(new int[][] { { 1, 2 }, { 3, 4 } })); // print 2D
        // 2D array
        int[][] grid = new int[3][4];
        int[][] grid2 = { { 1, 2 }, { 3, 4 } };
        touch(arr2, idx, copy, copy2, total, grid, grid2);

        // Integer array (needed for custom sort — primitives can't use Comparator)
        Integer[] boxed = { 3, 1, 2 };
        Arrays.sort(boxed, (x, y) -> y - x); // sort descending
        Arrays.sort(boxed, Comparator.reverseOrder()); // same
        // ─────────────────────────────────────────────
        // STRING
        // ─────────────────────────────────────────────
        String str = "Hello World";
        str.length(); // 11
        str.charAt(0); // 'H'
        str.substring(1, 5); // "ello" [1,5)
        str.indexOf("lo"); // 3 (-1 if not found)
        str.lastIndexOf('l'); // 9
        str.contains("World"); // true
        str.startsWith("He"); // true
        str.endsWith("ld"); // true
        str.equals("Hello World"); // true (use this, not ==)
        str.equalsIgnoreCase("hello world");
        str.toLowerCase();
        str.toUpperCase();
        str.trim(); // remove leading/trailing spaces
        str.strip(); // same but unicode-aware
        str.replace('l', 'r'); // "Herro Worrd"
        str.replace("World", "Java");
        str.replaceAll("[aeiou]", "*"); // regex replace
        str.split(" "); // ["Hello", "World"]
        str.split(",", 2); // limit splits
        String.join(", ", "a", "b", "c"); // "a, b, c"
        String.valueOf(42); // "42"
        Integer.parseInt("42"); // 42
        Long.parseLong("42");
        Double.parseDouble("3.14");
        str.toCharArray(); // char[]
        new String(new char[] { 'a', 'b' }); // "ab"
        str.isEmpty(); // false
        str.isBlank(); // false (checks whitespace too)
        str.compareTo("abc"); // lexicographic compare
        str.compareToIgnoreCase("hello world");
        // char checks
        Character.isDigit('5'); // true
        Character.isLetter('a');
        Character.isUpperCase('A');
        Character.toLowerCase('A');
        // ─────────────────────────────────────────────
        // STRINGBUILDER (mutable, faster than + in loops)
        // ─────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        sb.append("hello"); // append string/int/char/etc
        sb.append(' ').append("world"); // chaining
        sb.insert(5, ","); // insert at index
        sb.delete(5, 6); // delete [5,6)
        sb.deleteCharAt(0);
        sb.replace(0, 3, "Hi"); // replace [0,3) with "Hi"
        sb.reverse();
        sb.charAt(0);
        sb.setCharAt(0, 'H');
        sb.length();
        sb.indexOf("world");
        sb.toString(); // convert to String
        for (int i = 9; i >= 0; i--) {
            // sb.append(String.valueOf(i).repeat(cnt[i]));
        }
        // ─────────────────────────────────────────────
        // ARRAYLIST (dynamic array, like vector<int>)
        // ─────────────────────────────────────────────
        List<Integer> list = new ArrayList<>();
        // List.of(null); //throws NPE: List.of doesn't allow null elements — use
        List<Integer> nullAllowed = Collections.singletonList((Integer) null); // instead
        list.add(1); // append
        list.add(0, 99); // insert at index
        list.get(0); // access
        list.set(0, 5); // update
        list.remove(Integer.valueOf(5)); // remove by value
        list.remove(0); // remove by index
        list.size();
        list.isEmpty();
        list.contains(3);
        list.indexOf(3); // -1 if not found
        list.lastIndexOf(3);
        Collections.sort(list); // sort asc
        Collections.sort(list, (x, y) -> y - x); // sort desc
        Collections.reverse(list);
        Collections.shuffle(list);
        Collections.min(list);
        Collections.max(list);
        Collections.frequency(list, 3); // count occurrences of 3
        Collections.fill(list, 0);
        Collections.nCopies(5, 1); // [1,1,1,1,1] (immutable)
        list.subList(1, 3); // view [1,3), not a copy
        list.toArray(new Integer[0]);
        list.addAll(List.of(1, 2, 3));
        list.removeAll(List.of(1, 2));
        list.retainAll(List.of(1, 3)); // keep only these
        List.of(1, 2, 3); // immutable list
        new ArrayList<>(List.of(1, 2, 3)); // mutable copy
        // binary search (list must be sorted)
        int pos = Collections.binarySearch(list, 5); // index or -(insertion_point)-1
        // pos is not guaranteed to have smalles/largest pos in case of repeating
        // elements

        // ─────────────────────────────────────────────
        // LOWER_BOUND / UPPER_BOUND (C++ equivalent)
        // ─────────────────────────────────────────────
        // Note: Collections.binarySearch does not guarantee first/last among
        // duplicates.
        // lower_bound(x) = first index where arr[i] >= x
        // upper_bound(x) = first index where arr[i] > x
        Collections.sort(list);
        int target = 5;
        int lowerBound = lowerBound(list, target);
        int upperBound = upperBound(list, target);
        list.subList(lowerBound, upperBound); // gives you [lowerBound, upperBound) which basically is all the values
                                              // equal to that particular value
        touch(pos, nullAllowed, lowerBound, upperBound);

        // ─────────────────────────────────────────────
        // STACK (use Deque, not Stack class)
        // ─────────────────────────────────────────────
        Deque<Integer> stack = new ArrayDeque<>();
        // Stack (LIFO) with front as top:
        // push -> offerFirst/addFirst, pop -> pollFirst, top -> peekFirst
        stack.offerFirst(1); // push top
        stack.pollFirst(); // remove top (null if empty)
        stack.peekFirst(); // view top (null if empty)
        stack.isEmpty();
        stack.size();
        // ─────────────────────────────────────────────
        // QUEUE (FIFO)
        // ─────────────────────────────────────────────
        Deque<Integer> queue = new ArrayDeque<>();
        // Queue (FIFO): enqueue at back, dequeue/peek at front
        // enqueue -> offerLast/addLast, dequeue -> pollFirst, front -> peekFirst
        queue.offerLast(1); // enqueue
        queue.pollFirst(); // dequeue (null if empty)
        queue.peekFirst(); // front (null if empty)
        queue.isEmpty();
        queue.size();
        // ─────────────────────────────────────────────
        // DEQUE (double-ended queue)
        // ─────────────────────────────────────────────
        Deque<Integer> dq = new ArrayDeque<>();
        // Quick call pattern:
        // dq.offerFirst(1); dq.addFirst(1); dq.offerLast(2); dq.addLast(2);
        // dq.pollFirst(); dq.peekFirst(); dq.pollLast(); dq.peekLast();
        // insert front: offerFirst/addFirst (offer returns false on failure, add
        // throws)
        dq.offerFirst(1);
        dq.addFirst(1);
        // insert back: offerLast/addLast (offer returns false on failure, add throws)
        dq.offerLast(2);
        dq.addLast(2);
        // front ops: pollFirst removes+returns front (null if empty), peekFirst only
        // views front
        dq.pollFirst();
        dq.peekFirst();
        // back ops: pollLast removes+returns back (null if empty), peekLast only views
        // back
        dq.pollLast();
        dq.peekLast();

        // ─────────────────────────────────────────────
        // PRIORITY QUEUE (min-heap by default)
        // ─────────────────────────────────────────────
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        // custom: sort int[] by second element asc
        PriorityQueue<int[]> pq = new PriorityQueue<>((x, y) -> x[1] - y[1]);
        // custom: sort int[] by first desc, then second asc
        PriorityQueue<int[]> pq2 = new PriorityQueue<>((x, y) -> x[0] != y[0] ? y[0] - x[0] : x[1] - y[1]);
        // custom: Student max-heap by age
        PriorityQueue<Student> studentHeap = new PriorityQueue<>((x, y) -> y.age - x.age);
        minHeap.offer(3); // insert
        minHeap.poll(); // remove min
        minHeap.peek(); // view min (null if empty)
        minHeap.size();
        minHeap.isEmpty();
        minHeap.contains(3);
        touch(maxHeap, pq, pq2, studentHeap);

        // ─────────────────────────────────────────────
        // HASHMAP (unordered map, O(1) avg)
        // ─────────────────────────────────────────────
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1); // insert/update
        map.get("a"); // 1 (null if missing)
        map.getOrDefault("b", 0); // 0
        map.containsKey("a"); // true
        map.containsValue(1); // true
        map.remove("a"); // remove key
        map.size();
        map.isEmpty();
        map.putIfAbsent("c", 3); // only if key missing
        map.merge("a", 1, Integer::sum); // merge: map["a"] += 1 (or put 1 if absent)
        // frequency count pattern
        map.put("x", map.getOrDefault("x", 0) + 1);
        // iterate
        for (Map.Entry<String, Integer> e : map.entrySet())
            System.out.println(e.getKey() + " " + e.getValue());
        for (String key : map.keySet()) {
            touch(key);
        }
        for (Integer val : map.values()) {
            touch(val);
        }
        // sort entries by value
        map.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .forEach(e -> System.out.println(e.getKey() + "=" + e.getValue()));
        // ─────────────────────────────────────────────────────────
        // HASHMAP WITH CUSTOM KEY (uses Point.hashCode + equals)
        // ─────────────────────────────────────────────────────────
        Map<Point, Integer> pointMap = new HashMap<>();
        pointMap.put(new Point(1, 2), 10);
        pointMap.get(new Point(1, 2)); // 10 (works because hashCode+equals overridden)
        pointMap.containsKey(new Point(1, 2)); // true
        // array/list as key workaround — use String encoding
        Map<String, Integer> arrKeyMap = new HashMap<>();
        int[] key = { 1, 2, 3 };
        arrKeyMap.put(Arrays.toString(key), 42); // "1, 2, 3" → 42
        // ─────────────────────────────────────────────
        // TREEMAP (sorted by key, O(log n), like map<> in C++)
        // ─────────────────────────────────────────────
        TreeMap<Integer, String> tmap = new TreeMap<>(); // natural order (asc)
        TreeMap<Integer, String> tmapDesc = new TreeMap<>(Collections.reverseOrder()); // desc
        // custom: sort string keys by length, then lex
        TreeMap<String, Integer> tmapCustom = new TreeMap<>((x, y) -> {
            if (x.length() != y.length())
                return x.length() - y.length();
            return x.compareTo(y);
        });
        // TreeMap with non-Comparable key: use Point.comparator()
        TreeMap<Point, String> pointName = new TreeMap<>(Point.comparator());
        tmap.put(1, "a");
        tmap.get(1);
        tmap.firstKey(); // smallest key
        tmap.lastKey(); // largest key
        tmap.floorKey(3); // largest key <= 3 (null if none)
        tmap.ceilingKey(3); // smallest key >= 3 (null if none)
        tmap.lowerKey(3); // largest key < 3
        tmap.higherKey(3); // smallest key > 3
        tmap.headMap(5); // keys < 5
        tmap.tailMap(5); // keys >= 5
        tmap.subMap(2, 6); // keys in [2,6)
        tmap.subMap(2, true, 6, true); // keys in [2,6]
        tmap.descendingMap();
        tmap.pollFirstEntry(); // remove + return smallest entry
        tmap.pollLastEntry(); // remove + return largest entry
        touch(tmapDesc, tmapCustom, pointName);

        // ─────────────────────────────────────────────
        // HASHSET (unordered, O(1) avg)
        // ─────────────────────────────────────────────
        Set<Integer> set = new HashSet<>();
        set.add(1);
        set.remove(1);
        set.contains(1);
        set.size();
        set.isEmpty();
        set.addAll(List.of(1, 2, 3));
        set.retainAll(other_set); // intersection (keeps only common)
        set.removeAll(other_set); // difference (removes common)
        for (int x : set) {
            touch(x);
        }

        // ─────────────────────────────────────────────
        // TREESET (sorted set, O(log n), like set<> in C++)
        // ─────────────────────────────────────────────
        TreeSet<Integer> tset = new TreeSet<>(); // natural order
        TreeSet<Integer> tsetDesc = new TreeSet<>(Collections.reverseOrder()); // desc
        // custom: sort strings by length then lex
        TreeSet<String> tsetCustom = new TreeSet<>((x, y) -> {
            if (x.length() != y.length())
                return x.length() - y.length();
            return x.compareTo(y);
        });
        // Same pattern: use Point.comparator() for non-Comparable keys
        TreeMap<Point, Integer> tp = new TreeMap<>(Point.comparator());
        tset.add(5);
        tset.add(3);
        tset.add(8);
        tset.first(); // smallest (3)
        tset.last(); // largest (8)
        tset.floor(4); // largest <= 4 → 3 (null if none)
        tset.ceiling(4); // smallest >= 4 → 5 (null if none)
        tset.lower(5); // largest < 5 → 3
        tset.higher(5); // smallest > 5 → 8
        tset.headSet(5); // elements < 5
        tset.tailSet(5); // elements >= 5
        tset.subSet(3, 8); // elements in [3,8)
        tset.pollFirst(); // remove + return smallest
        tset.pollLast(); // remove + return largest
        tset.descendingSet();
        // Custom comparator set (e.g., reverse order)
        TreeSet<Integer> revSet = new TreeSet<>(Collections.reverseOrder());
        // Custom object set (sort by length then lex)
        TreeSet<String> strSet = new TreeSet<>((a1, b1) -> {
            if (a1.length() != b1.length())
                return a1.length() - b1.length();
            return a1.compareTo(b1);
        });
        touch(tsetDesc, tsetCustom, revSet, strSet);

        // ─────────────────────────────────────────────
        // LINKEDLIST (doubly-linked, O(1) insert/delete at ends)
        // ─────────────────────────────────────────────
        LinkedList<Integer> ll = new LinkedList<>();
        ll.addFirst(1);
        ll.addLast(2);
        ll.removeFirst();
        ll.removeLast();
        ll.peekFirst();
        ll.peekLast();
        ll.get(0); // O(n) random access
        // ─────────────────────────────────────────────
        // ITERATOR
        // ─────────────────────────────────────────────
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            int elem = it.next();
            if (elem == 3)
                it.remove(); // safe remove during iteration
        }
        // ListIterator (bidirectional)
        ListIterator<Integer> lit = list.listIterator(list.size()); // start from end
        while (lit.hasPrevious()) {
            lit.previous();
        }
        // ─────────────────────────────────────────────
        // CUSTOM COMPARATORS — all patterns in one place
        // ─────────────────────────────────────────────
        // 1. Sort 2D array: by first col asc, then second col desc
        int[][] matrix = { { 3, 2 }, { 1, 5 }, { 1, 2 } };
        Arrays.sort(matrix, (x, y) -> x[0] != y[0] ? x[0] - y[0] : y[1] - x[1]);
        // result: [[1,5], [1,2], [3,2]]

        // 2. Sort List<int[]>: by sum desc
        List<int[]> pairs = new ArrayList<>(Arrays.asList(new int[] { 1, 3 }, new int[] { 2, 2 }));
        pairs.sort((x, y) -> (y[0] + y[1]) - (x[0] + x[1]));
        // 3. Sort List<String>: by length asc, then lex asc
        List<String> words = new ArrayList<>(List.of("banana", "apple", "fig", "cherry"));
        words.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        // 4. Sort custom objects
        List<Student> students = new ArrayList<>();
        students.add(new Student("Alice", 20));
        students.add(new Student("Bob", 18));
        Collections.sort(students); // uses Student.compareTo (age asc)
        students.sort((x, y) -> y.age - x.age); // override: age desc
        students.sort(Comparator.comparing((Student st) -> st.name)); // by name
        // 5. Multi-key Comparator chain (cleaner syntax)
        students.sort(Comparator.comparingInt((Student st) -> st.age).thenComparing(st -> st.name));
        // 6. TreeMap with custom comparator (sort by string length)
        TreeMap<String, Integer> byLen = new TreeMap<>(
                Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        byLen.put("apple", 1);
        byLen.put("fig", 2);
        byLen.put("cherry", 3);
        // iteration order: fig, apple, cherry
        // 7. TreeSet with custom comparator (intervals sorted by start, then end)
        TreeSet<int[]> intervals = new TreeSet<>((x, y) -> x[0] != y[0] ? x[0] - y[0] : x[1] - y[1]);
        intervals.add(new int[] { 1, 5 });
        intervals.add(new int[] { 1, 3 });
        intervals.add(new int[] { 2, 4 });
        // 8. PriorityQueue: max-heap of int[] by first element
        PriorityQueue<int[]> maxPQ = new PriorityQueue<>((x, y) -> y[0] - x[0]);
        touch(maxPQ);

        // ─────────────────────────────────────────────
        // MATH
        // ─────────────────────────────────────────────
        int p = 3, q = 5;
        Math.max(p, q);
        Math.min(p, q);
        Math.abs(-5); // 5
        Math.pow(2, 10); // 1024.0
        Math.sqrt(16); // 4.0
        Math.log(Math.E); // 1.0 (natural log)
        Math.log10(100); // 2.0
        Math.floor(3.7); // 3.0
        Math.ceil(3.2); // 4.0
        Math.round(3.5); // 4
        int INT_MAX = Integer.MAX_VALUE; // 2^31 - 1 (~2.1 billion)
        int INT_MIN = Integer.MIN_VALUE; // -2^31
        long LONG_MAX = Long.MAX_VALUE; // 2^63 - 1
        gcd(12, 8); // 4 (defined as static method below)
        touch(INT_MAX, INT_MIN, LONG_MAX);

        // ─────────────────────────────────────────────
        // STREAMS (functional operations, like C++ ranges)
        // ─────────────────────────────────────────────
        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        nums.stream().filter(x -> x % 2 == 0) // keep evens
                .map(x -> x * x) // square
                .sorted() // sort
                .distinct() // remove duplicates
                .limit(3) // take first 3
                .collect(Collectors.toList());
        nums.stream().mapToInt(x -> x).sum();
        nums.stream().mapToInt(x -> x).average().getAsDouble();
        nums.stream().reduce(0, Integer::sum);
        nums.stream().count();
        nums.stream().anyMatch(x -> x > 3);
        nums.stream().allMatch(x -> x > 0);
        nums.stream().noneMatch(x -> x < 0);
        nums.stream().min(Comparator.naturalOrder()).get();
        nums.stream().max(Comparator.naturalOrder()).get();
        nums.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        // group by length
        Map<Integer, List<String>> grouped = Stream.of("a", "bb", "cc", "ddd")
                .collect(Collectors.groupingBy(String::length));
        touch(grouped);

        // ─────────────────────────────────────────────
        // USEFUL CONVERSIONS
        // ─────────────────────────────────────────────
        // int[] <-> List<Integer>
        int[] primitive = { 1, 2, 3 };
        List<Integer> boxedList = Arrays.stream(primitive).boxed().collect(Collectors.toList());
        int[] backToPrimitive = boxedList.stream().mapToInt(x -> x).toArray();
        // char[] <-> String
        char[] chars = str.toCharArray();
        String fromChars = new String(chars);
        // int <-> String
        String fromInt = String.valueOf(42);
        int fromStr = Integer.parseInt("42");
        // Integer <-> int (autoboxing handles most cases automatically)
        Integer boxedInt = 5; // auto-box
        int primInt = boxedInt; // auto-unbox
        touch(backToPrimitive, fromChars, fromInt, fromStr, primInt);
    }
}
