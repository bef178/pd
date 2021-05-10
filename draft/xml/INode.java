package pd.fenc.xml;

public interface INode {

    public enum Type {
        COMMENT, DECLARATION, TAGGED, TEXT;
    }

    public default String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb);
        return sb.toString();
    }

    public void serialize(StringBuilder sb);
}
