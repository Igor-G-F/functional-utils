package io.github.igorgf.function;

/**
 * This is the checked exception aware analogue of
 * {@link java.util.function.Supplier}. It has the same shape but declares
 * {@code throws X} on {@link #get()}, so a lambda or method reference
 * whose body throws a checked exception can be passed without being wrapped in
 * a {@code try/catch}.
 * <p>
 * There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 * <p>
 * For extensive use case summary see: {@link io.github.igorgf.function}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> Type of results supplied by this supplier.
 * @param <X> The type of exception the function may throw.
 */
@FunctionalInterface
public interface CheckedSupplier<T, X extends Throwable> {

    /**
     * Gets a result.
     *
     * @return The function result.
     *
     * @throws X If the function body throws an exception of type {@code X}.
     */
    T get() throws X;

}
