package pd.jaco;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

public class JacoGetter {

    public Object get(@NonNull Object o, List<String> keys) {
        for (String key : keys) {
            o = get(o, key);
            if (o == null) {
                break;
            }
        }
        return o;
    }

    /**
     * throws {@link JacoException}
     */
    public Object get(@NonNull Object o, String key) {
        if (o instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) o;
            return m.getOrDefault(key, null);
        } else if (o instanceof List) {
            List<?> a = (List<?>) o;
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw JacoException.keyNotIndex(key);
            }
            if (index < 0) {
                throw JacoException.negativeIndex(index);
            } else if (index < a.size()) {
                return a.get(index);
            } else {
                return null;
            }
        } else if (o.getClass().isArray()) {
            Object[] a = (Object[]) o;
            int index;
            try {
                index = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                throw JacoException.keyNotIndex(key);
            }
            if (index < 0) {
                throw JacoException.negativeIndex(index);
            } else if (index < a.length) {
                return a[index];
            } else {
                return null;
            }
        } else {
            // XXX reflection get?
            throw JacoException.invalidCollection(o.getClass().getSimpleName());
        }
    }
}
