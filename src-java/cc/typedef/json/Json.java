package cc.typedef.json;

public interface Json {

    public enum Type {
        SCALAR, LIST, DICT
    }

    public Type type();
}
