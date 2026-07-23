package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedRunnable;

import java.util.Objects;

/**
 * A deferred, fallible computation that captures its outcome as an
 * {@link Either} of a {@link Thrown} error or a successfully produced result
 * {@code R}. A {@code Try} <em>describes</em> an operation that may throw; it
 * does not run until {@link #execute()} is called.
 * <p>
 * <b>{@code Try} features:</b>
 * <ul>
 *   <li>
 *       Is <b>null safe</b>: a {@code Try} can never be built from a
 *       {@code null} function or {@code null} parameter, and {@link #execute()}
 *       never returns {@code null}. The entire API rejects {@code null} at
 *       every boundary, throwing {@link NullPointerException} on {@code null}.
 *   </li>
 *   <li>
 *       Is <b>lazy</b>: construction only records what to run. No user code is
 *       invoked until {@link #execute()}, allowing a described computation to
 *       be held, passed around, and executed later. A {@code Try} that is never
 *       executed does nothing.
 *   </li>
 *   <li>
 *       <b>Captures failure as a value</b>: any {@link Throwable} thrown during
 *       {@link #execute()} is caught and contained within a {@link Thrown}
 *       rather than propagated, turning control flow into a value,
 *       <em>with the deliberate exception of {@link Error}</em>.
 *   </li>
 * </ul>
 * <p>
 * <b>Fatal errors are not captured:</b><br>
 * {@link #execute()} captures every {@link Throwable} <em>except</em>
 * {@link Error}. An {@link Error} (such as {@link OutOfMemoryError} or
 * {@link StackOverflowError}) signals an unrecoverable condition, so it is
 * rethrown unchanged and propagates out of {@link #execute()} rather than being
 * hidden inside a {@link Thrown}. This is an intentional design choice: a
 * {@code Try} makes ordinary and exceptional outcomes into values, but it does
 * not obscure a dying JVM.
 * <p>
 * <b>Staged construction:</b><br>
 * A {@code Try} is assembled in stages: {@link #of(CheckedFunction)} (or
 * {@link #of(CheckedBiFunction)}) records the fallible function and returns an
 * intermediate step; the step then binds the argument(s) to yield an
 * executable {@code Try}. An optional {@link #withFinally(CheckedRunnable)}
 * wraps the computation with an action that always runs. Finally,
 * {@link #execute()} runs everything and collapses the outcome into an
 * {@link Either}:
 * <pre>{@code
 *     Either<Thrown, Integer> result = Try
 *             .of((String s) -> Integer.parseInt(s))
 *             .withParam("42")
 *             .withFinally(() -> log.info("parse attempted"))
 *             .execute();
 * }</pre>
 * <b>Exception Handling:</b><br>
 * The functions accepted by {@link #of(CheckedFunction)},
 * {@link #of(CheckedBiFunction)}, and {@link #withFinally(CheckedRunnable)} are
 * {@link CheckedFunction}, {@link CheckedBiFunction}, and
 * {@link CheckedRunnable} respectively, whose thrown-type bound is
 * {@link Throwable}, so a described computation may throw anything at all.
 * Unlike other types in this package, {@code Try} does <em>not</em> propagate
 * captured throwable to the caller: they are caught at {@link #execute()} and
 * contained as a {@link Thrown}. Therefore {@link #execute()} never requires a
 * surrounding {@code try/catch}, except for the {@link Error}s it
 * deliberately rethrows.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <R> the result type produced on normal completion
 */
public sealed interface Try<R> permits TryWithResources, TryFunction, TryBiFunction, WithFinally {

    /**
     * Runs the described computation and captures its outcome.
     * <p>
     * When the computation completes normally, the result is a new
     * {@link Right} containing the produced value {@code R}. When the
     * computation throws a {@link Throwable} that is not an {@link Error}, it
     * is caught and wrapped in a {@link Thrown} contained within a new
     * {@link Left}.
     * <p>
     * An {@link Error} is <b>not</b> captured: it is rethrown and propagates
     * out of this method unchanged. See the class documentation for the
     * rationale.
     * <p>
     * This method is the sole point of execution; until it is called no user
     * supplied function or parameter has been touched.
     *
     * @see Either
     * @see Thrown
     *
     * @return A {@code Right<R>} on normal completion. Otherwise, a
     *         {@code Left<Thrown>} capturing the thrown {@link Throwable}.
     *
     * @throws Error If the computation throws an {@link Error}, it is rethrown
     *         rather than captured.
     */
    Either<Thrown, R> execute();

