package pd.fenc;

public interface Int8Provider extends Int32Provider {

    /**
     * values in [-0x80, 0x7F]
     */
    int next();
}
