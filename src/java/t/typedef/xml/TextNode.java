package t.typedef.xml;

public class TextNode extends Node {

    CharSequence content = null; // trimmed

    public boolean isEmpty() {
        return content == null || content.length() == 0;
    }

    @Override
    public StringBuilder toString(StringBuilder o, Config c) {
        assert o != null;
        if (!isEmpty()) {
            c.printIndent(o).append(content).append('\n');
        }
        return o;
    }
}
