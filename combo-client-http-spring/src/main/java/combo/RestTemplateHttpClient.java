package combo;

import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import static java.net.URI.create;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public final class RestTemplateHttpClient implements HttpClient {

    private final RestTemplate restTemplate;

    private RestTemplateHttpClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override public <T> HttpResponse<T> get(final URI path, final Class<T> classOfT) {
        try {
            final ResponseEntity<T> response = restTemplate.getForEntity(path, classOfT);
            return new HttpResponse<>(response.getStatusCode().value(), response.getBody());
        } catch (final HttpClientErrorException e) {
            throw new HttpClientException(e);
        }
    }

    @Override public <T> HttpResponse<T> post(final URI path, final Object requestBody, final Class<T> responseType) {
        final ResponseEntity<T> response = restTemplate.postForEntity(path, jsonEntity(requestBody), responseType);
        return new HttpResponse<>(response.getStatusCode().value(), response.getBody());
    }

    private static HttpEntity<Object> jsonEntity(final Object body) {
        return new HttpEntity<>(body, new LinkedMultiValueMap<>(new HashMap<String, List<String>>() {{
            put(CONTENT_TYPE, singletonList(APPLICATION_JSON_VALUE));
        }}));
    }

    public static HttpClient restTemplateHttpClient(final URI baseUri) {
        return new RestTemplateHttpClient(restTemplate(baseUri));
    }

    private static RestTemplate restTemplate(final URI baseUri) {
        final RestTemplate restTemplate = new RestTemplate(singletonList(new StringHttpMessageConverter()));
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
