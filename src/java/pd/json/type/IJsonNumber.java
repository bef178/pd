package pd.json.type;

public interface IJsonNumber extends IJson {

    public default float getFloat32() {
        return (float) getFloat64();
    }

    public double getFloat64();

    public default int getInt32() {
        return (int) getInt64();
    }

    public long getInt64();

    @Override
    public default JsonType getJsonType() {
        return JsonType.NUMBER;
    }

    public IJsonNumber set(double value);

    public IJsonNumber set(long value);

    public IJsonNumber set(String raw);
}
