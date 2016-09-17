package t.typedef.json.simple;

import t.typedef.json.JsonDict;
import t.typedef.json.JsonList;
import t.typedef.json.JsonScalar;
import t.typedef.json.Producer;

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
