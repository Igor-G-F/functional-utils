package io.github.igorgf.function;

/**
 * This is the checked exception aware analogue of
 * {@link java.util.function.Consumer}. It has the same shape but declares
 * {@code throws X} on {@link #accept(Object)}, so a lambda or method reference
 * whose body throws a checked exception can be passed without being wrapped in
 * a {@code try/catch}.
 * <p>
 * Unlike most other functional interfaces, {@code CheckedConsumer} is expected
 * to operate via side-effects.
 * <p>
 * For extensive use case summary see: {@link io.github.igorgf.function}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> The type of the input to the operation.
 */
@FunctionalInterface
public interface CheckedConsumer<T, X extends Throwable> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t The input argument.
     *
     * @throws X If the function body throws an exception of type {@code X}.
     */
    void accept(T t) throws X;

}