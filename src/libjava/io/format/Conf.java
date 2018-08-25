package libjava.io.format;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import libjava.io.ParsingException;
import libjava.io.Pullable;

public class Conf {

    public static Entry<String, String> fromString(IntScanner scanner) {
        int ch = ScalarPicker.eatWhitespace(scanner);
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
        }

        String id = ScalarPicker.pickDottedIdentifier(scanner);

        ch = ScalarPicker.eatWhitespace(scanner);
        if (ch != '=') {
            throw new ParsingException();
        }

        ch = ScalarPicker.eatWhitespace(scanner);
        if (ch == '\"') {
            String value = ScalarPicker.pickString(scanner, '\"');
            scanner.next();
            ch = ScalarPicker.eatWhitespace(scanner);
            if (ch != Pullable.E_EOF) {
                throw new ParsingException();
            } else {
                return new SimpleImmutableEntry<String, String>(id, value);
            }
        } else {
            String value = ScalarPicker.pickString(scanner, Pullable.E_EOF);
            return new SimpleImmutableEntry<String, String>(id, value);
        }
    }
}
