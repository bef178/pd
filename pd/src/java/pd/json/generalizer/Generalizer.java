package pd.json.generalizer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import pd.fenc.ParsingException;
import pd.json.datafactory.JsonFactory;
import pd.json.datatype.Json;
import pd.json.datatype.JsonArray;
import pd.json.datatype.JsonBoolean;
import pd.json.datatype.JsonNumber;
import pd.json.datatype.JsonObject;

public class Generalizer {

    private final JsonFactory jsonFactory;

    private final GeneralizingConfig config;

    public Generalizer(JsonFactory jsonFactory, GeneralizingConfig config) {
        this.jsonFactory = jsonFactory;
        this.config = config;
    }

    /**
     * convert Java things to Json things<br/>
     * adopt public fields only<br/>
     */
    public Json convert(Object o) {
        if (o == null) {
            return jsonFactory.getJsonNull();
        }

        // intercept
        {
            Class<?> runtimeClass = o.getClass();
            // TODO search for super class and interfaces
            if (config.refs.containsKey(runtimeClass)) {
                return config.refs.get(runtimeClass).map(runtimeClass, o);
            }
        }

        if (o instanceof Json) {
            return (Json) o;
        }

        if (o instanceof Integer) {
            return jsonFactory.createJsonNumber((Integer) o);
        } else if (o instanceof Long) {
            return jsonFactory.createJsonNumber((Long) o);
        } else if (o instanceof Byte) {
            return jsonFactory.createJsonNumber((Byte) o);
        } else if (o instanceof Short) {
            return jsonFactory.createJsonNumber((Short) o);
        } else if (o instanceof Character) {
            return jsonFactory.createJsonNumber((Character) o);
        }

        if (o instanceof Float) {
            return jsonFactory.createJsonNumber((Float) o);
        } else if (o instanceof Double) {
            return jsonFactory.createJsonNumber((Double) o);
        }

        if (o instanceof Boolean) {
            return jsonFactory.createJsonBoolean((Boolean) o);
        }

        if (o instanceof String) {
            return jsonFactory.createJsonString((String) o);
        }

        if (o.getClass().isArray()) {
            JsonArray jsonArray = jsonFactory.createJsonArray();
            for (int i = 0; i < Array.getLength(o); i++) {
                Object element = Array.get(o, i);
                jsonArray.add(convert(element));
            }
            return jsonArray;
        } else if (o instanceof List) {
            List<?> list = (List<?>) o;
            JsonArray jsonArray = jsonFactory.createJsonArray();
            for (Object element : list) {
                jsonArray.add(convert(element));
            }
            return jsonArray;
        }

        if (o instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) o;
            JsonObject jsonObject = jsonFactory.createJsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                jsonObject.put(key, convert(entry.getValue()));
            }
            return jsonObject;
        } else {
            // a non-container object
            JsonObject jsonObject = jsonFactory.createJsonObject();
            // public fields only
            for (Field field : o.getClass().getFields()) {
                int fieldModifiers = field.getModifiers();
                if (Modifier.isStatic(fieldModifiers) || Modifier.isTransient(fieldModifiers)) {
                    continue;
                }
                String key = field.getName();
                try {
                    jsonObject.put(key, convertField(field, o));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new ParsingException(e);
                }
            }
            return jsonObject;
        }
    }

    private Json convertField(Field field, Object o) throws IllegalArgumentException, IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive()) {
            if (fieldType == int.class) {
                return convert(field.getInt(o));
            } else if (fieldType == long.class) {
                return convert(field.getLong(o));
            } else if (fieldType == byte.class) {
                return convert(field.getByte(o));
            } else if (fieldType == short.class) {
                return convert(field.getShort(o));
            } else if (fieldType == char.class) {
                return convert(field.getChar(o));
            }

            if (fieldType == float.class) {
                return convert(field.getFloat(o));
            } else if (fieldType == double.class) {
                return convert(field.getDouble(o));
            }

            if (fieldType == boolean.class) {
                return convert(field.getBoolean(o));
            }

            throw new ParsingException();
        }
        return convert(field.get(o));
    }

    public JsonNumber convert(long value) {
        return jsonFactory.createJsonNumber(value);
    }

    public JsonNumber convert(double value) {
        return jsonFactory.createJsonNumber(value);
    }

    public JsonBoolean convert(boolean value) {
        return jsonFactory.createJsonBoolean(value);
    }
}
