package example;

import combo.Combo;

import static combo.ComboFactory.httpCombo;
import static java.net.URI.create;

public final class Example {

    public static void main(final String[] args) {
        final Combo combo = httpCombo(create("http://combo-squirrel.herokuapp.com"));

        combo.subscribeTo("some.topic", String.class)
                .forEach(fact -> combo.publishFact("another.topic", fact));
    }
}
