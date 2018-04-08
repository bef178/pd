package libcliff.io.format;

import java.util.LinkedList;
import java.util.List;

import libcliff.io.InstallmentByteBuffer;
import libcliff.io.ParsingException;
import libcliff.io.Pullable;
import libcliff.io.codec.Utf8;

/**
 * a,"b",'c' => [a,b,c]
 */
public class Csv {

    private enum State {
        IDLE, REC_WIP, REC_END
    }

    private static final int EOF = -1;
    private static final int COMMA = ',';
    private static final int DOUBLE_QUOTE = '\"';
    private static final int SINGLE_QUOTE = '\'';

    // TODO think over the api
    public static List<byte[]> fromCsvLine(Pullable pullable) {

        List<byte[]> target = new LinkedList<>();
        InstallmentByteBuffer record = new InstallmentByteBuffer();
        int delimiter = -1;

        State state = State.IDLE;
        while (true) {
            int ch = pullable.pull();
            switch (state) {
                case IDLE:
                    switch (ch) {
                        case EOF:
                            return target;
                        case COMMA:
                            state = State.IDLE;
                            target.add(record.copyBytes());
                            record.wipe();
                            break;
                        case DOUBLE_QUOTE:
                        case SINGLE_QUOTE:
                            state = State.REC_WIP;
                            delimiter = ch;
                            break;
                        default:
                            state = State.REC_WIP;
                            Utf8.toUtf8Bytes(ch, record);
                            break;
                    }
                    break;
                case REC_WIP:
                    if (delimiter >= 0) {
                        if (ch == EOF) {
                            throw new ParsingException();
                        }
                        if (ch == delimiter) {
                            state = State.REC_END;
                            target.add(record.copyBytes());
                            record.wipe();
                            delimiter = -1;
                        } else {
                            Utf8.toUtf8Bytes(ch, record);
                        }
                    } else {
                        switch (ch) {
                            case EOF:
                            case COMMA:
                                state = State.IDLE;
                                target.add(record.copyBytes());
                                record.wipe();
                                break;
                            default:
                                Utf8.toUtf8Bytes(ch, record);
                                break;
                        }
                    }
                    break;
                case REC_END:
                    switch (ch) {
                        case EOF:
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
