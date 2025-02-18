package pd.util;

import java.util.ArrayList;

public class IntQueue {

    private final ArrayList<IntEntry<Void>> a;

    private final int capacity;

    private final QueueFullPolicy whenFull;

    public IntQueue() {
        this(null, QueueFullPolicy.SQUEEZE_OUT);
    }

    public IntQueue(int capacity) {
        this(capacity, QueueFullPolicy.SQUEEZE_OUT);
    }

    public IntQueue(Integer capacity, QueueFullPolicy whenFull) {
        if (capacity != null && capacity <= 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + capacity);
        }
        if (whenFull == null) {
            throw new IllegalArgumentException();
        }
        if (capacity == null) {
            capacity = 0;
        }
        this.capacity = capacity;
        this.whenFull = whenFull;
        this.a = new ArrayList<>(capacity);
    }

    public int capacity() {
        return capacity;
    }

    public int size() {
        return a.size();
    }

    public boolean isEmpty() {
        return a.isEmpty();
    }

    public int get(int i) {
        return a.get(i).key;
    }

    public void pushHead(int value) {
        if (capacity > 0 && a.size() == capacity) {
            switch (whenFull) {
                case SQUEEZE_OUT:
                    removeTail();
                    break;
                case THROW:
                    throw new RuntimeException("Queue full");
                default:
                    throw new IllegalStateException();
            }
        }
        a.add(0, new IntEntry<>(value));
    }

    /**
     * will remove head if capacity reached
     */
    public void pushTail(int value) {
        if (capacity > 0 && a.size() == capacity) {
            switch (whenFull) {
                case SQUEEZE_OUT:
                    removeHead();
                    break;
                case THROW:
                    throw new RuntimeException("Queue full");
                default:
                    throw new IllegalStateException();
            }
        }
        a.add(new IntEntry<>(value));
    }

    public void append(int value) {
        pushTail(value);
    }

    public int removeHead() {
        return a.remove(0).key;
    }

    public int removeTail() {
        return a.remove(a.size() - 1).key;
    }

    public int remove() {
        return removeHead();
    }

    public enum QueueFullPolicy {
        SQUEEZE_OUT,
        THROW;
    }
}
