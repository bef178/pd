package pd.codec.json.datafactory;

import pd.codec.json.datatype.IJsonNull;

final class SimpleJsonNull implements IJsonNull {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final SimpleJsonNull NULL = new SimpleJsonNull();

    private SimpleJsonNull() {
        // dummy
    }
}
