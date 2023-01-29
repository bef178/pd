package pd.codec.json.object2json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import pd.codec.json.IJson;
import pd.codec.json.IJsonArray;
import pd.codec.json.IJsonBoolean;
import pd.codec.json.IJsonFactory;
import pd.codec.json.IJsonNumber;
import pd.codec.json.IJsonObject;
import pd.fenc.ParsingException;

public class ObjectToJsonConverter {

    private final IJsonFactory f;

    private final JsonMapping jsonMapping;

    public ObjectToJsonConverter(IJsonFactory factory, JsonMapping jsonMapping) {
        this.f = factory;
        this.jsonMapping = jsonMapping;
    }

    /**
     * `Object` => `IJson`<br/>
     * adopt public fields only<br/>
     */
    public IJson convert(Object o) {
        if (o == null) {
            return f.getJsonNull();
        }

        if (jsonMapping.refs.containsKey(o.getClass())) {
            return jsonMapping.refs.get(o.getClass()).convert(o);
        }

        if (o instanceof IJson) {
            return (IJson) o;
        }

        if (o instanceof Integer) {
            return f.createJsonNumber((Integer) o);
        } else if (o instanceof Long) {
            return f.createJsonNumber((Long) o);
        } else if (o instanceof Byte) {
            return f.createJsonNumber((Byte) o);
        } else if (o instanceof Short) {
            return f.createJsonNumber((Short) o);
        } else if (o instanceof Character) {
            return f.createJsonNumber((Character) o);
        }

        if (o instanceof Float) {
            return f.createJsonNumber((Float) o);
        } else if (o instanceof Double) {
            return f.createJsonNumber((Double) o);
        }

        if (o instanceof Boolean) {
            return f.createJsonBoolean((Boolean) o);
        }

        if (o instanceof String) {
            return f.createJsonString((String) o);
        }

        if (o.getClass().isArray()) {
            IJsonArray jsonArray = f.createJsonArray();
            for (int i = 0; i < Array.getLength(o); i++) {
                Object element = Array.get(o, i);
                jsonArray.add(convert(element));
            }
            return jsonArray;
        } else if (o instanceof List) {
            List<?> list = (List<?>) o;
            IJsonArray jsonArray = f.createJsonArray();
            for (Object element : list) {
                jsonArray.add(convert(element));
            }
            return jsonArray;
        }

        if (o instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) o;
            IJsonObject jsonObject = f.createJsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                jsonObject.put(key, convert(entry.getValue()));
            }
            return jsonObject;
        } else {
            // a non-container object
            IJsonObject jsonObject = f.createJsonObject();
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

    private IJson convertField(Field field, Object o) throws IllegalArgumentException, IllegalAccessException {
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

    public IJsonNumber convert(long value) {
        return f.createJsonNumber(value);
    }

    public IJsonNumber convert(double value) {
        return f.createJsonNumber(value);
    }

    public IJsonBoolean convert(boolean value) {
        return f.createJsonBoolean(value);
    }
}
