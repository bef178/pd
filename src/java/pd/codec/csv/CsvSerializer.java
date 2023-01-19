package pd.codec.csv;

import static pd.codec.csv.CsvDeserializer.CRLF;
import static pd.util.AsciiUtil.COMMA;
import static pd.util.AsciiUtil.CR;
import static pd.util.AsciiUtil.DOUBLE_QUOTE;
import static pd.util.AsciiUtil.LF;

import java.util.List;
import java.util.PrimitiveIterator.OfInt;

class CsvSerializer {

    public static String serialize(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            serializeField(field, sb);
            sb.appendCodePoint(COMMA);
        }
        sb.setLength(sb.length() - 1);
        sb.append(CRLF);
        return sb.toString();
    }

    private static void serializeField(String field, StringBuilder out) {
        StringBuilder sb = new StringBuilder();
        boolean shouldQuote = false;

        OfInt it = field.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            switch (ch) {
                case DOUBLE_QUOTE:
                    shouldQuote = true;
                    sb.appendCodePoint(DOUBLE_QUOTE).appendCodePoint(DOUBLE_QUOTE);
                    break;
                case COMMA:
                case CR:
                case LF:
                    shouldQuote = true;
                    sb.appendCodePoint(ch);
                    break;
                default:
                    sb.appendCodePoint(ch);
                    break;
            }
        }

        if (shouldQuote) {
            out.appendCodePoint(DOUBLE_QUOTE).append(sb).appendCodePoint(DOUBLE_QUOTE);
        } else {
            out.append(sb);
        }
    }
}
