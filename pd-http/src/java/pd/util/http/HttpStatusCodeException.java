package pd.util.http;

public class HttpStatusCodeException extends RuntimeException {

    final int statusCode;

    public HttpStatusCodeException(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpStatusCodeException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCodeException(int statusCode, Throwable cause) {
        super(cause);
        this.statusCode = statusCode;
    }

    public boolean isClientError() {
        return statusCode / 100 == 4;
    }

    public boolean isServerError() {
        return statusCode / 100 == 5;
    }

    public int statusCode() {
        return statusCode;
    }
}
