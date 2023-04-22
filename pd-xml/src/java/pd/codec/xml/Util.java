package pd.codec.xml;

import java.util.PrimitiveIterator;

public class Util {

    public static void serializeName(String namespacePrefix, String name, StringBuilder sb) {
        if (namespacePrefix != null) {
            sb.append(namespacePrefix).append(':');
        }
        sb.append(name);
    }

    public static String serializeToQuotedString(String s) {
        final int BACK_SLASH = '\\';
        final int QUOTE = '\"';
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(QUOTE);
        PrimitiveIterator.OfInt it = s.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            if (ch == BACK_SLASH) {
                sb.append(BACK_SLASH).append(BACK_SLASH);
            } else if (ch == QUOTE) {
                sb.append(BACK_SLASH).append(QUOTE);
            } else {
                sb.appendCodePoint(ch);
            }
        }
        sb.appendCodePoint(QUOTE);
        return sb.toString();
    }
}
