package io.github.igorgf.control;

/**
 * Indicates that the value passed into the computation is a {@code null}.
 * Violating the API non-null policy.
 *
 * @see io.github.igorgf.control
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
public final class NullArgumentException extends ContractViolationException {

    NullArgumentException(String argumentName) {
        super("Argument \"" + argumentName + "\" is null.");
    }

}
