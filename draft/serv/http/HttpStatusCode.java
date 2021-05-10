package pd.net.serv.http;

public enum HttpStatusCode {

    Continue(100),

    OK(200),
    Created(201),
    Accepted(202),
    NoContent(204),
    ResetContent(205),
    PartialContent(206),

    MovedPermanently(301),
    Found(302),
    NotModified(304),

    BadRequest(400),
    Unauthorized(401),
    Forbidden(403),
    NotFound(404),
    NotAcceptable(406),
    Gone(410),
    LengthRequired(411),
    RequestEntityTooLarge(413),
    RequestUriTooLarge(414),

    InternalServerError(500),
    NotImplemented(501),
    BadGateway(502),
    ServiceUnavailable(503),
    HttpVersionNotSupported(505);

    public final int code;

    HttpStatusCode(int code) {
        this.code = code;
    }
}
