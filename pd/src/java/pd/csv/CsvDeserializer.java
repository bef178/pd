package pd.csv;

import java.util.LinkedList;
import java.util.List;

import lombok.NonNull;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;
import pd.fenc.UnicodeProvider;
import pd.util.AsciiExtension;

import static pd.util.AsciiExtension.COMMA;
import static pd.util.AsciiExtension.CR;
import static pd.util.AsciiExtension.EOF;
import static pd.util.AsciiExtension.LF;

class CsvDeserializer {

    private final ScalarPicker scalarPicker = ScalarPicker.singleton();

    public List<String> deserialize(@NonNull String s) {
        List<String> fields = new LinkedList<>();
        UnicodeProvider src = UnicodeProvider.wrap(s);
        while (src.hasNext()) {
            fields.add(parseField(src));
            if (src.hasNext()) {
                scalarPicker.eatOneOrThrow(src, COMMA);
            }
        }
        return fields;
    }

    /**
     * say `field` stands for the raw string, not the encoded string
     */
    private String parseField(UnicodeProvider src) {
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
                        case CR:
                        case LF:
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
                        case CR:
                        case LF:
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
                        case CR:
                        case LF:
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
