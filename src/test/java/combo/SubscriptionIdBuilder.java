package combo;

public final class SubscriptionIdBuilder {

    private String topicName = RDG.topicName().next();
    private String reference = RDG.subscriptionReference().next();

    public SubscriptionIdBuilder topicName(final String topicName) {
        this.topicName = topicName;
        return this;
    }

    public SubscriptionId build() {
        return new SubscriptionId(topicName, reference);
    }

    public static SubscriptionId randomSubscriptionId() {
        return subscriptionIdBuilder().build();
    }

    public static SubscriptionIdBuilder subscriptionIdBuilder() {
        return new SubscriptionIdBuilder();
    }

    private SubscriptionIdBuilder() {
    }
}
