package cc.typedef.adt;

import cc.typedef.io.Nextable;

public final class Blob implements Nextable {

    public byte[] a = null;

    public int i = 0;

    public Blob() {
        // dummy
    }

    public Blob(int n) {
        init(n);
    }

    public Blob(byte[] a, int i) {
        this.a = a;
        this.i = i;
    }

    @Override
    public boolean hasNext() {
        return i >= 0 && i < a.length;
    }

    public void init(int n) {
        assert n >= 0;
        a = new byte[n];
        i = 0;
    }

    public boolean isEmpty() {
        return a == null || a.length == 0;
    }

    @Override
    public int next() {
        return a[i++] & 0xFF;
    }

    public void next(byte b) {
        a[i++] = b;
    }

    @Override
    public int peek() {
        return a[i] & 0xFF;
    }
}
