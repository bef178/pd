package cc.typedef.json;

public interface Json {

    // considering interface Json be substituted by abstract JsonElement
    // but if so, MonoJson cannot be any of Scalar/List/Dict
    static abstract class JsonElement {

        JsonDict getJsonDict(int index) {
            unsupportedOperation();
            return null;
        }

        JsonDict getJsonDict(String key) {
            unsupportedOperation();
            return null;
        }

        JsonElement getJsonElement(int index) {
            unsupportedOperation();
            return null;
        }

        JsonList getJsonList(int index) {
            unsupportedOperation();
            return null;
        }

        JsonList getJsonList(String key) {
            unsupportedOperation();
            return null;
        }

        JsonScalar getJsonScalar(int index) {
            unsupportedOperation();
            return null;
        }

        JsonScalar getJsonScalar(String key) {
            unsupportedOperation();
            return null;
        }

        private void unsupportedOperation() {
            throw new UnsupportedOperationException(
                    getClass().getSimpleName());
        }
    }

    public enum Type {
        SCALAR, LIST, DICT
    }

    public Type type();
}
