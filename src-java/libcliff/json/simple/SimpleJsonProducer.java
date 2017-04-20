package libcliff.json.simple;

import libcliff.json.JsonDict;
import libcliff.json.JsonList;
import libcliff.json.JsonScalar;
import libcliff.json.Producer;

public class SimpleJsonProducer implements Producer {

    @Override
    public JsonDict produceJsonDict() {
        return new SimpleJsonDict();
    }

    @Override
    public JsonScalar produceJsonScalar() {
        return new SimpleJsonScalar();
    }

    @Override
    public JsonList produceJsonList() {
        return new SimpleJsonList();
    }
}
