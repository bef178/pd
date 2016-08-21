package t.typedef.xml;

public class CommentNode extends Node {

    CharSequence content = null; // trimmed

    public boolean isEmpty() {
        return content == null || content.length() == 0;
    }

    @Override
    public StringBuilder toString(StringBuilder o, Config c) {
        assert o != null;
        if (!isEmpty()) {
            c.printIndent(o).append("<!-- ").append(content).append(" -->");
        }
        return o;
    }
}
