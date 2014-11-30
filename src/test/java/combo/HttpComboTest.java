package combo;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.List;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionAssertJ.then;
import static com.googlecode.catchexception.apis.CatchExceptionAssertJ.when;
import static combo.ComboFactory.httpCombo;
import static combo.ComboServerRule.ComboServerResponse.*;
import static combo.HttpComboTest.ConsumedFact.randomConsumedFact;
import static combo.HttpComboTest.PublishedFact.randomPublishedFact;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.fyodor.generators.characters.CharacterSetFilter.LettersOnly;

public final class HttpComboTest {

    @Rule
    public final ComboServerRule comboServer = new ComboServerRule(8080);

    private final Combo combo = httpCombo(URI.create("http://localhost:8080"));

    @Test public void publishesFact() {
        //Given
        final String topicName = RDG.topicName().next();
        comboServer.whenFactIsPublished(topicName).thenRespondWith(ok());

        //When
        combo.publishFact(topicName, randomPublishedFact());

        //Then
        comboServer.verifyFactWasPublished(topicName);
    }

    @Test public void throwsExceptionWhenResponseToPublishedFactIsBadRequest() {
        //Given
        final String topicName = RDG.topicName().next();
        comboServer.whenFactIsPublished(topicName).thenRespondWith(badRequest());

        //When
        when(combo).publishFact(topicName, randomPublishedFact());

        //Then
        then(caughtException())
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 Bad Request");
    }

    @Test public void consumesFirstFactFromTopic() {
        //Given
        final String topicName = RDG.topicName().next();
        final String subscriptionId = RDG.subscriptionId().next();
        comboServer.whenTopicIsSubscribedTo(topicName)
                .thenRespondWith(subscriptionWithId(subscriptionId));

        final ConsumedFact consumedFact = randomConsumedFact();
        comboServer.whenNextFactIsRequested(topicName, subscriptionId)
                .thenRespondWith(fact(consumedFact));

        //When
        final List<ConsumedFact> facts = consumeFacts(topicName, ConsumedFact.class);

        //Then
        assertThat(facts).containsExactly(consumedFact);
    }

    @Test public void consumesFirstTwoFactsFromTopic() {
        //Given
        final String topicName = RDG.topicName().next();
        final String subscriptionId = RDG.subscriptionId().next();
        comboServer.whenTopicIsSubscribedTo(topicName)
                .thenRespondWith(subscriptionWithId(subscriptionId));

        final ConsumedFact firstFact = randomConsumedFact();
        final ConsumedFact secondFact = randomConsumedFact();
        comboServer.whenNextFactIsRequested(topicName, subscriptionId).thenRespondWith(
                fact(firstFact),
                fact(secondFact));

        //When
        final List<ConsumedFact> facts = consumeFacts(topicName, ConsumedFact.class);

        //Then
        assertThat(facts)
                .containsExactly(firstFact, secondFact);
    }

    @Test public void consumesZeroFactsWhenThereIsNoContent() {
        //Given
        final String topicName = RDG.topicName().next();
        final String subscriptionId = RDG.subscriptionId().next();
        comboServer.whenTopicIsSubscribedTo(topicName)
                .thenRespondWith(subscriptionWithId(subscriptionId));

        comboServer.whenNextFactIsRequested(topicName, subscriptionId)
                .thenRespondWith(noContent());

        //When
        final List<ConsumedFact> facts = consumeFacts(topicName, ConsumedFact.class);

        //Then
        assertThat(facts).isEmpty();
    }

    @Test public void onlyPresentFactsAreConsumed() {
        //Given
        final String topicName = RDG.topicName().next();
        final String subscriptionId = RDG.subscriptionId().next();
        comboServer.whenTopicIsSubscribedTo(topicName)
                .thenRespondWith(subscriptionWithId(subscriptionId));

        final ConsumedFact fact1 = randomConsumedFact();
        final ConsumedFact fact2 = randomConsumedFact();
        final ConsumedFact fact3 = randomConsumedFact();
        final ConsumedFact fact4 = randomConsumedFact();
        comboServer.whenNextFactIsRequested(topicName, subscriptionId)
                .thenRespondWith(noContent(), fact(fact1), fact(fact2), noContent(), fact(fact3), noContent(), fact(fact4), noContent());

        //When
        final List<ConsumedFact> facts = consumeFacts(topicName, ConsumedFact.class);

        //Then
        assertThat(facts)
                .containsExactly(fact1, fact2, fact3, fact4);
    }

    private <T> List<T> consumeFacts(final String topicName, final Class<? extends T> classOfT) {
        final FactCollector<T> factCollector = new FactCollector<>();

        try {
            combo.subscribeTo(topicName, classOfT).forEach(factCollector);
        } catch (final HttpClientErrorException e) {
            //Swallow exception, we're using it to terminate the fact request loop
        }

        return factCollector.facts();
    }

    static final class ConsumedFact {

        private final long someField;

        ConsumedFact(final long someField) {
            this.someField = someField;
        }

        @SuppressWarnings("UnusedDeclaration") long getSomeField() {
            return someField;
        }

        static ConsumedFact randomConsumedFact() {
            return new ConsumedFact(RDG.longVal().next());
        }

        @Override public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ConsumedFact that = (ConsumedFact) o;

            return someField == that.someField;

        }

        @Override public int hashCode() {
            return (int) (someField ^ (someField >>> 32));
        }
    }

    static final class PublishedFact {

        private final String someField;

        PublishedFact(final String someField) {
            this.someField = someField;
        }

        @SuppressWarnings("UnusedDeclaration") String getSomeField() {
            return someField;
        }

        static PublishedFact randomPublishedFact() {
            return new PublishedFact(RDG.string(30, LettersOnly).next());
        }

        @Override public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final PublishedFact that = (PublishedFact) o;

            return !(someField != null ? !someField.equals(that.someField) : that.someField != null);

        }

        @Override public int hashCode() {
            return someField != null ? someField.hashCode() : 0;
        }
    }
}
