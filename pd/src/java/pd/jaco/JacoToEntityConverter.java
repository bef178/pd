package pd.jaco;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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

        T result = ObjectExtension.convert(o, retargetedClass);
        if (result != null) {
            return result;
        }

        // TODO add mapToObject config

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
                    String path1 = PathExtension.join(path, "[" + i + "]");
                    a.add(jacoToEntity(o1.get(i), path1, Object.class));
                }
                return retargetedClass.cast(a);
            } else {
                throw new ParsingException(String.format("E: cannot build %s from %s", targetClass.getName(), o.getClass().getName()));
            }
        } else if (retargetedClass.isArray()) {
            if (o instanceof List) {
                List<Object> o1 = (List<Object>) o;
                Class<?> elementClass = retargetedClass.getComponentType();
                Object a = Array.newInstance(elementClass, o1.size());
                for (int i = 0; i < o1.size(); i++) {
                    String path1 = PathExtension.join(path, "[" + i + "]");
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
                    String path1 = PathExtension.join(path, "{" + entry.getKey() + "}");
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
        ToJavaTypeFunc<T> func = (ToJavaTypeFunc<T>) config.refs.get(targetClass);
        if (func != null) {
            return func.map(o, path, targetClass);
        }
        return null;
    }

    public static class Config {

        final LinkedHashMap<Class<?>, ToJavaTypeFunc<?>> refs = new LinkedHashMap<>();

        public Config() {
            register(List.class, ArrayList.class);
            register(Map.class, LinkedHashMap.class);
            register(long.class, Long.class);
            register(int.class, Integer.class);
            register(short.class, Short.class);
            register(byte.class, Byte.class);
            register(double.class, Double.class);
            register(float.class, Float.class);
            register(boolean.class, Boolean.class);
            register(char.class, Character.class);
        }

        public <T> void register(Class<T> fromClass, Class<? extends T> toClass) {
            register(fromClass, (jaco, p, t) -> toClass);
        }

        public <T> void register(Class<T> fromClass, ToJavaTypeFunc<T> f) {
            if (f == null) {
                throw new NullPointerException();
            }
            refs.put(fromClass, f);
        }
    }

    @FunctionalInterface
    public interface ToJavaTypeFunc<T> {
        Class<? extends T> map(Object o, String path, Class<T> targetClass);
    }
}
