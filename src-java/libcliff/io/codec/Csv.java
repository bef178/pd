package libcliff.io.codec;

import java.util.LinkedList;
import java.util.List;

import libcliff.io.Blob;
import libcliff.io.Pullable;

class Csv {

    private enum State {
        IDLE, REC_WIP, REC_END
    }

    private static final int EOL = -1;
    private static final int COMMA = ',';
    private static final int QUOTE = '\"';
    private static final int SINGLE_QUOTE = '\'';

    // TODO think over the api
    public static List<byte[]> fromCsvLine(Pullable pullable) {

        List<byte[]> target = new LinkedList<>();
        Blob record = new Blob(1024);
        int delimiter = -1;

        State state = State.IDLE;
        while (true) {
            int ch = pullable.pull();
            switch (state) {
                case IDLE:
                    switch (ch) {
                        case EOL:
                            return target;
                        case COMMA:
                            state = State.IDLE;
                            target.add(record.getBytes());
                            record.rewind();
                            break;
                        case QUOTE:
                        case SINGLE_QUOTE:
                            state = State.REC_WIP;
                            delimiter = ch;
                            break;
                        default:
                            state = State.REC_WIP;
                            record.push(ch);
                            break;
                    }
                    break;
                case REC_WIP:
                    if (delimiter >= 0) {
                        if (ch == EOL) {
                            throw new ParsingException();
                        }
                        if (ch == delimiter) {
                            state = State.REC_END;
                            target.add(record.getBytes());
                            record.rewind();
                            delimiter = -1;
                        } else {
                            record.push(ch);
                        }
                    } else {
                        switch (ch) {
                            case EOL:
                            case COMMA:
                                state = State.IDLE;
                                target.add(record.getBytes());
                                record.rewind();
                                break;
                            default:
                                record.push(ch);
                                break;
                        }
                    }
                    break;
                case REC_END:
                    switch (ch) {
                        case EOL:
                            return target;
                        case COMMA:
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