    /**
     * Entry point for describing a fallible single-argument computation.
     * <p>
     * Records the {@code function} to be executed and returns a
     * {@link TryFunctionStep}. The argument must then be supplied via
     * {@link TryFunctionStep#withParam(Object)} to produce an executable
     * {@code Try}. The {@code function} is not invoked here.
     *
     * @see TryFunctionStep
     *
     * @param <T> The input type of the {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The fallible computation to describe.
     *
     * @return A {@code TryFunctionStep<T, R>} awaiting its parameter.
     *
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <T, R> TryFunctionStep<T, R> of(
            CheckedFunction<T, R, Throwable> function
    ) {
        Objects.requireNonNull(function);
        return new TryFunctionStep<>(function);
    }

    /**
     * Entry point for describing a fallible two-argument computation.
     * <p>
     * Records the {@code function} to be executed and returns a
     * {@link TryBiFunctionStep}. The arguments must then be supplied via
     * {@link TryBiFunctionStep#withParams(Object, Object)} to produce an
     * executable {@code Try}. The {@code function} is not invoked here.
     *
     * @see TryBiFunctionStep
     *
     * @param <T> The first input type of the {@code function}.
     * @param <U> The second input type of the {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The fallible computation to describe.
     *
     * @return A {@code TryBiFunctionStep<T, U, R>} awaiting its parameters.
     *
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <T, U, R> TryBiFunctionStep<T, U, R> of(
            CheckedBiFunction<T, U, R, Throwable> function
    ) {
        Objects.requireNonNull(function);
        return new TryBiFunctionStep<>(function);
    }

    /**
     * Wraps {@code this} with an {@code action} that always runs after the
     * described computation completes, analogous to a {@code finally} block.
     * <p>
     * On {@link #execute()} the underlying computation runs first. If it
     * propagates an {@link Error}, that {@link Error} escapes immediately and
     * the {@code action} does <b>not</b> run. Otherwise, the {@code action}
     * runs, and the result depends on whether the {@code action} itself throws:
     * <ul>
     *   <li>
     *       If the {@code action} completes normally, the underlying outcome is
     *       returned unchanged.
     *   </li>
     *   <li>
     *       If the {@code action} throws an {@link Error}, that {@link Error} is
     *       rethrown and propagates out of {@link #execute()}.
     *   </li>
     *   <li>
     *       If the {@code action} throws a non-{@link Error} {@link Throwable}
     *       and the underlying computation had <b>succeeded</b>, the success is
     *       discarded and the {@code action}'s throwable is captured as a new
     *       {@link Left} of {@link Thrown}, so a failing finalizer never
     *       masquerades as success.
     *   </li>
     *   <li>
     *       If the {@code action} throws a non-{@link Error} {@link Throwable}
     *       and the underlying computation had <b>already failed</b>, the
     *       original {@link Thrown} is preserved and the {@code action}'s
     *       throwable is attached to it via
     *       {@link Throwable#addSuppressed(Throwable)}.
     *   </li>
     * </ul>
     * <p>
     * This preserves the original failure as the primary error rather than
     * letting a finalizer throwable mask it, mirroring the suppression
     * semantics of a <em>try with resources</em> statement.
     *
     * @param action The finalizing action to always run after the computation.
     *
     * @return A new {@code Try<R>} that runs {@code this} and then the
     *         {@code action}.
     *
     * @throws NullPointerException If {@code action} is {@code null}.
     */
    default Try<R> withFinally(
            CheckedRunnable<Throwable> action
    ) {
        Objects.requireNonNull(action);
        return new WithFinally<>(this, action);
    }

