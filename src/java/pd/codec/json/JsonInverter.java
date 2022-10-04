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
        assert targetClass != null;

        if (IJson.class.isAssignableFrom(targetClass)) {
            return targetClass.cast(json);
        }

        if (json == null) {
            return null;
        }

        if (json instanceof IJsonNull) {
            return null;
        }

        Object[] outValues = new Object[1];
        if (tryConvertToPrimitive(json, targetClass, outValues)) {
            return (T) outValues[0];
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
