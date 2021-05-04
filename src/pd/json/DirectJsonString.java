package pd.json;

class DirectJsonString implements IJsonString {

    private String value = null;

    public DirectJsonString() {
        this("");
    }

    public DirectJsonString(String value) {
        set(value);
    }

    @Override
    public void set(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
