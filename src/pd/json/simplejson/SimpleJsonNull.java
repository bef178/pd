package pd.json.simplejson;

import pd.json.type.IJsonNull;

class SimpleJsonNull implements IJsonNull {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final SimpleJsonNull jsonNull = new SimpleJsonNull();

    private SimpleJsonNull() {
        // dummy
    }
}
