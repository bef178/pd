package pd.codec.csv;

import static pd.fenc.IReader.EOF;
import static pd.util.AsciiUtil.COMMA;
import static pd.util.AsciiUtil.CR;
import static pd.util.AsciiUtil.DOUBLE_QUOTE;
import static pd.util.AsciiUtil.LF;

import java.util.LinkedList;
import java.util.List;

import pd.fenc.CharReader;
import pd.fenc.ParsingException;

class CsvDeserializer {

    static final String CRLF = new String(new char[] { CR, LF });

    public static List<String> deserialize(String csvText) {
        CharReader src = new CharReader(csvText);
        List<String> fields = new LinkedList<String>();
        while (true) {
            String field = pickField(src);
            int ch = src.hasNext() ? src.next() : EOF;
            switch (ch) {
                case EOF:
                    fields.add(field);
                    return fields;
                case COMMA:
                    fields.add(field);
                    break;
                default:
                    src.moveBack();
                    if (!src.tryEat(CRLF)) {
                        throw new ParsingException("E: unexpected token");
                    }
                    fields.add(field);
                    return fields;
            }
        }
    }

    private static String pickField(CharReader src) {
        final int STATE_READY = 0;
        final int STATE_QUOTED = 1;
        final int STATE_QUOTED2 = 2;
        final int STATE_UNQUOTED = 3;

        StringBuilder sb = new StringBuilder();
        int state = STATE_READY;
        while (true) {
            switch (state) {
                case STATE_READY: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case EOF:
                            return sb.toString();
                        case DOUBLE_QUOTE:
                            state = STATE_QUOTED;
                            break;
                        case COMMA:
                        case CR:
                        case LF:
                            src.moveBack();
                            return sb.toString();
                        default:
                            sb.appendCodePoint(ch);
                            state = STATE_UNQUOTED;
                            break;
                    }
                    break;
                }
                case STATE_QUOTED: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case EOF:
                            throw new ParsingException("E: unexpected EOF");
                        case DOUBLE_QUOTE:
                            state = STATE_QUOTED2;
                            break;
                        default:
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
                case STATE_QUOTED2: {
                    // might be the end of field or start of escaping
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case EOF:
                            return sb.toString();
                        case DOUBLE_QUOTE:
                            state = STATE_QUOTED;
                            sb.appendCodePoint(ch);
                            break;
                        case COMMA:
                        case CR:
                        case LF:
                            src.moveBack();
                            return sb.toString();
                        default:
                            throw new ParsingException("E: unexpected `" + new String(Character.toChars(ch)) + "`");
                    }
                    break;
                }
                case STATE_UNQUOTED: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case EOF:
                            return sb.toString();
                        case DOUBLE_QUOTE:
                            throw new ParsingException("E: unexpected `\"`");
                        case COMMA:
                        case CR:
                        case LF:
                            src.moveBack();
                            return sb.toString();
                        default:
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
            }
        }
    }
}
