package libcliff.io;

public interface Nextable {

    boolean hasNext();

    /**
     * return an int in [0, 255]
     */
    int next();

    int peek();
}
