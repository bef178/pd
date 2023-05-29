package pd.codec.json.mapper.json2java;

import java.util.LinkedHashMap;

import pd.codec.json.datatype.Json;

public class JsonToJavaConfig {

//    private final LinkedHashMap<RefKey<?>, MapToJavaType<?>> refs = new LinkedHashMap<>();
    private final LinkedHashMap<Class<?>, MapToJavaType<?>> refs = new LinkedHashMap<>();

    <T> Class<? extends T> find(Json json, String path, Class<T> targetClass) {
        if (path == null || targetClass == null) {
            throw new IllegalArgumentException();
        }

        MapToJavaType<T> func = findMapper(targetClass);
        if (func != null) {
            return func.map(json, path, targetClass); // TODO should do try-catch?
        }
        return null;
    }

//    @SuppressWarnings("unchecked")
//    private <T> MapToJavaType<T> findFunc(String path, Class<T> targetClass) {
//        Map.Entry<RefKey<?>, MapToJavaType<?>> entry;
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == targetClass)
//                .filter(a -> a.getKey().pathPattern != null && matchesFieldPath(a.getKey().pathPattern, path))
//                .max(Comparator.comparingInt(a -> findFieldPathScore(a.getKey().pathPattern, path)))
//                .orElse(null);
//        if (entry != null) {
//            return (MapToJavaType<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == null)
//                .filter(a -> a.getKey().pathPattern != null && matchesFieldPath(a.getKey().pathPattern, path))
//                .max(Comparator.comparingInt(a -> findFieldPathScore(a.getKey().pathPattern, path)))
//                .orElse(null);
//        if (entry != null) {
//            return (MapToJavaType<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == targetClass)
//                .filter(a -> a.getKey().pathPattern == null)
//                .findFirst()
//                .orElse(null);
//        if (entry != null) {
//            return (MapToJavaType<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == null)
//                .filter(a -> a.getKey().pathPattern == null)
//                .findFirst()
//                .orElse(null);
//        if (entry != null) {
//            return (MapToJavaType<T>) entry.getValue();
//        }
//        return null;
//    }

    @SuppressWarnings("unchecked")
    private <T> MapToJavaType<T> findMapper(Class<T> targetClass) {
        return (MapToJavaType<T>) refs.get(targetClass);
    }

    public <T> void register(Class<T> targetClass, Class<? extends T> implClass) {
        register(targetClass, (json, p, t) -> implClass);
    }

    public <T> void register(Class<T> targetClass, MapToJavaType<T> mapper) {
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
