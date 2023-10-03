package pd.codec;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.fenc.InstallmentByteBuffer;
import pd.util.StringExtension;

/**
 * query is a string<br/>
 * https://tools.ietf.org/rfc/rfc3986.txt<br/>
 */
public final class UriQueryCodec {

    public static final int majorSeparator = '&';

    public static final int kvSeparator = '=';

    private static String decodePctString(String s) {
        int[] ucs4 = s.codePoints().toArray();
        int i = 0;
        byte[] buffer = new byte[1];
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        while (i < ucs4.length) {
            int numConsumed = PercentCodec.decode1byte(ucs4, i, buffer, 0);
            i += numConsumed;
            dst.push(buffer[0] & 0xFF);
        }
        return new String(dst.copyBytes(), StandardCharsets.UTF_8);
    }

    private static String encodePctString(String s) {
        byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
        int i = 0;
        int[] buffer = new int[3];
        StringBuilder sb = new StringBuilder();
        while (i < utf8.length) {
            int numProduced = PercentCodec.encode1byte(utf8[i], buffer, 0);
            i++;
            for (int j = 0; j < numProduced; j++) {
                sb.appendCodePoint(buffer[j]);
            }
        }
        return sb.toString();
    }

    public static List<SimpleImmutableEntry<String, String>> parse(String queryString) {
        if (queryString == null) {
            return null;
        }

        List<SimpleImmutableEntry<String, String>> entries = new LinkedList<>();
        for (String entryString : StringExtension.split(queryString, majorSeparator)) {
            String key = null;
            String value = null;
            int i = entryString.indexOf(kvSeparator);
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
            entries.add(new SimpleImmutableEntry<String, String>(
                    decodePctString(key),
                    decodePctString(value)));
        }
        return entries;
    }

    public static String toQueryString(Collection<? extends Map.Entry<String, String>> entries) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : entries) {
            sb.append(encodePctString(entry.getKey()));
            sb.appendCodePoint(kvSeparator);
            sb.append(encodePctString(entry.getValue()));
            sb.appendCodePoint(majorSeparator);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
