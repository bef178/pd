package pd.json;

public class JsonCodec {

    private static class DirectJsonTokenFactory implements IJsonTokenFactory {

        @Override
        public IJsonArray newJsonArray() {
            return new DirectJsonArray();
        }

        @Override
        public DirectJsonBoolean newJsonBoolean(boolean value) {
            return new DirectJsonBoolean(value);
        }

        @Override
        public IJsonFloat newJsonFloat(double value) {
            return new DirectJsonFloat(value);
        }

        @Override
        public IJsonInt newJsonInt(long value) {
            return new DirectJsonInt(value);
        }

        @Override
        public DirectJsonNull newJsonNull() {
            return DirectJsonNull.defaultInstance;
        }

        @Override
        public IJsonString newJsonString(String value) {
            return new DirectJsonString(value);
        }

        @Override
        public IJsonObject newJsonObject() {
            return new DirectJsonObject();
        }
    }

    public interface IJsonTokenFactory {

        public IJsonArray newJsonArray();

        public IJsonBoolean newJsonBoolean(boolean value);

        public IJsonFloat newJsonFloat(double value);

        public IJsonInt newJsonInt(long value);

        public IJsonNull newJsonNull();

        public IJsonString newJsonString(String value);

        public IJsonObject newJsonObject();
    }

    public static final IJsonTokenFactory tokenFactory = new DirectJsonTokenFactory();
    private static final JsonSerializer serializer = new JsonSerializer();
    private static final JsonDeserializer deserializer = new JsonDeserializer();

    public static <T> T deserialize(String serialized, Class<T> expectedClass) {
        IJsonToken token = deserializer.deserialize(serialized);
        if (IJsonToken.class.isAssignableFrom(expectedClass)) {
            return expectedClass.cast(token);
        }
        return deserializer.deserialize(token, expectedClass);
    }

    public static String serialize(IJsonToken token) {
        return serialize(token, "", "", "", 0);
    }

    public static String serialize(IJsonToken token, String margin, String indent, String eol, int numIndents) {
        return serializer.serialize(token, margin, indent, eol, numIndents);
    }

    public static String serialize(Object object) {
        return serialize(object, "", "", "", 0);
    }

    public static String serialize(Object object, String margin, String indent, String eol, int numIndents) {
        IJsonToken token = serializer.serialize(object);
        return serializer.serialize(token, margin, indent, eol, numIndents);
    }
}
