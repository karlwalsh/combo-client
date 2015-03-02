package combo;

final class PojoFact {

    private final String field;

    PojoFact(final String field) {
        this.field = field;
    }

    String getField() {
        return field;
    }

    String asJsonString() {
        return "{\"field\":\"" + getField() + "\"}";
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PojoFact that = (PojoFact) o;

        return !(field != null ? !field.equals(that.field) : that.field != null);

    }

    @Override public int hashCode() {
        return field != null ? field.hashCode() : 0;
    }

    @Override public String toString() {
        return "PojoFact{" +
                "field='" + field + '\'' +
                '}';
    }
}
