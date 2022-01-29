package pd.json;

import pd.json.type.IJsonArray;
import pd.json.type.IJsonBoolean;
import pd.json.type.IJsonNull;
import pd.json.type.IJsonNumber;
import pd.json.type.IJsonObject;
import pd.json.type.IJsonString;

public interface IJsonFactory {

    public IJsonArray newJsonArray();

    public IJsonBoolean newJsonBoolean(boolean value);

    public IJsonNull newJsonNull();

    public IJsonNumber newJsonNumber();

    public IJsonObject newJsonObject();

    public IJsonString newJsonString();
}
