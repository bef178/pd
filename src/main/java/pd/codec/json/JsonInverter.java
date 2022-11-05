package pd.codec.json;

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
import pd.fenc.TextNumber;
import pd.util.PathUtil;

class JsonInverter {

    private final JsonTypeConfig config;

    public JsonInverter(JsonTypeConfig config) {
        assert config != null;
        this.config = config;
    }

    /**
     * `IJson` => `Object`<br/>
     */
    public <T> T convertToJava(IJson json, Class<T> targetClass) {
        assert targetClass != null;
        try {
            return convertToJava(json, targetClass, "/");
        } catch (Exception e) {
            throw new ParsingException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToJava(IJson json, Class<T> targetClass, String path)
            throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        // `targetClass` might be `null`

        if (json == null || json.getJsonType() == JsonType.NULL) {
            return null;
        }

        Class<?> indeedClass = config.findPathRef(path);
        if (indeedClass == null) {
            indeedClass = config.findTypeRef(targetClass);
        }
        if (indeedClass != null) {
            if (targetClass != null) {
                // check compatibility
                if (!targetClass.isAssignableFrom(indeedClass)) {
                    throw new ParsingException(String.format(
                            "E: mismatched type: %s => %s", targetClass.getName(), indeedClass.getName()));
                }
            }
        } else {
            if (targetClass != null) {
                indeedClass = targetClass;
            } else {
                indeedClass = getDefaultImplementationType(json.getJsonType());
            }
        }

        if (indeedClass.isAssignableFrom(json.getClass())) {
            return (T) json;
        }

        if (indeedClass.isInterface()) {
            throw new ParsingException("E: shall not instantiate an interface: " + indeedClass.getName());
        } else if (Modifier.isAbstract(indeedClass.getModifiers())) {
            throw new ParsingException("E: shall not instantiate an abstract class: " + indeedClass.getName());
        }

        Object[] outValues = new Object[1];
        if (tryConvertToPrimitive(json, indeedClass, outValues)) {
            return (T) outValues[0];
        }

        if (indeedClass.isArray()) {
            IJsonArray jsonArray = json.asJsonArray();
            Class<?> elementClass = indeedClass.getComponentType();
            Object array = Array.newInstance(elementClass, jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++) {
                String elementPath = PathUtil.resolve(path, "[" + i + "]");
                Array.set(array, i, convertToJava(jsonArray.get(i), elementClass, elementPath));
            }
            return (T) array;
        }

        if (List.class.isAssignableFrom(indeedClass)) {
            if (indeedClass.isAssignableFrom(IJsonArray.class)) {
                return (T) json.asJsonArray();
            }
            IJsonArray jsonArray = json.asJsonArray();
            Constructor<?> constructor = indeedClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            List<?> instance = (List<?>) constructor.newInstance();
            for (int i = 0; i < jsonArray.size(); i++) {
                String elementPath = PathUtil.resolve(path, "[" + i + "]");
                instance.add(convertToJava(jsonArray.get(i), null, elementPath));
            }
            return (T) instance;
        }

        if (Map.class.isAssignableFrom(indeedClass)) {
            if (indeedClass.isAssignableFrom(IJsonObject.class)) {
                return (T) json.asJsonObject();
            }
            IJsonObject jsonObject = json.asJsonObject();
            Constructor<?> constructor = indeedClass.getDeclaredConstructor();
            Map<String, ?> instance = (Map<String, ?>) constructor.newInstance();
            for (Map.Entry<String, IJson> entry : jsonObject.entrySet()) {
                String fieldPath = PathUtil.resolve(path, entry.getKey());
                instance.put(entry.getKey(), convertToJava(entry.getValue(), null, fieldPath));
            }
            return (T) instance;
        }

        // object
        IJsonObject jsonObject = json.asJsonObject();
        Constructor<?> constructor = indeedClass.getDeclaredConstructor();
        Object instance = constructor.newInstance();
        for (Field field : indeedClass.getFields()) {
            String fieldName = field.getName();
            if (jsonObject.containsKey(fieldName)) {
                field.setAccessible(true);
                String fieldPath = PathUtil.resolve(path, fieldName);
                Object fieldValue = convertToJava(jsonObject.get(fieldName), field.getType(), fieldPath);
                field.set(instance, fieldValue);
            }
        }
        return (T) instance;
    }

    private Class<?> getDefaultImplementationType(JsonType jsonType) {
        assert jsonType != null;
        switch (jsonType) {
            case NULL:
                return Object.class;
            case BOOLEAN:
                return Boolean.class;
            case NUMBER:
                return TextNumber.class;
            case STRING:
                return String.class;
            case ARRAY:
                return ArrayList.class;
            case OBJECT:
                return LinkedHashMap.class;
            default:
                throw new ParsingException();
        }
    }

    /**
     * `outValues` should consist of 1 element<br/>
     */
    private boolean tryConvertToPrimitive(IJson json, Class<?> targetClass, Object[] outValues) {
        if (targetClass == boolean.class || targetClass == Boolean.class) {
            outValues[0] = json.asJsonBoolean().getBoolean();
            return true;
        }

        if (targetClass == byte.class || targetClass == Byte.class) {
            outValues[0] = (byte) json.asJsonNumber().getInt32();
            return true;
        }

        if (targetClass == char.class || targetClass == Character.class) {
            outValues[0] = (char) json.asJsonNumber().getInt32();
            return true;
        }

        if (targetClass == short.class || targetClass == Short.class) {
            outValues[0] = (short) json.asJsonNumber().getInt32();
            return true;
        }

        if (targetClass == int.class || targetClass == Integer.class) {
            outValues[0] = json.asJsonNumber().getInt32();
            return true;
        }

        if (targetClass == long.class || targetClass == Long.class) {
            outValues[0] = json.asJsonNumber().getInt64();
            return true;
        }

        if (targetClass == float.class || targetClass == Float.class) {
            outValues[0] = json.asJsonNumber().getFloat32();
            return true;
        }

        if (targetClass == double.class || targetClass == Double.class) {
            outValues[0] = json.asJsonNumber().getFloat64();
            return true;
        }

        if (targetClass == String.class) {
            outValues[0] = json.asJsonString().getString();
            return true;
        }
        return false;
    }
}
