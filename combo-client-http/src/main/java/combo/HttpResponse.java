package combo;

public final class HttpResponse<T> {

    private final int statusCode;
    private final T body;

    public HttpResponse(final int statusCode, final T body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    int getStatusCode() {
        return statusCode;
    }

    T getBody() {
        return body;
    }
}
