package τ.typedef.json.mono;

import τ.typedef.json.Json;
import τ.typedef.json.Json.Type;
import τ.typedef.json.JsonMapping;
import τ.typedef.json.JsonScalar;
import τ.typedef.json.JsonSequence;

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
