package pd.injano;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

class PrioritizedMemberMethod {

    public static final Comparator<PrioritizedMemberMethod> comparator = new Comparator<PrioritizedMemberMethod>() {

        @Override
        public int compare(PrioritizedMemberMethod o1, PrioritizedMemberMethod o2) {
            int d = o1.priority - o2.priority;
            if (d != 0) {
                return d;
            }
            return Comparator.comparing(String::toString).compare(
                    o1.instance.getClass().getCanonicalName(),
                    o2.instance.getClass().getCanonicalName());
        }
    };

    public Method method;

    public Object instance;

    public int priority;

    public void invoke() {
        try {
            method.setAccessible(true);
            method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke @OnConstructed method \"" + method.getName() + "\"", e);
        }
    }
}
