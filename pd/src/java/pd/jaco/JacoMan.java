package pd.jaco;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

/**
 * Inspired by JSON, define JACO as a simple java container object type:
 * ```
 * T = Map<String, T> | Array<T> | String | Int64 | Float64 | Boolean | NULL
 * ```
 *
 * If force `Map` to be `LinkedHashMap` and `Array` to be `LinkedList`, JACO is a good carrier for json.
 *
 * And paths could be used to get/set value from/to JACO.
 */
public class JacoMan {

    public Object getWithPath(Object o, @NonNull String path) {
        return getWithPath(o, path.split("/"));
    }

    public Object getWithPath(Object o, @NonNull String[] path) {
        if (o == null) {
            return null;
        }
        for (String key : path) {
            o = get(o, key);
            if (o == null) {
                break;
            }
        }
        return o;
    }

    private Object get(@NonNull Object o, @NonNull String key) {
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
        } else {
            throw JacoException.invalidDataType(o.getClass().getSimpleName());
        }
    }

    public Object setWithPath(Object o, @NonNull String path, Object value) {
        return setWithPath(o, path.split("/"), value);
    }

    public Object setWithPath(Object o, @NonNull String[] path, Object value) {
        if (o == null) {
            o = createByKey(path[0]);
        }
        Object node = o;
        for (int i = 0; i < path.length - 1; i++) {
            String key = path[i];
            Object node1 = get(node, key);
            if (node1 == null) {
                String nextKey = path[i + 1];
                node1 = createByKey(nextKey);
                set(node, key, node1);
            }
            node = node1;
        }
        set(node, path[path.length - 1], value);
        return o;
    }

    private Object set(@NonNull Object o, @NonNull String key, Object value) {
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
        } else {
            throw JacoException.invalidDataType(o.getClass().getSimpleName());
        }
    }

    private Object createByKey(String nextKey) {
        boolean prefersSequential = false;
        try {
            int nextIndex = Integer.parseInt(nextKey);
            if (nextIndex >= 0 && nextIndex < 1024) {
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

    public Object removeWithPath(Object o, @NonNull String path) {
        return removeWithPath(o, path.split("/"));
    }

    public Object removeWithPath(Object o, @NonNull String[] path) {
        if (o == null) {
            return null;
        }
        for (int i = 0; i < path.length - 1; i++) {
            String key = path[i];
            Object value = get(o, key);
            if (value == null) {
                return null;
            }
            o = value;
        }
        return remove(o, path[path.length - 1]);
    }

    private Object remove(@NonNull Object o, @NonNull String key) {
        if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) o;
            return m.remove(key);
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
                return a.remove(index);
            } else {
                return null;
            }
        } else {
            throw JacoException.invalidDataType(o.getClass().getSimpleName());
        }
    }

    public Map<String[], Object> flatten(Object o) {
        LinkedHashMap<String[], Object> result = new LinkedHashMap<>();
        flatten(new LinkedList<>(), o, result);
        return result;
    }

    private void flatten(LinkedList<String> path, Object o, Map<String[], Object> out) {
        if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) o;
            for (Map.Entry<Object, Object> entry : m.entrySet()) {
                String key = entry.getKey().toString();
                path.add(key);
                flatten(path, entry.getValue(), out);
                path.removeLast();
            }
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> a = (List<Object>) o;
            for (int i = 0; i < a.size(); i++) {
                String key = Integer.toString(i);
                path.add(key);
                flatten(path, a.get(i), out);
                path.removeLast();
            }
        } else {
            // treat `o` as value
            out.put(path.toArray(new String[0]), o);
        }
    }
}
