package pd.codec.json;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JsonInverter {

    /**
     * `IJson` => `Object`<br/>
     */
    @SuppressWarnings("unchecked")
    public <T> T convertToJava(IJson json, Class<T> targetClass) {
        if (IJson.class.isAssignableFrom(targetClass)) {
            return targetClass.cast(json);
        }

        if (json instanceof IJsonNull) {
            return null;
        }

        if (targetClass == boolean.class || targetClass == Boolean.class) {
            return (T) (Boolean) IJsonBoolean.class.cast(json).getBoolean();
        }

        if (targetClass == byte.class || targetClass == Byte.class
                || targetClass == char.class || targetClass == Character.class
                || targetClass == short.class || targetClass == Short.class
                || targetClass == int.class || targetClass == Integer.class
                || targetClass == long.class || targetClass == Long.class) {
            return (T) (Long) IJsonNumber.class.cast(json).getInt64();
        }

        if (targetClass == float.class || targetClass == Float.class
                || targetClass == double.class || targetClass == Double.class) {
            return (T) (Double) IJsonNumber.class.cast(json).getFloat64();
        }

        if (targetClass == String.class) {
            return (T) IJsonString.class.cast(json).getString();
        }

        if (List.class.isAssignableFrom(targetClass)) {
            // TODO introduce TypeRegister
            throw new UnsupportedOperationException();
        } else if (targetClass.isArray()) {
            IJsonArray jsonArray = IJsonArray.class.cast(json);
            Class<?> elementClass = targetClass.getComponentType();
            Object array = Array.newInstance(elementClass, jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++) {
                Array.set(array, i, convertToJava(jsonArray.get(i), elementClass));
            }
            return (T) array;
        }

        if (Map.class.isAssignableFrom(targetClass)) {
            if (targetClass.isAssignableFrom(LinkedHashMap.class)) {
            }
            // TODO
            throw new UnsupportedOperationException();
        }

        // TODO Object
        throw new UnsupportedOperationException();
    }
}
