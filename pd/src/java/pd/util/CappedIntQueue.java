package pd.util;

public class CappedIntQueue extends IntQueue {

    private final int capacity;

    public CappedIntQueue(int capacity) {
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    public void pushHead(int value) {
        super.pushHead(value);
        if (size() > capacity) {
            removeTail();
        }
    }

    public void append(int value) {
        super.append(value);
        if (size() > capacity) {
            remove();
        }
    }
}
