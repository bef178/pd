package pd.util;

public class ObjectExtension {

    public static <T> T convert(Object o, Class<T> targetClass) {
        if (o == null) {
            return null;
        }

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
                return targetClass.cast(o1.shortValue());
            } else if (targetClass == Double.class) {
                return targetClass.cast(o1.doubleValue());
            } else if (targetClass == Float.class) {
                return targetClass.cast(o1.floatValue());
            }
        }

        // XXX try json rebuild?
        throw new RuntimeException(String.format("NotConvertible: cannot convert `%s` to `%s`", o.getClass().getName(), targetClass.getName()));
    }
}
