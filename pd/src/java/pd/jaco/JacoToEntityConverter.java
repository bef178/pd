package pd.jaco;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.fenc.ParsingException;
import pd.util.ObjectExtension;
import pd.util.PathExtension;

public class JacoToEntityConverter {

    public final Config config;

    public JacoToEntityConverter() {
        this(new Config());
    }

    public JacoToEntityConverter(Config config) {
        this.config = config;
    }

    public <T> T toEntity(Object o, Class<T> targetClass, String startPath) {
        if (startPath == null || startPath.isEmpty()) {
            throw new IllegalArgumentException("startPath should not be null or empty");
        }
        try {
            return jacoToEntity(o, startPath, targetClass);
        } catch (Exception e) {
            throw new ParsingException(e);
        }
    }

    /**
     * convert Json things to Java things
     */
    @SuppressWarnings("unchecked")
    private <T> T jacoToEntity(Object o, String path, Class<T> targetClass)
            throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        if (targetClass == null) {
            throw new IllegalArgumentException();
        }

        Class<? extends T> retargetedClass = retargetClassWithConfig(o, path, targetClass);
        if (retargetedClass == null) {
            retargetedClass = targetClass;
        }

        if (o == null) {
            if (retargetedClass == long.class) {
                return retargetedClass.cast(0L);
            } else if (retargetedClass == int.class) {
                return retargetedClass.cast(0);
            } else if (retargetedClass == short.class) {
                return retargetedClass.cast((short) 0);
            } else if (retargetedClass == byte.class) {
                return retargetedClass.cast((byte) 0);
            } else if (retargetedClass == double.class) {
                return retargetedClass.cast((double) 0);
            } else if (retargetedClass == float.class) {
                return retargetedClass.cast((float) 0);
            }

            if (retargetedClass == boolean.class) {
                return retargetedClass.cast(false);
            }

            if (retargetedClass == char.class) {
                return retargetedClass.cast((char) 0);
            }

            return null;
        }

        {
            T result = ObjectExtension.convert(o, retargetedClass);
            if (result != null) {
                return result;
            }
        }

        {
            JacoToEntityFunc<T> f = (JacoToEntityFunc<T>) config.jacoToEntityMappings.get(targetClass);
            if (f != null) {
                T[] values = f.map(o, path, targetClass);
                if (values != null) {
                    return values[0];
                }
            }
        }

        if (retargetedClass.isInterface()) {
            throw new ParsingException("E: cannot instantiate an interface: " + retargetedClass.getName());
        } else if (Modifier.isAbstract(retargetedClass.getModifiers())) {
            throw new ParsingException("E: cannot instantiate an abstract class: " + retargetedClass.getName());
        }

