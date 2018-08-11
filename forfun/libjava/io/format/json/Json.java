package libjava.io.format.json;

import libjava.io.Pushable;
import libjava.io.format.FormattingConfig;

public interface Json {

    public void serialize(FormattingConfig config, Pushable it);
}
