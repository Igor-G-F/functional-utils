package io.github.igorgf.function;

/**
 * This is the checked exception aware analogue of
 * {@link java.util.function.Function}. It has the same shape but declares
 * {@code throws X} on {@link #apply(Object)}, so a lambda or method reference
 * whose body throws a checked exception can be passed without being wrapped in
 * a {@code try/catch}.
 * <p>
 * For extensive use case summary see: {@link io.github.igorgf.function}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> The type of the input to the function.
 * @param <R> The type of the result of the function.
 * @param <X> The type of exception the function may throw.
 */
@FunctionalInterface
public interface CheckedFunction<T, R, X extends Throwable> {

    /**
     * Applies this function to the given argument, producing a result or
     * throwing the declared exception {@code X}.
     *
     * @param t The function argument.
     *
     * @return The function result.
     *
     * @throws X If the function body throws an exception of type {@code X}.
     */
    R apply(T t) throws X;

    /**
     * Returns a checked function that always returns its input argument.
     * <p>
     * This is the checked exception aware analogue of
     * {@link java.util.function.Function#identity()}.
     *
     * @param <T> The type of the input and result.
     * @param <X> The declared (but never thrown) exception type.
     *
     * @return A checked function that returns its input unchanged.
     */
    static <T, X extends Throwable> CheckedFunction<T, T, X> identity() {
        return t -> t;
    }

}
