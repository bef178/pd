package pd.json;

class DirectJsonNull implements IJsonNull {

    public static final DirectJsonNull defaultInstance = new DirectJsonNull();

    private DirectJsonNull() {
        // dummy
    }
}
