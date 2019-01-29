package pd.io.format.json;

import pd.io.Pushable;
import pd.io.format.FormattingConfig;

public interface Json {

    public void serialize(FormattingConfig config, Pushable it);
}
