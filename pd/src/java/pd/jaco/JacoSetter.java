package pd.jaco;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JacoSetter {

    private static final JacoGetter jacoGetter = new JacoGetter();

    public Object set(Object o, List<String> keys, Object o1) {
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            Object value = jacoGetter.getOrNull(o, key);
            if (value == null) {
                // create collection
                String nextKey = keys.get(i + 1);
                boolean preferSequential = false;
                try {
                    int nextIndex = Integer.parseInt(nextKey);
                    if (nextIndex >= 0 && nextIndex < 100) {
                        preferSequential = true;
                    }
                } catch (Exception ignored) {
                }
                if (preferSequential) {
                    value = new LinkedList<>();
                } else {
                    value = new LinkedHashMap<>();
                }
                set(o, key, value);
            }
            o = value;
        }
        return set(o, keys.get(keys.size() - 1), o1);
    }

    public Object set(Object o, String key, Object o1) {
        if (o == null) {
            throw JacoException.invalidCollection("null");
        } else if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) o;
            return m.put(key, o1);
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> a = (List<Object>) o;
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw JacoException.keyNotFound(o.getClass(), key);
            }
            if (index < 0) {
                throw JacoException.keyNotFound(o.getClass(), key);
            } else if (index < a.size()) {
                return a.set(index, o1);
            } else {
                while (a.size() < index) {
                    a.add(null);
                }
                a.add(o1);
                return null;
            }
        } else if (o.getClass().isArray()) {
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw JacoException.keyNotFound(o.getClass(), key);
            }
            Object[] a = (Object[]) o;
            if (index >= 0 && index < a.length) {
                Object old = a[index];
                a[index] = o1;
                return old;
            } else {
                throw JacoException.keyNotFound(o.getClass(), key);
            }
        } else {
            // XXX reflection get?
            throw JacoException.invalidCollection(o.getClass().getSimpleName());
        }
    }
}
