package libcliff.io.format;

import java.util.LinkedList;
import java.util.List;

import libcliff.io.InstallmentByteBuffer;
import libcliff.io.ParsingException;
import libcliff.io.Pullable;
import libcliff.io.codec.Utf8;

/**
 * a,"b",c => [a,b,c]
 */
public class Csv {

    private static final int COMMA = ',';
    private static final int DOUBLE_QUOTE = '\"';

    public static List<String> fromCsvLine(Feeder puller) {
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
            } else {
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
}
