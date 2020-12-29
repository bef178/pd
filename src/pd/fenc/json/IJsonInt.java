package pd.fenc.json;

public interface IJsonInt extends IJsonValue {

    public void set(long value);

    public int valueToInt32();

    public long valueToInt64();
}
