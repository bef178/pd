package pd.codec.json.json2object;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import pd.codec.json.IJson;

public class TypeMapping {

    private final LinkedHashMap<MappingKey<?>, IJsonToTypeFunc<?>> refs = new LinkedHashMap<>();

    public <T> Class<? extends T> find(Class<T> targetClass, String fieldPath, IJson json) {
        if (targetClass == null || fieldPath == null) {
            throw new NullPointerException();
        }

        IJsonToTypeFunc<T> func = findFunc(targetClass, fieldPath);
        if (func != null) {
            return func.map(targetClass, fieldPath, json); // TODO should do try-catch?
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> IJsonToTypeFunc<T> findFunc(Class<T> targetClass, String fieldPath) {
        Map.Entry<MappingKey<?>, IJsonToTypeFunc<?>> entry;
        entry = refs.entrySet().stream()
                .filter(a -> a.getKey().targetClass == targetClass)
                .filter(a -> a.getKey().pathPattern != null && matchesFieldPath(a.getKey().pathPattern, fieldPath))
                .max(Comparator.comparingInt(a -> findFieldPathScore(a.getKey().pathPattern, fieldPath)))
                .orElse(null);
        if (entry != null) {
            return (IJsonToTypeFunc<T>) entry.getValue();
        }
        entry = refs.entrySet().stream()
                .filter(a -> a.getKey().targetClass == null)
                .filter(a -> a.getKey().pathPattern != null && matchesFieldPath(a.getKey().pathPattern, fieldPath))
                .max(Comparator.comparingInt(a -> findFieldPathScore(a.getKey().pathPattern, fieldPath)))
                .orElse(null);
        if (entry != null) {
            return (IJsonToTypeFunc<T>) entry.getValue();
        }
        entry = refs.entrySet().stream()
                .filter(a -> a.getKey().targetClass == targetClass)
                .filter(a -> a.getKey().pathPattern == null)
                .findFirst()
                .orElse(null);
        if (entry != null) {
            return (IJsonToTypeFunc<T>) entry.getValue();
        }
        entry = refs.entrySet().stream()
                .filter(a -> a.getKey().targetClass == null)
                .filter(a -> a.getKey().pathPattern == null)
                .findFirst()
                .orElse(null);
        if (entry != null) {
            return (IJsonToTypeFunc<T>) entry.getValue();
        }
        return null;
    }

    private int findFieldPathScore(String pathPattern, String fieldPath) {
        return PathPattern.singleton().score(pathPattern, fieldPath);
    }

    private boolean matchesFieldPath(String pathPattern, String fieldPath) {
        return PathPattern.singleton().matches(pathPattern, fieldPath);
    }

    public <T> void register(Class<T> targetClass, Class<? extends T> actualClass) {
        register(targetClass, null, (t, p, json) -> actualClass);
    }

    public <T> void register(Class<T> targetClass, String pathPattern, Class<? extends T> actualClass) {
        register(targetClass, pathPattern, (t, p, json) -> actualClass);
    }

    public <T> void register(String pathPattern, Class<? extends T> actualClass) {
        register(null, pathPattern, (t, p, json) -> actualClass);
    }

    <T> void register(Class<T> targetClass, String pathPattern, IJsonToTypeFunc<T> func) {
        if (func == null) {
            throw new NullPointerException();
        }
        MappingKey<T> key = new MappingKey<>(targetClass, pathPattern);
        // the new value will take place of the old value
        // TODO should log: swapped out `{}` => `{}`", key, old
        refs.put(key, func);
    }

    public <T> void register(Class<T> targetClass, String pathPattern, IJsonToTypeFunc1<T> func) {
        if (targetClass == null || pathPattern == null) {
            throw new NullPointerException();
        }
        register(targetClass, pathPattern, (t, p, json) -> func.map(json));
    }

    static class MappingKey<T> {
        Class<T> targetClass;
        String pathPattern;

        public MappingKey(Class<T> targetClass, String pathPattern) {
            this.targetClass = targetClass;
            this.pathPattern = pathPattern;
        }
    }
}
