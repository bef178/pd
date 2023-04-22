package pd.codec.csv;

import java.util.List;
import java.util.PrimitiveIterator.OfInt;

import pd.util.AsciiExtension;

class CsvSerializer {

    public static String serialize(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            serializeField(field, sb);
            sb.appendCodePoint(AsciiExtension.COMMA);
        }
        sb.setLength(sb.length() - 1);
        sb.append(CsvDeserializer.CRLF);
        return sb.toString();
    }

    private static void serializeField(String field, StringBuilder out) {
        StringBuilder sb = new StringBuilder();
        boolean shouldQuote = false;

        OfInt it = field.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            switch (ch) {
                case AsciiExtension.DOUBLE_QUOTE:
                    shouldQuote = true;
                    sb.appendCodePoint(AsciiExtension.DOUBLE_QUOTE).appendCodePoint(AsciiExtension.DOUBLE_QUOTE);
                    break;
                case AsciiExtension.COMMA:
                case AsciiExtension.CR:
                case AsciiExtension.LF:
                    shouldQuote = true;
                    sb.appendCodePoint(ch);
                    break;
                default:
                    sb.appendCodePoint(ch);
                    break;
            }
        }

        if (shouldQuote) {
            out.appendCodePoint(AsciiExtension.DOUBLE_QUOTE).append(sb).appendCodePoint(AsciiExtension.DOUBLE_QUOTE);
        } else {
            out.append(sb);
        }
    }
}
