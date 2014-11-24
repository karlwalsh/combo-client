package combo;

public interface Combo {
    <T> Subscription<T> subscribeTo(String topic, Class<? extends T> factClass);

    <T> void publishFact(String topicName, T fact);
}
