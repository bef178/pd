package pd.fenc.json;

public interface IJsonFloat extends IJsonInt {

    public void set(double value);

    /**
     * 1,8,23
     */
    public float valueToFloat32();

    /**
     * 1,11,52
     */
    public double valueToFoat64();
}
