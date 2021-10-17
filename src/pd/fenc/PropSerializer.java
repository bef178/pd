package pd.fenc;

import static pd.fenc.IReader.EOF;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

public class PropSerializer {

    public static Entry<String, String> deserialize(CharReader src) {

        src.eatWhitespaces();

        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
        }
        src.moveBack();

        String key = ScalarPicker.pickDottedIdentifier(src);

        src.eatWhitespaces();

        src.eatOrThrow('=');

        src.eatWhitespaces();

        ch = src.hasNext() ? src.next() : EOF;
        if (ch == '\"') {
            String value = ScalarPicker.pickBackSlashEscapedString(src, '\"');
            src.next();
            src.eatWhitespaces();
            src.eatOrThrow(EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        } else {
            src.moveBack();
            src.eatWhitespaces();
            String value = ScalarPicker.pickBackSlashEscapedString(src, EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        }
    }
}
