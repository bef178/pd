package τ.typedef.basic;

public class NamedTag {

    /**
     * the trunk/content/description of this tag
     */
    private String desc = null;

    /**
     * refer to the concept that this tag describes<br/>
     * better to be a non-ambiguous constant
     */
    private String name = null;

    /**
     * the explanation/comment/remarks against the content
     */
    private String remarks = null;

    public String desc() {
        return desc;
    }

    public String name() {
        return name;
    }

    public String remarks() {
        return remarks;
    }

    public boolean hasDesc() {
        return desc != null;
    }

    public boolean hasName() {
        return name != null;
    }

    public boolean hasRemarks() {
        return remarks != null;
    }

    public NamedTag desc(String desc) {
        this.desc = desc;
        return this;
    }

    public NamedTag name(String name) {
        this.name = name;
        return this;
    }

    public NamedTag remarks(String remarks) {
        this.remarks = remarks;
        return this;
    }
}
