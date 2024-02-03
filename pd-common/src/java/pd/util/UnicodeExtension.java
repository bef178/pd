package pd.util;

import static pd.fenc.ScalarPicker.EOF;

public class UnicodeExtension {

    public static String toString(int ch) {
        if (ch == EOF) {
            return "EOF";
        }
        return new String(Character.toChars(ch));
    }
}
