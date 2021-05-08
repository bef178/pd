package pd.fenc;

public interface ICharReader {

    public boolean hasNext();

    /**
     * provide an unicode character
     */
    public int next();
}
