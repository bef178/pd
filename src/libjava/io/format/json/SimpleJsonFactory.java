package libjava.io.format.json;

public class SimpleJsonFactory implements JsonFactory {

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
