package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * A {@link Try} that requires one {@link AutoCloseable} resource to
 * successfully execute. The resource is supplied and opened by the
 * {@code resSupplier} when {@link #execute()} is called.
 * <p>
 * This mirrors the functionality of a try with resources block.
 *
 * @see Try.TryArg
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> {@inheritDoc} Must be {@link AutoCloseable}.
 * @param <R> {@inheritDoc}
 */
public record TryResource<T extends AutoCloseable, R>(
        CheckedFunction<T, R, ? extends Throwable> function,
        CheckedSupplier<T, ? extends Throwable> resSupplier
) implements Try.TryArg<T, R> {

    public TryResource {
        requireNonNull(function, "function");
        requireNonNull(resSupplier, "resSupplier");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This mirrors the functionality of a try with resources block.
     *
     * @return {@inheritDoc}
     *
     * @throws Error {@inheritDoc}
     * @throws NullResultException If {@code resSupplier} or {@code function}
     *         return a {@code null}.
     */
    @Override
    public Either<Thrown, R> execute() throws NullResultException {
        try (
                T res = requireNonNullResult(resSupplier, "resSupplier")
        ) {
            var result = requireNonNullResult(function, res);
            return Either.right(result);
        } catch (Error | ContractViolationException x) {
            throw x;
        } catch (Throwable x) {
            return Either.left(new Thrown(x));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param resSupplier {@inheritDoc}
     *
     * @return A new {@code TryValue<T, R>}, preserving the contained
     *         {@link #function()}, but with the new {@code resSupplier}.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public TryResource<T, R> withArg(
            CheckedSupplier<T, ? extends Throwable> resSupplier
    ) throws NullArgumentException {
        return new TryResource<>(function, resSupplier);
    }
}
