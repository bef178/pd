package pd.fenc.json;

import java.util.PrimitiveIterator.OfInt;

class ImplDirectJsonString implements IJsonString {

    static String serialize(String value) {
        StringBuilder dst = new StringBuilder();
        dst.append('\"');
        OfInt it = value.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            if (ch == '\"') {
                dst.append('\\');
                dst.append('\"');
            } else {
                dst.appendCodePoint(ch);
            }
        }
        dst.append('\"');
        return dst.toString();
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
