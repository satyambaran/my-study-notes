import java.util.Arrays;
import java.util.Scanner;

public class PlaceCarsOnX_Axis {
    public static void main(String[] args) {
        int n;
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(System.in);
        n = sc.nextInt();
        int[] xCord = new int[n], yCord = new int[n];
        for (int i = 0; i < n; i++) {
            xCord[i] = sc.nextInt();
        }
        for (int i = 0; i < n; i++) {
            yCord[i] = sc.nextInt();
        }
        Arrays.sort(xCord);
        Arrays.sort(yCord);
        int ans = 0;
        for (int i = 0; i < n; i++) {
            xCord[i] -= i;
        }
        for (int i = 0; i < n; i++) {
            yCord[i] -= i;
        }
        int xMed, yMed;
        if (n % 2 == 0) {
            xMed = xCord[n / 2];
            yMed = yCord[n / 2];
        } else {
            xMed = xCord[n / 2] + xCord[(n / 2) - 1];
            yMed = yCord[n / 2] + yCord[(n / 2) - 1];
        }
        for (int i = 0; i < n; i++) {
            ans += Math.abs(xMed - xCord[i]);
            ans += Math.abs(yMed - yCord[i]);
        }
        System.out.println(ans);

        /*
         * p1, p2, p3 , p4 ....
         * k, k+1, k+2, k+3 ....
         * 
         * p1, p2-1, p3-2, p4-3 ....
         * k, k, k, k ....
         * 
         * np1, np2, np3, np4 ....
         * all of them should be equal to k
         */
    }
}