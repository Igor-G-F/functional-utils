package io.github.igorgf.control;

/**
 * Indicates that the result of the computation is a {@code null}. Violating the
 * API non-null policy. See: {@link io.github.igorgf.control}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
public final class NullResultException extends ContractViolationException {

    NullResultException(String message) {
        super(message);
    }

    NullResultException() {
        super("Function result is a \"null\".");
    }

}
