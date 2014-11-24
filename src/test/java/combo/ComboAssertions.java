package combo;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.Optional;

public final class ComboAssertions extends Assertions {

    public static ConsumedFactAssert assertThat(final ConsumedFact actual) {
        return new ConsumedFactAssert(actual);
    }

    public static <T> OptionalAssert<T> assertThat(final Optional<T> actual) {
        return new OptionalAssert<>(actual);
    }

    private ComboAssertions() {
    }

    static final class OptionalAssert<T> extends AbstractAssert<OptionalAssert<T>, Optional<T>> {

        protected OptionalAssert(final Optional<T> actual) {
            super(actual, OptionalAssert.class);
        }

        OptionalAssert<T> isAbsent() {
            isNotNull();

            assertThat(actual.isPresent())
                    .describedAs("Expecting optional value to be absent")
                    .isFalse();

            return this;
        }
    }
}
