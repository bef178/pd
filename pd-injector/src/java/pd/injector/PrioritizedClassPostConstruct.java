package pd.injector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

import pd.injector.annotation.Managed;

class PrioritizedClassPostConstruct {

    public static final Comparator<PrioritizedClassPostConstruct> comparator = Comparator
            .comparingInt((PrioritizedClassPostConstruct o) -> o.classAnnotation.priority())
            .thenComparing(o -> o.instance.getClass().getCanonicalName());

    public Object instance;

    public Method method;

    public Managed classAnnotation;

    public void invoke() {
        try {
            method.setAccessible(true);
            method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke @OnConstructed method \"" + method.getName() + "\"", e);
        }
    }
}
