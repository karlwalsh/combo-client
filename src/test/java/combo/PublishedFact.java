package combo;

public final class PublishedFact {

    private final String someString;
    private final Integer someInteger;

    public PublishedFact(final String someString, final Integer someInteger) {
        this.someString = someString;
        this.someInteger = someInteger;
    }

    public String getSomeString() {
        return someString;
    }

    public Integer getSomeInteger() {
        return someInteger;
    }
}
