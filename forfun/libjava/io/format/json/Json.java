package libjava.io.format.json;

import libjava.io.Pushable;

public interface Json {

    public void serialize(FormattingConfig config, Pushable it);
}
