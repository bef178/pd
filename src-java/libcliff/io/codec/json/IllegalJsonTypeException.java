package libcliff.io.codec.json;

public class IllegalJsonTypeException extends RuntimeException {

    private static final long serialVersionUID = 4454328436221010291L;

    public IllegalJsonTypeException() {
        super();
    }

    public IllegalJsonTypeException(String message) {
        super(message);
    }
}
