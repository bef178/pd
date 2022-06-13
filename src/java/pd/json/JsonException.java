package pd.json;

public class JsonException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 590194730620799223L;

    public JsonException() {
        super();
    }

    public JsonException(String message) {
        super(message);
    }

    public JsonException(Throwable cause) {
        super(cause);
    }
}
