package libjava.adt;

public final class NamedTag extends KeyValue {

    public NamedTag(String tag) {
        this(null, tag);
    }

    public NamedTag(String cti, String tag) {
        super(cti == null || cti.isEmpty() ? null : cti, tag);
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * cti: the concept and/or scope of the tag
     */
    public String getCti() {
        return key;
    }

    public String getTag() {
        return value;
    }
}
