package libcliff.io;

public interface Pushable {

    /**
     * accept an int in [0, 255]
     */
    public Pushable push(int b);

    public Pushable push(byte[] a, int i, int j);
}
