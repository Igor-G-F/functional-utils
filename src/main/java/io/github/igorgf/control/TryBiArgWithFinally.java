package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
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
 * @param <U> {@inheritDoc}
 * @param <R> {@inheritDoc}
 */
public record TryBiArgWithFinally<T, U, R>(
        TryBiArg<T, U, R> delegate,
        CheckedRunnable<? extends Throwable> finallyAction
) implements Try.TryBiArg<T, U, R>, Try.TryWithFinally<R> {

    public TryBiArgWithFinally {
        requireNonNull(delegate, "delegate");
        requireNonNull(finallyAction, "finallyAction");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Produce a new {@code TryBiArgWithFinally}, preserving the contained
     * {@link #finallyAction()}, and a new {@code delegate} that contains the
     * new argument suppliers.
     *
     * @param arg0Supplier {@inheritDoc}
     * @param arg1Supplier {@inheritDoc}
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public TryBiArg<T, U, R> withArgs(
            CheckedSupplier<T, ? extends Throwable> arg0Supplier,
            CheckedSupplier<U, ? extends Throwable> arg1Supplier
    ) throws NullArgumentException {
        return new TryBiArgWithFinally<>(delegate.withArgs(arg0Supplier, arg1Supplier), finallyAction);
    }

    /**
     * Capture a {@link CheckedRunnable} that will be executed as the
     * <em>finally</em> statement of this {@link Try}.
     *
     * @return A new {@code TryBiArgWithFinally<T, U, R>} that preserves the
     *         contained {@code delegate}, also containing the provided
     *         {@code finallyAction}.
     *
     * @param finallyAction {@inheritDoc}
     *
     * @throws NullArgumentException If {@code finallyAction} is a {@code null}.
     */
    @Override
    public TryBiArgWithFinally<T, U, R> withFinally(
            CheckedRunnable<? extends Throwable> finallyAction
    ) throws NullArgumentException {
        return new TryBiArgWithFinally<>(delegate, finallyAction);
    }

    /**
     * Return the <em>function</em> contained in the {@link #delegate()}.
     */
    @Override
    public CheckedBiFunction<T, U, R, ? extends Throwable> function() {
        return delegate.function();
    }

}