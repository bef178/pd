package pd.cprime;

import java.util.LinkedList;
import java.util.List;

public final class Cstring {

    public static boolean equals(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    public static int hashCode(String... a) {
        int hashCode = 0;
        for (String s : a) {
            hashCode = hashCode * 31 + s == null ? 0 : s.hashCode();
        }
        return hashCode;
    }

    public static String[] split(String s, final int delimeter) {
        List<String> list = new LinkedList<String>();
        int start = 0;
        int end = 0;
        while (end < s.length()) {
            char c = s.charAt(end);
            int ch;
            if (Character.isHighSurrogate(c)) {
                ch = s.codePointAt(end);
                ++end;
            } else if (Character.isLowSurrogate(c)) {
                throw new IllegalArgumentException();
            } else {
                ch = c;
            }
            ++end;
            if (ch == delimeter) {
                String token = s.substring(start, end);
                list.add(token);
                start = end;
            }
        }
        if (s.isEmpty() || start != end) {
            list.add(s.substring(start, end));
        }
        return list.toArray(new String[0]);
    }
}
