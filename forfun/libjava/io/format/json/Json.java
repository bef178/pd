package libjava.io.format.json;

import libjava.io.Pushable;
import libjava.io.format.json.JsonSerializer.Config;

public interface Json {

    public void serialize(Config config, Pushable it);
}
