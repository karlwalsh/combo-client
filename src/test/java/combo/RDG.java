package combo;

import uk.org.fyodor.generators.Generator;
import uk.org.fyodor.generators.characters.CharacterSetFilter;

public final class RDG extends uk.org.fyodor.generators.RDG {

    public static Generator<String> topicName() {
        return string(10, CharacterSetFilter.LettersAndDigits);
    }

    public static Generator<String> subscriptionReference() {
        return string(10, CharacterSetFilter.LettersAndDigits);
    }

    private RDG() {
    }
}
