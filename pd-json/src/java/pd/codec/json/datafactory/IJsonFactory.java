package pd.codec.json.datafactory;

import pd.codec.json.datatype.IJsonNull;
import pd.codec.json.datatype.IJsonArray;
import pd.codec.json.datatype.IJsonBoolean;
import pd.codec.json.datatype.IJsonNumber;
import pd.codec.json.datatype.IJsonObject;
import pd.codec.json.datatype.IJsonString;

public interface IJsonFactory {

    static IJsonFactory getFactory() {
        return SimpleJsonFactory.singleton();
    }

    IJsonArray createJsonArray();

    IJsonBoolean createJsonBoolean();

    default IJsonBoolean createJsonBoolean(boolean value) {
        return createJsonBoolean().set(value);
    }

    IJsonNumber createJsonNumber();

    default IJsonNumber createJsonNumber(double value) {
        return createJsonNumber().set(value);
    }

    default IJsonNumber createJsonNumber(long value) {
        return createJsonNumber().set(value);
    }

    IJsonObject createJsonObject();

    IJsonString createJsonString();

    default IJsonString createJsonString(String value) {
        return createJsonString().set(value);
    }

    IJsonNull getJsonNull();
}
