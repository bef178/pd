package pd.util;

class IntNode<E> extends IntEntry<E> {

    IntNode<E> next = null;

    public IntNode(int key) {
        this(key, null);
    }

    public IntNode(int key, E value) {
        super(key);
        this.value = value;
    }
}
