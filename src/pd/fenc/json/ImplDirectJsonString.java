package pd.fenc.json;

import pd.fenc.Util;

class ImplDirectJsonString implements IJsonString {

    public static String serialize(String value) {
        return Util.serializeToQuotedString(value);
    }

    private String value = null;

    ImplDirectJsonString(String value) {
        this.value = value;
    }

    @Override
    public String serialize() {
        return serialize(value);
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
