package combo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static combo.HttpCombo.httpCombo;
import static java.lang.String.format;
import static java.net.URI.create;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public final class HttpComboTest {

    @Rule public final ExpectedException thrown = ExpectedException.none();

    @Test public void publishStringFact() {
        //Given
        final String topicName = RDG.topicName().next();
        final String fact = RDG.string().next();

        //Given
        final HttpClient httpClient = mock(HttpClient.class);
        given(httpClient.post(create(format("/topics/%s/facts", topicName)), fact, String.class)).willReturn(ok());

        //When
        httpCombo(httpClient).publishFact(topicName, fact);

        //Then
        verify(httpClient).post(create(format("/topics/%s/facts", topicName)), fact, String.class);
    }

    @Test public void publishPojoFactAsJsonString() {
        //Given
        final String topicName = RDG.topicName().next();
        final PojoFact fact = RDG.pojoFact().next();

        //Given
        final HttpClient httpClient = mock(HttpClient.class);
        given(httpClient.post(any(URI.class), anyString(), any())).willReturn(ok());

        //When
        httpCombo(httpClient).publishFact(topicName, fact);

        //Then
        verify(httpClient).post(
                create(format("/topics/%s/facts", topicName)),
                fact.asJsonString(),
                String.class);
    }

    @Test public void consumePojoFacts() {
        //Given
        final String topicName = RDG.topicName().next();
        final String subscriptionId = RDG.subscriptionId().next();

        //And
        final HttpClient httpClient = mock(HttpClient.class);
        given(httpClient.post(create(format("/topics/%s/subscriptions", topicName)), "", String.class))
                .willReturn(new HttpResponse<>(200, jsonMap("subscription_id", subscriptionId)));

        final PojoFact fact1 = RDG.pojoFact().next();
        final PojoFact fact2 = RDG.pojoFact().next();
        given(httpClient.get(create(format("/topics/%s/subscriptions/%s/next", topicName, subscriptionId)), String.class))
                .willReturn(ok(fact1.asJsonString()), ok(fact2.asJsonString()))
                .willThrow(new NoMoreFactsException());

        //When
        final List<PojoFact> facts = collectFrom(httpCombo(httpClient).facts(topicName, PojoFact.class));

        //Then
        assertThat(facts, hasItems(fact1, fact2));
    }

    @Test public void consumeStringFacts() {
        //Given
        final String topicName = RDG.topicName().next();
        final String subscriptionId = RDG.subscriptionId().next();

        //And
        final HttpClient httpClient = mock(HttpClient.class);
        given(httpClient.post(create(format("/topics/%s/subscriptions", topicName)), "", String.class))
                .willReturn(new HttpResponse<>(200, jsonMap("subscription_id", subscriptionId)));

        given(httpClient.get(create(format("/topics/%s/subscriptions/%s/next", topicName, subscriptionId)), String.class))
                .willReturn(ok("fact 1"), ok("fact 2"), ok("fact 3"))
                .willThrow(new NoMoreFactsException());

        //When
        final List<String> facts = collectFrom(httpCombo(httpClient).facts(topicName, String.class));

        //Then
        assertThat(facts, hasItems("fact 1", "fact 2", "fact 3"));
    }

    @Test public void ignoreNoContentResponses() {
        //Given
        final String topicName = RDG.topicName().next();
        final String subscriptionId = RDG.subscriptionId().next();

        //And
        final HttpClient httpClient = mock(HttpClient.class);
        given(httpClient.post(create(format("/topics/%s/subscriptions", topicName)), "", String.class))
                .willReturn(ok(jsonMap("subscription_id", subscriptionId)));

        given(httpClient.get(create(format("/topics/%s/subscriptions/%s/next", topicName, subscriptionId)), String.class))
                .willReturn(noContent(), ok("fact 1"), noContent(), noContent(), ok("fact 2"), noContent(), ok("fact 3"), noContent())
                .willThrow(new NoMoreFactsException());

        //When
        final List<String> facts = collectFrom(httpCombo(httpClient).facts(topicName, String.class));

        //Then
        assertThat(facts, hasItems("fact 1", "fact 2", "fact 3"));
    }

    @Test
    public void cannotConsumeFactsFromNullTopic() {
        //Expect
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot consume facts from null topic");

        //When
        httpCombo(mock(HttpClient.class)).facts(null, String.class);
    }

    @Test
    public void cannotConsumeFactsOfUnspecifiedType() {
        //Expect
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot consume facts of an unspecified type. Use 'Map.class' if you don't have a more specific type");

        //When
        httpCombo(mock(HttpClient.class)).facts(RDG.topicName().next(), null);
    }

    @Test
    public void cannotPublishFactToNullTopic() {
        //Expect
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot publish facts to a null topic");

        //When
        httpCombo(mock(HttpClient.class)).publishFact(null, RDG.string().next());
    }

    private static <T> HttpResponse<T> ok() {
        return new HttpResponse<>(200, null);
    }

    private static <T> HttpResponse<T> ok(final T body) {
        return new HttpResponse<>(200, body);
    }

    private static HttpResponse<String> noContent() {
        return new HttpResponse<>(204, null);
    }

    private static String jsonMap(final String key, final String value) {
        return "{\"" + key + "\":\"" + value + "\"}";
    }

    private static <T> List<T> collectFrom(final Stream<T> stream) {
        final List<T> facts = new LinkedList<>();
        try {
            stream.forEach(facts::add);
        } catch (final NoMoreFactsException e) {
            //Ignore this exception, we're using it to terminate the fact stream
        }
        return facts;
    }

    private static final class NoMoreFactsException extends RuntimeException {
        public NoMoreFactsException() {
            super("This exception is used to terminate the fact stream");
        }
    }

}