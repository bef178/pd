package pd.codec;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;

import pd.fenc.BackableUnicodeProvider;
import pd.fenc.ScalarPicker;

import static pd.util.AsciiExtension.EOF;

public class PropertyCodec {

    private static final ScalarPicker scalarPicker = ScalarPicker.singleton();

    public static Map.Entry<String, String> deserializeEntry(BackableUnicodeProvider src) {

        scalarPicker.eatWhitespacesIfAny(src);

        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<>(null, null);
        }
        src.back();

        String key = scalarPicker.pickDottedIdentifier(src);

        scalarPicker.eatWhitespacesIfAny(src);

        scalarPicker.eat(src, '=');

        scalarPicker.eatWhitespacesIfAny(src);

        ch = src.hasNext() ? src.next() : EOF;
        if (ch == '\"') {
            String value = scalarPicker.pickBackSlashEscapedString(src, '\"');
            src.next();
            scalarPicker.eatWhitespacesIfAny(src);
            scalarPicker.eat(src, EOF);
            return new SimpleImmutableEntry<>(key, value);
        } else {
            src.back();
            scalarPicker.eatWhitespacesIfAny(src);
            String value = scalarPicker.pickBackSlashEscapedString(src, EOF);
            return new SimpleImmutableEntry<>(key, value);
        }
    }
}
