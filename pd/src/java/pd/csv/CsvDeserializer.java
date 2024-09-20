package pd.csv;

import java.util.LinkedList;
import java.util.List;

import pd.fenc.BackableUnicodeProvider;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;
import pd.util.AsciiExtension;

import static pd.util.AsciiExtension.EOF;

class CsvDeserializer {

    static final ScalarPicker scalarPicker = ScalarPicker.singleton();

    static final String CRLF = new String(new char[] { AsciiExtension.CR, AsciiExtension.LF });

    public static List<String> deserialize(String csvText) {
        BackableUnicodeProvider src = new BackableUnicodeProvider(csvText);
        List<String> fields = new LinkedList<>();
        while (true) {
            String field = pickField(src);
            int ch = src.hasNext() ? src.next() : EOF;
            switch (ch) {
                case EOF:
                    fields.add(field);
                    return fields;
                case AsciiExtension.COMMA:
                    fields.add(field);
                    break;
                default:
                    src.back();
                    if (!scalarPicker.tryEat(src, CRLF)) {
                        throw new ParsingException("E: unexpected token");
                    }
                    fields.add(field);
                    return fields;
            }
        }
    }

    private static String pickField(BackableUnicodeProvider src) {
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
                        case AsciiExtension.DOUBLE_QUOTE:
                            state = STATE_QUOTED;
                            break;
                        case AsciiExtension.COMMA:
                        case AsciiExtension.CR:
                        case AsciiExtension.LF:
                            src.back();
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
                        case AsciiExtension.DOUBLE_QUOTE:
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
                        case AsciiExtension.DOUBLE_QUOTE:
                            state = STATE_QUOTED;
                            sb.appendCodePoint(ch);
                            break;
                        case AsciiExtension.COMMA:
                        case AsciiExtension.CR:
                        case AsciiExtension.LF:
                            src.back();
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
                        case AsciiExtension.DOUBLE_QUOTE:
                            throw new ParsingException("E: unexpected `\"`");
                        case AsciiExtension.COMMA:
                        case AsciiExtension.CR:
                        case AsciiExtension.LF:
                            src.back();
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
