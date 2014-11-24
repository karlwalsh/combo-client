package combo;

final class SubscriptionId {

    private final String topicName;
    private final String comboId;

    SubscriptionId(final String topicName, final String comboId) {
        this.topicName = topicName;
        this.comboId = comboId;
    }

    String topicName() {
        return topicName;
    }

    String comboId() {
        return comboId;
    }
}
