package pd.codec.json;

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
