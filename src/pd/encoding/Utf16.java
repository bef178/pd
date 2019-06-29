package pd.encoding;

/**
 * All about Unicode codepoint.<br/>
 */
public class Utf16 {

    public static int decodeBe(char[] utf16) {
        assert utf16 != null && utf16.length > 0;
        if (isFollowerSurrogate(utf16[0])) {
            throw new IllegalArgumentException();
        } else if (!isLeaderSurrogate(utf16[0])) {
            return utf16[0];
        } else if (!isFollowerSurrogate(utf16[1])) {
            throw new IllegalArgumentException();
        } else {
            int codepoint = utf16[0] & 0xFFFF & ~0xD800;
            codepoint <<= 10;
            codepoint |= utf16[1] & 0xFFFF & ~0xDC00;
            codepoint += 0x10000;
            return codepoint;
        }
    }

    public static char[] encodeBe(int codepoint) {
        if (codepoint < 0) {
            throw new IllegalArgumentException();
        } else if (codepoint <= 0xD7FF) {
            return new char[] { (char) codepoint };
        } else if (codepoint <= 0xDFFF) {
            throw new IllegalArgumentException();
        } else if (codepoint <= 0xFFFF) {
            return new char[] { (char) codepoint };
        } else if (codepoint <= 0x10FFFF) {
            codepoint -= 0x10000; // 20 bits in [0, 0xFFFFF]
            return new char[] {
                    (char) ((codepoint >> 10) & 0x03FF | 0xD800),
                    (char) (codepoint & 0x03FF | 0XDC00)
            };
        } else {
            return null;
        }
    }

    public static boolean isFollowerSurrogate(char codeunit) {
        return codeunit >= 0xDC00 && codeunit <= 0xDFFF;
    }

    public static boolean isLeaderSurrogate(char codeunit) {
        return codeunit >= 0xD800 && codeunit <= 0xDBFF;
    }
}
