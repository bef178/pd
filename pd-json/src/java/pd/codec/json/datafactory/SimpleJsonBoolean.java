package pd.codec.json.datafactory;

import java.util.Objects;

import pd.codec.json.datatype.JsonBoolean;

final class SimpleJsonBoolean implements JsonBoolean {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean value;

    public SimpleJsonBoolean() {
        this(false);
    }

    private SimpleJsonBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof JsonBoolean) {
            JsonBoolean another = (JsonBoolean) o;
            return Objects.equals(another.getBoolean(), getBoolean());
        }
        return false;
    }

    @Override
    public boolean getBoolean() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public SimpleJsonBoolean set(boolean value) {
        this.value = value;
        return this;
    }
}
