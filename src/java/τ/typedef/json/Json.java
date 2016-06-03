package τ.typedef.json;

public interface Json {

    public class IllegalTypeException extends RuntimeException {

        private static final long serialVersionUID = 4454328436221010291L;

        public IllegalTypeException() {
            super();
        }

        public IllegalTypeException(String message) {
            super(message);
        }
    }

    public interface Producer {

        public JsonMapping produceMapping();

        public JsonScalar produceScalar();

        public JsonSequence produceSequence();
    }

    public enum Type {
        SCALAR, SEQUENCE, MAPPING
    }

    public Type type();
}
