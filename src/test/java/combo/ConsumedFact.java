package combo;

final class ConsumedFact {

    private final long comboTimestamp;
    private final long comboId;
    private final String comboTopic;

    ConsumedFact(final long comboTimestamp, final long comboId, final String comboTopic) {
        this.comboTimestamp = comboTimestamp;
        this.comboId = comboId;
        this.comboTopic = comboTopic;
    }

    long getComboTimestamp() {
        return comboTimestamp;
    }

    long getComboId() {
        return comboId;
    }

    String getComboTopic() {
        return comboTopic;
    }
}
