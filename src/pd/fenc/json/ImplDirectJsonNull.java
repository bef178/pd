package pd.fenc.json;

class ImplDirectJsonNull implements IJsonNull {

    public static final ImplDirectJsonNull instance = new ImplDirectJsonNull();

    private ImplDirectJsonNull() {
        // dummy
    }

    @Override
    public String serialize() {
        return "null";
    }
}
