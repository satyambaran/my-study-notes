
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

/*
    In java, we can extend only with one parent, but can implement any number of interface
    public class ArrayList extends AbstractList implements List, RandomAccess, Cloneable, java.io.Serializable
*/
@SuppressWarnings("unused")
public class InnerCode {
    private static final boolean LEETCODE = false;
    private static final boolean ONLINE_JUDGE = false;
    private static int a, b, m, n, i, j, s, t, tot = 0, ans = 0;
    private static double dd;
    private static float ff;

    public static void main(String[] args) {
        // try {
        // FileInputStream fis = new FileInputStream("./bin/input.txt");
        // System.setIn(fis);

        // FileOutputStream fos = new FileOutputStream("./bin/output.txt");
        // PrintStream ps = new PrintStream(fos);
        // System.setOut(ps);

        // FileOutputStream errorFos = new FileOutputStream("./bin/error.txt");
        // PrintStream errorPs = new PrintStream(errorFos);
        // System.setErr(errorPs);

        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        solve();
    }

    private static void solve() {
        Scanner sc = new Scanner(System.in);
        try {
            ff = sc.nextFloat();
            System.out.println(10 * ff);
            t = sc.nextInt();
            while (t-- > 0) {
                // n = sc.nextInt();
                ff = sc.nextFloat();
                String str = sc.next();
                // it will read the next token, basically from first non-space to just before
                // the space
                // Clear the newline character from the buffer after reading integers
                // sc.nextLine();
                // String str = sc.nextLine();// it will read the whole line from the current
                // point
                // // need to call sc.nextLine(); to move the reader to nextline
                System.out.print(str + " ");
                System.out.println(n);
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        } finally {
            sc.close();
        }
    }
}

class ListNode {
    int val;
    ListNode next;

    ListNode() {
    }

    ListNode(int val) {
        this.val = val;
    }

    ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }
}

class Solution {
    public int[][] merge(int[][] v) {
        if (v.length == 0) {
            return new int[0][0];
        }
        ///
        /*
         * int[] k = new int[5];
         * int[] k = {1, 2, 3, 4, 5};
         * k = new int[] { 1, 2, 3, 4, 5 };
         * 
         * List<Integer> list = new ArrayList<>();
         * Collections.sort(list);
         * System.out.println("Sorted List: " + list);
         * 
         * int[] array = {5, 3, 8, 1, 9, 2};
         * Arrays.sort(array);
         * System.out.println("Sorted Array: " + Arrays.toString(array));
         */
        Arrays.sort(v, new Comparator<int[]>() {
            public int compare(int[] a, int[] b) {
                return a[0] - b[0];
            }
        });
        ///
        List<List<Integer>> ans = new ArrayList<>();
        ans.add(Arrays.asList(v[0][0], v[0][1]));
        for (int i = 1; i < v.length; i++) {
            if (ans.get(ans.size() - 1).get(1) < v[i][0]) {
                ans.add(Arrays.asList(v[i][0], v[i][1]));
            } else if (ans.get(ans.size() - 1).get(1) < v[i][1]) {
                ans.get(ans.size() - 1).set(1, v[i][1]);
            }
        }
        int[][] result = new int[ans.size()][2];
        for (int i = 0; i < ans.size(); i++) {
            result[i][0] = ans.get(i).get(0);
            result[i][1] = ans.get(i).get(1);
        }
        return result;
    }

