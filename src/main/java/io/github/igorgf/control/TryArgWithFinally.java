package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedRunnable;
import io.github.igorgf.function.CheckedSupplier;

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
 * @param <T> {@inheritDoc}
 * @param <R> {@inheritDoc}
 */
public record TryArgWithFinally<T, R>(
        TryArg<T, R> delegate,
        CheckedRunnable<? extends Throwable> finallyAction
) implements Try.TryArg<T, R>, Try.TryWithFinally<R> {

    public TryArgWithFinally {
        requireNonNull(delegate, "delegate");
        requireNonNull(finallyAction, "finallyAction");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Produce a new {@code TryArgWithFinally}, preserving the contained
     * {@link #finallyAction()}, and a new {@code delegate} that contains the
     * new {@code argSupplier}.
     *
     * @param argSupplier The {@link CheckedSupplier} that will supply the
     *        argument to the <em>function</em> executed by this {@link Try}.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public TryArgWithFinally<T, R> withArg(
            CheckedSupplier<T, ? extends Throwable> argSupplier
    ) throws NullArgumentException {
        return new TryArgWithFinally<>(delegate.withArg(argSupplier), finallyAction);
    }

    /**
     * {@inheritDoc}
     *
     * @return A new {@code TryArgWithFinally<T, R>} that preserves the contained
     *         {@code delegate}, also containing the provided
     *         {@code finallyAction}.
     *
     * @param finallyAction {@inheritDoc}
     *
     * @throws NullArgumentException If {@code finallyAction} is a {@code null}.
     */
    @Override
    public TryArgWithFinally<T, R> withFinally(
            CheckedRunnable<? extends Throwable> finallyAction
    ) throws NullArgumentException {
        return new TryArgWithFinally<>(delegate, finallyAction);
    }

    /**
     * Return the <em>function</em> contained in the {@link #delegate()}.
     */
    @Override
    public CheckedFunction<T, R, ? extends Throwable> function() {
        return delegate().function();
    }

}