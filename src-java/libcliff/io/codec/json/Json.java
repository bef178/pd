package libcliff.io.codec.json;

public interface Json {

    /**
     * assert type equals or throw {@link IllegalJsonTypeException}
     */
    public static <T extends Json> T checkType(T json, JsonType expected) {
        if (json == null || json.type() != expected) {
            throw new IllegalJsonTypeException();
        }
        return json;
    }

    public JsonType type();
}
