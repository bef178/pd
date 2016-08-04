package t.typedef.json.mono;

import t.typedef.json.Json;
import t.typedef.json.JsonMapping;
import t.typedef.json.JsonScalar;
import t.typedef.json.JsonSequence;
import t.typedef.json.Json.Type;

public class MonoJsonProducer implements Json.Producer {

    @Override
    public JsonMapping produceMapping() {
        return new MonoJson(Type.MAPPING);
    }

    @Override
    public JsonScalar produceScalar() {
        return new MonoJson(Type.SCALAR);
    }

    @Override
    public JsonSequence produceSequence() {
        return new MonoJson(Type.SEQUENCE);
    }
}
