package io.github.igorgf.function;

/**
 * This is the checked exception aware analogue of
 * {@link java.util.function.BiConsumer}, and a two-arity specialization of
 * {@link CheckedConsumer}. It has the same shape but declares {@code throws X}
 * on {@link #accept(Object, Object)}, so a lambda or method reference whose
 * body throws a checked exception can be passed without being wrapped in a
 * {@code try/catch}.
 * <p>
 * Unlike most other functional interfaces, {@code CheckedBiConsumer} is
 * expected to operate via side-effects.
 * <p>
 * For extensive use case summary see: {@link io.github.igorgf.function}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> The type of the first input to the consumer.
 * @param <U> The type of the second input to the consumer.
 */
@FunctionalInterface
public interface CheckedBiConsumer<T, U, X extends Throwable> {

    /**
     * Performs this operation on the given two arguments.
     *
     * @param t The first input argument.
     * @param u The second input argument.
     *
     * @throws X If the function body throws an exception of type {@code X}.
     */
    void accept(T t, U u) throws X;

}