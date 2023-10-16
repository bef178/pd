package pd.codec;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import pd.fenc.ScalarPicker;
import pd.fenc.UnicodeProvider;

import static pd.fenc.Int32Provider.EOF;

public class PropSerializer {

    private static final ScalarPicker scalarPicker = ScalarPicker.singleton();

    public static Entry<String, String> deserialize(UnicodeProvider src) {

        src.eatWhitespacesIfAny();

        int ch = src.hasNext() ? src.next() : EOF;
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
        }
        src.back();

        String key = scalarPicker.pickDottedIdentifier(src);

        src.eatWhitespacesIfAny();

        src.eat('=');

        src.eatWhitespacesIfAny();

        ch = src.hasNext() ? src.next() : EOF;
        if (ch == '\"') {
            String value = scalarPicker.pickBackSlashEscapedString(src, '\"');
            src.next();
            src.eatWhitespacesIfAny();
            src.eat(EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        } else {
            src.back();
            src.eatWhitespacesIfAny();
            String value = scalarPicker.pickBackSlashEscapedString(src, EOF);
            return new SimpleImmutableEntry<String, String>(key, value);
        }
    }
}
