package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * A {@link Try} that requires one argument to successfully execute. The
 * argument is supplied by the {@code argSupplier} when {@link #execute()} is
 * called.
 *
 * @see Try.TryArg
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> {@inheritDoc}
 * @param <R> {@inheritDoc}
 */
public record TryValue<T, R>(
        CheckedFunction<T, R, ? extends Throwable> function,
        CheckedSupplier<T, ? extends Throwable> argSupplier
) implements Try.TryArg<T, R> {

    public TryValue {
        requireNonNull(function, "function");
        requireNonNull(argSupplier, "argSupplier");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws Error {@inheritDoc}
     * @throws NullResultException If {@code argSupplier} or {@code function}
     *         return a {@code null}.
     */
    @Override
    public Either<Thrown, R> execute() throws Error, NullResultException {
        try {
            var arg = requireNonNullResult(argSupplier, "argSupplier");
            var funcResult = requireNonNullResult(function, arg);
            return Either.right(funcResult);
        } catch (Error | ContractViolationException x) {
            throw x;
        } catch (Throwable x) {
            return Either.left(new Thrown(x));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param argSupplier {@inheritDoc}
     *
     * @return A new {@code TryValue<T, R>}, preserving the contained
     *         {@link #function()}, but with the new {@code argSupplier}.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public TryValue<T, R> withArg(
            CheckedSupplier<T, ? extends Throwable> argSupplier
    ) throws NullArgumentException {
        return new TryValue<>(function, argSupplier);
    }

}
