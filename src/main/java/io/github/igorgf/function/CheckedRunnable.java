package io.github.igorgf.function;

/**
 * This is the checked exception aware analogue of {@link Runnable}. It has the
 * same shape but declares {@code throws X} on {@link #run()}, so a lambda or
 * method reference whose body throws a checked exception can be passed without
 * being wrapped in a {@code try/catch}.
 * <p>
 * Represents an operation that does not return a result.
 * <p>
 * For extensive use case summary see: {@link io.github.igorgf.function}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <X> The type of exception the function may throw.
 */
@FunctionalInterface
public interface CheckedRunnable<X extends Throwable> {

    /**
     * Runs this operation.
     *
     * @throws X If the function body throws an exception of type {@code X}.
     */
    void run() throws X;

}