package pd.fenc.xml;

public class DeclarationNode implements INode {

    public String ver = "1.0";

    public String enc = "utf8";

    public Boolean sta = true;

    @Override
    public void serialize(StringBuilder sb) {
        sb.append("<?xml");
        if (ver != null) {
            sb.append(' ').append("version").append('=').append(ver);
        }
        if (enc != null) {
            sb.append(' ').append("encoding").append('=').append(enc);
        }
        if (sta != null) {
            sb.append(' ').append("standalone").append('=').append(sta ? "yes" : "no");
        }
        sb.append("?>");
    }
}
