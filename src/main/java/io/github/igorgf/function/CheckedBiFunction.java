package io.github.igorgf.function;

/**
 * This is the checked exception aware analogue of
 * {@link java.util.function.BiFunction}, and a two-arity specialization of
 * {@link CheckedFunction}. It has the same shape but declares {@code throws X}
 * on {@link #apply(Object, Object)}, so a lambda or method reference whose body
 * throws a checked exception can be passed without being wrapped in a
 * {@code try/catch}.
 * <p>
 * For usage examples and extensive summary see:
 * {@link io.github.igorgf.function}.
 *
 * @see CheckedFunction
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> The type of the first input to the function.
 * @param <U> The type of the second input to the function.
 * @param <R> The type of the result of the function.
 * @param <X> The type of exception the function may throw.
 */
@FunctionalInterface
public interface CheckedBiFunction<T, U, R, X extends Throwable> {

    /**
     * Applies this function to the given two arguments, producing a result or
     * throwing the declared exception {@code X}.
     *
     * @param t The first function argument.
     * @param u The second function argument.
     *
     * @return The function result.
     *
     * @throws X If the function body throws an exception of type {@code X}.
     */
    R apply(T t, U u) throws X;

}
