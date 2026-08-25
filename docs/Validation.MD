# [Validation](/src/main/java/io/github/igorgf/control/Validation.java)

A disjoint union (sum type) representing one of three distinct states:
- [Valid](/src/main/java/io/github/igorgf/control/Valid.java): 
A validated value.
- [Accumulated](/src/main/java/io/github/igorgf/control/Accumulated.java): 
  A list of accumulated validation errors.
- [Critical](/src/main/java/io/github/igorgf/control/Critical.java):
  A single major error that short circuits the validation process.

Use `Validation` to accumulate and reason about multiple errors resulting from 
some (entity, state, etc.) validation process.

Use for building extensible programmatic validation systems, where each step
can exist in isolation and then the results can be accumulated into a single
outcome.
```java
record User(UUID id, String username, String fullName, LocalDate dateOfBirth) {}

interface UserRepository {
    boolean existsById(UUID id);
    boolean isUsernameTaken(String username);
    User persist(User user);
}

// Create validators for distinct checks, this is easily extensible
interface UserValidator {

    UserRepository getRepository();

    default Validation<String, User> idClash(User user) {
        // Construct the Validation programmatically
        if (getRepository().existsById(user.id())) {
            // A Critical result will bypass further Validation
            return Validation.critical("ID Clash, contact an administrator.");
        }
        return Validation.valid(user);
    }

    default Validation<String, User> uniqueUsername(User user) {
        // Construct using the predicate driven factories
        return Validation.validate(
                user,
                u -> getRepository().existsById(u.id()),
                u -> "username '" + u.username() + "' is already taken"
        );
    }

    Validation<String, User> adult(User user);

}

Either<List<String>, User> createUser(
        User user, UserValidator userValidator, UserRepository repository
) throws IllegalArgumentException {
    // Trigger validators and combine the result
    Validation<String, User> validation = Validation
            .validate(
                    user,
                    u -> u.fullName() != null,
                    _ -> "fullName can't be null"
            )
            .combine(userValidator.uniqueUsername(user))
            .combine(userValidator.adult(user));

    // Reason about the result using pattern matching
    return switch (validation) {
        // If Valid the User object will be present to be processed
        case Valid<String, User>(User newUser) -> Either.right(repository.persist(newUser));
        // If Critical then further validations were skipped, and previous errors were discarded
        case Critical<String, User>(String criticalError) -> throw new IllegalArgumentException(criticalError);
        // If Accumulated then all non-Critical errors are aggregated in a list
        case Accumulated<String, User>(List<String> errors) -> Either.left(errors);
    };
}

void examples() {
    var idClash = new User()
}
```



TODO: document the API and edge uses cases like recovery. 

**NOTE:** *Use [Either](Either.MD), when requiring single error handling.*    
**NOTE:** *Use [Option](Option.MD), when reasoning explicitly about a presence or absence of a value.*