    @SuppressWarnings("unused")
    public ListNode modifiedList(int[] nums, ListNode head) {
        int[] k = new int[5];
        k = new int[] { 1, 2, 3, 4, 5 };
        HashSet<Integer> hs = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            hs.add(nums[i]);
        }
        ListNode temp = head, prev = null;
        while (head != null) {
            System.out.print(temp.val);
            if (hs.contains(temp.val)) {
                if (prev == null) {
                    head = head.next;
                    temp = temp.next;
                } else {
                    prev.next = temp.next;
                }
            } else {
                prev = temp;
            }
            temp = temp.next;
        }
        return head;
    }

    public String addStrings(String a, String b) {
        StringBuilder sb = new StringBuilder();
        int i = a.length() - 1, j = b.length() - 1, carry = 0;
        while (i >= 0 || j >= 0 || carry > 0) {
            int sum = carry;
            if (i >= 0) {
                sum += (a.charAt(i) - '0');
                i--;
            }
            if (j >= 0) {
                sum += (b.charAt(j) - '0');
                j--;
            }
            // ^
            sb.append(sum % 10);
            // ^
            carry = sum / 10;
        }
        return sb.reverse().toString();
    }

    public String multiply(String num1, String num2) {
        /*
         * char[] charArray = { 'H', 'e', 'l', 'l', 'o' };
         * String str = new String(charArray);
         * String str = "Hello";
         * char[] charArray = str.toCharArray();
         */
        if (num2.length() > num1.length())
            return multiply(num2, num1);
        StringBuilder sb = new StringBuilder();
        int n = num1.length(), m = num2.length();
        int[] pos = new int[m + n];
        for (int i = n - 1; i > -1; i--) {
            for (int j = m - 1; j > -1; j--) {
                int a = num1.charAt(i) - '0';
                int b = num2.charAt(j) - '0';
                int p1 = i + j, p2 = i + j + 1;
                int sum = (a * b) + pos[p2];
                pos[p2] = sum % 10;
                pos[p1] += sum / 10;
            }
        }
        for (int i = 0; i < m + n; i++) {
            if (sb.length() != 0 || pos[i] != 0)
                sb.append(pos[i]);
        }
        if (sb.length() == 0) {
            sb.append(0);
        }
        return sb.toString();
    }

    public String discountPrices(String sentence, int discount) {
        StringBuilder sb = new StringBuilder();
        String[] words = sentence.split(" ");
        for (String word : words) {
            sb.append(convert(word, discount) + " ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String convert(String word, int discount) {
        if (word.length() > 11)
            return word;
        if (word.charAt(0) == '$') {
            // ^
            try {
                long price = Long.parseLong(word.substring(1));
                double priceD = price - ((double) price * discount) / 100d;
                StringBuilder sb = new StringBuilder();
                sb.append('$');
                sb.append(String.format("%.2f", priceD));
                return sb.toString();
            } catch (Exception e) {
                return word;
            }
        }
        return word;
    }

    public List<Integer> addToArrayForm(int[] num, int k) {
        // ^
        LinkedList<Integer> ans = new LinkedList<>();
        for (int i = num.length - 1; i > -1; i--) {
            k += num[i];
            ans.addFirst(k % 10);
            k /= 10;
        }
        while (k > 0) {
            ans.addFirst(k % 10);
            k /= 10;
        }
        return ans;
    }

    public int[] createTargetArray(int[] nums, int[] index) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) {
            list.add(index[i], nums[i]);
        }
        int[] ans = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ans[i] = list.get(i);
        }
        return ans;
    }

    public List<Integer> twoOutOfThree(int[] nums1, int[] nums2, int[] nums3) {
        HashSet<Integer> hs1 = new HashSet<>();
        HashSet<Integer> hs2 = new HashSet<>();
        HashSet<Integer> hs3 = new HashSet<>();
        HashSet<Integer> hs4 = new HashSet<>();
        for (int i = 0; i < nums1.length; i++) {
            hs1.add(nums1[i]);
            // hs4.add(nums1[i]);
        }
        for (int i = 0; i < nums2.length; i++) {
            hs2.add(nums2[i]);
            if (hs1.contains(nums2[i])) {
                hs4.add(nums2[i]);
            }
        }
        for (int i = 0; i < nums3.length; i++) {
            hs3.add(nums3[i]);
            if (hs1.contains(nums3[i]) || hs2.contains(nums3[i])) {
                hs4.add(nums3[i]);
            }
        }
        return new ArrayList<>(hs4);
    }

    public int findKOr(int[] nums, int k) {
        int ans = 0, tot = 0, tott = nums.length, l = 0;
        while (tott >= k) {
            tot = 0;
            tott = 0;
            for (int i = 0; i < nums.length; i++) {
                if (nums[i] % 2 == 1) {
                    tot++;
                }
                nums[i] /= 2;
                if (nums[i] > 0) {
                    tott++;
                }
            }
            System.out.printf("%d %d", tot, tott);
            if (tot >= k) {
                ans |= (1 << l);
            }
            l++;
        }
        return ans;
    }

    int[] di = { 0, -1, 0, 1 };
    int[] dj = { -1, 0, 1, 0 };

    public int[][] highestPeak(int[][] isWater) {
        int n = isWater.length, m = isWater[0].length;
        // int[][] ans = new int[n][m];
        // List<Integer> list = new ArrayList<>(Collections.nCopies(n, -1));
        // Queue<int[]> q = new LinkedList<>();
        Queue<int[]> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (isWater[i][j] == 1) {
                    q.offer(new int[] { i, j });
                    isWater[i][j] = 0;
                } else {
                    isWater[i][j] = -1;
                }
            }
        }
        int level = 0;
        while (q.size() > 0) {
            int len = q.size();
            while (len-- > 0) {
                int[] cur = q.poll();
                for (int i = 0; i < 4; i++) {
                    int ni = cur[0] + di[i], nj = cur[1] + dj[i];
                    if (ni >= 0 && nj >= 0 && ni < n && nj < m) {
                        if (isWater[ni][nj] == -1) {
                            isWater[ni][nj] = level;
                            q.offer(new int[] { ni, nj });
                        }
                    }
                }
            }
            level++;
        }
        return isWater;
    }

    public List<Integer> luckyNumbers(int[][] matrix) {
        List<Integer> lst = new ArrayList<>();
        int n = matrix.length, m = matrix[0].length;
        int[] rows = new int[n], cols = new int[m];
        for (int i = 0; i < n; i++) {
            rows[i] = Integer.MAX_VALUE;
        }
        for (int i = 0; i < m; i++) {
            cols[i] = 0;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                rows[i] = Math.min(rows[i], matrix[i][j]);
                cols[j] = Math.min(cols[j], matrix[i][j]);
            }
        }
        HashSet<Integer> hs = new HashSet<>();
        for (int k : rows) {
            hs.add(k);
        }
        for (int i = 0; i < cols.length; i++) {
            if (hs.contains(cols[i])) {
                lst.add(cols[i]);
            }
        }
        return lst;
    }

    boolean check(int[] stations, long mid, int r, int k) {
        int n = stations.length;
        int[] temp = new int[n];
        long sum = 0;
        for (int i = 0; i < n; i++) {
            temp[i] = stations[i];
            if (i < r) {
                sum += temp[i];
            }
        }
        for (int i = 0; i < n; i++) {
            sum -= (i - r > 0 ? temp[i - r - 1] : 0);
            sum += (i + r < n ? temp[i + r] : 0);
            if (sum < mid) {
                if (mid - sum < k) {
                    return false;
                }
                temp[Math.min(i + r, n - 1)] += (mid - sum);
                k -= (mid - sum);
                sum = mid;
            }
        }
        return true;
    }

    public long maxPower(int[] stations, int r, int k) {
        long ans = 0, min = 0, max, mid;
        for (int i = 0; i < stations.length; i++) {
            ans += stations[i];
        }
        max = ans + k;
        while (min <= max) {
            mid = min + (max - min) / 2;
            if (check(stations, mid, r, k)) {
                min = mid + 1;
                ans = mid;
            } else {
                max = mid - 1;
            }
        }
        return ans;
    }

    public class FrequencyMapExample {
        public static void main(String[] args) {
            List<Integer> numbers = Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 4, 4);

            // Step 1: Create a frequency map for each integer
            Map<Integer, Integer> countMap = new HashMap<>();
            for (int num : numbers) {
                countMap.put(num, countMap.getOrDefault(num, 0) + 1);
            }

            // Step 2: Create a TreeMap to store frequencies and their corresponding lists
            TreeMap<Integer, List<Integer>> freqMap = new TreeMap<>();

            // Step 3: Populate the TreeMap
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                int num = entry.getKey();
                int frequency = entry.getValue();

                // Use computeIfAbsent to add the number to the list for its frequency
                freqMap.computeIfAbsent(frequency, k -> new ArrayList<>()).add(num);
            }

            // Print the resulting TreeMap
            System.out.println(freqMap);
        }
    }

    public int sumDistance(int[] nums, String s, int d) {
        final int mod = (int) 1e9 + 7;
        int n = nums.length;
        int tot = 0, ans = 0;
        for (int i = 0; i < n; i++) {
            if (s.charAt(i) == 'R')
                nums[i] += d;
            else
                nums[i] -= d;
            tot = (tot + nums[i]) % mod;
        }
        Arrays.sort(nums);
        for (int i = 0; i < n; i++) {
            tot = (tot - nums[i]) % mod;
            ans = ans - (nums[i] * (n - 1 - i)) % mod;
            ans = ans - (tot * (n - 1 - i)) % mod;
        }
        return ans;
    }

    public long makeSubKSumEqual(int[] arr, int k) {
        long ans = 0l, tot = 0l;
        int n = arr.length;
        for (int i = 0; i < k; i++) {
            tot = 0l;
            for (int j = i; j < n; j += k) {
                tot += (long) arr[j];
            }
            if (tot % k > k - (tot % k)) {
                tot -= (tot % k);
            }
            tot /= k;
            for (int j = i; j < n; j += k) {
                ans += Math.abs(tot - arr[j]);
            }
        }
        return ans;
    }

    public int kthSmallest(int[][] mat, int k) {
        int sum = 0, n = mat.length, tot = 0;
        TreeMap<Integer, Integer> mp = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            sum += mat[i][0];
            for (int j = 0; j < k; j++) {
                if (tot < k - 1) {
                    tot++;
                    if (mp.containsKey(mat[i][j] - mat[i][0])) {
                        mp.put(mat[i][j] - mat[i][0], mp.get(mat[i][j] - mat[i][0]) + 1);
                    } else {
                        mp.put(mat[i][j] - mat[i][0], 1);
                    }
                } else if (mp.lastKey() <= (mat[i][j] - mat[i][0])) {
                    break;
                } else {
                    if (mp.containsKey(mat[i][j] - mat[i][0])) {
                        mp.put(mat[i][j] - mat[i][0], mp.get(mat[i][j] - mat[i][0]) + 1);
                    } else {
                        mp.put(mat[i][j] - mat[i][0], 1);
                    }
                    if (mp.get(mp.lastKey()) == 1) {
                        mp.remove(mp.lastKey());
                    } else {
                        mp.put(mp.lastKey(), mp.lastKey() - 1);
                    }
                }
            }
        }
        return sum + mp.get(mp.lastKey());
    }

    public int maxTotalReward(int[] reward) {
        Arrays.sort(reward);
        TreeSet<Integer> values = new TreeSet<>();
        int n = reward.length;
        for (int i = 0; i < n; i++) {
            Integer k = values.lower(reward[i]);
            values.add(reward[i] + k);
        }
        return values.last();
    }
}

