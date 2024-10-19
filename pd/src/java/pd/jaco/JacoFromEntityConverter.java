package pd.jaco;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.fenc.ParsingException;

public class JacoFromEntityConverter {

    public final Config config;

    public JacoFromEntityConverter(Config config) {
        this.config = config;
    }

    /**
     * convert java things to jaco things<br/>
     * do public fields only<br/>
     */
    public Object fromEntity(Object o) {
        if (o == null) {
            return null;
        }

        // intercept
        {
            // TODO search for super class and interfaces
            EntityToJacoFunc f = config.entityToJacoMappings.get(o.getClass());
            if (f != null) {
                return f.map(o);
            }
        }

        if (o instanceof Long) {
            return o;
        } else if (o instanceof Integer) {
            return ((Integer) o).longValue();
        } else if (o instanceof Short) {
            return ((Short) o).longValue();
        } else if (o instanceof Byte) {
            return ((Byte) o).longValue();
        }

        if (o instanceof Float) {
            return ((Float) o).doubleValue();
        } else if (o instanceof Double) {
            return o;
        }

        if (o instanceof Boolean) {
            return o;
        }

        if (o instanceof String) {
            return o;
        } else if (o instanceof Character) {
            return o.toString();
        }

        if (o.getClass().isEnum()) {
            return o.toString();
        }

        if (o.getClass().isArray()) {
            List<Object> a = new LinkedList<>();
            for (int i = 0; i < Array.getLength(o); i++) {
                Object element = Array.get(o, i);
                a.add(fromEntity(element));
            }
            return a;
        } else if (o instanceof List) {
            List<?> list = (List<?>) o;
            List<Object> a = new LinkedList<>();
            for (Object element : list) {
                a.add(fromEntity(element));
            }
            return a;
        }

        if (o instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) o;
            Map<String, Object> m = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                Object value = fromEntity(entry.getValue());
                if (value == null) {
                    if (config.keepsNull) {
                        m.put(key, null);
                    }
                } else {
                    m.put(key, value);
                }
            }
            return m;
        } else {
            // a non-container object
            Map<String, Object> m = new LinkedHashMap<>();
            // public fields only
            for (Field field : o.getClass().getFields()) {
                int fieldModifiers = field.getModifiers();
                if (Modifier.isStatic(fieldModifiers) || Modifier.isTransient(fieldModifiers)) {
                    continue;
                }
                String key = field.getName();
                Object value;
                try {
                    value = fieldToJaco(field, o);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new ParsingException(e);
                }
                if (value == null) {
                    if (config.keepsNull) {
                        m.put(key, null);
                    }
                } else {
                    m.put(key, value);
                }
            }
            return m;
        }
    }

    private Object fieldToJaco(Field field, Object o) throws IllegalArgumentException, IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive()) {
            if (fieldType == long.class) {
                return field.getLong(o);
            } else if (fieldType == int.class) {
                return (long) field.getInt(o);
            } else if (fieldType == short.class) {
                return (long) field.getShort(o);
            } else if (fieldType == byte.class) {
                return (long) field.getByte(o);
            }

            if (fieldType == float.class) {
                return (double) field.getFloat(o);
            } else if (fieldType == double.class) {
                return field.getDouble(o);
            }

            if (fieldType == boolean.class) {
                return field.getBoolean(o);
            }

            if (fieldType == char.class) {
                return String.valueOf(field.getChar(o));
            }

            throw new ParsingException();
        }
        return fromEntity(field.get(o));
    }

    public static class Config {

        final LinkedHashMap<Class<?>, EntityToJacoFunc> entityToJacoMappings = new LinkedHashMap<>();

        public boolean keepsNull = false;

        public Config() {
            register(Instant.class, (entity) -> ((Instant) entity).toString());
        }

        public void register(Class<?> entityClass, EntityToJacoFunc f) {
            if (f == null) {
                throw new NullPointerException();
            }
            // XXX should log: swapped out `{}` => `{}`", key, old
            entityToJacoMappings.put(entityClass, f);
        }
    }

    public interface EntityToJacoFunc {
        Object map(Object entity);
    }
}
