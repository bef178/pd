package pd.codec.json.datatype;

import pd.codec.json.JsonType;

public interface IJsonNumber extends IJson {

    default float getFloat32() {
        return (float) getFloat64();
    }

    double getFloat64();

    default int getInt32() {
        return (int) getInt64();
    }

    long getInt64();

    boolean isRoundNumber();

    @Override
    default JsonType getJsonType() {
        return JsonType.NUMBER;
    }

    IJsonNumber set(double value);

    IJsonNumber set(long value);

    IJsonNumber set(String raw);
}
