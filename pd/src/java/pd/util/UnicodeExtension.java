package pd.util;

import java.util.Arrays;

import static pd.util.AsciiExtension.EOF;

public class UnicodeExtension {

    public static String toString(int ch) {
        if (ch == EOF) {
            return "EOF";
        }
        return new String(Character.toChars(ch));
    }

    public static String toString(int ch, int n) {
        int[] a = new int[n];
        Arrays.fill(a, ch);
        return new String(a, 0, a.length);
    }

    public static String toString(int... a) {
        return new String(a, 0, a.length);
    }
}
