package pd.json.datafactory;

import pd.json.datatype.JsonNull;

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
