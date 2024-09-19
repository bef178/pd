package pd.jaco;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

public class JacoSetter {

    public final JacoGetter jacoGetter;

    public JacoSetter(JacoGetter jacoGetter) {
        this.jacoGetter = jacoGetter;
    }

    public Object set(@NonNull Object o, List<String> keys, Object value) {
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            Object o1 = jacoGetter.get(o, key);
            if (o1 == null) {
                String nextKey = keys.get(i + 1);
                o1 = createWithNextKey(nextKey);
                set(o, key, o1);
            }
            o = o1;
        }
        return set(o, keys.get(keys.size() - 1), value);
    }

    /**
     * throws {@link JacoException}
     */
    public Object set(@NonNull Object o, String key, Object value) {
        if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) o;
            return m.put(key, value);
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> a = (List<Object>) o;
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw JacoException.keyNotIndex(key);
            }
            if (index < 0) {
                throw JacoException.negativeIndex(index);
            } else if (index < a.size()) {
                return a.set(index, value);
            } else {
                while (a.size() < index) {
                    a.add(null);
                }
                a.add(value);
                return null;
            }
        } else if (o.getClass().isArray()) {
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw JacoException.keyNotIndex(key);
            }
            Object[] a = (Object[]) o;
            if (index < 0) {
                throw JacoException.negativeIndex(index);
            } else if (index < a.length) {
                Object old = a[index];
                a[index] = value;
                return old;
            } else {
                throw JacoException.indexTooLarge(index, a.length);
            }
        } else {
            // XXX reflection get?
            throw JacoException.invalidCollection(o.getClass().getSimpleName());
        }
    }

    public Object createWithNextKey(String nextKey) {
        boolean prefersSequential = false;
        try {
            int nextIndex = Integer.parseInt(nextKey);
            if (nextIndex >= 0 && nextIndex < 64) {
                prefersSequential = true;
            }
        } catch (Exception ignored) {
        }
        if (prefersSequential) {
            return new LinkedList<>();
        } else {
            return new LinkedHashMap<>();
        }
    }
}
