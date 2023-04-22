package pd.codec.xml;

public class DeclarationNode implements INode {

    public String version = "1.0";

    public String encoding = "UTF-8";

    public Boolean standalone = true;

    @Override
    public void serialize(StringBuilder sb) {
        sb.append("<?xml");
        if (version != null) {
            sb.append(' ').append("version").append('=').append(version);
        }
        if (encoding != null) {
            sb.append(' ').append("encoding").append('=').append(encoding);
        }
        if (standalone != null) {
            sb.append(' ').append("standalone").append('=').append(standalone ? "yes" : "no");
        }
        sb.append("?>");
    }
}
