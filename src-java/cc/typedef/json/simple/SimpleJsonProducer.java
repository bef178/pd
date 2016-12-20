package cc.typedef.json.simple;

import cc.typedef.json.JsonDict;
import cc.typedef.json.JsonList;
import cc.typedef.json.JsonScalar;
import cc.typedef.json.Producer;

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
