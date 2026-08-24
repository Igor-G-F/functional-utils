package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedSupplier;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * A {@link Try} that requires two arguments to successfully execute. The
 * arguments are supplied by {@code arg0Supplier} and {@code arg1Supplier}
 * when {@link #execute()} is called.
 *
 * @see Try.TryBiArg
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> {@inheritDoc}
 * @param <U> {@inheritDoc}
 * @param <R> {@inheritDoc}
 */
public record TryBiValue<T, U, R>(
        CheckedBiFunction<T, U, R, ? extends Throwable> function,
        CheckedSupplier<T, ? extends Throwable> arg0Supplier,
        CheckedSupplier<U, ? extends Throwable> arg1Supplier
) implements Try.TryBiArg<T, U, R> {

    public TryBiValue {
        requireNonNull(function, "function");
        requireNonNull(arg0Supplier, "arg0Supplier");
        requireNonNull(arg1Supplier, "arg1Supplier");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws Error {@inheritDoc}
     * @throws NullResultException If {@code arg0Supplier},
     *         {@code arg1Supplier}, or {@code function} return a {@code null}.
     */
    @Override
    public Either<Thrown<Throwable>, R> execute() throws Error, NullResultException {
        try {
            var arg = requireNonNullResult(arg0Supplier, "arg0Supplier");
            var arg2 = requireNonNullResult(arg1Supplier, "arg1Supplier");
            var funcResult = requireNonNullResult(function, arg, arg2);
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
     * @param arg0Supplier {@inheritDoc}
     * @param arg1Supplier {@inheritDoc}
     *
     * @return A new {@code TryBiValue<T, U, R>}, preserving the contained
     *         {@link #function()}, but with the new arg suppliers.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public TryBiArg<T, U, R> withArgs(
            CheckedSupplier<T, ? extends Throwable> arg0Supplier,
            CheckedSupplier<U, ? extends Throwable> arg1Supplier
    ) {
        return new TryBiValue<>(function, arg0Supplier, arg1Supplier);
    }
}
