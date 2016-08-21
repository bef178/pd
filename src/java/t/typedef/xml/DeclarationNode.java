package t.typedef.xml;

public class DeclarationNode extends Node {

    public Attribute VERSION = new Attribute()
            .setName("version").setValue("1.0");
    public Attribute ENCODING = new Attribute()
            .setName("encoding").setValue("UTF-8");
    public Attribute STANDALONE = new Attribute()
            .setName("standalone").setValue("yes");

    @Override
    public StringBuilder toString(StringBuilder factory, Config c) {
        assert factory != null;
        if (VERSION != null) {
            factory.append("<?xml");
            if (true) {
                factory.append(' ');
                VERSION.toString(factory);
            }
            if (ENCODING != null) {
                factory.append(' ');
                ENCODING.toString(factory);
            }
            if (STANDALONE != null) {
                factory.append(' ');
                STANDALONE.toString(factory);
            }
            factory.append(" ?>");
        }
        return factory;
    }
}
