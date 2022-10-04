package pd.codec.json;

import java.util.Objects;

final class SimpleJsonBoolean implements IJsonBoolean {

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
    public boolean getBoolean() {
        return value;
    }

    @Override
    public SimpleJsonBoolean set(boolean value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof IJsonBoolean) {
            IJsonBoolean another = (IJsonBoolean) o;
            return Objects.equals(another.getBoolean(), getBoolean());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
