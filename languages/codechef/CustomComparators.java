import java.util.*;

public class a {
    public static void main(String[] args) {
        int[][] array = {
                { 3, 4, 1 },
                { 5, 1, 2 },
                { 3, 2, 4 },
                { 7, 9, 6 },
                { 5, 8, 3 }
        };
        Arrays.sort(array, new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                if (a[0] < b[0]) {
                    return -1;
                } else if (a[0] == b[0] && a[1] < b[1]) {
                    return -1;
                }
                return 1;
            }
        });
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.printf("%d ", array[i][j]);
            }
            System.out.println();
        }
        Arrays.sort(array, new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                if (a[0] != b[0]) {
                    return Integer.compare(a[0], b[0]);// x<y -> -ve; x==y -> 0; x>y -> +ve;
                } else {
                    return Integer.compare(a[1], b[1]);
                }
            }
        });
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.printf("%d ", array[i][j]);
            }
            System.out.println();
        }
        Arrays.sort(array, new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                if (a[0] != b[0]) {
                    return a[0] - b[0];// x<y -> -ve; x==y -> 0; x>y -> +ve;
                } else {
                    return a[1] - b[1];
                }
            }
        });
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.printf("%d ", array[i][j]);
            }
            System.out.println();
        }
        String[] str = { "satyam", "kundan", "kumar", "baranwal", "gunjan" };
        Arrays.sort(str, new Comparator<String>() {

            @Override
            public int compare(String x, String y) {
                if (x.length() < y.length()) {
                    return 1;
                }
                if (x.charAt(x.length() - 1) > y.charAt(y.length() - 1)) {
                    return 1;
                } else if (x.charAt(x.length() - 1) == y.charAt(y.length() - 1)) {
                    if (x.charAt(0) > y.charAt(0)) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    return -1;
                }
            }
        });
        System.out.println();
        for (int i = 0; i < str.length; i++) {
            System.out.printf("%s ", str[i]);
        }
        System.out.println();


    }
}
