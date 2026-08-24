package io.github.igorgf.control;

import io.github.igorgf.function.CheckedRunnable;
import io.github.igorgf.function.CheckedSupplier;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * A {@link Try} that requires NO argument to successfully execute.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <R> The result type produced by the function.
 */
public record TryNoArg<R>(
        CheckedSupplier<R, ? extends Throwable> function
) implements Try<R> {

    public TryNoArg {
        requireNonNull(function, "function");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws Error {@inheritDoc}
     * @throws NullResultException If {@code function} returns a {@code null}.
     */
    @Override
    public Either<Thrown<Throwable>, R> execute() throws Error, NullResultException {
        try {
            var funcResult = requireNonNullResult(function, "function");
            return Either.right(funcResult);
        } catch (Error | ContractViolationException x) {
            throw x;
        } catch (Throwable x) {
            return Either.left(Thrown.of(x));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see TryNoArgWithFinally
     *
     * @return A new {@code TryNoArgWithFinally<R>} that wraps {@code this} as a
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
        return new TryNoArgWithFinally<>(this, finallyAction);
    }

}
