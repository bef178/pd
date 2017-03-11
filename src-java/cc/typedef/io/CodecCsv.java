package cc.typedef.io;

import java.util.LinkedList;
import java.util.List;

class CodecCsv {

    private enum State {
        IDLE, REC_WIP, REC_END
    }

    private static final int EOL = -1;

    /**
     * decode a single csv line<br/>
     * return [ '' ] for empty line<br/>
     */
    public static List<String> decode(Nextable it) {
        assert it != null;

        List<String> target = new LinkedList<>();
        StringBuilder record = new StringBuilder();
        int delimiter = -1;

        State state = State.IDLE;
        while (true) {
            int ch = it.hasNext() ? it.next() : EOL;
            switch (state) {
                case IDLE:
                    switch (ch) {
                        case EOL:
                            return target;
                        case ',':
                            state = State.IDLE;
                            target.add(record.toString());
                            record.setLength(0);
                            break;
                        case '\"':
                        case '\'':
                            state = State.REC_WIP;
                            delimiter = ch;
                            break;
                        case '\\':
                            state = State.REC_WIP;
                            record.appendCodePoint(Codec.Glyph.decode('\\', it));
                            break;
                        default:
                            state = State.REC_WIP;
                            record.appendCodePoint(ch);
                            break;
                    }
                    break;
                case REC_WIP:
                    if (ch == '\\') {
                        state = State.REC_WIP;
                        record.appendCodePoint(Codec.Glyph.decode('\\', it));
                        break;
                    }
                    if (delimiter >= 0) {
                        if (ch == EOL) {
                            throw new ParsingException();
                        }
                        if (ch == delimiter) {
                            state = State.REC_END;
                            target.add(record.toString());
                            record.setLength(0);
                            delimiter = -1;
                        } else {
                            record.appendCodePoint(ch);
                        }
                    } else {
                        switch (ch) {
                            case EOL:
                            case ',':
                                state = State.IDLE;
                                target.add(record.toString());
                                record.setLength(0);
                                break;
                            default:
                                record.appendCodePoint(ch);
                                break;
                        }
                    }
                    break;
                case REC_END:
                    switch (ch) {
                        case EOL:
                            return target;
                        case ',':
                            state = State.IDLE;
                            break;
                        default:
                            throw new ParsingException();
                    }
                    break;
            }
        }
    }
}
