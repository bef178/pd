package pd.io.format.json;

import pd.io.format.json.MonoJson.JsonType;

public class MonoJsonProducer implements JsonProducer {

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
