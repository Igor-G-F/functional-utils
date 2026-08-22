package io.github.igorgf.control;

/**
 * Marker for identifying exceptions as API contract violations.
 * Violating the policies of {@link io.github.igorgf.control}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
public sealed class ContractViolationException extends RuntimeException permits EmptyArgumentException, EmptyResultException, NullArgumentException, NullResultException {
    ContractViolationException(String message) {
        super("Contract violation. " + message);
    }
}