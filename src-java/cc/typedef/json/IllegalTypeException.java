package cc.typedef.json;

public class IllegalTypeException extends RuntimeException {

    private static final long serialVersionUID = 4454328436221010291L;

    public IllegalTypeException() {
        super();
    }

    public IllegalTypeException(String message) {
        super(message);
    }
}
