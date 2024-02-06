package pd.codec;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.util.StringExtension;

/**
 * query is a string<br/>
 * https://tools.ietf.org/rfc/rfc3986.txt<br/>
 */
public final class QueryStringCodec {

    public static final int MAJOR_DELIMITER = '&';

    public static final int MINOR_DELIMITER = '=';

    public static String serialize(Collection<? extends Map.Entry<String, String>> entries) {
        return serialize(entries, MAJOR_DELIMITER, MINOR_DELIMITER);
    }

    public static String serialize(Collection<? extends Map.Entry<String, String>> entries, int majorDelimiter, int minorDelimiter) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : entries) {
            PercentCodec.encode(entry.getKey(), sb);
            sb.appendCodePoint(minorDelimiter);
            PercentCodec.encode(entry.getValue(), sb);
            sb.appendCodePoint(majorDelimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static List<Map.Entry<String, String>> deserialize(String queryString) {
        return deserialize(queryString, MAJOR_DELIMITER, MINOR_DELIMITER);
    }

    public static List<Map.Entry<String, String>> deserialize(String queryString, int majorDelimiter, int minorDelimiter) {
        if (queryString == null) {
            return null;
        }

        List<Map.Entry<String, String>> entries = new LinkedList<>();
        for (String entryString : StringExtension.split(queryString, majorDelimiter)) {
            String key;
            String value;
            int i = entryString.indexOf(minorDelimiter);
            if (i == -1) {
                // "a" => { "a" : "" }
                key = entryString;
                value = "";
            } else {
                // "a=" => { "a" : "" }
                // "a=b=1" => { "a" : "b=1" }
                key = entryString.substring(0, i);
                value = entryString.substring(i + 1);
            }
            entries.add(new SimpleImmutableEntry<>(PercentCodec.decode(key), PercentCodec.decode(value)));
        }
        return entries;
    }
}
