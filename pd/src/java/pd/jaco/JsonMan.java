package pd.jaco;

import pd.jaco.bridge.JacoFromEntityConverter;
import pd.jaco.bridge.JacoFromJsonDeserializer;
import pd.jaco.bridge.JacoToEntityConverter;
import pd.jaco.bridge.JacoToJsonSerializer;

public class JsonMan {

    public final JacoFromEntityConverter jacoFromEntityConverter = new JacoFromEntityConverter();
    public final JacoToEntityConverter jacoToEntityConverter = new JacoToEntityConverter();
    public final JacoToJsonSerializer jacoToJsonSerializer = new JacoToJsonSerializer();
    public final JacoFromJsonDeserializer jacoFromJsonDeserializer = new JacoFromJsonDeserializer();

    public String serialize(Object entity) {
        Object jaco = entityToJaco(entity);
        return jacoToJson(jaco);
    }

    public <T> T deserialize(String json, Class<T> targetClass) {
        return deserialize(json, targetClass, targetClass.getSimpleName());
    }

    public <T> T deserialize(String json, Class<T> targetClass, String startPath) {
        Object jaco = jsonToJaco(json);
        return jacoToEntity(jaco, targetClass, startPath);
    }

    public String jacoToJson(Object jaco) {
        if (jaco == null) {
            return null;
        }
        return jacoToJsonSerializer.toJson(jaco);
    }

    public Object jsonToJaco(String json) {
        if (json == null) {
            return null;
        }
        return jacoFromJsonDeserializer.fromJson(json);
    }

    public <T> T jacoToEntity(Object jaco, Class<T> targetClass, String startPath) {
        if (jaco == null) {
            return null;
        }
        return jacoToEntityConverter.toEntity(jaco, targetClass, startPath);
    }

    public Object entityToJaco(Object entity) {
        if (entity == null) {
            return null;
        }
        return jacoFromEntityConverter.fromEntity(entity);
    }
}
