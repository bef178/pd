package pd.codec;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.fenc.InstallmentByteBuffer;

/**
 * query is a string<br/>
 * https://tools.ietf.org/rfc/rfc3986.txt<br/>
 */
public final class UriQuery {

    private static String decodePctString(String s) {
        int[] ucs4 = s.codePoints().toArray();
        int i = 0;
        byte[] buffer = new byte[1];
        InstallmentByteBuffer dst = new InstallmentByteBuffer();
        while (i < ucs4.length) {
            int numConsumed = PctCodec.decode1byte(ucs4, i, buffer, 0);
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
            int numProduced = PctCodec.encode1byte(utf8[i], buffer, 0);
            i++;
            for (int j = 0; j < numProduced; j++) {
                sb.appendCodePoint(buffer[j]);
            }
        }
        return sb.toString();
    }

    public static List<SimpleEntry<String, String>> parse(String queryString) {
        if (queryString == null) {
            return null;
        }

        List<SimpleEntry<String, String>> queries = new LinkedList<>();
        for (String entryString : queryString.split("&")) {
            String key = null;
            String value = null;
            int i = entryString.indexOf('=');
            if (i < 0) {
                // a&b=1 => a=&b=1
                key = entryString;
                value = "";
            } else {
                key = entryString.substring(0, i);
                value = entryString.substring(i + 1);
            }
            queries.add(new SimpleEntry<String, String>(
                    decodePctString(key),
                    decodePctString(value)));
        }
        return queries;
    }

    public static String toQueryString(Iterator<Map.Entry<String, String>> it) {
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            sb.append(encodePctString(entry.getKey()));
            sb.append("=");
            sb.append(encodePctString(entry.getValue()));
            sb.append("&");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String toQueryString(Map<String, String> queryMap) {
        if (queryMap == null) {
            return null;
        }

        Iterator<Map.Entry<String, String>> it = queryMap.entrySet().iterator();
        return toQueryString(it);
    }
}
