package t.typedef.json.simple;

import t.typedef.json.Json;
import t.typedef.json.JsonMapping;
import t.typedef.json.JsonScalar;
import t.typedef.json.JsonSequence;

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
