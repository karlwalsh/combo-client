package combo;

import java.util.Optional;
import java.util.function.Consumer;

public interface Subscription<T> {

    Optional<T> nextFact();

    void forEach(Consumer<T> factConsumer);
}
