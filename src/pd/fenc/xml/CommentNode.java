package pd.fenc.xml;

public class CommentNode implements INode {

    public String content = null;

    @Override
    public void serialize(StringBuilder sb) {
        sb.append("<!--")
                .append(content == null ? "" : content)
                .append("-->");
    }
}
