package pd.codec;

import static pd.fenc.IReader.EOF;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import pd.fenc.CharReader;
import pd.fenc.ScalarPicker;

public class PropSerializer {

    public static Entry<String, String> deserialize(CharReader src) {
        ScalarPicker picker = new ScalarPicker();

        src.eatWhitespaces();

        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
        }
        src.moveBack();

        String key = picker.pickDottedIdentifier(src);

        src.eatWhitespaces();

        src.eatOrThrow('=');

        src.eatWhitespaces();

        ch = src.hasNext() ? src.next() : EOF;
        if (ch == '\"') {
            String value = picker.pickBackSlashEscapedString(src, '\"');
            src.next();
            src.eatWhitespaces();
            src.eatOrThrow(EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        } else {
            src.moveBack();
            src.eatWhitespaces();
            String value = picker.pickBackSlashEscapedString(src, EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        }
    }
}
