package pd.json;

public interface IJsonFloat extends IJsonToken {

    public void set(double value);

    /**
     * 1:8:23
     */
    public float float32();

    /**
     * 1:11:52
     */
    public double float64();
}
