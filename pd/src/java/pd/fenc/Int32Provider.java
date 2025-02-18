package pd.fenc;

public interface Int32Provider {

    boolean hasNext();

    /**
     * values in [-0x80000000, 0x7FFFFFFF]
     */
    int next();

    int position();
}
