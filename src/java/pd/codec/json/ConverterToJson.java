package pd.codec.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import pd.fenc.ParsingException;

class ConverterToJson {

    private final Config config;

    public ConverterToJson(Config config) {
        if (config == null) {
            throw new NullPointerException();
        }
        this.config = config;
    }

    /**
     * `Object` => `IJson`<br/>
     * adopt public fields only<br/>
     */
    @SuppressWarnings("unchecked")
    public IJson convertToJson(Object o) {
        if (o != null && IJson.class.isAssignableFrom(o.getClass())) {
            return (IJson) o;
        }

        if (o != null) {
            if (config.encoders.containsKey(o.getClass())) {
                return config.encoders.get(o.getClass()).convert(o);
            }
        }

        IJsonFactory factory = config.f;

        if (o == null) {
            return factory.getJsonNull();
        }
        if (o instanceof Byte) {
            return factory.createJsonNumber((Byte) o);
        }
        if (o instanceof Short) {
            return factory.createJsonNumber((Short) o);
        }
        if (o instanceof Integer) {
            return factory.createJsonNumber((Integer) o);
        }
        if (o instanceof Long) {
            return factory.createJsonNumber((Long) o);
        }
        if (o instanceof Float) {
            return factory.createJsonNumber((Float) o);
        }
        if (o instanceof Double) {
            return factory.createJsonNumber((Double) o);
        }
        if (o instanceof Boolean) {
            return factory.createJsonBoolean((Boolean) o);
        }
        if (o instanceof Character) {
            return factory.createJsonNumber((Character) o);
        }
        if (o instanceof String) {
            return factory.createJsonString((String) o);
        }
        if (o instanceof List) {
            IJsonArray a = factory.createJsonArray();
            for (Object element : (List<Object>) o) {
                a.add(convertToJson(element));
            }
            return a;
        }
        if (o.getClass().isArray()) {
            IJsonArray a = factory.createJsonArray();
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(o, i);
                a.add(convertToJson(element));
            }
            return a;
        }
        if (o instanceof Map) {
            IJsonObject jsonObject = factory.createJsonObject();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) o).entrySet()) {
                String key = entry.getKey().toString();
                jsonObject.put(key, convertToJson(entry.getValue()));
            }
            return jsonObject;
        }

        IJsonObject jsonObject = factory.createJsonObject();
        // public fields only
        for (Field field : o.getClass().getFields()) {
            int fieldModifiers = field.getModifiers();
            if (Modifier.isStatic(fieldModifiers) || Modifier.isTransient(fieldModifiers)) {
                continue;
            }
            String key = field.getName();
            try {
                jsonObject.put(key, convertToJson(o, field));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new ParsingException(e);
            }
        }
        return jsonObject;
    }

    private IJson convertToJson(Object o, Field field) throws IllegalArgumentException, IllegalAccessException {
        IJsonFactory factory = config.f;

        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive()) {
            if (fieldType == byte.class) {
                return factory.createJsonNumber(field.getByte(o));
            }
            if (fieldType == short.class) {
                return factory.createJsonNumber(field.getShort(o));
            }
            if (fieldType == int.class) {
                return factory.createJsonNumber(field.getInt(o));
            }
            if (fieldType == long.class) {
                return factory.createJsonNumber(field.getLong(o));
            }
            if (fieldType == float.class) {
                return factory.createJsonNumber(field.getFloat(o));
            }
            if (fieldType == double.class) {
                return factory.createJsonNumber(field.getDouble(o));
            }
            if (fieldType == boolean.class) {
                return factory.createJsonBoolean(field.getBoolean(o));
            }
            if (fieldType == char.class) {
                return factory.createJsonNumber(field.getChar(o));
            }
            throw new ParsingException();
        }
        return convertToJson(field.get(o));
    }
}
