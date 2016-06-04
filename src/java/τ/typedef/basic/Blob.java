package τ.typedef.basic;

public final class Blob {

    public byte[] a = null;

    public int i = 0;

    public Blob() {
        // dummy
    }

    public Blob(byte[] a, int i) {
        this.a = a;
        this.i = i;
    }

    public void init(int n) {
        assert n >= 0;
        a = new byte[n];
        i = 0;
    }

    public boolean isEmpty() {
        return a == null || a.length == 0;
    }

    public byte next() {
        return a[i++];
    }

    public void next(byte b) {
        a[i++] = b;
    }
}
