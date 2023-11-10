package pd.codec.json.specializer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import pd.codec.json.datatype.Json;
import pd.codec.json.datatype.JsonArray;
import pd.codec.json.datatype.JsonObject;
import pd.fenc.ParsingException;
import pd.file.PathExtension;

public class Specializer {

    public final SpecializingConfig config;

    public Specializer(SpecializingConfig config) {
        this.config = config;
    }

    public <T> T convert(Json json, Class<T> targetClass) {
        try {
            return convert(json, "/", targetClass);
        } catch (Exception e) {
            throw new ParsingException(e);
        }
    }

    /**
     * convert Json things to Java things
     */
    @SuppressWarnings("unchecked")
    private <T> T convert(Json json, String path, Class<T> targetClass)
            throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        if (targetClass == null) {
            throw new IllegalArgumentException();
        }

        if (json == null) {
            return null;
        }

        Class<? extends T> mappedClass = config.find(json, path, targetClass);
        if (mappedClass == null) {
            if (targetClass == Object.class) {
                mappedClass = inferMappedClass(json);
            }
            if (mappedClass == null) {
                mappedClass = targetClass;
            }
        }

        Class<? extends T> mappedClass1 = config.find(json, path, mappedClass);
        if (mappedClass1 != null) {
            mappedClass = mappedClass1;
        }

        T result = inferBoxedPrimitiveOrStringInstance(json, mappedClass);
        if (result != null) {
            return result;
        }

        if (mappedClass.isInterface()) {
            throw new ParsingException("E: cannot instantiate an interface: " + mappedClass.getName());
        } else if (Modifier.isAbstract(mappedClass.getModifiers())) {
            throw new ParsingException("E: cannot instantiate an abstract class: " + mappedClass.getName());
        }

        if (mappedClass.isAssignableFrom(JsonArray.class)) {
            return (T) json.asJsonArray();
        } else if (mappedClass.isArray()) {
            JsonArray jsonArray = json.asJsonArray();
            Class<?> elementClass = mappedClass.getComponentType();
            Object array = Array.newInstance(elementClass, jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++) {
                String path1 = PathExtension.resolve(path, "[" + i + "]");
                Array.set(array, i, convert(jsonArray.get(i), path1, elementClass));
            }
            return (T) array;
        } else if (List.class.isAssignableFrom(mappedClass)) {
            JsonArray jsonArray = json.asJsonArray();
            Constructor<?> constructor = mappedClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            List<Object> list = (List<Object>) constructor.newInstance();
            for (int i = 0; i < jsonArray.size(); i++) {
                String path1 = PathExtension.resolve(path, "[" + i + "]");
                list.add(convert(jsonArray.get(i), path1, Object.class));
            }
            return (T) list;
        }

        if (mappedClass.isAssignableFrom(JsonObject.class)) {
            return (T) json.asJsonObject();
        } else if (Map.class.isAssignableFrom(mappedClass)) {
            JsonObject jsonObject = json.asJsonObject();
            Constructor<?> constructor = mappedClass.getDeclaredConstructor();
            Map<String, Object> map = (Map<String, Object>) constructor.newInstance();
            for (Map.Entry<String, Json> entry : jsonObject.entrySet()) {
                String path1 = PathExtension.resolve(path, "{" + entry.getKey() + "}");
                map.put(entry.getKey(), convert(entry.getValue(), path1, Object.class));
            }
            return (T) map;
        }

        // not array, not list, not map: that is a "normal" object
        JsonObject jsonObject = json.asJsonObject();
        Constructor<?> constructor = mappedClass.getDeclaredConstructor();
        Object object = constructor.newInstance();
        for (Field field : mappedClass.getFields()) {
            String fieldName = field.getName();
            if (jsonObject.containsKey(fieldName)) {
                field.setAccessible(true);
                String path1 = PathExtension.resolve(path, fieldName);
                field.set(object, convert(jsonObject.get(fieldName), path1, field.getType()));
            }
        }
        return (T) object;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> inferMappedClass(Json json) {
        switch (json.getJsonType()) {
            case NULL:
                return null;
            case BOOLEAN:
                return (Class<? extends T>) Boolean.class;
            case NUMBER: {
                return json.asJsonNumber().isRoundNumber()
                        ? (Class<? extends T>) Long.class
                        : (Class<? extends T>) Double.class;
            }
            case STRING:
                return (Class<? extends T>) String.class;
            case ARRAY:
                return (Class<? extends T>) List.class;
            case OBJECT:
                return (Class<? extends T>) Map.class;
            default:
                throw new ParsingException();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T inferBoxedPrimitiveOrStringInstance(Json json, Class<? extends T> mappedClass) {
        if (mappedClass == int.class || mappedClass == Integer.class) {
            return (T) (Integer) json.asJsonNumber().getInt32();
        } else if (mappedClass == long.class || mappedClass == Long.class) {
            return (T) (Long) json.asJsonNumber().getInt64();
        } else if (mappedClass == byte.class || mappedClass == Byte.class) {
            return (T) (Byte) (byte) json.asJsonNumber().getInt32();
        } else if (mappedClass == short.class || mappedClass == Short.class) {
            return (T) (Short) (short) json.asJsonNumber().getInt32();
        } else if (mappedClass == char.class || mappedClass == Character.class) {
            return (T) (Character) (char) json.asJsonNumber().getInt32();
        } else if (mappedClass == float.class || mappedClass == Float.class) {
            return (T) (Float) json.asJsonNumber().getFloat32();
        } else if (mappedClass == double.class || mappedClass == Double.class) {
            return (T) (Double) json.asJsonNumber().getFloat64();
        } else if (mappedClass == boolean.class || mappedClass == Boolean.class) {
            return (T) (Boolean) json.asJsonBoolean().getBoolean();
        } else if (mappedClass == String.class) {
            return (T) json.asJsonString().getString();
        }
        return null;
    }
}
