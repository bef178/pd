package pd.codec.json.datafactory;

import pd.codec.json.datatype.JsonNull;

final class SimpleJsonNull implements JsonNull {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final SimpleJsonNull NULL = new SimpleJsonNull();

    private SimpleJsonNull() {
        // dummy
    }
}
