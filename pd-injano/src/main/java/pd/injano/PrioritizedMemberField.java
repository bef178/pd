package pd.injano;

import java.lang.reflect.Field;
import java.util.Comparator;

class PrioritizedMemberField {

    public static final Comparator<PrioritizedMemberField> comparator = new Comparator<PrioritizedMemberField>() {

        @Override
        public int compare(PrioritizedMemberField o1, PrioritizedMemberField o2) {
            int d = o1.priority - o2.priority;
            if (d != 0) {
                return d;
            }
            return Comparator.comparing(String::toString).compare(
                    o1.instance.getClass().getCanonicalName(),
                    o2.instance.getClass().getCanonicalName());
        }
    };

    public Field field;

    public Object instance;

    public int priority;

    public void assign(Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to set field \"" + field.getName() + "\"", e);
        }
    }
}
