package cc.typedef.json.mono;

import cc.typedef.json.JsonDict;
import cc.typedef.json.JsonList;
import cc.typedef.json.JsonScalar;
import cc.typedef.json.Producer;
import cc.typedef.json.Json.Type;

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
