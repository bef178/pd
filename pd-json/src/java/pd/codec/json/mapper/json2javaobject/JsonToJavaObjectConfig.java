package pd.codec.json.mapper.json2javaobject;

import java.util.LinkedHashMap;

import lombok.Data;
import pd.codec.json.datatype.IJson;

public class JsonToJavaObjectConfig {

//    private final LinkedHashMap<RefKey<?>, IMapToJavaType<?>> refs = new LinkedHashMap<>();
    private final LinkedHashMap<Class<?>, IMapToJavaType<?>> refs = new LinkedHashMap<>();

    <T> Class<? extends T> find(IJson json, String path, Class<T> targetClass) {
        if (path == null || targetClass == null) {
            throw new IllegalArgumentException();
        }

        IMapToJavaType<T> func = findMapper(targetClass);
        if (func != null) {
            return func.map(json, path, targetClass); // TODO should do try-catch?
        }
        return null;
    }

//    @SuppressWarnings("unchecked")
//    private <T> IMapToJavaType<T> findFunc(String path, Class<T> targetClass) {
//        Map.Entry<RefKey<?>, IMapToJavaType<?>> entry;
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == targetClass)
//                .filter(a -> a.getKey().pathPattern != null && matchesFieldPath(a.getKey().pathPattern, path))
//                .max(Comparator.comparingInt(a -> findFieldPathScore(a.getKey().pathPattern, path)))
//                .orElse(null);
//        if (entry != null) {
//            return (IMapToJavaType<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == null)
//                .filter(a -> a.getKey().pathPattern != null && matchesFieldPath(a.getKey().pathPattern, path))
//                .max(Comparator.comparingInt(a -> findFieldPathScore(a.getKey().pathPattern, path)))
//                .orElse(null);
//        if (entry != null) {
//            return (IMapToJavaType<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == targetClass)
//                .filter(a -> a.getKey().pathPattern == null)
//                .findFirst()
//                .orElse(null);
//        if (entry != null) {
//            return (IMapToJavaType<T>) entry.getValue();
//        }
//        entry = refs.entrySet().stream()
//                .filter(a -> a.getKey().targetClass == null)
//                .filter(a -> a.getKey().pathPattern == null)
//                .findFirst()
//                .orElse(null);
//        if (entry != null) {
//            return (IMapToJavaType<T>) entry.getValue();
//        }
//        return null;
//    }

    @SuppressWarnings("unchecked")
    private <T> IMapToJavaType<T> findMapper(Class<T> targetClass) {
        return (IMapToJavaType<T>) refs.get(targetClass);
    }

    public <T> void register(Class<T> targetClass, Class<? extends T> implClass) {
        register(targetClass, (json, p, t) -> implClass);
    }

    public <T> void register(Class<T> targetClass, IMapToJavaType<T> mapper) {
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
