package t.typedef.basic;

public class NamedTag {

    public String value = null;

    /**
     * the concept/category of this tag<br/>
     */
    private final String name;

    public NamedTag() {
        this(null);
    }

    public NamedTag(String name) {
        this.name = (name == null) ? "" : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NamedTag) {
            NamedTag t = (NamedTag) o;
            if (this.name != t.name) {
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
