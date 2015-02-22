package combo;

import combo.HttpClient.HttpResponse;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Stream.generate;

final class HttpCombo implements Combo {

    private final FactProvider factProvider;
    private final FactPublisher factPublisher;
    private final TopicSubscriber topicSubscriber;

    HttpCombo(final HttpClient httpClient) {
        this.factProvider = new FactProvider(httpClient);
        this.factPublisher = new FactPublisher(httpClient);
        this.topicSubscriber = new TopicSubscriber(httpClient);
    }

    public <T> Stream<T> facts(final String topicName, final Class<? extends T> factClass) {
        final SubscriptionId subscriptionId = topicSubscriber.subscribeTo(topicName);
        return generate(() -> factProvider.nextFact(subscriptionId, factClass));
    }

    @Override public <T> void publishFact(final String topicName, final T fact) {
        factPublisher.publishFact(topicName, fact);
    }

    private static final class FactProvider {

        public static final int HTTP_NO_CONTENT = 204;

        private final HttpClient httpClient;

        private FactProvider(final HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private <T> T nextFact(final SubscriptionId subscriptionId, final Class<? extends T> classOfFact) {
            final HttpResponse<? extends T> response = httpClient.get(Paths.nextFact(subscriptionId), classOfFact);

            if (response.getStatusCode() == HTTP_NO_CONTENT) {
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
            return new UriTemplate("/topics/{topicName}/subscriptions").expand(topicName);
        }

        private static URI nextFact(final SubscriptionId subscriptionId) {
            return new UriTemplate("/topics/{topicName}/subscriptions/{subscriptionId}/next")
                    .expand(subscriptionId.topicName(), subscriptionId.comboId());
        }

        public static URI facts(final String topicName) {
            return new UriTemplate("/topics/{topicName}/facts").expand(topicName);
        }

    }
}
