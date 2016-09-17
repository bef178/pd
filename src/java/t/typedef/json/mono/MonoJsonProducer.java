package t.typedef.json.mono;

import t.typedef.json.Json.Type;
import t.typedef.json.JsonDict;
import t.typedef.json.JsonList;
import t.typedef.json.JsonScalar;
import t.typedef.json.Producer;

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
