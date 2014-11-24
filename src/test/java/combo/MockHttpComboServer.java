package combo;

import com.google.gson.Gson;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.org.fyodor.generators.RDG;

import java.net.URI;

import static org.springframework.test.web.client.MockRestServiceServer.createServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static uk.org.fyodor.generators.characters.CharacterSetFilter.LettersAndDigits;

final class MockHttpComboServer {

    private final MockRestServiceServer server;
    private final URI baseUri;

    MockHttpComboServer(final RestTemplate restTemplate, final URI baseUri) {
        this.baseUri = baseUri;
        server = createServer(restTemplate);
    }

    void expectServerWillRespondWithNextFactForSubscription(final SubscriptionId subscriptionId) {
        server.expect(requestTo(baseUri + "/topics/" + subscriptionId.topicName() + "/subscriptions/" + subscriptionId.comboId() + "/next"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(nextFactResponse(subscriptionId.topicName(), RDG.integer().next(), RDG.integer().next()), MediaType.APPLICATION_JSON));
    }

    void expectServerWillRespondWithNextFactForSubscription(final SubscriptionId subscriptionId,
                                                            final int comboTimestamp,
                                                            final int comboId) {
        server.expect(requestTo(baseUri + "/topics/" + subscriptionId.topicName() + "/subscriptions/" + subscriptionId.comboId() + "/next"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(nextFactResponse(subscriptionId.topicName(), comboTimestamp, comboId), MediaType.APPLICATION_JSON));
    }

    SubscriptionId expectServerWillAcceptSubscriptionRequestForTopic(final String topicName) {
        final String reference = RDG.string(10, LettersAndDigits).next();
        final SubscriptionId subscriptionId = new SubscriptionId(topicName, reference);
        server.expect(requestTo(baseUri + "/topics/" + topicName + "/subscriptions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(subscriptionResponse(subscriptionId), MediaType.APPLICATION_JSON));
        return subscriptionId;
    }

    void expectServerWillRespondWithNoContent() {
        server.expect(anything()).andRespond(withNoContent());
    }

    private static String subscriptionResponse(final SubscriptionId subscriptionId) {
        return "{ \"retrieval_url\": \"http://combo.example.com/topics/" + subscriptionId.topicName() + "/subscriptions/" + subscriptionId.comboId() + "\", \"subscription_id\": \"" + subscriptionId.comboId() + "\"}";
    }

    private static String nextFactResponse(final String topicName, final int comboTimestamp, final int comboId) {
        return "{ \"comboTimestamp\": " + comboTimestamp + ", \"comboTopic\": \"" + topicName + "\", \"comboId\": " + comboId + " }";
    }

    public void respondToPublishedFactWithAccepted(final String topicName, final Object fact) {
        server.expect(requestTo(baseUri + "/topics/" + topicName + "/facts"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(new Gson().toJson(fact)))
                .andRespond(withStatus(HttpStatus.ACCEPTED));
    }

    public void respondToPublishedFactWithBadRequest(final String topicName, final Object fact) {
        server.expect(requestTo(baseUri + "/topics/" + topicName + "/facts"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(new Gson().toJson(fact)))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
    }

    public void verify() {
        server.verify();
    }
}
