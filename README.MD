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

### README Contents

[Leveraging `record` and `sealed` types](#leveraging-record-and-sealed-types)   
[Null policy](#null-policy)    
[Checked Functional Interfaces](#checked-functional-interfaces)    

### Type Documentation Links
[Option](/docs/Option.MD)    
[Either](/docs/Either.MD)   
[Validation](/docs/Validation.MD)    
[Try](/docs/Try.MD)    
[Thrown](/docs/Thrown.MD)    

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