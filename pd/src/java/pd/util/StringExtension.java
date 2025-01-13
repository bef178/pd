package pd.util;

import java.util.LinkedList;
import java.util.List;

public class StringExtension {

    public static int compare(String s1, String s2) {
        if (s1 == null) {
            if (s2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (s2 == null) {
                return 1;
            } else {
                return s1.compareTo(s2);
            }
        }
    }

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
            hashCode = hashCode * 31 + (s == null ? 0 : s.hashCode());
        }
        return hashCode;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static String[] split(String s, int ch) {
        return split(s, ch, Integer.MAX_VALUE);
    }

    public static String[] split(String s, int ch, int maxResults) {
        List<String> a = new LinkedList<>();
        int startIndex = 0;
        int endIndex;
        while (a.size() < maxResults - 1 && (endIndex = s.indexOf(ch, startIndex)) != -1) {
            a.add(s.substring(startIndex, endIndex));
            startIndex = endIndex + 1;
        }
        a.add(s.substring(startIndex));
        return a.toArray(new String[0]);
    }

    @SuppressWarnings("unused")
    private static String[] split1(String s, final int delimiter) {
        List<String> list = new LinkedList<>();
        int start = -1;
        int i = 0;
        while (i < s.length()) {
            if (start == -1) {
                start = i;
            }
            char c = s.charAt(i);
            int ch;
            boolean atHighSurrogate = false;
            if (Character.isHighSurrogate(c)) {
                ch = s.codePointAt(i);
                atHighSurrogate = true;
            } else if (Character.isLowSurrogate(c)) {
                throw new IllegalArgumentException();
            } else {
                ch = c;
            }
            if (ch == delimiter) {
                String token = s.substring(start, i);
                list.add(token);
                start = -1;
            }
            if (atHighSurrogate) {
                i += 2;
            } else {
                i++;
            }
        }
        if (start == -1) {
            start = i;
        }
        list.add(s.substring(start));
        return list.toArray(new String[0]);
    }

    private StringExtension() {
        // private dummy
    }
}
