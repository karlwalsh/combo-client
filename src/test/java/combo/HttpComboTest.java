package combo;

import org.junit.Test;
import org.springframework.http.HttpRequest;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionAssertJ.then;
import static com.googlecode.catchexception.apis.CatchExceptionAssertJ.when;
import static combo.ComboAssertions.assertThat;
import static combo.ComboFactory.httpCombo;

public final class HttpComboTest {

    private MockHttpComboServer server;
    private final URI baseUri = RDG.uri().next();
    private final Combo combo = httpCombo(baseUri, setMockHttpComboServerFromHttpComboRestTemplate());

    @Test public void publishesFact() {
        //Given
        final String topicName = RDG.topicName().next();
        final PublishedFact fact = new PublishedFact(RDG.string().next(), RDG.integer().next());
        server.respondToPublishedFactWithAccepted(topicName, fact);

        //When
        combo.publishFact(topicName, fact);

        //Then
        server.verify();
    }

    @Test public void throwsExceptionWhenResponseToPublishedFactIsBadRequest() {
        //Given
        final String topicName = RDG.topicName().next();
        final PublishedFact fact = new PublishedFact(RDG.string().next(), RDG.integer().next());
        server.respondToPublishedFactWithBadRequest(topicName, fact);

        //When
        when(combo).publishFact(topicName, fact);

        //Then
        then(caughtException())
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 Bad Request");
    }

    @Test public void consumesFirstFactFromTopic() {
        //Given
        final String topicName = RDG.topicName().next();
        final SubscriptionId subscriptionId = server.expectServerWillAcceptSubscriptionRequestForTopic(topicName);

        final int comboTimestamp = RDG.integer().next();
        final int comboId = RDG.integer().next();
        server.expectServerWillRespondWithNextFactForSubscription(subscriptionId, comboTimestamp, comboId);

        //When
        final List<ConsumedFact> facts = consumeFacts(topicName);

        //Then
        assertThat(facts.get(0))
                .hasId(comboId)
                .hasTimestamp(comboTimestamp)
                .hasTopicName(topicName);
    }

    @Test public void consumesFirstTwoFactsFromTopic() {
        //Given
        final String topicName = RDG.topicName().next();
        final SubscriptionId subscriptionId = server.expectServerWillAcceptSubscriptionRequestForTopic(topicName);

        final int secondFactTimestamp = RDG.integer().next();
        final int secondFactId = RDG.integer().next();
        server.expectServerWillRespondWithNextFactForSubscription(subscriptionId, uk.org.fyodor.generators.RDG.integer().next(), uk.org.fyodor.generators.RDG.integer().next());
        server.expectServerWillRespondWithNextFactForSubscription(subscriptionId, secondFactTimestamp, secondFactId);

        //When
        final List<ConsumedFact> facts = consumeFacts(topicName);

        //Then
        assertThat(facts.get(1))
                .hasId(secondFactId)
                .hasTimestamp(secondFactTimestamp)
                .hasTopicName(topicName);
    }

    @Test public void consumesZeroFactsWhenThereIsNotContent() {
        //Given
        final String topicName = RDG.topicName().next();

        server.expectServerWillAcceptSubscriptionRequestForTopic(topicName);
        server.expectServerWillRespondWithNoContent();

        //When
        final List<ConsumedFact> facts = consumeFacts(topicName);

        //Then
        assertThat(facts).isEmpty();
        server.verify();
    }

    @Test public void onlyPresentFactsAreConsumed() {
        //Given
        final String topicName = RDG.topicName().next();
        final SubscriptionId subscriptionId = server.expectServerWillAcceptSubscriptionRequestForTopic(topicName);

        server.expectServerWillRespondWithNoContent();
        server.expectServerWillRespondWithNextFactForSubscription(subscriptionId);
        server.expectServerWillRespondWithNoContent();
        server.expectServerWillRespondWithNoContent();
        server.expectServerWillRespondWithNoContent();
        server.expectServerWillRespondWithNextFactForSubscription(subscriptionId);
        server.expectServerWillRespondWithNoContent();

        //When
        final List<ConsumedFact> facts = consumeFacts(topicName);

        //Then
        assertThat(facts).hasSize(2);
    }

    @Test
    public void visitorsCanAddRestTemplateInterceptors() {
        //Given
        final LinkedList<HttpRequest> httpRequests = new LinkedList<>();
        final Combo combo = httpCombo(baseUri,
                setMockHttpComboServerFromHttpComboRestTemplate(),
                restTemplate -> restTemplate.getInterceptors().add((request, body, execution) -> {
                    httpRequests.add(request);
                    return execution.execute(request, body);
                }));

        //And
        final String topicName = RDG.topicName().next();
        final String fact = "Some Fact";
        server.respondToPublishedFactWithAccepted(topicName, fact);

        //When
        combo.publishFact(topicName, fact);

        //Then
        assertThat(httpRequests).hasSize(1);
    }

    private List<ConsumedFact> consumeFacts(final String topicName) {
        final FactCollector<ConsumedFact> factCollector = new FactCollector<>();

        try {
            combo.subscribeTo(topicName, ConsumedFact.class).forEach(factCollector);
        } catch (final AssertionError ae) {
            //Swallow exception, we're using it to terminate the fact request loop
        }

        return factCollector.facts();
    }

    private ComboFactory.RestTemplateVisitor setMockHttpComboServerFromHttpComboRestTemplate() {
        return restTemplate -> this.server = new MockHttpComboServer(restTemplate, baseUri);
    }
}
