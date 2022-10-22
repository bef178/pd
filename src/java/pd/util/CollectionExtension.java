package pd.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * naming: Extension having some methods those cannot be placed in host class for some reason
 */
public class CollectionExtension<E> {

    /**
     * one += another
     */
    public static <E> void merge(Collection<E> one, Collection<E> another) {
        Iterator<E> it = another.iterator();
        while (it.hasNext()) {
            E e = it.next();
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
        Iterator<E> it = one.iterator();
        while (it.hasNext()) {
            if (!another.contains(it.next())) {
                it.remove();
            }
        }
    }

    private CollectionExtension() {
        // private dummy
    }
}
