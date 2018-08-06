package libjava.io.format;

import java.util.Arrays;
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

    public static List<String> fromString(IntScanner scanner) {
        List<String> items = new LinkedList<String>();
        while (true) {
            int ch = scanner.pull();
            if (ch == Pullable.E_EOF) {
                items.add("");
                return items;
            } else if (ch == COMMA) {
                items.add("");
                continue;
            } else if (ch == DOUBLE_QUOTE) {
                items.add(ScalarPicker.pickString(scanner, DOUBLE_QUOTE));
                ch = scanner.pull();
                if (ch == Pullable.E_EOF) {
                    return items;
                } else if (ch == COMMA) {
                    continue;
                } else {
                    scanner.back();
                    throw new ParsingException();
                }
            } else if (ch == SINGLE_QUOTE) {
                items.add(ScalarPicker.pickString(scanner, SINGLE_QUOTE));
                ch = scanner.pull();
                if (ch == Pullable.E_EOF) {
                    return items;
                } else if (ch == COMMA) {
                    continue;
                } else {
                    scanner.back();
                    throw new ParsingException();
                }
            } else {
                scanner.back();
                items.add(ScalarPicker.pickString(scanner, COMMA, true));
                // check EOF
                scanner.back();
                int last = scanner.pull();
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

    public static List<String> fromString(String s) {
        return fromString(IntScanner.wrap(s));
    }

    public static String toString(List<String> items) {
        return toString(items, 0);
    }

    public static String toString(List<String> items, int delimeter) {
        Iterator<String> it = items.iterator();
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        while (it.hasNext()) {
            if (delimeter > 0) {
                buffer.push(delimeter);
            }
            buffer.append(it.next());
            if (delimeter > 0) {
                buffer.push(delimeter);
            }
            if (it.hasNext()) {
                buffer.push(',');
            }
        }
        return new String(buffer.copyBytes());
    }

    public static String toString(String... items) {
        return toString(Arrays.asList(items));
    }
}
