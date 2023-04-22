package pd.codec.xml;

public interface INode {

    default String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb);
        return sb.toString();
    }

    void serialize(StringBuilder sb);

    enum Type {
        TAGGED, TEXT, DECLARATION, COMMENT
    }
}
