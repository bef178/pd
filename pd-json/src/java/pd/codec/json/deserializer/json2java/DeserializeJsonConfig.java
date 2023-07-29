package pd.codec.json.deserializer.json2java;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.json.datatype.Json;

public class DeserializeJsonConfig {

//    private final LinkedHashMap<RefKey<?>, FindJavaTypeFunc<?>> refs = new LinkedHashMap<>();
    private final LinkedHashMap<Class<?>, FindJavaTypeFunc<?>> refs = new LinkedHashMap<>();

    public DeserializeJsonConfig() {
        register(List.class, ArrayList.class);
        register(Map.class, LinkedHashMap.class);
    }

    <T> Class<? extends T> find(Json json, String path, Class<T> targetClass) {
        if (path == null || targetClass == null) {
            throw new IllegalArgumentException();
        }

        FindJavaTypeFunc<T> func = findMapper(targetClass);
        if (func != null) {
            return func.map(json, path, targetClass); // TODO should do try-catch?
        }
        return null;
    }

//    @SuppressWarnings("unchecked")
//    private <T> FindJavaTypeFunc<T> findFunc(String path, Class<T> targetClass) {
//        Map.Entry<RefKey<?>, FindJavaTypeFunc<?>> entry;
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == targetClass)
//                .filter(a -> a.getKey().pathPattern != null && matchesFieldPath(a.getKey().pathPattern, path))
//                .max(Comparator.comparingInt(a -> findFieldPathScore(a.getKey().pathPattern, path)))
//                .orElse(null);
//        if (entry != null) {
//            return (FindJavaTypeFunc<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == null)
//                .filter(a -> a.getKey().pathPattern != null && matchesFieldPath(a.getKey().pathPattern, path))
//                .max(Comparator.comparingInt(a -> findFieldPathScore(a.getKey().pathPattern, path)))
//                .orElse(null);
//        if (entry != null) {
//            return (FindJavaTypeFunc<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == targetClass)
//                .filter(a -> a.getKey().pathPattern == null)
//                .findFirst()
//                .orElse(null);
//        if (entry != null) {
//            return (FindJavaTypeFunc<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == null)
//                .filter(a -> a.getKey().pathPattern == null)
//                .findFirst()
//                .orElse(null);
//        if (entry != null) {
//            return (FindJavaTypeFunc<T>) entry.getValue();
//        }
//        return null;
//    }

    @SuppressWarnings("unchecked")
    private <T> FindJavaTypeFunc<T> findMapper(Class<T> targetClass) {
        return (FindJavaTypeFunc<T>) refs.get(targetClass);
    }

    public <T> void register(Class<T> targetClass, Class<? extends T> implClass) {
        register(targetClass, (json, p, t) -> implClass);
    }

    public <T> void register(Class<T> targetClass, FindJavaTypeFunc<T> mapper) {
        if (mapper == null) {
            throw new NullPointerException();
        }
        // TODO should log: swapped out `{}` => `{}`", key, old
        refs.put(targetClass, mapper);
    }

//    @Data
//    static final class RefKey<T> {
//        String pathPattern;
//        Class<T> targetClass;
//
//        public RefKey(String pathPattern, Class<T> targetClass) {
//            this.pathPattern = pathPattern;
//            this.targetClass = targetClass;
//        }
//    }
}
