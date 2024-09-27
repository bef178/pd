package pd.util;

import lombok.NonNull;

public class ObjectExtension {

    /**
     * return `null` iff cannot convert
     */
    public static <T> T convert(@NonNull Object o, Class<T> targetClass) {
        if (targetClass.isAssignableFrom(o.getClass())) {
            return targetClass.cast(o);
        }

        if (o instanceof Number) {
            Number o1 = (Number) o;
            if (targetClass == Long.class) {
                return targetClass.cast(o1.longValue());
            } else if (targetClass == Integer.class) {
                return targetClass.cast(o1.intValue());
            } else if (targetClass == Short.class) {
                return targetClass.cast(o1.shortValue());
            } else if (targetClass == Byte.class) {
                return targetClass.cast(o1.byteValue());
            } else if (targetClass == Double.class) {
                return targetClass.cast(o1.doubleValue());
            } else if (targetClass == Float.class) {
                return targetClass.cast(o1.floatValue());
            }
            if (targetClass == Character.class) {
                return targetClass.cast((char) o1.shortValue());
            }
        }

        if (o instanceof Boolean) {
            if (targetClass == Boolean.class) {
                return targetClass.cast(o);
            }
        }

        if (o instanceof Character) {
            if (targetClass == Long.class) {
                int value = Character.getNumericValue((Character) o);
                return targetClass.cast((long) value);
            } else if (targetClass == Integer.class) {
                int value = Character.getNumericValue((Character) o);
                return targetClass.cast(value);
            } else if (targetClass == Short.class) {
                int value = Character.getNumericValue((Character) o);
                return targetClass.cast((short) value);
            } else if (targetClass == Byte.class) {
                int value = Character.getNumericValue((Character) o);
                return targetClass.cast((byte) value);
            } else if (targetClass == Double.class) {
                int value = Character.getNumericValue((Character) o);
                return targetClass.cast((double) value);
            } else if (targetClass == Float.class) {
                int value = Character.getNumericValue((Character) o);
                return targetClass.cast((float) value);
            }
            if (targetClass == Character.class) {
                return targetClass.cast(o);
            }
            if (targetClass == String.class) {
                return targetClass.cast(o.toString());
            }
        }

        if (o instanceof String) {
            if (targetClass == String.class) {
                return targetClass.cast(o);
            }
        }

        return null;
    }
}
