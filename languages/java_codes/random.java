import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

public class random {
    public static void main(String[] args) {
        Node.fun();
        // int arr[][] = {
        // { 1, 2, 3 },
        // { 4, 5, 6 },
        // { 7, 8, 9 }
        // };

        // Node head = new Node(0), cur, prev, cache;
        // cache = head;
        // for (int i = 0; i < arr.length; i++) {
        // for (int j = 0; j < arr[i].length; j++) {
        // if (j == 0) {
        // cur = new Node(arr[i][j]);
        // cache = cur;
        // prev.below = cur;
        // prev = prev.next;
        // } else {
        // cur.next = new Node(arr[i][j]);
        // cur = cur.next;
        // prev.below = cur;
        // prev = prev.next;
        // }
        // }
        // prev = cache;
        // }
    }

}

class Node {
    public int val;
    public Node right, down;

    Node(int val) {
        this.val = val;
        right = null;
        down = null;
    }

    Node(int val, Node right, Node down) {
        this.val = val;
        this.down = down;
        this.right = right;
    }

    List<Integer>[] adjList;

    static String s = "the day is sunny the the\n" +
            "the sunny is is";

    static void fun() {
        String[] strArr = s.split("\\s+");
        for (int i = 0; i < strArr.length; i++)
            System.out.printf("%s --- ", strArr[i]);

    }
}