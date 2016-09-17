package t.typedef.json;

public interface Producer {

    public JsonDict produceJsonDict();

    public JsonList produceJsonList();

    public JsonScalar produceJsonScalar();
}
