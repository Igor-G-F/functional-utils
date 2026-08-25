# [Try](/src/main/java/io/github/igorgf/control/Try.java)

A deferred, fallible computation that captures its outcome as an `Either` of a 
`Thrown<Throwable>` or a successfully produced result. A `Try` describes an 
operation that may throw; it does not run until `execute()` is called. This is
an abstraction over the java `try {} catch(Exception x) {}` block.

### try/catch
```java
Either<Thrown<Throwable>, String> rawJavaExample(String rawInput) {
    try {
        return Either.right(parse(rawInput));
    } catch (Exception x) {
        return Either.left(Thrown.of(x));
    }
}

Either<Thrown<Throwable>, String> functionalExample(String rawInput) {
    return Try.of(this::parse)
            .withArg(rawInput)
            .execute();
}
```

### try with resources/catch
```java
Either<Thrown<Throwable>, String> rawJavaExample(Path path) {
    try (InputStream in = Files.newInputStream(path)) {
        return Either.right(readAll(in));
    } catch (Exception x) {
        return Either.left(Thrown.of(x));
    }
}

Either<Thrown<Throwable>, String> functionalExample(Path path) {
    return Try.ofResource(this::readAll)
            // stream is deferred until execution in a try with resources block
            .withArg(() -> Files.newInputStream(path))
            .execute();
}
```

### try/catch/finally
```java
Either<Thrown<Throwable>, String> tryCatchFinallyRaw(String rawInput) {
    try {
        return Either.right(parse(rawInput));
    } catch (Exception x) {
        return Either.left(Thrown.of(x));
    } finally {
        releaseBuffer();
    }
}

Either<Thrown<Throwable>, String> tryCatchFinallyWithTry(String rawInput) {
    return Try.of(this::parse)
            .withArg(rawInput)
            .withFinally(this::releaseBuffer)
            .execute();
}
```

### Defer execution and execute against values programmatically
```java
// register the function, Integer.parseInt(s) throws a NumberFormatException on failure
Try.ArgStep<String, Integer> tryToInt = Try.of((String s) -> Integer.parseInt(s));

List<Integer> toInts(List<String> numbers) {
    List<Integer> ints = new ArrayList<>();
    for (String sn :  numbers) {
        // register the argument and execute, reason on the result using switch
        switch (tryToInt.withArg(sn).execute()) {
            case Right<?, Integer>(Integer n) -> ints.add(n);
            case Left<?, ?> _ -> ints.add(0);
        }
    }
    return ints;
}

// Try returns an Either which can be used for even more expressive code
List<Integer> toInts2(List<String> numbers) {
    List<Integer> ints = new ArrayList<>();
    for (String sn :  numbers) tryToInt
            .withArg(sn)
            .execute()
            .fold(_ -> 0, n -> n);
    return ints;
}
List<Integer> toInts2(List<String> numbers) {
    return numbers.stream().map(s -> tryToInt
            .withArg(s)
            .execute()
            .fold(_ -> 0, n -> n)
    ).toList();
}
```

TODO: add more use cases, document API