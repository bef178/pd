package pd.fenc;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

public class PropSerializer {

    public static Entry<String, String> deserialize(Int32Scanner scanner) {
        int ch = ScalarPicker.nextSkippingWhitespaces(scanner);
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
        }

        String key = ScalarPicker.pickDottedIdentifier(scanner);

        ch = ScalarPicker.nextSkippingWhitespaces(scanner);
        if (ch != '=') {
            throw new ParsingException();
        }

        ch = ScalarPicker.nextSkippingWhitespaces(scanner);
        if (ch == '\"') {
            String value = ScalarPicker.pickString(scanner, '\"');
            scanner.next();
            ch = ScalarPicker.nextSkippingWhitespaces(scanner);
            if (ch != IReader.EOF) {
                throw new ParsingException();
            } else {
                return new SimpleImmutableEntry<String, String>(key, value);
            }
        } else {
            String value = ScalarPicker.pickString(scanner, IReader.EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        }
    }
}
