package pd.json;

import java.io.Serializable;

public interface IJsonToken extends Serializable {

    public default <T extends IJsonToken> T cast(Class<T> expectedClass) {
        return Util.cast(this, expectedClass);
    }
}
