package combo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static java.net.URI.create;
import static java.util.Collections.singletonList;

public final class ComboFactory {

    private ComboFactory() {
        throw new AssertionError("Use my static methods instead");
    }

    public static Combo httpCombo(final URI baseUrl) {
        return new HttpCombo(restTemplate(baseUrl));
    }

    private static RestTemplate restTemplate(final URI baseUri) {
        final RestTemplate restTemplate = new RestTemplate(singletonList(new GsonHttpMessageConverter()));
        restTemplate.getInterceptors()
                .add((request, body, execution) -> execution.execute(new BaseUriRequestDecorator(request, baseUri), body));
        return restTemplate;
    }

    private static final class BaseUriRequestDecorator implements HttpRequest {

        private final HttpRequest request;
        private final URI baseUri;

        private BaseUriRequestDecorator(final HttpRequest request, final URI baseUri) {
            this.request = request;
            this.baseUri = baseUri;
        }

        @Override public HttpMethod getMethod() {
            return request.getMethod();
        }

        @Override public URI getURI() {
            return create(baseUri + request.getURI().toString());
        }

        @Override public HttpHeaders getHeaders() {
            return request.getHeaders();
        }
    }
}
