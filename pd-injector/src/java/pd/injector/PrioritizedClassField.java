package pd.injector;

import java.lang.reflect.Field;
import java.util.Comparator;

import pd.injector.annotation.Managed;

class PrioritizedClassField {

    public static final Comparator<PrioritizedClassField> comparator = Comparator
            .comparingInt((PrioritizedClassField o) -> o.classAnnotation.priority())
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
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to set field \"" + field.getName() + "\"", e);
        }
    }
}
