package io.github.igorgf.control;

/**
 * Indicates that the value passed into the computation is Empty.
 * <p>
 * This is not intended to alert about {@code null} arguments, it's intended to flag
 * empty arguments, e.g. an empty list. To flag a null argument use {@link NullArgumentException}.
 *
 * @see NullArgumentException
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
public final class EmptyArgumentException extends ContractViolationException {

    EmptyArgumentException(String argumentName) {
        super("Argument " + argumentName + " is empty. ");
    }

}
