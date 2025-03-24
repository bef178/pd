package pd.injector;

import java.lang.reflect.Field;
import java.util.Comparator;

import pd.injector.annotation.Managed;
import pd.util.ObjectExtension;

class PrioritizedClassField {

    public static final Comparator<PrioritizedClassField> comparator = Comparator
            .comparingInt((PrioritizedClassField o) -> o.classAnnotation != null ? o.classAnnotation.priority() : 100)
            .thenComparing(o -> o.instance.getClass().getCanonicalName())
            .thenComparingInt(o -> !o.fieldAnnotation.value().isEmpty() ? 0 : 1)
            .thenComparingInt(o -> o.fieldAnnotation.priority());

    public Object instance;

    public Field field;

    public Managed classAnnotation;

    public Managed fieldAnnotation;

    public void assign(Object value) {
        try {
            field.setAccessible(true);
            Class<?> targetClass = field.getType();
            if (targetClass == long.class) {
                targetClass = Long.class;
            } else if (targetClass == int.class) {
                targetClass = Integer.class;
            } else if (targetClass == short.class) {
                targetClass = Short.class;
            } else if (targetClass == byte.class) {
                targetClass = Byte.class;
            } else if (targetClass == double.class) {
                targetClass = Double.class;
            } else if (targetClass == float.class) {
                targetClass = Float.class;
            } else if (targetClass == boolean.class) {
                targetClass = Boolean.class;
            } else if (targetClass == char.class) {
                targetClass = Character.class;
            }
            field.set(instance, ObjectExtension.convert(value, targetClass));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to set field \"" + field.getName() + "\"", e);
        }
    }
}
