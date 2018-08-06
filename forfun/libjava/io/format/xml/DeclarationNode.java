package libjava.io.format.xml;

public class DeclarationNode extends Node {

    public Attribute VERSION = new Attribute()
            .setName("version").setValue("1.0");
    public Attribute ENCODING = new Attribute()
            .setName("encoding").setValue("UTF-8");
    public Attribute STANDALONE = new Attribute()
            .setName("standalone").setValue("yes");

    @Override
    public StringBuilder toString(StringBuilder o, Config c) {
        assert o != null;
        if (VERSION != null) {
            o.append("<?xml");
            if (true) {
                o.append(' ');
                VERSION.toString(o);
            }
            if (ENCODING != null) {
                o.append(' ');
                ENCODING.toString(o);
            }
            if (STANDALONE != null) {
                o.append(' ');
                STANDALONE.toString(o);
            }
            o.append(" ?>");
        }
        return o;
    }
}
