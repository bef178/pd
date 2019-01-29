package pd.io.format.json;

public class SimpleJsonProducer implements JsonProducer {

    @Override
    public JsonObject createJsonObject() {
        return new SimpleJsonObject();
    }

    @Override
    public JsonScalar createJsonScalar() {
        return new SimpleJsonScalar();
    }

    @Override
    public JsonVector createJsonVector() {
        return new SimpleJsonVector();
    }
}
