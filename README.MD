# functional-utils

Core functional control-flow types for error handling and value presence.

This package provides sum types (algebraic data types) for representing optional 
values, disjoint results, and validation outcomes in a null safe, functional 
style:
- **[Option](src/main/java/io/github/igorgf/control/Option.java)** - presence or 
  absence of a value 
- **[Either](src/main/java/io/github/igorgf/control/Either.java)** - a disjoint 
  union of two possible outcomes
- **[Validation](src/main/java/io/github/igorgf/control/Validation.java)** - a 
  validated value or an accumulated list of errors
- **[Try](src/main/java/io/github/igorgf/control/Try.java)** - a computation that 
  may throw, with exception handling
- **[Thrown](src/main/java/io/github/igorgf/control/Thrown.java)** - an exception 
  container for fluent reasoning and handling
#### [Full Javadocs Available Here](https://igor-g-f.github.io/functional-utils/)

---

## Contents

- [Leveraging `record` and `sealed` types](#leveraging-record-and-sealed-types)
- [Null policy](#null-policy)
- [Checked Functional Interfaces](#checked-functional-interfaces)
- [Option](#option)
  - [Create a new `Option`](#create-a-new-option)
  - [Comparison on `Empty`](#comparison-on-empty)
  - [Transformations of contained value](#transformations)
  - [Inspection and Reasoning](#inspection-and-reasoning)
  - [Exist and Extractions](#exits-and-extractions)
- [Either](#either)
  - [???]()
- [Validation](#validation)
- [Try](#try)
- [Thrown](#thrown)

---

## Leveraging `record` and `sealed` types:
The control types are all based on `sealed` interfaces with explicit 
implementations leveraging the `record` type. This yields a variety of useful 
benefits:
- **Exhaustive Pattern Matching with `switch`:** Switching over a *sealed* type 
  gives the compiler full visibility over the possible subtypes. `default` case
  can be safely omitted when all cases are covered, and teh compiler will make
  it clear if that has been done. 
- **Closed extensibility model:** No third party can create or overload 
  implementations of these control types, giving full predictability and 
  confidence in the system. `default` can be used aggressively without having
  to consider any unsupported or unknown cases.
- **Immutability by default:** `record` fields are final, no setters or mutable 
  state to consider when using the control types. Null check boundaries are 
  placed on the canonical constructors to prevent `null` value entry into the 
  system. 
  
## Null policy:
All methods in this package reject `null` arguments and results by throwing some 
[ContractViolationException](src/main/java/io/github/igorgf/control/ContractViolationException.java). 
This is a precondition violation (a bug in the caller), not a domain result, and 
is therefore not represented in the return type. The functional types in this 
package are `null` safe by construction and result, once a value is wrapped in 
the above listed types it is guaranteed non-null. Also, the resutls of any 
computations performed by these types are guaranteed non-null.

---

## [Checked Functional Interfaces](src/main/java/io/github/igorgf/function)
Intended for use with the control flow types in `io.github.igorgf.control`. 
These interfaces mirror the standard `java.util.function` interfaces but declare 
checked exceptions via a generic `X extends Throwable` parameter, enabling 
precise checked exception tracking through `Try` and related types.

For example, in standard `java.util.function` interfaces, when some checked
exception is explicitly thrown or propagated, it has to be handled within the
function by declaring a `try` block and rethrowing as a `RuntimeException`:
```java
// The following illegal and won't compile.
void example() {
    Function<String, String> func = n -> { throw new Exception(); };
    
    func.apply("Hello World!");
}

// This will compile.
void example() {
    Function<String, String> func = s -> { 
        try {
            throw new Exception(s);
        } catch (Throwable x) {
            throw new RuntimeException(x);
        }
    };

    func.apply("Hello World!");
}
```

Now using the members of `io.github.igorgf.function` you can comfortably 
propagate the exception types:
```java
// "throws" can be used to propagate the exception.
// Alternatively .apply() call could be surrounded with a try catch.
void example() throws Throwable { 
    CheckedFunction<String, String, Throwable> func = s -> {
        // No try catch block needed, handling is delegated to the caller.
        throw new Exception(s); 
    };

    // Calling .apply() propagates the exception to the caller requiring handling at the call site.
    func.apply("Hello World!");
}

// When binding to a RuntimeException there's nothing to handle.
void example() {
    CheckedFunction<String, String, RuntimeException> func = s -> s + s;
    
    func.apply("Hello World!");
}
```

Also type erasure can be leveraged to make the exception propagation only 
required for explicit throws of checked exceptions:
```java
<X extends Throwable> void welcome(
        CheckedFunction<String, String, X> func
) throws X {
    func.apply("Hello World!");
}

// No need to handle where no checked exception is thrown.
void example() {
    welcome(n -> n + n);
    welcome(n -> { throw new RuntimeException(n); });
}

// But when an exception is explicitly thrown it must be handled.
void example() throws Exception {
    welcome(n -> { throw new Exception(n); });
}
```

---

## [Option](src/main/java/io/github/igorgf/control/Option.java)

A sum type representing the presence or absence of a value, as a null-safe 
alternative to `java.util.Optional`. Unlike `java.util.Optional`, `Option` is a 
sealed type with explicit [Present](src/main/java/io/github/igorgf/control/Present.java) 
and [Empty](src/main/java/io/github/igorgf/control/Empty.java) cases, it 
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

**NOTE:** *Use [Either](#either), when requiring error handling on absent values.*

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

### Transformations:

#### Use `map` to transform the contained value when `Present`:
```java
Option<String> scream = opt.map(s -> s.toUpperCase() + "!!!");
```

#### Use `flatMap` to control the boxing of the result when `Present`:
```java
// e.g. to conditionally short circuit the flow
Option<String> example = opt.flatMap(s -> {
    if (s.length() %  2 != 0) return Option.empty();
    return Option.of(s);
});
```

#### Use `filter` to conditionally short-circuit when `Present`:
```java
// returns `Empty` if predicate is not met
Option<String> lenDiv5 = opt.filter(s -> s.length() % 5 == 0);
```

#### Use `or` to recover from `Empty`:
```java
// Not executed if "Present"
Option<String> recover = opt.or(() -> Option.of("No condition was met!"));
```

### Inspection and Reasoning:

#### Use `isPresent` and `isEmpty` to inspect whether `Present` or `Empty`:
```java
Option<String> opt = Option.of("Hello World!");
boolean value = opt.isPresent(); // True
boolean empty = opt.isEmpty(); // False
```

#### Use exhaustive pattern matching to handle cases:
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

### Exits and Extractions:

#### Use `fold` to yield from either case:
```java
String banner = opt.fold(
        s -> "You've got mail! \"" + s + "\"",
        () -> "Mailbox is empty."
);
```

#### Use `ifPresent` to perform an action on the contained value when `Present`:

```java
void example(Option<String> opt) {
  opt.ifPresent(s -> System.out.println("opt is Present[" + s + "]"));
}
```

#### Use `ifEmpty` to perform an action when `Empty`:
```java
void example(Option<String> opt) {
    opt.ifEmpty(() -> System.out.println("opt is Empty"));
}
```

#### Use `ifPresentOrElse` to handle either of the two cases:
```java
void example(Option<String> opt) {
    opt.ifPresentOrElse(
            s -> System.out.println("opt is Present[" + s + "]"), 
            () -> System.out.println("opt is Empty")
    );
}
```

#### Use `orElse` to extract the value or provide a replacement when `Empty`:
```java
String extracted = opt.orElse("opt was Empty");
```

#### Use `orElseNullable` to extract the value or provide a nullable replacement when `Empty`:
```java
String extracted = opt.orElseNullable(null); // null here will yield an "Empty"
```

#### Use `orElseGet` to extract the value, or execute a supplier and return its yield when `Empty`:
```java
String extracted = opt.orElseGet(() -> "Being Lazy.");
```

#### Use `orElseThrow` to extract the value, or throw some exact exception when `Empty`:
```java
String extracted = opt.orElseThrow(() -> new IllegalStateException("No, no, no! Bad! Empty is bad!"));
```

#### Use `orThrow` to extract the value, or throw an EmptyResultException when `Empty`:
```java
String extracted = opt.orThrow();
```

---

<!-- TODO: Work on the Either docs -->

## [Either](src/main/java/io/github/igorgf/control/Either.java)

A disjoint union (sum type) representing exactly one of two possible outcomes:
[Left](src/main/java/io/github/igorgf/control/Left.java) or 
[Right](src/main/java/io/github/igorgf/control/Right.java). This is NOT a 
"pair type". Either is a general purpose XOR type for reasoning about two 
mutually exclusive results. Either propagates checked exceptions from mapping 
functions. Option does not accept `null` values on entries or exits.

When the outcomes are mutually exclusive:
```java
interface Json { 
    String toString();
}
record Raw(byte[] data, String contentType) {}

interface Client {
    Either<Raw, Json> get(URI uri);

    default String getAsString(URI uri) {
        return switch (get(uri)) {
            case Left<Raw, Json>(Raw(var data, var contentType)) when contentType.equals("text/plain") ->
                    "Plain text: " + new String(data, StandardCharsets.UTF_8);

            case Left<Raw, Json>(Raw(var data, var contentType)) ->
                    "Binary data (" + data.length + " bytes, " + contentType + ")";

            case Right<Raw, Json>(var json) -> json.toString();
        };
    }
}
```

Also, useful for expressing error states:
```java
record User(String UUID, String username) {}
record Error(int code, String message) {}

interface UserRepo {
    Option<User> findByName(String username);
    Option<User> update(User user);
    User create(String username);
    
    /**
     * Expressing error states via a type instead of exceptions makes the API
     * more explicit. It does not obscure behaviour like unchecked exceptions, 
     * and allows passing errors around without requiring try catch blocks like 
     * checked exceptions do.
     */
    default Either<Error, User> putUser(String username, String newUsername) {
        if (username.equalsIgnoreCase("admin")) {
            return Either.left(new Error(
                    400, "\"admin\" user cannot be renamed"
            ));
        }
        if (newUsername.equalsIgnoreCase("admin")) {
            return Either.left(new Error(
                    400, "\"admin\" is a reserved username"
            ));
        }
    
        return switch (findByName(username.toLowerCase())) {
            case Present<User>(var u) -> {
                var updatedUser = update(new User(u.UUID, newUsername));
                yield updatedUser.fold(
                        Either::right,
                        () -> Either.left(new Error(500, "failed to update user, contact an administrator."))
                );
            }
            case Empty<User> _ -> Either.right(create(newUsername));
        };
    }
}
```

**NOTE:** *Use [Validation](#validation), when requiring error accumulation over multiple values or checks.*   
**NOTE:** *Use [Option](#option), when reasoning explicitly about a presence or absence of a value.*

---

## [Validation](src/main/java/io/github/igorgf/control/Validation.java)

awafasfsaf

**NOTE:** *Use [Either](#either), when requiring error handling on a single.*
**NOTE:** *Use [Option](#option), when reasoning explicitly about a presence or absence of a value.*

---

## [Try](src/main/java/io/github/igorgf/control/Try.java)

asfsfafas

---

## [Thrown](src/main/java/io/github/igorgf/control/Thrown.java)

asfsfafas