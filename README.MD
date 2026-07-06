# functional-utils

Core functional control-flow types for error handling and value presence.

This package provides sum types (algebraic data types) for representing optional 
values, disjoint results, and validation outcomes in a null safe, functional 
style:
- [Option](src/main/java/io/github/igorgf/control/Option.java) - presence or 
absence of a value 
- [Either](src/main/java/io/github/igorgf/control/Either.java) - a disjoint 
union of two possible outcomes
- [Validation](src/main/java/io/github/igorgf/control/Validation.java) - a 
validated value or an accumulated list of errors
- [Try](src/main/java/io/github/igorgf/control/Try.java) - a computation that 
may throw, with exception handling

**Null policy:** All methods in this package reject null arguments by throwing 
`NullPointerException`. This is a precondition violation (a bug in the caller), 
not a domain result, and is therefore not represented in the return type. The 
functional types in this package are null safe by construction, once a value is 
wrapped in the above listed types it is guaranteed non-null.

**Exception policy:** Checked exceptions are used only for precondition 
violations (e.g. `Validation.invalid(List.of())` throws `EmptyValueException`).

**Checked functional interfaces:** for use with the control flow types in 
`io.github.igorgf.control`. These interfaces mirror the standard 
`java.util.function` interfaces but declare checked exceptions via a generic 
`X extends Exception` parameter, enabling precise checked exception tracking 
through `Try` and related types.


**[Javadocs Available Here](https://igor-g-f.github.io/functional-utils/)**