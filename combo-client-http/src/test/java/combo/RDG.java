package combo;

import uk.org.fyodor.generators.Generator;
import uk.org.fyodor.generators.characters.CharacterSetFilter;

import static uk.org.fyodor.generators.characters.CharacterSetFilter.LettersOnly;

final class RDG extends uk.org.fyodor.generators.RDG {

    static Generator<String> topicName() {
        return string(10, CharacterSetFilter.LettersAndDigits);
    }

    static Generator<String> subscriptionId() {
        return string(10, CharacterSetFilter.LettersAndDigits);
    }

    static Generator<PojoFact> pojoFact() {
        return () -> new PojoFact(RDG.string(10, LettersOnly).next());
    }

    private RDG() {
    }
}
