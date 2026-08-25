# [Either](/src/main/java/io/github/igorgf/control/Either.java)

A disjoint union (sum type) representing exactly one of two possible outcomes:
[Left](/src/main/java/io/github/igorgf/control/Left.java) or
[Right](/src/main/java/io/github/igorgf/control/Right.java). This is NOT a
"pair type". Either is a general purpose XOR type for reasoning about two
mutually exclusive results. Either propagates checked exceptions from mapping
functions. Option does not accept `null` values on entries or exits.

Use `Either` when the outcomes are mutually exclusive:
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

**NOTE:** *Use [Validation](Validation.MD), when requiring error accumulation over multiple values or checks.*   
**NOTE:** *Use [Option](Option.MD), when reasoning explicitly about a presence or absence of a value.*