        if (List.class.isAssignableFrom(retargetedClass)) {
            if (o instanceof List) {
                List<Object> o1 = (List<Object>) o;
                Constructor<? extends T> constructor = retargetedClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                List<Object> a = (List<Object>) constructor.newInstance();
                for (int i = 0; i < o1.size(); i++) {
                    String path1 = PathExtension.join(path, String.valueOf(i));
                    a.add(jacoToEntity(o1.get(i), path1, Object.class));
                }
                return retargetedClass.cast(a);
            } else {
                throw new ParsingException(String.format("E: cannot build %s from %s", targetClass.getName(), o.getClass().getName()));
            }
        } else if (retargetedClass.isEnum()) {
            if (o instanceof String) {
                Object[] values = retargetedClass.getEnumConstants();
                for (Object value : values) {
                    if (value.toString().equals(o)) {
                        return retargetedClass.cast(value);
                    }
                }
            }
            throw new ParsingException(String.format("E: cannot build %s from %s", targetClass.getName(), o.getClass().getName()));
        } else if (retargetedClass.isArray()) {
            if (o instanceof List) {
                List<Object> o1 = (List<Object>) o;
                Class<?> elementClass = retargetedClass.getComponentType();
                Object a = Array.newInstance(elementClass, o1.size());
                for (int i = 0; i < o1.size(); i++) {
                    String path1 = PathExtension.join(path, String.valueOf(i));
                    Array.set(a, i, jacoToEntity(o1.get(i), path1, elementClass));
                }
                return targetClass.cast(a);
            } else {
                throw new ParsingException(String.format("E: cannot build %s from %s", targetClass.getName(), o.getClass().getName()));
            }
        } else if (Map.class.isAssignableFrom(retargetedClass)) {
            if (o instanceof Map) {
                Map<Object, Object> o1 = (Map<Object, Object>) o;
                Constructor<? extends T> constructor = retargetedClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Map<Object, Object> m = (Map<Object, Object>) constructor.newInstance();
                for (Map.Entry<Object, Object> entry : o1.entrySet()) {
                    String path1 = PathExtension.join(path, String.valueOf(entry.getKey()));
                    m.put(entry.getKey(), jacoToEntity(entry.getValue(), path1, Object.class));
                }
                return retargetedClass.cast(m);
            } else {
                throw new ParsingException(String.format("E: cannot build %s from %s", targetClass.getName(), o.getClass().getName()));
            }
        } else {
            // not array, not list, not map: that is a "normal" object
            if (o instanceof Map) {
                Map<Object, Object> o1 = (Map<Object, Object>) o;
                Constructor<?> constructor = retargetedClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object object = constructor.newInstance();
                for (Field field : retargetedClass.getFields()) {
                    String fieldName = field.getName();
                    if (o1.containsKey(fieldName)) {
                        field.setAccessible(true);
                        String path1 = PathExtension.join(path, fieldName);
                        field.set(object, jacoToEntity(o1.get(fieldName), path1, field.getType()));
                    }
                }
                return targetClass.cast(object);
            } else {
                throw new ParsingException(String.format("E: cannot build %s from %s", targetClass.getName(), o.getClass().getName()));
            }
        }
    }

    /**
     * return `null` if it cannot retarget
     */
    <T> Class<? extends T> retargetClassWithConfig(Object o, String path, final Class<T> targetClass) {
        @SuppressWarnings("unchecked")
        EntityTypeFunc<T> f = (EntityTypeFunc<T>) config.entityTypeMappings.get(targetClass);
        if (f != null) {
            return f.map(o, path, targetClass);
        }
        return null;
    }

    public static class Config {

        final LinkedHashMap<Class<?>, EntityTypeFunc<?>> entityTypeMappings = new LinkedHashMap<>();

        final LinkedHashMap<Class<?>, JacoToEntityFunc<?>> jacoToEntityMappings = new LinkedHashMap<>();

        public Config() {
            registerEntityTypeMapping(List.class, ArrayList.class);
            registerEntityTypeMapping(Map.class, LinkedHashMap.class);
            registerEntityTypeMapping(long.class, Long.class);
            registerEntityTypeMapping(int.class, Integer.class);
            registerEntityTypeMapping(short.class, Short.class);
            registerEntityTypeMapping(byte.class, Byte.class);
            registerEntityTypeMapping(double.class, Double.class);
            registerEntityTypeMapping(float.class, Float.class);
            registerEntityTypeMapping(boolean.class, Boolean.class);
            registerEntityTypeMapping(char.class, Character.class);

            registerJacoToEntityMapping(Instant.class, (jaco, path, entityType) -> {
                if (entityType == Instant.class) {
                    if (jaco instanceof String) {
                        Instant[] values = new Instant[1];
                        values[0] = Instant.parse((String) jaco);
                        return values;
                    }
                }
                return null;
            });
        }

        public <T> void registerEntityTypeMapping(Class<T> fromClass, Class<? extends T> toClass) {
            registerEntityTypeMapping(fromClass, (jaco, p, t) -> toClass);
        }

        public <T> void registerEntityTypeMapping(Class<T> fromClass, EntityTypeFunc<T> f) {
            if (f == null) {
                throw new NullPointerException();
            }
            entityTypeMappings.put(fromClass, f);
        }

        public <T> void registerJacoToEntityMapping(Class<T> entityType, JacoToEntityFunc<T> f) {
            if (f == null) {
                throw new NullPointerException();
            }
            jacoToEntityMappings.put(entityType, f);
        }
    }

    /**
     * maps `entityType` to its extended (instantiable) class
     */
    @FunctionalInterface
    public interface EntityTypeFunc<T> {
        Class<? extends T> map(Object jaco, String path, Class<T> entityType);
    }

    @FunctionalInterface
    public interface JacoToEntityFunc<T> {
        T[] map(Object jaco, String path, Class<T> entityType);
    }
}
