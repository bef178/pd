package libjava.io.format.json;

import libjava.io.format.json.MonoJson.JsonType;

public class MonoJsonFactory implements JsonFactory {

    @Override
    public JsonObject createJsonObject() {
        return new MonoJson(JsonType.OBJECT);
    }

    @Override
    public JsonScalar createJsonScalar() {
        return new MonoJson(JsonType.SCALAR);
    }

    @Override
    public JsonVector createJsonVector() {
        return new MonoJson(JsonType.VECTOR);
    }
}
