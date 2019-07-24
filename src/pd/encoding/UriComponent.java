package pd.encoding;

import java.io.UnsupportedEncodingException;
import java.util.PrimitiveIterator.OfInt;

import pd.ctype.Ctype;

/**
 * use percent-encoding if not safe<br/>
 * https://tools.ietf.org/html/rfc3986<br/>
 */
public final class UriComponent {

    public static CharSequence decode(CharSequence cs) {
        StringBuilder sb = new StringBuilder();
        OfInt it = cs.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            if (ch == '%') {
                int v0 = Hexdig.decode(it.nextInt());
                int v1 = Hexdig.decode(it.nextInt());
                sb.appendCodePoint((v0 << 4) | v1);
            } else {
                sb.appendCodePoint(ch);
            }
        }
        return sb;
    }

    public static CharSequence encode(String s) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = null;
        try {
            bytes = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        for (byte aByte : bytes) {
            if (isSafe(aByte)) {
                sb.appendCodePoint(aByte);
            } else {
                sb.append('%');
                sb.appendCodePoint(Hexdig.encode(aByte >>> 4));
                sb.appendCodePoint(Hexdig.encode(aByte & 0x0F));
            }
        }
        return sb;
    }

    private static boolean isSafe(byte aByte) {
        if (aByte >= 0 && aByte < 0x100) {
            // unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
            if (Ctype.isAlpha(aByte) || Ctype.isDigit(aByte)) {
                return true;
            }
            switch (aByte) {
                case '-':
                case '.':
                case '_':
                case '~':
                    return true;
            }
            return false;
        }
        throw new IllegalArgumentException();
    }

    private UriComponent() {
        // dummy
    }
}
