package pd.io.format;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pd.io.InstallmentByteBuffer;
import pd.io.ParsingException;
import pd.io.Pullable;
import pd.io.Pushable;

/**
 * a,"b",c => [a,b,c]
 */
public class Csv {

    private static final int COMMA = ',';
    private static final int BACKSLASH = '\\';

    private static final int DOUBLE_QUOTE = '\"';
    private static final int SINGLE_QUOTE = '\'';

    public static List<String> fromString(IntScanner it) {
        return fromString(it, COMMA);
    }

    public static List<String> fromString(IntScanner it, final int separator) {
        List<String> values = new LinkedList<String>();
        while (true) {
            int ch = it.pull();
            if (ch == Pullable.E_EOF) {
                values.add("");
                return values;
            } else if (ch == separator) {
                values.add("");
                continue;
            } else if (ch == DOUBLE_QUOTE || ch == SINGLE_QUOTE) {
                values.add(fromString(it, ch, false));
                it.pull();
                ch = it.pull();
                if (ch == Pullable.E_EOF) {
                    return values;
                } else if (ch == separator) {
                    continue;
                } else {
                    it.back();
                    throw new ParsingException();
                }
            } else {
                it.back();
                values.add(fromString(it, separator, true));
                int last = it.pull();
                if (last == separator) {
                    continue;
                } else {
                    // meet EOF
                    return values;
                }
            }
        }
    }

    private static String fromString(IntScanner it, int closingSymbol, boolean silent) {
        boolean escaped = false;
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        Pullable p = ScalarPicker.pickStringAsPullablePipe(it, closingSymbol, silent);
        for (int ch = p.pull(); ch != Pullable.E_EOF; ch = p.pull()) {
            if (escaped) {
                escaped = false;
                if (ch != DOUBLE_QUOTE && ch != SINGLE_QUOTE) {
                    buffer.push(BACKSLASH);
                }
                buffer.push(ch);
            } else if (ch == BACKSLASH) {
                escaped = true;
            } else {
                buffer.push(ch);
            }
        }
        return new String(buffer.copyBytes(), StandardCharsets.UTF_8);
    }

    public static List<String> fromString(String s) {
        return fromString(IntScanner.wrap(s));
    }

    private static void toString(CharSequence cs, Pushable ostream) {
        assert cs != null;
        assert ostream != null;
        Pullable p = Pullable.wrap(cs);
        for (int ch = p.pull(); ch != Pullable.E_EOF; ch = p.pull()) {
            if (ch == DOUBLE_QUOTE || ch == SINGLE_QUOTE) {
                ostream.push(BACKSLASH);
            }
            ostream.push(ch);
        }
    }

    public static String toString(List<String> items) {
        return toString(items, COMMA, DOUBLE_QUOTE);
    }

    public static String toString(List<String> items, int separator, int delimeter) {
        assert separator > 0;
        assert delimeter == 0 || delimeter == DOUBLE_QUOTE || delimeter == SINGLE_QUOTE;
        Iterator<String> it = items.iterator();
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        while (it.hasNext()) {
            if (delimeter > 0) {
                buffer.push(delimeter);
            }
            toString(it.next(), buffer);
            if (delimeter > 0) {
                buffer.push(delimeter);
            }
            if (it.hasNext()) {
                buffer.push(separator);
            }
        }
        return new String(buffer.copyBytes());
    }

    public static String toString(String... items) {
        return toString(Arrays.asList(items));
    }
}
