package pd.csv;

import static pd.csv.CsvSerializer.getCommas;
import static pd.fenc.Cascii.DOUBLE_QUOTE;
import static pd.fenc.IReader.EOF;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import pd.fenc.CharReader;

public class CsvDeserializer {

    public static List<String> deserialize(String raw, int comma, String crlf) {
        HashSet<Integer> commas = getCommas(comma, crlf);
        CharReader src = new CharReader(raw);
        List<String> record = new LinkedList<String>();
        while (true) {
            String field = pickCsvField(src, commas);
            int ch = src.hasNext() ? src.next() : EOF;
            if (ch == EOF) {
                record.add(field);
                break;
            } else if (ch == comma) {
                record.add(field);
                continue;
            } else {
                src.moveBack();
                src.eatOrThrow(crlf);
                record.add(field);
                break;
            }
        }
        return record;
    }

    private static String pickCsvField(CharReader src, HashSet<Integer> commas) {
        final int STATE_START = 0;
        final int STATE_UNQUOTED = 1;
        final int STATE_QUOTED = 2;
        final int STATE_QUOTED_QUOTE = 3;

        StringBuilder sb = new StringBuilder();
        int state = 0;
        while (true) {
            switch (state) {
                case STATE_START: {
                    if (!src.hasNext()) {
                        return sb.toString();
                    }

                    int ch = src.next();
                    if (ch == DOUBLE_QUOTE) {
                        state = STATE_QUOTED;
                    } else if (commas.contains(ch)) {
                        src.moveBack();
                        return sb.toString();
                    } else {
                        sb.appendCodePoint(ch);
                        state = STATE_UNQUOTED;
                    }
                    break;
                }
                case STATE_UNQUOTED: {
                    if (!src.hasNext()) {
                        return sb.toString();
                    }

                    int ch = src.next();
                    if (ch == DOUBLE_QUOTE) {
                        throw new IllegalArgumentException();
                    } else if (commas.contains(ch)) {
                        src.moveBack();
                        return sb.toString();
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
                }
                case STATE_QUOTED: {
                    if (!src.hasNext()) {
                        throw new IllegalArgumentException();
                    }

                    int ch = src.next();
                    if (ch == DOUBLE_QUOTE) {
                        state = STATE_QUOTED_QUOTE;
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
                }
                case STATE_QUOTED_QUOTE: {
                    if (!src.hasNext()) {
                        return sb.toString();
                    }

                    int ch = src.next();
                    if (ch == DOUBLE_QUOTE) {
                        sb.appendCodePoint(ch);
                        state = STATE_QUOTED;
                    } else {
                        src.moveBack();
                        return sb.toString();
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
