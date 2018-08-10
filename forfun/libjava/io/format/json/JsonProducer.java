package libjava.io.format.json;

interface JsonProducer {

    public JsonObject createJsonObject();

    public JsonScalar createJsonScalar();

    public JsonVector createJsonVector();
}
