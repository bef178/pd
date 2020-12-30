package pd.fenc.xml;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TaggedNode implements INode {

    /**
     * default namespace; null if no such attribute;
     */
    public String xmlns = null;

    public final LinkedHashMap<String, String> namespaces = new LinkedHashMap<String, String>();

    private final Attribute tagName = new Attribute();

    public final List<Attribute> attributes = new LinkedList<>();

    private final List<INode> children = new LinkedList<>();

    private TaggedNode parent = null;

    public TaggedNode getParent() {
        return parent;
    }

    public String getNsprefix() {
        return tagName.nsprefix;
    }

    public String getLocalName() {
        return tagName.key;
    }

    public void setLocalName(String localName) {
        tagName.key = localName;
    }

    public void setNsprefix(String nsprefix) {
        tagName.nsprefix = nsprefix;
    }

    public void setParent(TaggedNode parent) {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        if (parent != null) {
            parent.addChild(this);
        }
    }

    private void addChild(TaggedNode node) {
        if (node == null) {
            return;
        }
        if (!children.contains(node)) {
            children.add(node);
            node.parent = this;
        }
    }

    public void removeChild(TaggedNode node) {
        if (node == null) {
            return;
        }
        if (children.remove(node)) {
            node.parent = null;
        }
    }

    @Override
    public void serialize(StringBuilder sb) {

        sb.append('<');

        tagName.serialize(sb);

        if (xmlns != null) {
            sb.append(' ').append("xmlns").append('=').append(xmlns);
        }

        for (Map.Entry<String, String> ns : namespaces.entrySet()) {
            String nsprefix = ns.getKey();
            String nsUri = ns.getValue();
            if (nsprefix == null || nsprefix.isEmpty()) {
                throw new IllegalArgumentException();
            }
            sb.append(' ').append("xmlns").append(':').append(nsprefix).append('=').append(nsUri);
        }

        for (Attribute attr : attributes) {
            sb.append(' ');
            attr.serialize(sb);
        }

        if (children.isEmpty()) {
            sb.append("/>");
            return;
        }

        sb.append('>');

        for (INode child : children) {
            child.serialize(sb);
        }

        sb.append("</");

        tagName.serialize(sb);

        sb.append('>');
    }
}
