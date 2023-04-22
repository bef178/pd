package pd.codec.xml;

import java.util.LinkedList;
import java.util.List;

public class TaggedNode implements INode {

    public String namespacePrefix;

    public String tagName;

    public final List<Attribute> attributes = new LinkedList<>();

    public final List<INode> children = new LinkedList<>();

    private TaggedNode parent = null;

    public TaggedNode getParent() {
        return parent;
    }

    public void addChild(TaggedNode node) {
        addChild(node, children.size());
    }

    public void addChild(TaggedNode node, int index) {
        if (node == null) {
            throw new NullPointerException();
        }
        if (index < 0 || index > children.size()) {
            throw new IndexOutOfBoundsException();
        } else if (index == children.size()) {
            children.add(node);
        } else {
            children.add(index, node);
        }
        node.parent = this;
    }

    public void removeChild(TaggedNode node) {
        if (node == null) {
            throw new NullPointerException();
        }
        if (children.remove(node)) {
            node.parent = null;
        }
    }

    @Override
    public void serialize(StringBuilder sb) {
        sb.append('<');
        Util.serializeName(namespacePrefix, tagName, sb);

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
        Util.serializeName(namespacePrefix, tagName, sb);
        sb.append('>');
    }
}
