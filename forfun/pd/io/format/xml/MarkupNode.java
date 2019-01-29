package pd.io.format.xml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import pd.io.format.FormattingConfig;

public class MarkupNode extends Node {

    private Attribute tagName = null;

    /**
     * <code>null<code> iff no such attribute in this markup<br/>
     */
    private Attribute defaultNamespace = null;

    private Collection<Attribute> namespaces = new LinkedList<>();

    private Collection<Attribute> attributes = new LinkedList<>();

    private List<Node> children = new LinkedList<>();

    private Node parent = null;

    private StringBuilder appendTagName(StringBuilder o) {
        if (!tagName.getNamespace().isEmpty()) {
            o.append(tagName.getNamespace()).append(":");
        }
        o.append(tagName.getName());
        return o;
    }

    public Node getParent() {
        return this.parent;
    }

    private boolean isValid() {
        return tagName != null || tagName.isValid();
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public StringBuilder toString(StringBuilder o, FormattingConfig c) {
        assert o != null;

        if (!isValid()) {
            throw new IllegalArgumentException();
        }

        appendIndent(o, c);
        o.append('<');
        appendTagName(o);
        if (defaultNamespace != null) {
            if (defaultNamespace.getNamespace().isEmpty()
                    && defaultNamespace.getName().equals("xmlns")) {
                o.append(' ');
                defaultNamespace.toString(o);
            } else {
                throw new IllegalArgumentException();
            }
        }
        o.append('\n');

        c.numIndents += 2;
        for (Attribute ns : namespaces) {
            if (ns.getNamespace().equals("xmlns")
                    && !ns.getName().isEmpty()
                    && !ns.getValue().isEmpty()) {
                appendIndent(o, c);
                ns.toString(o);
                o.append('\n');
            } else {
                throw new IllegalArgumentException();
            }
        }
        for (Attribute attr : attributes) {
            appendIndent(o, c);
            attr.toString(o);
            o.append('\n');
        }
        c.numIndents -= 2;

        if (children.isEmpty()) {
            o.append("/>").append('\n');
        } else {
            o.append('>').append('\n');

            c.numIndents++;
            for (Node child : children) {
                child.toString(o, c);
            }
            c.numIndents--;

            appendIndent(o, c);
            o.append("</");
            appendTagName(o);
            o.append('>').append('\n');
        }
        return o;
    }
}
