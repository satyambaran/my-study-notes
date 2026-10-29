import java.util.*;

public class InputParser {
    static class Input {
        Scanner sc = new Scanner(System.in);

        int nextInt() { return sc.nextInt(); }
        String token() { return sc.next(); }

        // Ignore separators, then keep exactly n characters, including internal spaces.
        String fixed(int n) {
            sc.skip("\\s*");
            String value = sc.findWithinHorizon("(?s).{" + n + "}", 0);
            if (value == null) throw new InputMismatchException("String is too short");
            return value;
        }

        // Return the next non-empty line.
        String line() {
            while (sc.hasNextLine()) {
                String value = sc.nextLine().trim();
                if (!value.isEmpty()) return value;
            }
            return "";
        }
    }

    public static void main(String[] args) {
        Input in = new Input();

        int x = in.nextInt();
        String[] text = new String[x];
        for (int i = 0; i < x; i++) {
            int length = in.nextInt();
            text[i] = in.fixed(length);
        }

        int n = in.nextInt(), m = in.nextInt(), sum = 0;
        int[][] matrix = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                sum += matrix[i][j] = in.nextInt();
            }
        }

        int b = in.nextInt();
        String[] lines = new String[b];
        for (int i = 0; i < b; i++) lines[i] = in.line();

        System.out.println("text: " + Arrays.toString(text));
        for (int[] row : matrix) System.out.println(Arrays.toString(row));
        System.out.println("sum: " + sum);
        System.out.println("lines: " + Arrays.toString(lines));
    }
}
