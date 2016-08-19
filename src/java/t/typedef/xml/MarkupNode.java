package t.typedef.xml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MarkupNode extends Node {

    private Tag tag = null;

    /**
     * <code>null<code> iff no such attribute in this markup<br/>
     */
    private DefaultNamespace defaultNamespace = null;

    private Collection<Namespace> namespaces = new LinkedList<>();

    private Collection<Attribute> attributes = new LinkedList<>();

    private List<Node> children = new LinkedList<>();

    private Node parent = null;

    private boolean isValid() {
        return tag != null || tag.isValid();
    }

    @Override
    public StringBuilder toString(StringBuilder factory) {
        if (!isValid()) {
            throw new IllegalArgumentException();
        }

        factory.append('<');
        tag.toString(factory);
        if (defaultNamespace != null) {
            factory.append(' ');
            defaultNamespace.toString(factory);
        }
        for (Namespace ns : namespaces) {
            factory.append('\n');
            ns.toString(factory);
        }
        for (Attribute attr : attributes) {
            factory.append('\n');
            attr.toString(factory);
        }
        if (children.isEmpty()) {
            factory.append("/>");
        } else {
            factory.append('>').append('\n');
            for (Node child : children) {
                child.toString(factory);
            }
            factory.append("</");
            tag.toString(factory);
            factory.append('>');
        }
        return factory;
    }
}
