package io.github.igorgf.control;

import io.github.igorgf.function.CheckedRunnable;

import static io.github.igorgf.control.ControlUtils.requireNonNull;

/**
 * A {@link Try} that decorates a {@link #delegate()} with an
 * {@link #finallyAction()} that always runs after it (unless the
 * {@code delegate} propagates an {@link Error}), applying the finalisation
 * semantics documented on {@link TryWithFinally#execute()}.
 *
 * @see TryWithFinally#execute()
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <R> The result type produced by the delegate.
 */
public record TryNoArgWithFinally<R>(
        Try<R> delegate,
        CheckedRunnable<? extends Throwable> finallyAction
) implements Try.TryWithFinally<R> {

    public TryNoArgWithFinally {
        requireNonNull(delegate, "delegate");
        requireNonNull(finallyAction, "finallyAction");
    }

    /**
     * {@inheritDoc}
     *
     * @return A new {@code TryNoArgWithFinally<R>} that preserves the contained
     *         {@code delegate}, also containing the provided
     *         {@code finallyAction}.
     *
     * @param finallyAction {@inheritDoc}
     *
     * @throws NullArgumentException If {@code finallyAction} is a {@code null}.
     */
    @Override
    public TryNoArgWithFinally<R> withFinally(
            CheckedRunnable<? extends Throwable> finallyAction
    ) throws NullArgumentException {
        return new TryNoArgWithFinally<>(delegate, finallyAction);
    }
}