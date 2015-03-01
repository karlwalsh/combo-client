package combo;

import java.net.URI;

public interface HttpClient {

    <T> HttpResponse<T> get(URI path, Class<T> classOfT);

    <T> HttpResponse<T> post(URI path, Object body, Class<T> responseType);

}
