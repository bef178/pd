package cc.typedef.basic;

final public class NamedTag extends KeyValue {

    public NamedTag() {
        this(null);
    }

    public NamedTag(String name) {
        setName(name);
    }

    /**
     * the concept/category of this tag<br/>
     * @return a non-null String
     */
    public String getName() {
        return key;
    }

    /**
     * @return a non-null String
     */
    public String getTag() {
        return value;
    }

    @Override
    public boolean isValid() {
        return !getTag().isEmpty();
    }

    private void setName(String name) {
        key = (name == null) ? "" : name;
    }

    public void setTag(String tag) {
        value = (tag == null) ? "" : tag;
    }
}
