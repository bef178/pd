package pd.jaco;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JacoGetter {

    public Object get(Object o, List<String> keys) {
        for (String key : keys) {
            o = get(o, key);
        }
        return o;
    }

    public Object get(Object o, String... keys) {
        return get(o, Arrays.asList(keys));
    }

    /**
     * throws {@link JacoException}
     */
    public Object get(Object o, String key) {
        if (o == null) {
            throw JacoException.invalidCollection("null");
        } else if (o instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) o;
            if (m.containsKey(key)) {
                return m.get(key);
            } else {
                throw JacoException.keyNotFound(o.getClass(), key);
            }
        } else if (o instanceof List) {
            List<?> a = (List<?>) o;
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw JacoException.keyNotFound(o.getClass(), key);
            }
            if (index >= 0 && index < a.size()) {
                return a.get(index);
            } else {
                throw JacoException.keyNotFound(o.getClass(), key);
            }
        } else if (o.getClass().isArray()) {
            Object[] a = (Object[]) o;
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw JacoException.keyNotFound(o.getClass(), key);
            }
            if (index >= 0 && index < a.length) {
                return a[index];
            } else {
                throw JacoException.keyNotFound(o.getClass(), key);
            }
        } else {
            // XXX reflection get?
            throw JacoException.invalidCollection(o.getClass().getSimpleName());
        }
    }

    public Object getOrNull(Object o, String key) {
        try {
            return get(o, key);
        } catch (Exception ignored) {
            return null;
        }
    }
}
