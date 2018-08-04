package libjava.io.format;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import libjava.io.InstallmentByteBuffer;
import libjava.io.ParsingException;
import libjava.io.Pullable;

/**
 * a,"b",c => [a,b,c]
 */
public class Csv {

    private static final int COMMA = ',';
    private static final int DOUBLE_QUOTE = '\"';
    private static final int SINGLE_QUOTE = '\'';

    public static List<String> fromLine(Feeder puller) {
        List<String> items = new LinkedList<>();
        while (true) {
            int ch = puller.pull();
            if (ch == Pullable.EOF) {
                items.add("");
                return items;
            } else if (ch == COMMA) {
                items.add("");
                continue;
            } else if (ch == DOUBLE_QUOTE) {
                items.add(Parser.pickString(puller, DOUBLE_QUOTE));
                ch = puller.pull();
                if (ch == Pullable.EOF) {
                    return items;
                } else if (ch == COMMA) {
                    continue;
                } else {
                    puller.back();
                    throw new ParsingException();
                }
            } else if (ch == SINGLE_QUOTE) {
                items.add(Parser.pickString(puller, SINGLE_QUOTE));
                ch = puller.pull();
                if (ch == Pullable.EOF) {
                    return items;
                } else if (ch == COMMA) {
                    continue;
                } else {
                    puller.back();
                    throw new ParsingException();
                }
            } else {
                puller.back();
                items.add(Parser.pickString(puller, COMMA, true));
                // check EOF
                puller.back();
                int last = puller.pull();
                if (last == COMMA) {
                    // comma is consumed
                    continue;
                } else {
                    // meet EOF
                    return items;
                }
            }
        }
    }

    public static List<String> fromLine(String s) {
        return fromLine(Feeder.wrap(s));
    }

    public static String toLine(List<String> items) {
        return toLine(items, 0);
    }

    public static String toLine(List<String> items, int delimeter) {
        InstallmentByteBuffer pusher = new InstallmentByteBuffer();
        Iterator<String> it = items.iterator();
        while (it.hasNext()) {
            if (delimeter > 0) {
                pusher.push(delimeter);
            }
            pusher.append(it.next());
            if (delimeter > 0) {
                pusher.push(delimeter);
            }
            if (it.hasNext()) {
                pusher.push(',');
            }
        }
        return new String(pusher.copyBytes());
    }
}
