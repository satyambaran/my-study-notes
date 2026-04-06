import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MS_R1 {
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
         * dada daba bada baba
         * adad abad adab abab
         * aadd
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
    public int hashCode() {
        return nodeId;
    }

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
 * Original list:
 * head -> m -> i -> c -> r -> o -> s -> o -> f -> t -> NULL
 * 
 * Modified list:
 * head -> m -> t -> i -> f -> c -> o -> r -> s -> o -> NULL
 */