package pd.jaco;

public class JsonMan {

    public final JacoMan jacoMan = new JacoMan();

    public String serialize(Object entity) {
        Object jaco = jacoMan.fromEntity(entity);
        return jacoMan.toJson(jaco);
    }

    public <T> T deserialize(String json, Class<T> targetClass) {
        return deserialize(json, targetClass, targetClass.getSimpleName());
    }

    public <T> T deserialize(String json, Class<T> targetClass, String startPath) {
        Object jaco = jacoMan.fromJson(json);
        return jacoMan.toEntity(jaco, targetClass, startPath);
    }
}
