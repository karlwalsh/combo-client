package combo;

import static combo.HttpResponse.Status.NO_CONTENT;

public final class HttpResponse<T> {

    private final int statusCode;
    private final T body;

    public HttpResponse(final int statusCode, final T body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public T getBody() {
        return body;
    }

    public <NEW_TYPE> HttpResponse<NEW_TYPE> withBody(final NEW_TYPE body) {
        return new HttpResponse<>(statusCode, body);
    }

    public static <T> HttpResponse<T> noContent() {
        return new HttpResponse<>(NO_CONTENT, null);
    }

    public static final class Status {
        public static final int NO_CONTENT = 204;
    }
}
