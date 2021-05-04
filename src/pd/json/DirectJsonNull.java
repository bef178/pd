package pd.json;

class DirectJsonNull implements IJsonNull {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final DirectJsonNull defaultInstance = new DirectJsonNull();

    private DirectJsonNull() {
        // dummy
    }
}