class Bitset {
    StringBuilder sb, fsb;
    int ones, size;

    public Bitset(int _size) {
        size = _size;
        for (int i = 0; i < size; i++) {
            sb.append('0');
            fsb.append('1');
        }
        ones = 0;
    }

    public void fix(int idx) {
        if (sb.charAt(idx) == '0') {
            ones++;
            sb.setCharAt(idx, (char) 1);
            fsb.setCharAt(idx, (char) 0);
        }
    }

    public void unfix(int idx) {
        if (sb.charAt(idx) == '1') {
            ones--;
            sb.setCharAt(idx, (char) 0);
            fsb.setCharAt(idx, (char) 1);
        }
    }

    public void flip() {
        StringBuilder temp = sb;
        sb = fsb;
        fsb = temp;
    }

    public boolean all() {
        return ones == size;
    }

    public boolean one() {
        return ones > 0;
    }

    public int count() {
        return ones;
    }

    public String toString() {
        return sb.toString();
    }
}

class UndergroundSystem {
    class Passenger {
        int id, startTime, endTime;
        String startStation, endStation;

        Passenger(int id, String startStation, int startTime) {
            this.id = id;
            this.startStation = startStation;
            this.startTime = startTime;
        }
    }

    HashMap<String, HashMap<String, int[]>> stations;
    HashMap<Integer, Passenger> passengers;

    public UndergroundSystem() {
        stations = new HashMap<>();
        passengers = new HashMap<>();
    }

    public void checkIn(int id, String stationName, int t) {
        Passenger passenger = new Passenger(id, stationName, t);
        passengers.put(id, passenger);
    }

    public void checkOut(int id, String stationName, int t) {
        Passenger passenger = passengers.get(id);
        String startStation = passenger.startStation;
        int startTime = passenger.startTime;
        if (!stations.containsKey(startStation)) {
            stations.put(startStation, new HashMap<>());
        }
        HashMap<String, int[]> neighMap = stations.get(startStation);
        if (!neighMap.containsKey(stationName)) {
            neighMap.put(stationName, new int[] { 0, 0 });
        }
        int[] temp = neighMap.get(stationName);
        temp[0] += 1;
        temp[1] += (t - startTime);
        passengers.remove(id);
    }

    public double getAverageTime(String startStation, String endStation) {
        int[] temp = stations.get(startStation).get(endStation);
        return (1.0 * temp[1]) / (1.0 * temp[0]);
    }

    public int getHash(String startStation, String endStation) {
        return (int) (startStation + "#" + endStation).hashCode();
    }
}
