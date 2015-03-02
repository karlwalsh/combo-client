package combo;

import java.net.URI;

public interface HttpClient {

    <T> HttpResponse<T> get(URI path, Class<T> responseType);

    <T> HttpResponse<T> post(URI path, Object requestBody, Class<T> responseType);

}
