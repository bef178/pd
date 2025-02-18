package pd.fenc;

public interface Int8Provider {

    boolean hasNext();

    /**
     * values in [-0x80, 0x7F]
     */
    int next();

    int position();
}
