package pd.fenc;

import static pd.fenc.IReader.EOF;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

public class PropSerializer {

    public static Entry<String, String> deserialize(Int32Scanner scanner) {

        scanner.eatWhitespaces();

        int ch = scanner.hasNext() ? scanner.next() : EOF;
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
        }
        scanner.moveBack();

        String key = ScalarPicker.pickDottedIdentifier(scanner);

        scanner.eatWhitespaces();

        scanner.eatOrThrow('=');

        scanner.eatWhitespaces();

        ch = scanner.hasNext() ? scanner.next() : EOF;
        if (ch == '\"') {
            String value = ScalarPicker.pickString(scanner, '\"');
            scanner.next();
            scanner.eatWhitespaces();
            scanner.eatOrThrow(EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        } else {
            scanner.moveBack();
            scanner.eatWhitespaces();
            String value = ScalarPicker.pickString(scanner, EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        }
    }
}
