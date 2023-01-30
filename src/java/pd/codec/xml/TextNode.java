package pd.codec.xml;

public class TextNode implements INode {

    public String content = null;

    @Override
    public void serialize(StringBuilder sb) {
        if (content != null) {
            sb.append(content);
        }
    }
}
