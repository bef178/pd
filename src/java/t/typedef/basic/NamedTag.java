package t.typedef.basic;

public class NamedTag {

    public String value = null;

    /**
     * refer to the concept that this tag describes<br/>
     */
    private final int type;

    public NamedTag() {
        this(-1);
    }

    public NamedTag(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NamedTag) {
            NamedTag t = (NamedTag) o;
            if (this.type != t.type) {
                return false;
            }
            if (this.value != null) {
                return this.value.equals(t.value);
            } else {
                return t.value == null;
            }
        }
        return false;
    }
}
