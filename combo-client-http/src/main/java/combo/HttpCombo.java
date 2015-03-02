package combo;

import com.google.gson.Gson;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static combo.HttpResponse.Status.NO_CONTENT;
import static combo.HttpResponse.noContent;
import static java.lang.String.format;
import static java.net.URI.create;
import static java.util.stream.Stream.generate;

public final class HttpCombo implements Combo {

    private final FactProvider factProvider;
    private final FactPublisher factPublisher;
    private final TopicSubscriber topicSubscriber;

    private HttpCombo(final HttpClient httpClient) {
        final HttpClient gsonHttpClient = gsonHttpClient(httpClient);
        this.factProvider = new FactProvider(gsonHttpClient);
        this.factPublisher = new FactPublisher(gsonHttpClient);
        this.topicSubscriber = new TopicSubscriber(gsonHttpClient);
    }

    @Override public <T> Stream<T> facts(final String topicName, final Class<? extends T> factClass) {
        checkNotNull(topicName, "Cannot consume facts from null topic");
        checkNotNull(factClass, "Cannot consume facts of an unspecified type. Use 'Map.class' if you don't have a more specific type");

        final SubscriptionId subscriptionId = topicSubscriber.subscribeTo(topicName);

        return generate(() -> factProvider.nextFact(subscriptionId, factClass));
    }

    @Override public <T> void publishFact(final String topicName, final T fact) {
        checkNotNull(topicName, "Cannot publish facts to a null topic");

        factPublisher.publishFact(topicName, fact);
    }

    public static Combo httpCombo(final HttpClient httpClient) {
        return new HttpCombo(httpClient);
    }

    private static GsonHttpClient gsonHttpClient(final HttpClient httpClient) {
        return new GsonHttpClient(httpClient, new Gson());
    }

    private static void checkNotNull(final Object argument, final String message) {
        if (argument == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static final class GsonHttpClient implements HttpClient {

        private final HttpClient httpClient;
        private final Gson gson;

        private GsonHttpClient(final HttpClient httpClient, final Gson gson) {
            this.httpClient = httpClient;
            this.gson = gson;
        }

        @Override public <T> HttpResponse<T> get(final URI path, final Class<T> responseType) {
            return responseBodyTransformer(gson, responseType)
                    .apply(httpClient.get(path, String.class));
        }

        @Override
        public <T> HttpResponse<T> post(final URI path, final Object requestBody, final Class<T> responseType) {
            final String requestBodyString = requestBodyTransformer(gson).apply(requestBody);

            return responseBodyTransformer(gson, responseType)
                    .apply(httpClient.post(path, requestBodyString, String.class));
        }

        private static Function<Object, String> requestBodyTransformer(final Gson gson) {
            return requestBody -> requestBody instanceof String
                    ? (String) requestBody
                    : gson.toJson(requestBody);
        }

        @SuppressWarnings("unchecked")
        private static <T> Function<HttpResponse<String>, HttpResponse<T>> responseBodyTransformer(final Gson gson, final Class<? extends T> responseType) {
            return response -> {
                if (String.class == responseType) {
                    return (HttpResponse<T>) response;
                }

                return response.getStatusCode() == NO_CONTENT
                        ? noContent()
                        : response.withBody(gson.fromJson(response.getBody(), responseType));
            };
        }
    }

    private static final class FactProvider {

        private final HttpClient httpClient;

        private FactProvider(final HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private <T> T nextFact(final SubscriptionId subscriptionId, final Class<? extends T> classOfFact) {
            final HttpResponse<? extends T> response = httpClient.get(Paths.nextFact(subscriptionId), classOfFact);

            if (response.getStatusCode() == NO_CONTENT) {
                return nextFact(subscriptionId, classOfFact);
            }

            return response.getBody();
        }
    }

    private static final class TopicSubscriber {

        private final HttpClient httpClient;

        private TopicSubscriber(final HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private SubscriptionId subscribeTo(final String topicName) {
            final HttpResponse<Map> response = httpClient.post(Paths.subscriptions(topicName), "", Map.class);
            final String comboId = (String) response.getBody().get("subscription_id");
            return new SubscriptionId(topicName, comboId);
        }
    }

    private static final class FactPublisher {

        private final HttpClient httpClient;

        private FactPublisher(final HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private <T> void publishFact(final String topicName, final T fact) {
            httpClient.post(Paths.facts(topicName), fact, Void.class);
        }
    }

    private static final class SubscriptionId {

        private final String topicName;
        private final String comboId;

        private SubscriptionId(final String topicName, final String comboId) {
            this.topicName = topicName;
            this.comboId = comboId;
        }

        private String topicName() {
            return topicName;
        }

        private String comboId() {
            return comboId;
        }
    }

    private static final class Paths {
        private static URI subscriptions(final String topicName) {
            return create(format("/topics/%s/subscriptions", topicName));
        }

        private static URI nextFact(final SubscriptionId subscriptionId) {
            return create(format("/topics/%s/subscriptions/%s/next", subscriptionId.topicName(), subscriptionId.comboId()));
        }

        public static URI facts(final String topicName) {
            return create(format("/topics/%s/facts", topicName));
        }
    }
}