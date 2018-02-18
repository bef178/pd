package libcliff.io.codec.json;

public interface JsonFactory {

    public JsonObject createJsonObject();

    public JsonScalar createJsonScalar();

    public JsonVector createJsonVector();
}
