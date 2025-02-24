package pd.util;

import java.util.AbstractMap;
import java.util.Map;

import pd.fenc.ScalarPicker;
import pd.fenc.UnicodeProvider;

import static pd.util.AsciiExtension.isAlpha;
import static pd.util.AsciiExtension.isDigit;

public class PropertiesCodec {

    private final ScalarPicker scalarPicker = ScalarPicker.singleton();

    public String encodeEntry(Map.Entry<String, String> entry) {
        if (entry == null) {
            throw new IllegalArgumentException();
        }
        if (entry.getKey() == null) {
            throw new IllegalArgumentException();
        }
        if (entry.getKey().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (!entry.getKey().codePoints().allMatch(a -> isAlpha(a) || isDigit(a) || a == '_' || a == '-' || a == '.')) {
            throw new IllegalArgumentException();
        }

        if (entry.getValue() == null) {
            throw new IllegalArgumentException();
        }
        if (entry.getValue().contains("\n") || entry.getValue().contains("\r")) {
            throw new IllegalArgumentException();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(entry.getKey());
        sb.appendCodePoint('=');
        sb.append(entry.getValue());
        return sb.toString();
    }

    public Map.Entry<String, String> decodeEntry(String s) {
        return decodeEntry(UnicodeProvider.wrap(s));
    }

    private Map.Entry<String, String> decodeEntry(UnicodeProvider src) {
        if (!src.hasNext()) {
            return null;
        }

        int ch = src.next();
        if (ch == '#') {
            return null;
        }

        src.back();

        String key = scalarPicker.pickString(src, a -> isAlpha(a) || isDigit(a) || a == '_' || a == '-' || a == '.');
        scalarPicker.eatWhitespaces(src);
        scalarPicker.eatOneOrThrow(src, '=');
        scalarPicker.eatWhitespaces(src);
        String value = scalarPicker.pickString(src, a -> a != '\r' && a != '\n').trim();

        // eat possible CR or LF or CR,LF
        scalarPicker.tryEatOne(src, a -> a == '\r');
        scalarPicker.tryEatOne(src, a -> a == '\n');

        return new AbstractMap.SimpleEntry<>(key, value);
    }
}
