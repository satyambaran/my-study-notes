import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MICROSOFT ROUND 1 — Regex Pattern Matching & Linked List Interleaving
 * =======================================================================
 *
 * PROBLEM 1: LINKED LIST INTERLEAVING Given a singly linked list: a -> b -> c
 * -> d -> e -> f Interleave from both ends: a -> f -> b -> e -> c -> d
 *
 * Approach: - Use a HashMap to track parent (previous) nodes. - Maintain
 * firstNode and lastNode pointers. - Alternate: link first -> last -> next of
 * first -> ...
 *
 * Better approach (without extra space): 1. Find middle using slow/fast
 * pointers. 2. Reverse the second half. 3. Merge the two halves alternately.
 *
 * PROBLEM 2: REGEX STRING VALIDATION Given a regex pattern and text, check if
 * the text matches. Uses Java's Pattern/Matcher API.
 *
 * Java Regex Quick Reference: ^ Start of string $ End of string (?:...)
 * Non-capturing group | Alternation (OR) * Zero or more + One or more ? Zero or
 * one \\d Digit, \\w Word char, \\s Whitespace
 */
public class Microsoft_R1_RegexAndLinkedList {
    public static void main(String[] args) {
        // --- Regex Demo ---
        String regexPattern = "^[a-d]{2,4}$"; // 2-4 chars from {a,b,c,d}
        String[] tests = { "ab", "abcd", "abcde", "ef", "aa" };
        System.out.println("=== Regex Matching ===");
        for (String test : tests) {
            System.out.printf("%-8s -> %s%n", test, matches(regexPattern, test));
        }
        // --- Linked List Interleaving Demo ---
        System.out.println("\n=== Linked List Interleaving ===");
        // Build: m -> i -> c -> r -> o -> s -> o -> f -> t
        String word = "microsoft";
        Node head = buildList(word);
        System.out.print("Original: ");
        printList(head);
        head = interleave(head);
        System.out.print("Interleaved: ");
        printList(head);
        // Expected: m -> t -> i -> f -> c -> o -> r -> s -> o
    }

    static boolean matches(String regex, String text) { return Pattern.compile(regex).matcher(text).matches(); }

    /**
     * Interleave a linked list from both ends using the reverse-and-merge approach.
     */
    static Node interleave(Node head) {
        if (head == null || head.next == null)
            return head;
        // Step 1: Find middle (slow-fast pointer)
        Node slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        // Step 2: Reverse second half
        Node secondHalf = reverse(slow.next);
        slow.next = null; // Cut the list
        // Step 3: Merge alternately
        Node first = head, second = secondHalf;
        while (second != null) {
            Node nextFirst = first.next;
            Node nextSecond = second.next;
            first.next = second;
            second.next = nextFirst;
            first = nextFirst;
            second = nextSecond;
        }
        return head;
    }

    static Node reverse(Node head) {
        Node prev = null, curr = head;
        while (curr != null) {
            Node next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    static Node buildList(String word) {
        Node dummy = new Node(' ');
        Node curr = dummy;
        for (char c : word.toCharArray()) {
            curr.next = new Node(c);
            curr = curr.next;
        }
        return dummy.next;
    }

    static void printList(Node head) {
        while (head != null) {
            System.out.print(head.val);
            if (head.next != null)
                System.out.print(" -> ");
            head = head.next;
        }
        System.out.println();
    }
}

class Node {
    char val;
    Node next;

    Node(char val) {
        this.val = val;
        this.next = null;
    }
}

class MS_R1 {
    public static void main(String[] args) {
        // String s = "acegfdb";
        // // String s = "aceghfdb";
        // // String s = "m";
        // // String s = "";
        // Node head = new Node(' ');
        // Node temp = head;
        // HashMap<Node, Node> parent = new HashMap<>();
        // for (int i = 0; i < s.length(); i++) {
        // Node cur = new Node(s.charAt(i));
        // parent.put(cur, temp);
        // temp.next = cur;
        // temp = cur;
        // }
        // Node lastNode = temp;
        // Node firstNode = head.next;
        // for (int i = 0; i < s.length() / 2; i++) {
        // Node nextFirst = firstNode.next;
        // Node nextLast = parent.get(lastNode);
        // firstNode.next = lastNode;
        // lastNode.next = nextFirst;
        // firstNode = nextFirst;
        // lastNode = nextLast;
        // }
        // firstNode.next = null;
        // while (head != null) {
        // System.out.printf("%c ", head.val);
        // head = head.next;
        // }
        Test t = new Test();
        String regexPattern = "^(?:(?:aa)|(?:bb)|(?:cc)|(?:dd)|(?:ca)|(?:ac)|(?:db)|(?:bd)|(?:abcd)|(?:abdc)|(?:acbd)|(?:acdb)|(?:adbc)|(?:adcb)|(?:bacd)|(?:badc)|(?:bcad)|(?:bcda)|(?:bdac)|(?:bdca)|(?:cabd)|(?:cadb)|(?:cbda)|(?:cbad)|(?:cdab)|(?:cdba)|(?:dabc)|(?:dacb)|(?:dbac)|(?:dbca)|(?:dcab)|(?:dcba))*$";
        /*
         * 
         * 
         * dada daba bada baba adad abad adab abab aadd
         * 
         * 
         * 
         */
        String text = "aabbccdddada";
        t.checker(regexPattern, text);
    }
}

class Node {
    public char val;
    public Node next;
    public int nodeId;
    static int id = 0;

    public Node(char _val, Node _next) {
        val = _val;
        next = _next;
        nodeId = id++;
    }

    public Node(char _val) {
        val = _val;
        next = null;
        nodeId = id++;
    }

    @Override
    public int hashCode() { return nodeId; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        Node other = (Node) obj;
        return other.nodeId == nodeId;
    }
}

class Test {
    public void checker(String regexPattern, String text) {
        Pattern p = Pattern.compile(regexPattern);
        Matcher m = p.matcher(text);
        System.out.println(m.find());
    }
}
/*
 * Original list: head -> m -> i -> c -> r -> o -> s -> o -> f -> t -> NULL
 * 
 * Modified list: head -> m -> t -> i -> f -> c -> o -> r -> s -> o -> NULL
 */
