package pd.uri;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pd.util.PercentCodec;
import pd.util.StringExtension;

/**
 * ref: <a href="https://datatracker.ietf.org/doc/html/rfc3986">rfc3986</a>
 */
public class QueryObject {

    public static final int defaultMajorDelimiter = '&';

    public static final int defaultMinorDelimiter = '=';

    private static final PercentCodec percentCodec = new PercentCodec();

    public static QueryObject parse(String queryString) {
        QueryObject queryObject = new QueryObject();
        queryObject.parseString(queryString);
        return queryObject;
    }

    public final LinkedList<Map.Entry<String, String>> params = new LinkedList<>();

    public void clear() {
        params.clear();
    }

    public void add(String key, String value) {
        params.add(new AbstractMap.SimpleEntry<>(key, value));
    }

    public void add(LinkedList<Map.Entry<String, String>> params) {
        this.params.addAll(params);
    }

    public void add(QueryObject another) {
        params.addAll(another.params);
    }

    public List<String> get(String key) {
        return params.stream()
                .filter(a1 -> a1.getKey().equals(key))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public void parseString(String queryString) {
        parseString(queryString, defaultMajorDelimiter, defaultMinorDelimiter);
    }

    public void parseString(String queryString, int majorDelimiter, int minorDelimiter) {
        if (queryString == null) {
            throw new IllegalArgumentException();
        }

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
            add(percentCodec.decode(key), percentCodec.decode(value));
        }
    }

    public String toString() {
        return toString(defaultMajorDelimiter, defaultMinorDelimiter);
    }

    public String toString(int majorDelimiter, int minorDelimiter) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params) {
            percentCodec.encode(entry.getKey(), sb);
            sb.appendCodePoint(minorDelimiter);
            percentCodec.encode(entry.getValue(), sb);
            sb.appendCodePoint(majorDelimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
