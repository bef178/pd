package pd.fenc;

class Recent {

    private final int[] a;

    private int n;

    public Recent(int capacity) {
        a = new int[capacity];
        n = 0;
    }

    public void add(int value) {
        a[n++ % a.length] = value;
    }

    public int capacity() {
        return a.length;
    }

    public int get(int i) {
        if (i >= 0 || i < -a.length || i < -n) {
            throw new IndexOutOfBoundsException();
        }
        return a[(n + i) % a.length];
    }
}