    /**
     * An intermediate stage produced by {@link Try#of(CheckedFunction)} that
     * holds a described single argument {@code function} awaiting its argument.
     *
     * @see Try#of(CheckedFunction)
     *
     * @param <T> The input type of the {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The described fallible computation.
     */
    record TryFunctionStep<T, R>(
            CheckedFunction<T, R, Throwable> function
    ) {
        /**
         * Binds the {@code param} to the described function, producing an
         * executable {@code Try}.
         *
         * @param param The argument to apply the function to on execution.
         *
         * @return A new executable {@code Try<R>}.
         *
         * @throws NullPointerException If {@code param} is {@code null}.
         */
        public Try<R> withParam(T param) {
            Objects.requireNonNull(param);
            return new TryFunction<>(function, param);
        }
    }

    /**
     * An intermediate stage produced by {@link Try#of(CheckedBiFunction)} that
     * holds a described two argument {@code function} awaiting its arguments.
     *
     * @see Try#of(CheckedBiFunction)
     *
     * @param <T> The first input type of the {@code function}.
     * @param <U> The second input type of the {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The described fallible computation.
     */
    record TryBiFunctionStep<T, U, R>(
            CheckedBiFunction<T, U, R, Throwable> function
    ) {

        /**
         * Binds {@code param} and {@code param2} to the described function,
         * producing an executable {@code Try}.
         *
         * @param param The first argument to apply on execution.
         * @param param2 The second argument to apply on execution.
         *
         * @return A new executable {@code Try<R>}.
         *
         * @throws NullPointerException If {@code param} or {@code param2} is
         *         {@code null}.
         */
        public Try<R> withParams(T param, U param2) {
            Objects.requireNonNull(param);
            Objects.requireNonNull(param2);
            return new TryBiFunction<>(function, param, param2);
        }
    }

}


/**
 * A {@link Try} describing the application of a single-argument
 * {@link CheckedFunction} to a bound {@code param}.
 *
 * @param <T> The input type of the function.
 * @param <R> The result type produced by the function.
 * @param function The fallible computation to run on execution.
 * @param param The argument applied to the function on execution.
 */
record TryFunction<T, R>(
        CheckedFunction<T, R, Throwable> function,
        T param
) implements Try<R> {
    @Override
    public Either<Thrown, R> execute() {
        try {
            return Either.right(function.apply(param));
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            return Either.left(new Thrown(e));
        }
    }
}

/**
 * A {@link Try} describing the application of a two-argument
 * {@link CheckedBiFunction} to bound {@code param} and {@code param2}.
 *
 * @param <T> The first input type of the function.
 * @param <U> The second input type of the function.
 * @param <R> The result type produced by the function.
 * @param function The fallible computation to run on execution.
 * @param param The first argument applied to the function on execution.
 * @param param2 The second argument applied to the function on execution.
 */
record TryBiFunction<T, U, R>(
        CheckedBiFunction<T, U, R, Throwable> function,
        T param,
        U param2
) implements Try<R> {
    @Override
    public Either<Thrown, R> execute() {
        try {
            return Either.right(function.apply(param, param2));
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            return Either.left(new Thrown(e));
        }
    }
}

/**
 * A {@link Try} that decorates a {@code delegate} with an {@code action} that
 * always runs after it (unless the {@code delegate} propagates an
 * {@link Error}), applying the finalization semantics documented on
 * {@link Try#withFinally(CheckedRunnable)}.
 *
 * @param <R> The result type produced by the delegate.
 * @param delegate The underlying computation to run first.
 * @param action The finalizing action to always run afterward.
 */
record WithFinally<R>(
        Try<R> delegate,
        CheckedRunnable<Throwable> action
) implements Try<R> {
    @Override
    public Either<Thrown, R> execute() {
        // If an Error from the delegate propagates here; the action is skipped.
        Either<Thrown, R> result = delegate.execute();
        try {
            action.run();
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            return switch (result) {
                case Right<Thrown, R>(_) -> Either.left(new Thrown(e));
                case Left<Thrown, R>(var thrown) -> {
                    thrown.get().addSuppressed(e);
                    yield result;
                }
            };
        }
        return result;
    }
}