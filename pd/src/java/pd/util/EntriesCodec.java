package pd.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

import pd.fenc.ScalarPicker;
import pd.fenc.UnicodeProvider;

import static pd.util.AsciiExtension.SP;

public class EntriesCodec {

    private final ScalarPicker scalarPicker = ScalarPicker.singleton();

    private final int delimiter;

    private final int valueDelimiter;
    private final int serializationNumSpacesBeforeDelimiter;
    private final int serializationNumSpacesAfterDelimiter;
    private final IntPredicate keyPredicate;
    private final IntPredicate valuePredicate;

    public EntriesCodec() {
        this('=', '&');
    }

    /**
     * typically:<br/>
     *  - for jar manifest file, use ':", '\n'<br/>
     *  - for properties file, use '=', '\n'<br/>
     *  - for uri query params, use '=', '&'<br/>
     */
    public EntriesCodec(int delimiter, int valueDelimiter) {
        this(delimiter, valueDelimiter, 0, 0);
    }

    public EntriesCodec(int delimiter, int valueDelimiter, int serializationNumSpacesBeforeDelimiter, int serializationNumSpacesAfterDelimiter) {
        this.delimiter = delimiter;
        this.valueDelimiter = valueDelimiter;
        this.keyPredicate = ch -> ch != delimiter && ch != valueDelimiter;
        this.valuePredicate = ch -> ch != valueDelimiter;
        this.serializationNumSpacesBeforeDelimiter = serializationNumSpacesBeforeDelimiter;
        this.serializationNumSpacesAfterDelimiter = serializationNumSpacesAfterDelimiter;
    }

    /**
     * serialize to a string, without trailing delimiter
     */
    public String encode(Collection<Map.Entry<String, String>> entries) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : entries) {
            checkInputOrThrow(entry);
            sb.append(entry.getKey());
            for (int i = 0; i < serializationNumSpacesBeforeDelimiter; i++) {
                sb.appendCodePoint(SP);
            }
            sb.appendCodePoint(delimiter);
            for (int i = 0; i < serializationNumSpacesAfterDelimiter; i++) {
                sb.appendCodePoint(SP);
            }
            sb.append(entry.getValue());
            sb.appendCodePoint(valueDelimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private void checkInputOrThrow(Map.Entry<String, String> entry) {
        if (entry == null) {
            throw new IllegalArgumentException();
        }
        if (entry.getKey() == null) {
            throw new IllegalArgumentException();
        }
        if (entry.getKey().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (!entry.getKey().codePoints().allMatch(keyPredicate)) {
            throw new IllegalArgumentException();
        }

        if (entry.getValue() == null) {
            throw new IllegalArgumentException();
        }
        if (entry.getValue().contains("\n") || entry.getValue().contains("\r")) {
            throw new IllegalArgumentException();
        }
    }

    public List<Map.Entry<String, String>> decode(String s) {
        return decode(UnicodeProvider.wrap(s));
    }

    private List<Map.Entry<String, String>> decode(UnicodeProvider src) {
        List<Map.Entry<String, String>> entries = new LinkedList<>();
        while (src.hasNext()) {
            String key = scalarPicker.pickBackSlashEscapedString(src, keyPredicate);
            String value = null;
            if (scalarPicker.tryEatOne(src, delimiter)) {
                value = scalarPicker.pickBackSlashEscapedString(src, valuePredicate);
            }
            scalarPicker.tryEatOne(src, valueDelimiter);

            entries.add(new AbstractMap.SimpleEntry<>(key, value));
        }
        return entries;
    }
}
