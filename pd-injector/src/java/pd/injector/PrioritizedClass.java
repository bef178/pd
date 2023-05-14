package pd.injector;

import java.util.Comparator;

class PrioritizedClass {

    public static final Comparator<PrioritizedClass> comparator = new Comparator<PrioritizedClass>() {

        @Override
        public int compare(PrioritizedClass o1, PrioritizedClass o2) {
            int d = o1.priority - o2.priority;
            if (d != 0) {
                return d;
            }
            return Comparator.comparing(String::toString).compare(
                    o1.clazz.getCanonicalName(),
                    o2.clazz.getCanonicalName());
        }
    };

    public Class<?> clazz;

    public int priority;
}
