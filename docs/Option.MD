# [Option](/src/main/java/io/github/igorgf/control/Option.java)

A sum type representing the presence or absence of a value, as a null-safe
alternative to `java.util.Optional`. Unlike `java.util.Optional`, `Option` is a
sealed type with explicit [Present](/src/main/java/io/github/igorgf/control/Present.java)
and [Empty](/src/main/java/io/github/igorgf/control/Empty.java) cases, it
propagates checked exceptions from mapping functions. Option does not accept
`null` values on entries or exits unless explicitly stated.

Useful for reasoning about return values that are known to be possibly missing,
without having to juggle null checks, using `Option` makes optional return
values in the API more explicit, e.g.
```java
record User(String UUID, String username) {}

interface UserRepo { 
    Option<User> findByName(String username);
    User create(String username);
    
    default User getOrCreate(String username) {
        return switch (findByName(username.toLowerCase())) {
            case Present<User>(var u) -> u;
            case Empty<User> _ -> create(username);
        };
    }
}
```

Also, useful for reasoning about optional arguments, making it more terse to
declare default/alternative values, e.g.
```java
record Config(Integer port, URI uri) {}

Config configure(Option<Integer> port, Option<URI> uri) throws URISyntaxException {
    return new Config(
            port.orElse(8080),
            uri.orElseGet(() -> new URI("defaultdomain.net"))
    );
}
```

**NOTE:** *Use [Either](Either.MD), when requiring error handling on absent values.*

### Create a new `Option`:
```java
Option<Integer> oInt = Option.of(7);
Option<Long> oL = Option.ofNullable(7L);
Option<Long> oLNull = Option.ofNullable(null); // Creates a new Empty<Long>
Option<String> empty = Option.empty();
```

### Comparison on `Empty`:
Due to type erasure, `Empty` instances carry no runtime type information. Two
`Empty` instances are equal by `Empty.equals(Object)` (as they;re `record`s with
no components) regardless of their type parameter.
```java
Option<String> emptyS = Option.empty();
Option<Long> emptyL = Option.ofNullable(null);

var areEqual = emptyS.equals(emptyL); // Always true. May raise a warning.
```

## Transformations:

### Use `map` to transform the contained value when `Present`:
```java
Option<String> scream = opt.map(s -> s.toUpperCase() + "!!!");
```

### Use `flatMap` to control the boxing of the result when `Present`:
```java
// e.g. to conditionally short circuit the flow
Option<String> example = opt.flatMap(s -> {
    if (s.length() %  2 != 0) return Option.empty();
    return Option.of(s);
});
```

### Use `filter` to conditionally short-circuit when `Present`:
```java
// returns `Empty` if predicate is not met
Option<String> lenDiv5 = opt.filter(s -> s.length() % 5 == 0);
```

### Use `or` to recover from `Empty`:
```java
// Not executed if "Present"
Option<String> recover = opt.or(() -> Option.of("No condition was met!"));
```

## Inspection and Reasoning:

### Use `isPresent` and `isEmpty` to inspect whether `Present` or `Empty`:
```java
Option<String> opt = Option.of("Hello World!");
boolean value = opt.isPresent(); // True
boolean empty = opt.isEmpty(); // False
```

### Use exhaustive pattern matching to handle cases:
```java
String banner = switch(opt) {
    case Present<String>(String s) when s.length() % 2 == 0 -> 
            "You've got mail, with an even number of characters! \"" + s + "\"";
    case Present<String>(String s) -> 
            "You've got mail! \"" + s + "\"";
    case Empty<String> _ -> 
            "No Message!";
};
```

## Exits and Extractions:

### Use `fold` to yield from either case:
```java
String banner = opt.fold(
        s -> "You've got mail! \"" + s + "\"",
        () -> "Mailbox is empty."
);
```

### Use `ifPresent` to perform an action on the contained value when `Present`:

```java
void example(Option<String> opt) {
  opt.ifPresent(s -> System.out.println("opt is Present[" + s + "]"));
}
```

### Use `ifEmpty` to perform an action when `Empty`:
```java
void example(Option<String> opt) {
    opt.ifEmpty(() -> System.out.println("opt is Empty"));
}
```

### Use `ifPresentOrElse` to handle either of the two cases:
```java
void example(Option<String> opt) {
    opt.ifPresentOrElse(
            s -> System.out.println("opt is Present[" + s + "]"), 
            () -> System.out.println("opt is Empty")
    );
}
```

### Use `orElse` to extract the value or provide a replacement when `Empty`:
```java
String extracted = opt.orElse("opt was Empty");
```

### Use `orElseNullable` to extract the value or provide a nullable replacement when `Empty`:
```java
String extracted = opt.orElseNullable(null); // null here will yield an "Empty"
```

### Use `orElseGet` to extract the value, or execute a supplier and return its yield when `Empty`:
```java
String extracted = opt.orElseGet(() -> "Being Lazy.");
```

### Use `orElseThrow` to extract the value, or throw some exact exception when `Empty`:
```java
String extracted = opt.orElseThrow(() -> new IllegalStateException("No, no, no! Bad! Empty is bad!"));
```

### Use `orThrow` to extract the value, or throw an EmptyResultException when `Empty`:
```java
String extracted = opt.orThrow();
```
