/*
 * Click `Run` to execute the snippet below!
 */

import java.io.*;
import java.util.*;

/*
 * To execute Java, please define "static void main" on a class
 * named Solution.
 *
 * If you need more classes, simply define them inline.
 */

// all the methods should work in O(1) time
// getValue(int index)
// setValue(int index, int value)
// setAllValues(int value)
/*
    keep a default value
    and a data versioning

    setAllValues(val){
      defaultValue=value;
      version++;
    }
    setValue(key, val){
      hashMap[key] = {val,version}
    }
    getValue(key){
      {val, valVersion} = hashaMap[key]
      if(valVersion<version){
        return val
      }else{
        return defaultVal;
      }
    }
 */
class Node {
    public int val;
    public int version;

    Node(int _val, int _version) {
        val = _val;
        version = _version;
    }
}

// space: O(1), time: O(1) in setAllValues
class HashMapV3 {
    HashMap<Integer, Node> hashMap;
    Integer defaultValue;
    Integer version;

    HashMapV3() {
        hashMap = new HashMap<>();
        version = 1;
        defaultValue = null;
    }

    public void setAllValues(int value) {
        defaultValue = value;
        version++;
    }

    public void setValue(int key, int val) {
        hashMap.put(key, new Node(val, version));
    }

    public Integer getValue(int key) {
        if (hashMap.containsKey(key)) {
            Node node = hashMap.get(key);
            if (node.version < version) {
                return defaultValue;
            } else {
                return node.val;
            }
        } else {
            return null;
        }
    }

}
// space: O(1), time: O(n) in setAllValues
class HashMapV2 {
    Set<Integer> existingKeys;
    HashMap<Integer, Integer> updatedMap;
    Integer defaultVal;
    boolean flag = false;

    HashMapV2() {
        existingKeys = new HashSet<>();
        updatedMap = new HashMap<>();
        defaultVal = null;
    }

    Integer getValue(int key) {
        if (flag) {
            if (updatedMap.containsKey(key)) {
                return defaultVal;
            }
        }
        if (updatedMap.containsKey(key)) {
            return updatedMap.get(key);
        }
        return null;
    }

    /*
     * setAllValues(5)
     * setValue(7, 8)-> flag=false;
     * getValue(6) -> 5
     * 
     */
    void setValue(int key, int val) {
        updatedMap.put(key, val);
        flag = false;
    }

    void setAllValues(int val) {
        existingKeys.addAll(updatedMap.keySet());
        updatedMap.clear();
        defaultVal = val;
        flag = true;
    }
    /*
     * after setAllValues
     * return the value from newly called setValue
     * return defaultVal
     */
}

class Solution {
    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("Hello, World!");
        strings.add("Welcome to CoderPad.");
        strings.add("This pad is running Java " + Runtime.version().feature());

        for (String string : strings) {
            System.out.println(string);
        }
    }
}

// Your previous Plain Text content is preserved below:

// This is just a simple shared plaintext pad, with no execution capabilities.

// When you know what language you'd like to use for your interview,
// simply choose it from the dots menu on the tab, or add a new language
// tab using the Languages button on the left.

// You can also change the default language your pads are created with
// in your account settings: https://app.coderpad.io/settings

// Enjoy your interview!