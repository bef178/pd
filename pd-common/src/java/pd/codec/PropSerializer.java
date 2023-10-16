package pd.codec;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import pd.fenc.ScalarPicker;
import pd.fenc.UnicodeProvider;

import static pd.fenc.Int32Provider.EOF;

public class PropSerializer {

    private static final ScalarPicker scalarPicker = ScalarPicker.singleton();

    public static Entry<String, String> deserialize(UnicodeProvider src) {

        scalarPicker.eatWhitespacesIfAny(src);

        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
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
            return new SimpleImmutableEntry<String, String>(key, value);
        } else {
            src.back();
            scalarPicker.eatWhitespacesIfAny(src);
            String value = scalarPicker.pickBackSlashEscapedString(src, EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        }
    }
}
