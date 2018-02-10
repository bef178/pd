package libcliff.io;

import java.util.Arrays;

public final class Blob extends BytePipe implements Nextable {

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
    protected int pullByte() {
        return next();
    }

    public Blob push(byte[] a, int i, int j) {
        while (i < j) {
            push(a[i++]);
        }
        return this;
    }

    @Override
    protected int pushByte(int aByte) {
        a[i++] = (byte) (aByte & 0xFF);
        return 1;
    }

    public void rewind() {
        this.i = 0;
    }
}
