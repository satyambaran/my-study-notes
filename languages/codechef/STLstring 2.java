import java.util.*;

public class STLstring {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        char[] charArray = { 'H', 'e', 'l', 'l', 'o' };
        for (char c : charArray) {
            System.out.print(c + " ");
        }
        char c = charArray[0]; // H
        charArray[0] = 'J'; // J e l l o
        int length = charArray.length; // 5
        char[] subArray = Arrays.copyOfRange(charArray, 1, 3); // e l
        Arrays.sort(charArray);

        char[] newArray = new char[charArray.length + 1];
        System.arraycopy(charArray, 0, newArray, 0, 2);
        newArray[2] = 'a'; // Insert 'a' at index 2
        System.arraycopy(charArray, 2, newArray, 3, charArray.length - 2);
        System.out.println(newArray);

        String str = new String("Hello");
        for (int i = 0; i < str.length(); i++) {
            System.out.print(str.charAt(i) + " "); // H e l l o
        }
        c = str.charAt(0);
        length = str.length();
        String subStr = str.substring(1, 3);

        charArray = str.toCharArray();
        Arrays.sort(charArray);

        str = new String(charArray);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sb.length(); i++) {
            System.out.print(sb.charAt(i) + " "); // H e l l o
        }
        length = sb.length();
        sb.append(", there");
        sb.insert(2, 'a'); // Heallo
        c = sb.charAt(0); // H
        sb.setCharAt(0, 'J'); // Jello
        subStr = sb.substring(1, 3); // ! substring on strongbuilder returns a string

    }
}
/*
 * ### Summary of Common Methods
 * 
 * | Operation | `char[]` | `String` | `StringBuilder` |
 * |------------|-------------------------|----------------------------------|--
 * --------------------------|
 * | Iterate | `for (char c : arr)` | `for (int i=0; i<str.length(); i++)` |
 * `for (int i=0; i<sb.length(); i++)` |
 * | Insert | Manual via new array | Not directly; create new string |
 * `sb.insert(index, char)` |
 * | Get | `arr[index]` | `str.charAt(index)` | `sb.charAt(index)` |
 * | Set | `arr[index] = 'x'` | Not directly; create new string |
 * `sb.setCharAt(index, 'x')` |
 * | Length | `arr.length` | `str.length()` | `sb.length()` |
 * | Substring | `Arrays.copyOfRange(arr, start, end)` | `str.substring(start,
 * end)` | `sb.substring(start, end)` |
 * | Sort | `Arrays.sort(arr)` | Convert to `char[]` and sort | Convert to
 * `char[]` and sort|
 * 
 */