public class MS_R3 {

}
// you can also use imports, for example:
// import java.util.*;

class Solution {

    public static void main(String[] args) {
        // you can write to stdout for debugging purposes, e.g.
        System.out.println("This is a debug message");
        traverse(root, Integer.MIN_VALUE);
    }

    int preceedingValue = INT_MIN;

    void traverse(int value, Node root, int minSoFar = INT_MIN){
        if(root==null) return;
        if(root.val==value){
            preceedingValue = getPreceedingValue(root, minSoFar);
            return;
        } else if(root.val>value) {
            traverse(value, root.left, minSoFar);
        } else {
            traverse(value, root.right, root.val);
        }
    }

    int getPreceedingValue(Node root, int minSoFar) {
        if (root.left == null)
            return minSoFar;
        else {
            Node leftNode = root.left;
            while (leftNode.right != null) {
                leftNode = leftNode.right;
            }
            return leftNode.val;
        }
    }
}

class Node {
    Node left;
    Node right;
    int val;

}