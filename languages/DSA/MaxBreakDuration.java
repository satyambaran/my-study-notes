import java.util.*;

class Node {
    public static int id_gen = 1; // Static variable to generate unique IDs
    public int id; // Instance variable for storing node ID
    public Node next; // Reference to the next node

    Node() {
        id = id_gen++; // Assign unique ID to the node and increment the ID generator
    }

    public void setNext(Node _next) {
        next = _next; // Set the next node reference
    }
}

class Solution {
    public int halveArray(int[] nums) {
        int n = 50;
        int tot = n * (n + 1) / 2;
        int k = (tot - 1252 - 1) / 2;
        // 1252 + 2*k+1 = n*(n+1)/2;
        // n*(n+1) = 2504+4*k+2;
        // n*n + n = 2506+4*k;
        // int a = 1, b = 1, c = -2506 - 4 * k;
        // n = (sqrt(b^2 - 4*a*c)-b)/2;
        // n = (int) Math.sqrt(1 + 16 * k + 10024) - 1 / 2;
        // 100^2=10000
        // 101^2=10201
        // 10201-10024=177=1+16*k=>
        // k=11
        // n*n+n = 2506+4*k=2550 = 50*51
        // n*(n+1)=50*51
        // n=50

        int ans = 0;
        PriorityQueue<Double> pq = new PriorityQueue<>(new Comparator<Double>() {
            @Override
            public int compare(Double a, Double b) {
                if (a > b) {
                    return -1;
                }
                return 1;
            }
        });
        double sum = 0d, totSum;
        for (int i = 0; i < nums.length; i++) {
            pq.add((double) nums[i]);
            sum += (double) nums[i];
        }
        totSum = sum;
        while (2 * sum > totSum) {
            Double cur = pq.poll();
            sum -= (cur / 2.0);
            pq.offer(cur / 2.0);
            ans++;
        }

        return ans;
    }
}

class Gap {
    public int start;
    public int end;
    public int left;
    public int right;

    Gap(int start, int end, int k) {
        this.start = start;
        this.end = end;
        this.left = k + 2;
        this.right = k + 2;
    }

    public int getGap() {
        return end - start;
    }

    public void setRight(int _r) {
        right = _r;
    }

    public void setLeft(int _l) {
        left = _l;
    }

    @Override
    public String toString() {
        return String.format("start: %s, end: %s, left: %s, right: %s", start, end, left, right);
    }
}

public class MaxBreakDuration {
    public static int maxRectSum(int[][] rect) {
        int n = rect.length, m = rect[0].length, ans = 0;
        int maxAns = 0, l = 0, r = 0;
        while (l < m) {
            r = l;
            int[] row = new int[n];
            while (r < m) {
                for (int i = 0; i < n; i++) {
                    row[i] += rect[i][r];
                }
                int curSum = 0;
                for (int i = 0; i < n; i++) {
                    curSum += row[i];
                    if (curSum < 0) {
                        curSum = 0;
                    }
                    maxAns = Math.max(maxAns, curSum);
                }
                r++;
            }
            l++;
        }
        return maxAns;
    }

    public static int findMaxBreakDuration(int n, int k, int t, int[] start, int[] end) {
        int maxBreak = 0;
        List<Gap> gaps = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (i == 0) {
                if (start[i] == 0) {
                } else {
                    gaps.add(new Gap(0, start[i], k));
                }
            } else {
                gaps.add(new Gap(end[i - 1], start[i], k));
            }
            if (i == n - 1 && end[i] < t) {
                gaps.add(new Gap(end[i], t, k));
            }
        }
        // for (int i = 0; i < gaps.size(); i++) {
        // System.out.println(gaps.get(i).toString());
        // }
        for (int i = 0; i < gaps.size(); i++) {
            if (i > 0 && gaps.get(i - 1).getGap() == 0) {
                gaps.get(i).setRight(gaps.get(i - 1).right - 1);
            } else {
                int j = i + 1;
                Gap gap;
                while (j < gaps.size()) {
                    gap = gaps.get(j);
                    if (gap.end == gap.start) {
                        j++;
                    } else {
                        break;
                    }
                }
                if (j == gaps.size()) {
                    gaps.get(i).setRight(k + 2);
                } else {
                    gaps.get(i).setRight(j - i);
                }
            }
            if (i > 0 && gaps.get(i - 1).getGap() == 0) {
                gaps.get(i).setLeft(gaps.get(i - 1).left + 1);
            } else {
                Gap gap;
                int j = i - 1;
                while (j > -1) {
                    gap = gaps.get(j);
                    if (gap.end == gap.start) {
                        j--;
                    } else {
                        break;
                    }
                }
                if (j == -1) {
                    gaps.get(i).setLeft(k + 2);
                } else {
                    gaps.get(i).setLeft(i - j);
                }
            }
        }
        // for (int i = 0; i < gaps.size(); i++) {
        // System.out.println(gaps.get(i).toString());
        // }
        Iterator<Gap> it = gaps.iterator();
        while (it.hasNext()) {
            Gap gap = it.next();
            if (gap.left + gap.right <= k) {
                maxBreak = Math.max(maxBreak, gap.end - gap.start + 2);
            } else {
                if (gap.left <= k) {
                    maxBreak = Math.max(maxBreak, gap.end - gap.start + 1);
                } else if (gap.right <= k) {
                    maxBreak = Math.max(maxBreak, gap.end - gap.start + 1);
                } else {
                    maxBreak = Math.max(maxBreak, gap.end - gap.start);
                }
            }
        }
        return maxBreak;
    }

    public static void main(String[] args) {
        int[][] rect = { { 1, 2, -1, -4, -20 },
                { -8, -3, 4, 2, 1 },
                { 3, 8, 10, 1, 3 },
                { -4, -1, 1, 7, -6 } };
        System.out.println(maxRectSum(rect));
        int n; // Number of intervals
        int k = 1; // Number of intervals we can shift
        int t = 15;
        int[] start1 = { 0, 4, 6, 7 }; // Start times
        int[] end1 = { 1, 5, 7, 8 }; // End times
        n = start1.length;
        System.out.println("Maximum Break Duration: " + findMaxBreakDuration(n, k, t, start1, end1));
        k = 2;
        System.out.println("Maximum Break Duration: " + findMaxBreakDuration(n, k, t, start1, end1));
        int[] start2 = { 0, 4, 6, 7, 8, 11 }; // Start times
        int[] end2 = { 4, 6, 7, 8, 11, 15 }; // End times
        n = start2.length;
        System.out.println("Maximum Break Duration: " + findMaxBreakDuration(n, k, t, start2, end2));
        int[] start3 = { 0, 4, 6, 7, 10, 12 }; // Start times
        int[] end3 = { 4, 6, 7, 8, 11, 15 }; // End times
        n = start3.length;
        System.out.println("Maximum Break Duration: " + findMaxBreakDuration(n, k, t, start3, end3));
    }
}