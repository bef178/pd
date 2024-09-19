package pd.jaco;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

public class JacoSetter {

    private static final JacoGetter jacoGetter = new JacoGetter();

    public Object set(@NonNull Object o, List<String> keys, Object value) {
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            Object o1;
            {
                try {
                    o1 = jacoGetter.get(o, key);
                } catch (Exception ignored) {
                    o1 = null;
                }
            }
            if (o1 == null) {
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
                    o1 = new LinkedList<>();
                } else {
                    o1 = new LinkedHashMap<>();
                }
                set(o, key, o1);
            }
            o = o1;
        }
        return set(o, keys.get(keys.size() - 1), value);
    }

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
                throw JacoException.keyNotFound(o.getClass(), key);
            }
            if (index < 0) {
                throw JacoException.keyNotFound(o.getClass(), key);
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
                throw JacoException.keyNotFound(o.getClass(), key);
            }
            Object[] a = (Object[]) o;
            if (index >= 0 && index < a.length) {
                Object old = a[index];
                a[index] = value;
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
