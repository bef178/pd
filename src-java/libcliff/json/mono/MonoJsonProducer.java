package libcliff.json.mono;

import libcliff.json.JsonDict;
import libcliff.json.JsonList;
import libcliff.json.JsonScalar;
import libcliff.json.Producer;
import libcliff.json.Json.Type;

public class MonoJsonProducer implements Producer {

    @Override
    public JsonDict produceJsonDict() {
        return new MonoJson(Type.DICT);
    }

    @Override
    public JsonScalar produceJsonScalar() {
        return new MonoJson(Type.SCALAR);
    }

    @Override
    public JsonList produceJsonList() {
        return new MonoJson(Type.LIST);
    }
}
