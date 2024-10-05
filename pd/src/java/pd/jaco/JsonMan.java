package pd.jaco;

public class JsonMan {

    private final JacoMan jacoMan = new JacoMan();

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

    public JacoFromEntityConverter.Config getFromEntityConfig() {
        return jacoMan.fromEntityConfig;
    }

    public JacoToEntityConverter.Config getToEntityConfig() {
        return jacoMan.toEntityConfig;
    }

    public JacoToJsonSerializer.Config getToJsonConfig() {
        return jacoMan.toJsonConfig;
    }
}
