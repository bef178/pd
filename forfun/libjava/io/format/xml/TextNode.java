package libjava.io.format.xml;

public class TextNode extends Node {

    CharSequence content = null; // trimmed

    public boolean isEmpty() {
        return content == null || content.length() == 0;
    }

    @Override
    public StringBuilder toString(StringBuilder o, Config c) {
        assert o != null;
        if (!isEmpty()) {
            appendIndent(o, c).append(content).append('\n');
        }
        return o;
    }
}
