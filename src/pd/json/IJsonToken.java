package pd.json;

public interface IJsonToken {

    public default <T extends IJsonToken> T cast(Class<T> expectedClass) {
        return Util.cast(this, expectedClass);
    }
}
