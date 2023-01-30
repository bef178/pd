package pd.codec.xml;

/**
 * Xml spec is very complicated. Let's use the simple and intuitive part.
 */
public class Document {

    public DeclarationNode header;

    public TaggedNode root;

    public void serialize(StringBuilder sb) {
        if (root != null) {
            if (header != null) {
                header.serialize(sb);
            }
            root.serialize(sb);
        }
    }
}
