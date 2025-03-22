package pd.injector;

import java.util.Comparator;

class PrioritizedClass {

    public static final Comparator<PrioritizedClass> comparator = Comparator
            .comparingInt((PrioritizedClass o) -> o.priority)
            .thenComparing(o -> o.clazz.getCanonicalName());

    public Class<?> clazz;

    public int priority;
}
