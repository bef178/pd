package pd.json.datafactory;

import pd.json.datatype.JsonArray;
import pd.json.datatype.JsonBoolean;
import pd.json.datatype.JsonNull;
import pd.json.datatype.JsonNumber;
import pd.json.datatype.JsonObject;
import pd.json.datatype.JsonString;

public interface JsonFactory {

    static JsonFactory getFactory() {
        return SimpleJsonFactory.singleton();
    }

    JsonArray createJsonArray();

    JsonBoolean createJsonBoolean();

    default JsonBoolean createJsonBoolean(boolean value) {
        return createJsonBoolean().set(value);
    }

    JsonNumber createJsonNumber();

    default JsonNumber createJsonNumber(double value) {
        return createJsonNumber().set(value);
    }

    default JsonNumber createJsonNumber(long value) {
        return createJsonNumber().set(value);
    }

    JsonObject createJsonObject();

    JsonString createJsonString();

    default JsonString createJsonString(String value) {
        return createJsonString().set(value);
    }

    JsonNull getJsonNull();
}
