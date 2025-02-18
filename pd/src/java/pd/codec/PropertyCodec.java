package pd.codec;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;

import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;
import pd.fenc.UnicodeProvider;

import static pd.util.AsciiExtension.EOF;

public class PropertyCodec {

    private static final ScalarPicker scalarPicker = ScalarPicker.singleton();

    public static Map.Entry<String, String> deserializeEntry(UnicodeProvider src) {

        scalarPicker.eatWhitespaces(src);

        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<>(null, null);
        }
        src.back();

        String key = scalarPicker.pickDottedIdentifierOrThrow(src);

        scalarPicker.eatWhitespaces(src);

        scalarPicker.eatOneOrThrow(src, '=');

        scalarPicker.eatWhitespaces(src);

        ch = src.hasNext() ? src.next() : EOF;
        if (ch == '\"') {
            String value = scalarPicker.pickBackSlashEscapedString(src, a -> a != '\"');
            src.next();
            scalarPicker.eatWhitespaces(src);
            if (!src.hasNext()) {
                throw new ParsingException();
            }
            return new SimpleImmutableEntry<>(key, value);
        } else {
            src.back();
            scalarPicker.eatWhitespaces(src);
            String value = scalarPicker.pickBackSlashEscapedString(src, a -> true);
            return new SimpleImmutableEntry<>(key, value);
        }
    }
}
