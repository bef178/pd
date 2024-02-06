package pd.util;

import static pd.util.AsciiExtension.EOF;

public class UnicodeExtension {

    public static String toString(int ch) {
        if (ch == EOF) {
            return "EOF";
        }
        return new String(Character.toChars(ch));
    }
}
