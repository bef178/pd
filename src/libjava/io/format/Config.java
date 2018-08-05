package libjava.io.format;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import libjava.io.ParsingException;
import libjava.io.Pullable;

public class Config {

    public static Entry<String, String> fromConfigLine(IntScanner puller) {
        int ch = ScalarPicker.eatWhitespace(puller);
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
        }

        String id = ScalarPicker.pickDottedIdentifier(puller);

        ch = ScalarPicker.eatWhitespace(puller);
        if (ch != '=') {
            throw new ParsingException();
        }

        ch = ScalarPicker.eatWhitespace(puller);
        if (ch == '\"') {
            String value = ScalarPicker.pickString(puller, '\"');
            ch = ScalarPicker.eatWhitespace(puller);
            if (ch != Pullable.E_EOF) {
                throw new ParsingException();
            } else {
                return new SimpleImmutableEntry<String, String>(id, value);
            }
        } else {
            String value = ScalarPicker.pickString(puller, Pullable.E_EOF);
            return new SimpleImmutableEntry<String, String>(id, value);
        }
    }
}
