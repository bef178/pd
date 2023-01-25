package pd.codec.json.json2object;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import pd.codec.json.IJson;
import pd.codec.json.IJsonArray;
import pd.codec.json.IJsonNumber;
import pd.codec.json.IJsonObject;
import pd.fenc.ParsingException;
import pd.util.PathUtil;

public class JsonToObjectConverter {

    public final TypeMapping typeMapping;

    public JsonToObjectConverter(TypeMapping typeMapping) {
        this.typeMapping = typeMapping;
    }

    public <T> T convert(IJson json, Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException();
        }
        try {
            return convert(json, targetClass, "/");
        } catch (Exception e) {
            throw new ParsingException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(IJson json, Class<T> targetClass, String fieldPath)
            throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        if (targetClass == null) {
            throw new NullPointerException();
        }

        if (json == null) {
            return null;
        }

        // overwrite targetClass if not specified
        if (targetClass == Object.class) {
            switch (json.getJsonType()) {
                case NULL:
                    return null;
                case BOOLEAN:
                    targetClass = (Class<T>) Boolean.class;
                    break;
                case NUMBER: {
                    IJsonNumber jsonNumber = json.asJsonNumber();
                    targetClass = jsonNumber.isRoundNumber()
                            ? (Class<T>) Long.class
                            : (Class<T>) Double.class;
                    break;
                }
                case STRING:
                    targetClass = (Class<T>) String.class;
                    break;
                case ARRAY:
                    targetClass = (Class<T>) List.class;
                    break;
                case OBJECT:
                    targetClass = (Class<T>) Map.class;
                    break;
                default:
                    throw new ParsingException();
            }
        }

        // primitive and String
        if (targetClass == int.class || targetClass == Integer.class) {
            return (T) (Integer) json.asJsonNumber().getInt32();
        } else if (targetClass == long.class || targetClass == Long.class) {
            return (T) (Long) json.asJsonNumber().getInt64();
        } else if (targetClass == byte.class || targetClass == Byte.class) {
            return (T) (Byte) (byte) json.asJsonNumber().getInt32();
        } else if (targetClass == short.class || targetClass == Short.class) {
            return (T) (Short) (short) json.asJsonNumber().getInt32();
        } else if (targetClass == char.class || targetClass == Character.class) {
            return (T) (Character) (char) json.asJsonNumber().getInt32();
        } else if (targetClass == float.class || targetClass == Float.class) {
            return (T) (Float) json.asJsonNumber().getFloat32();
        } else if (targetClass == double.class || targetClass == Double.class) {
            return (T) (Double) json.asJsonNumber().getFloat64();
        } else if (targetClass == boolean.class || targetClass == Boolean.class) {
            return (T) (Boolean) json.asJsonBoolean().getBoolean();
        } else if (targetClass == String.class) {
            return (T) json.asJsonString().getString();
        }

        Class<? extends T> actualClass = typeMapping.find(targetClass, fieldPath, json);
        if (actualClass == null) {
            actualClass = targetClass;
        }

        if (actualClass.isInterface()) {
            throw new ParsingException("E: cannot instantiate an interface: " + actualClass.getName());
        } else if (Modifier.isAbstract(actualClass.getModifiers())) {
            throw new ParsingException("E: cannot instantiate an abstract class: " + actualClass.getName());
        }

        if (actualClass.isArray()) {
            IJsonArray jsonArray = json.asJsonArray();
            Class<?> elementClass = actualClass.getComponentType();
            Object array = Array.newInstance(elementClass, jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++) {
                String elementPath = PathUtil.resolve(fieldPath, "[" + i + "]");
                Array.set(array, i, convert(jsonArray.get(i), elementClass, elementPath));
            }
            return (T) array;
        } else if (List.class.isAssignableFrom(actualClass)) {
            if (actualClass.isAssignableFrom(IJsonArray.class)) {
                return (T) json.asJsonArray();
            }
            IJsonArray jsonArray = json.asJsonArray();
            Constructor<?> constructor = actualClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            List<Object> instance = (List<Object>) constructor.newInstance();
            for (int i = 0; i < jsonArray.size(); i++) {
                String elementPath = PathUtil.resolve(fieldPath, "[" + i + "]");
                instance.add(convert(jsonArray.get(i), Object.class, elementPath));
            }
            return (T) instance;
        }

        if (Map.class.isAssignableFrom(actualClass)) {
            if (actualClass.isAssignableFrom(IJsonObject.class)) {
                return (T) json.asJsonObject();
            }
            IJsonObject jsonObject = json.asJsonObject();
            Constructor<?> constructor = actualClass.getDeclaredConstructor();
            Map<String, Object> instance = (Map<String, Object>) constructor.newInstance();
            for (Map.Entry<String, IJson> entry : jsonObject.entrySet()) {
                String valuePath = PathUtil.resolve(fieldPath, "{" + entry.getKey() + "}");
                instance.put(entry.getKey(), convert(entry.getValue(), Object.class, valuePath));
            }
            return (T) instance;
        }

        // a non-container object
        IJsonObject jsonObject = json.asJsonObject();
        Constructor<?> constructor = actualClass.getDeclaredConstructor();
        Object instance = constructor.newInstance();
        for (Field field : actualClass.getFields()) {
            String fieldName = field.getName();
            if (jsonObject.containsKey(fieldName)) {
                field.setAccessible(true);
                field.set(instance, convert(jsonObject.get(fieldName), field.getType(), PathUtil.resolve(fieldPath, fieldName)));
            }
        }
        return (T) instance;
    }
}
