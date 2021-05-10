package pd.csv;

import static pd.fenc.Cascii.DOUBLE_QUOTE;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;

/**
 * ["a","b","c"] => "a,b,c\r\n"
 */
public class CsvSerializer {

    static HashSet<Integer> getCommas(int comma, String crlf) {
        HashSet<Integer> commas = new HashSet<Integer>();
        commas.add(comma);
        commas.add(DOUBLE_QUOTE);
        if (crlf.length() == 1) {
            commas.add((int) crlf.charAt(0));
        } else {
            OfInt it = crlf.codePoints().iterator();
            while (it.hasNext()) {
                int ch = it.nextInt();
                commas.add(ch);
            }
        }
        return commas;
    }

    public static String serialize(List<String> record, int comma, String crlf) {
        HashSet<Integer> commas = getCommas(comma, crlf);

        StringBuilder sb = new StringBuilder();
        Iterator<String> it = record.iterator();
        while (it.hasNext()) {
            String field = it.next();
            serializeCsvField(field, commas, sb);
            if (it.hasNext()) {
                sb.appendCodePoint(comma);
            }
        }
        sb.append(crlf);
        return sb.toString();
    }

    private static void serializeCsvField(String field, HashSet<Integer> commas, StringBuilder sb) {
        boolean mustQuote = false;
        {
            OfInt it = field.codePoints().iterator();
            while (it.hasNext()) {
                int ch = it.nextInt();
                if (commas.contains(ch)) {
                    mustQuote = true;
                    break;
                }
            }
        }

        if (mustQuote) {
            sb.appendCodePoint(DOUBLE_QUOTE);
        }

        OfInt it = field.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            if (ch == DOUBLE_QUOTE) {
                sb.appendCodePoint(DOUBLE_QUOTE).appendCodePoint(DOUBLE_QUOTE);
            } else {
                sb.appendCodePoint(ch);
            }
        }

        if (mustQuote) {
            sb.appendCodePoint(DOUBLE_QUOTE);
        }
    }
}
