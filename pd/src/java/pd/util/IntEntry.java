package pd.util;

public class IntEntry<E> {

    public final int key;

    public E value;

    public IntEntry(int key) {
        this(key, null);
    }

    public IntEntry(int key, E value) {
        this.key = key;
        this.value = value;
    }
}
