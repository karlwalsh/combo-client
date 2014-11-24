package combo;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

final class ConsumedFactAssert extends AbstractAssert<ConsumedFactAssert, ConsumedFact> {

    ConsumedFactAssert(final ConsumedFact actual) {
        super(actual, ConsumedFactAssert.class);
    }

    ConsumedFactAssert hasTimestamp(final long timestamp) {
        isNotNull();

        Assertions.assertThat(actual.getComboTimestamp())
                .describedAs("combo fact timestamp")
                .isEqualTo(timestamp);

        return this;
    }

    ConsumedFactAssert hasId(final long id) {
        isNotNull();

        Assertions.assertThat(actual.getComboId())
                .describedAs("combo fact id")
                .isEqualTo(id);

        return this;
    }

    ConsumedFactAssert hasTopicName(final String topicName) {
        isNotNull();

        Assertions.assertThat(actual.getComboTopic())
                .describedAs("combo topic name")
                .isEqualTo(topicName);

        return this;
    }
}
