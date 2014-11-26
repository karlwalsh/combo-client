ComboClient
===========

##Example usage##
```java
public static void main(final String[] args) {
    final Combo combo = ComboFactory.httpCombo(URI.create("http://combo-squirrel.herokuapp.com"));

    combo.subscribeTo("some.topic", Map.class)
            .forEach(fact -> combo.publishFact("another.topic", fact));
}
```
The first line creates a combo instance that uses `org.springframework.web.client.RestTemplate` to communicate with the combo server located at the specified URI. *This instance of Combo is thread-safe.*
```java
final Combo combo = ComboFactory.httpCombo(URI.create("http://combo-squirrel.herokuapp.com"));
```

##Subscribing to topics##

```java
combo.subscribeTo("some.topic", Map.class)
```

This will create a `Subscription` to '`some.topic` for the type `java.lang.Map`. Gson is used to serilialise the json response from combo, so provide your desired type here. If you don't have one, then `java.lang.Map` may be a good choice.

A `Subscription` instance allows you to fetch the next fact, or consume facts indefintely (until the program terminates, or there is an error)

```java
public interface Subscription<T> {

    Optional<T> nextFact();

    void forEach(Consumer<T> factConsumer);
}
```

For each new fact consumed, the consumer will be invoked. In the above example, the consumer is simply re-publishing the fact to a different topic.

*The `Subscription.forEach` method will not return unless there is an exception. If you wish to subscribe to multiple topics, you will need to handle that yourself.*

##Publishing to topics##

```java
combo.publishFact("another.topic", fact)
```

This will serialise the fact to json (using Gson) and publish the json to the combo server

##Caveats##
Requires Java 8
