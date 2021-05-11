package pd.fenc;

public interface IWriter {

    public int position();

    public IWriter push(int value);
}
