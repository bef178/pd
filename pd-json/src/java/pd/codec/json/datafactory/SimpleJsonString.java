package pd.codec.json.datafactory;

import java.util.Objects;

import pd.codec.json.datatype.JsonString;

final class SimpleJsonString implements JsonString {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String value = null;

    public SimpleJsonString() {
        this("");
    }

    public SimpleJsonString(String value) {
        set(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof JsonString) {
            JsonString another = (JsonString) o;
            return Objects.equals(another.getString(), getString());
        }
        return false;
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public SimpleJsonString set(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return value;
    }
}
