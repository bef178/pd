package pd.codec.json.datatype;

import pd.codec.json.JsonType;

public interface JsonNumber extends Json {

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

    JsonNumber set(double value);

    JsonNumber set(long value);

    JsonNumber set(String raw);
}
