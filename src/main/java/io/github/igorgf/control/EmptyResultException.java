package io.github.igorgf.control;

/**
 * Indicates that the result of the computation is Empty.
 * <p>
 * This is not intended to alert about values that are type bound but not
 * present. To flag a null result use {@link NullResultException}.
 *
 * @see NullResultException
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
public final class EmptyResultException extends ContractViolationException {

    EmptyResultException() {
        super("Result is empty.");
    }

}
