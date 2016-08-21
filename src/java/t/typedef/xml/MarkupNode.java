package t.typedef.xml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

    private boolean isValid() {
        return tagName != null || tagName.isValid();
    }

    private StringBuilder printTagName(StringBuilder o) {
        if (!tagName.getNamespace().isEmpty()) {
            o.append(tagName.getNamespace()).append(":");
        }
        o.append(tagName.getName());
        return o;
    }

    @Override
    public StringBuilder toString(StringBuilder o, Config c) {
        assert o != null;

        if (!isValid()) {
            throw new IllegalArgumentException();
        }

        c.printIndent(o);
        o.append('<');
        printTagName(o);
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

        c.indent += 2;
        for (Attribute ns : namespaces) {
            if (ns.getNamespace().equals("xmlns")
                    && !ns.getName().isEmpty()
                    && !ns.getValue().isEmpty()) {
                c.printIndent(o);
                ns.toString(o);
                o.append('\n');
            } else {
                throw new IllegalArgumentException();
            }
        }
        for (Attribute attr : attributes) {
            c.printIndent(o);
            attr.toString(o);
            o.append('\n');
        }
        c.indent -= 2;

        if (children.isEmpty()) {
            o.append("/>").append('\n');
        } else {
            o.append('>').append('\n');

            c.indent++;
            for (Node child : children) {
                child.toString(o, c);
            }
            c.indent--;

            c.printIndent(o);
            o.append("</");
            printTagName(o);
            o.append('>').append('\n');
        }
        return o;
    }
}
