package libcliff.adt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Mappin {

    private static String[] strsplit(String s, final int delimeter) {
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

    private Map<String, Object> map = new HashMap<String, Object>();

    public Mappin() {
        map = new HashMap<>();
    }

    public Mappin(Mappin other) {
        map = new HashMap<>(other.map);
    }

    public void clear() {
        map.clear();
    }

    public Mappin complement(Mappin other) {
        Mappin mappin = new Mappin();
        for (Map.Entry<String, Object> entry : this.map.entrySet()) {
            if (!other.map.containsKey(entry.getKey())) {
                mappin.map.put(entry.getKey(), entry.getValue());
            }
        }
        return mappin;
    }

    public Mappin copy() {
        return new Mappin(this);
    }

    public void fromOneLine(String s) {
        fromOneLine(s, '&', '=');
    }

    public void fromOneLine(String s, int spMajor, int spMinor) {
        String[] a = strsplit(s, spMajor);
        for (int i = 0; i < a.length; ++i) {
            String[] pair = strsplit(a[i], spMinor);
            this.map.put(pair[0], pair[1]);
        }
    }

    public Mappin intersect(Mappin other) {
        Mappin mappin = new Mappin();
        for (Map.Entry<String, Object> entry : this.map.entrySet()) {
            if (other.map.containsKey(entry.getKey())) {
                mappin.map.put(entry.getKey(), entry.getValue());
            }
        }
        return mappin;
    }

    public void merge(Mappin other) {
        for (Map.Entry<String, Object> entry : other.map.entrySet()) {
            if (!this.map.containsKey(entry.getKey())) {
                this.map.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public CharSequence toOneLine() {
        return toOneLine('&', '=');
    }

    public CharSequence toOneLine(int spMajor, int spMinor) {
        StringBuffer a = new StringBuffer();
        for (Map.Entry<String, Object> entry : this.map.entrySet()) {
            a.append(entry.getKey()).append(spMinor).append(entry.getValue());
            a.append(spMajor);
        }
        a.deleteCharAt(a.length() - 1);
        return a;
    }

    public Mappin unite(Mappin other) {
        Mappin mappin = this.copy();
        mappin.merge(other);
        return mappin;
    }
}
