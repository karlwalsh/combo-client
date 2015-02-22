package combo;

import java.net.URI;

interface HttpClient {

    <T> HttpResponse<T> get(final URI path, final Class<T> classOfT);

    <T> HttpResponse<T> post(final URI path, final Object body, final Class<T> responseType);

    static final class HttpResponse<T> {

        private final int statusCode;
        private final T body;

        HttpResponse(final int statusCode, final T body) {
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

    static final class HttpClientException extends RuntimeException {
        HttpClientException(final Throwable cause) {
            super(cause);
        }
    }
}
