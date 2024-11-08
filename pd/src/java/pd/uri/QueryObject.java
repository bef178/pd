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

    private static final PercentCodec percentCodec = new PercentCodec();

    public static QueryObject parse(String queryString) {
        QueryObject queryObject = new QueryObject();
        parseTo(queryObject, queryString, queryObject.majorDelimiter, queryObject.minorDelimiter);
        return queryObject;
    }

    private static void parseTo(QueryObject queryObject, String queryString, int majorDelimiter, int minorDelimiter) {
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
            queryObject.add(percentCodec.decode(key), percentCodec.decode(value));
        }
    }

    public final int majorDelimiter;

    public final int minorDelimiter;

    public final LinkedList<Map.Entry<String, String>> params = new LinkedList<>();

    public QueryObject() {
        this('&', '=');
    }

    public QueryObject(int majorDelimiter, int minorDelimiter) {
        this.majorDelimiter = majorDelimiter;
        this.minorDelimiter = minorDelimiter;
    }

    public void clear() {
        params.clear();
    }

    public void add(String key, String value) {
        params.add(new AbstractMap.SimpleEntry<>(key, value));
    }

    public List<String> get(String key) {
        return params.stream()
                .filter(a1 -> a1.getKey().equals(key))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public String toString() {
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
