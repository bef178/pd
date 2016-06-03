package τ.typedef.json.simple;

import τ.typedef.json.Json;
import τ.typedef.json.JsonMapping;
import τ.typedef.json.JsonScalar;
import τ.typedef.json.JsonSequence;

public class SimpleJsonProducer implements Json.Producer {

    @Override
    public JsonMapping produceMapping() {
        return new SimpleJsonMapping();
    }

    @Override
    public JsonScalar produceScalar() {
        return new SimpleJsonScalar();
    }

    @Override
    public JsonSequence produceSequence() {
        return new SimpleJsonSequence();
    }
}
