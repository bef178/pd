package pd.util;

import java.util.ArrayList;

public class IntQueue {

    private final ArrayList<IntItem> a;

    public IntQueue() {
        a = new ArrayList<>(64);
    }

    public int size() {
        return a.size();
    }

    public boolean isEmpty() {
        return a.isEmpty();
    }

    public void pushHead(int value) {
        a.add(0, new IntItem(value));
    }

    public void append(int value) {
        a.add(new IntItem(value));
    }

    public int get(int i) {
        return a.get(i).value;
    }

    public int remove() {
        return a.remove(0).value;
    }

    public int removeTail() {
        return a.remove(a.size() - 1).value;
    }

    private static final class IntItem {

        public final int value;

        public IntItem(int value) {
            this.value = value;
        }
    }
}
