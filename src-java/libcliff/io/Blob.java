package libcliff.io;

import java.util.Arrays;

public final class Blob implements Nextable, BytePipe {

    public byte[] a = null;

    public int i = 0;

    public Blob(byte[] a, int i) {
        this.a = a;
        this.i = i;
    }

    public Blob(int n) {
        init(n);
    }

    public byte[] getBytes() {
        return Arrays.copyOfRange(a, 0, i);
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

    @Override
    public int peek() {
        return a[i] & 0xFF;
    }

    @Override
    public int pull() {
        return next();
    }

    public Blob push(byte[] a, int i, int j) {
        while (i < j) {
            push(a[i++]);
        }
        return this;
    }

    @Override
    public int push(int ch) {
        a[i++] = (byte) (ch & 0xFF);
        return 1;
    }

    public void rewind() {
        this.i = 0;
    }
}
