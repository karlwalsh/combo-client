package combo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static java.net.URI.create;
import static java.util.Collections.singletonList;

public final class ComboFactory {

    private ComboFactory() {
        throw new AssertionError("Use my static methods instead");
    }

    public static Combo httpCombo(final URI baseUri) {
        final RestTemplate restTemplate = restTemplate(baseUri);
        return new HttpCombo(restTemplateBasedHttpClient(restTemplate));
    }

    private static HttpClient restTemplateBasedHttpClient(final RestTemplate restTemplate) {
        return new HttpClient() {
            @Override public <T> HttpResponse<T> get(final URI path, final Class<T> classOfT) {
                try {
                    final ResponseEntity<T> response = restTemplate.getForEntity(path, classOfT);
                    return new HttpResponse<>(response.getStatusCode().value(), response.getBody());
                } catch (final HttpClientErrorException e) {
                    throw new HttpClientException(e);
                }
            }

            @Override public <T> HttpResponse<T> post(final URI path, final Object body, final Class<T> responseType) {
                final ResponseEntity<T> response = restTemplate.postForEntity(path, body, responseType);
                return new HttpResponse<>(response.getStatusCode().value(), response.getBody());
            }
        };
    }

    private static RestTemplate restTemplate(final URI baseUri) {
        final RestTemplate restTemplate = new RestTemplate(singletonList(new GsonHttpMessageConverter()));
        restTemplate.getInterceptors().add((request, body, execution)
                -> execution.execute(new BaseUriRequestDecorator(request, baseUri), body));
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
