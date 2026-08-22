package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedSupplier;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * A {@link Try} that requires two {@link AutoCloseable} resources to
 * successfully execute. The resources are supplied and opened by the
 * {@code res0Supplier} and {@code res1Supplier} when {@link #execute()} is
 * called.
 * <p>
 * This mirrors the functionality of a try with resources block.
 *
 * @see Try.TryBiArg
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> {@inheritDoc} Must be {@link AutoCloseable}.
 * @param <U> {@inheritDoc} Must be {@link AutoCloseable}.
 * @param <R> {@inheritDoc}
 */
public record TryBiResource<T extends AutoCloseable, U extends AutoCloseable, R>(
        CheckedBiFunction<T, U, R, ? extends Throwable> function,
        CheckedSupplier<T, ? extends Throwable> res0Supplier,
        CheckedSupplier<U, ? extends Throwable> res1Supplier
) implements Try.TryBiArg<T, U, R> {

    public TryBiResource {
        requireNonNull(function, "function");
        requireNonNull(res0Supplier, "res0Supplier");
        requireNonNull(res1Supplier, "res1Supplier");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This mirrors the functionality of a try with resources block.
     *
     * @return {@inheritDoc}
     *
     * @throws Error {@inheritDoc}
     * @throws NullResultException If {@code res0Supplier},
     *         {@code res1Supplier}, or {@code function} return a {@code null}.
     */
    @Override
    public Either<Thrown, R> execute() throws NullResultException, Error {
        try (
                T res0 = requireNonNullResult(res0Supplier, "res0Supplier");
                U res1 = requireNonNullResult(res1Supplier, "res1Supplier")
        ) {
            var funcResult = requireNonNullResult(function, res0, res1);
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
     * @param res0Supplier {@inheritDoc} Must be {@link AutoCloseable}.
     * @param res1Supplier {@inheritDoc} Must be {@link AutoCloseable}.
     *
     * @return A new {@code TryBiValue<T, U, R>}, preserving the contained
     *         {@link #function()}, but with the new resource suppliers.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public TryBiArg<T, U, R> withArgs(
            CheckedSupplier<T, ? extends Throwable> res0Supplier,
            CheckedSupplier<U, ? extends Throwable> res1Supplier
    ) {
        return new TryBiResource<>(function, res0Supplier, res1Supplier);
    }
}
