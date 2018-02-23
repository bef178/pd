package libcliff.io;

import java.util.Arrays;

public final class Blob implements Pushable {

    public static Blob wrap(byte[] a, int i) {
        Blob o = new Blob();
        o.a = a;
        o.i = i;
        return o;
    }

    public byte[] a = null;

    public int i = 0;

    private Blob() {
        // dummy
    }

    public Blob(int n) {
        assert n >= 0;
        a = new byte[n];
        i = 0;
    }

    public byte[] getBytes() {
        return Arrays.copyOfRange(a, 0, i);
    }

    /**
     * accept ch mapped into [0,0xFF]
     */
    @Override
    public int push(int ch) {
        a[i++] = (byte) (ch & 0xFF);
        return 1;
    }

    public void wipe() {
        Arrays.fill(a, (byte) 0);
        this.i = 0;
    }
}
