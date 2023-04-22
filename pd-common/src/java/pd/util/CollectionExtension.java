package pd.util;

import java.util.Collection;

public class CollectionExtension<E> {

    /**
     * one += another
     */
    public static <E> void merge(Collection<E> one, Collection<E> another) {
        for (E e : another) {
            if (!one.contains(e)) {
                one.add(e);
            }
        }
    }

    /**
     * one -= another
     */
    public static <E> void toComplement(Collection<E> one, Collection<E> another) {
        one.removeAll(another);
    }

    /**
     * one &= another
     */
    public static <E> void toIntersection(Collection<E> one, Collection<E> another) {
        one.removeIf(e -> !another.contains(e));
    }

    private CollectionExtension() {
        // private dummy
    }
}
