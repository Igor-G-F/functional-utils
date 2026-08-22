package io.github.igorgf.control;

import io.github.igorgf.function.*;

import java.util.function.BiFunction;

import static io.github.igorgf.control.ControlUtils.requireNonNull;

/**
 * A deferred, fallible computation that captures its outcome as an
 * {@link Either} of a {@link Thrown} throwable or a successfully produced
 * result {@code R}. A {@code Try} <em>describes</em> an operation that may
 * throw; it does not run until {@link #execute()} is called.
 * <p>
 * <b>{@code Try} features:</b>
 * <ul>
 *   <li>
 *       Is <b>null safe</b>: a {@code Try} can never be built from a
 *       {@code null} function or {@code null} parameter; and {@link #execute()}
 *       never returns and never contains a {@code null}. The entire API rejects
 *       {@code null} at every boundary, throwing some
 *       {@link ContractViolationException} on {@code null} arguments or results.
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
 *       rather than propagated, turning control flow into a value. <em>Fatal
 *       errors are not captured</em>, {@link #execute()} does not capture
 *       {@link Error} or {@link ContractViolationException}.
 *   </li>
 * </ul>
 * <p>
 * <b>Staged construction:</b><br>
 * A {@code Try} is assembled in stages: {@link #of(CheckedFunction)} etc.
 * records the fallible function and returns an intermediate step; the step then
 * binds the argument(s) to yield an executable {@code Try}. An optional
 * {@link #withFinally(CheckedRunnable)} wraps the computation with an action
 * that always runs. Finally, {@link #execute()} runs everything and collapses
 * the outcome into a {@link Either}:
 * <pre>{@code
 *     Either<Thrown, Integer> result = Try
 *             .of((String s) -> Integer.parseInt(s))
 *             .withArg("42")
 *             .withFinally(() -> log.info("parse attempted"))
 *             .execute();
 * }</pre>
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <R> The result type produced on normal completion.
 */
public sealed interface Try<R> permits Try.TryArg, Try.TryBiArg, TryNoArg, Try.TryWithFinally {

    /**
     * Runs the contained computation and captures its outcome.
     * <p>
     * When the computation completes normally, the result is a new
     * {@link Right} containing the produced value {@code R}. When the
     * computation throws a {@link Error} or {@link ContractViolationException},
     * they get rethrown. When the computation throws any other
     * {@link Throwable}, it is caught and wrapped in a {@link Thrown} contained
     * within a new {@link Left}.
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
     * @throws ContractViolationException If the computation throws some
     *         {@link ContractViolationException}, it is rethrown rather than
     *         captured.
     */
    Either<Thrown, R> execute() throws Error, ContractViolationException;

    /**
     * Produces a {@link TryWithFinally} that wraps {@code this} as a
     * {@code delegate}, also containing the provided {@code finallyAction}.
     * <p>
     * The produced {@link TryWithFinally} mirrors the
     * {@code try {} catch(Throwable x) {} finally {}} semantics, executing the
     * {@code finallyAction} after the {@code delegate} {@link #execute()} has
     * been attempted.
     *
     * @see TryWithFinally
     *
     * @param finallyAction The finalising action to always run after the
     *        {@code delegate} {@link #execute()}.
     */
    TryWithFinally<R> withFinally(
            CheckedRunnable<? extends Throwable> finallyAction
    );

    /**
     * Entry point for describing a fallible single-argument computation.
     * <p>
     * Binds the {@code function} to be executed and returns a
     * {@link ArgFuncStep}, an intermediate container that can be used to declare
     * the argument that the {@code function} will be supplied, or to declare
     * finally logic.
     *
     * @see ArgFuncStep
     * @see ArgFuncStep#withArg(CheckedSupplier)
     * @see ArgFuncStep#withFinally(CheckedRunnable)
     *
     * @param <T> The input type of the {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The fallible computation to describe.
     *
     * @return A {@code ArgFuncStep<T, R>} awaiting its arguments.
     *
     * @throws NullArgumentException If {@code function} is {@code null}.
     */
    static <T, R> ArgStep<T, R> of(
            CheckedFunction<T, R, ? extends Throwable> function
    ) throws NullArgumentException {
        return new ArgFuncStep<>(function, TryValue::new);
    }

    /**
     * Entry point for describing a fallible two-argument computation.
     * <p>
     * Binds the {@code function} to be executed and returns a
     * {@link BiArgStep}, an intermediate container that can be used to declare
     * the arguments that the {@code function} will be supplied, or to declare
     * finally logic.
     *
     * @see BiArgFuncStep
     * @see BiArgFuncStep#withArgs(CheckedSupplier, CheckedSupplier)
     * @see BiArgFuncStep#withFinally(CheckedRunnable)
     *
     * @param <T> The first argument type of the {@code function}.
     * @param <U> The second argument type of the {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The fallible computation to describe.
     *
     * @return A {@code BiArgFuncStep<T, U, R>} awaiting its arguments.
     *
     * @throws NullArgumentException If {@code function} is {@code null}.
     */
    static <T, U, R> BiArgStep<T, U, R> of(
            CheckedBiFunction<T, U, R, ? extends Throwable> function
    ) throws NullArgumentException {
        return new BiArgFuncStep<>(function, TryBiValue::new);
    }

    /**
     * Entry point for describing a fallible computation over a single
     * {@link AutoCloseable} resource.
     * <p>
     * Binds the {@code function} to be executed and returns a
     * {@link ArgFuncStep}, an intermediate container that can be used to declare
     * the resource that the {@code function} will be supplied, or to declare
     * finally logic.
     * <p>
     * This entry leads down to a {@link TryResource} implementation of
     * {@code Try}, which mirrors the functionality of a try with resources
     * block.
     *
     * @see TryResource
     * @see ArgFuncStep
     * @see ArgFuncStep#withArg(CheckedSupplier)
     * @see ArgFuncStep#withFinally(CheckedRunnable)
     *
     * @param <T> The {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The fallible computation to describe.
     *
     * @return A {@code ArgFuncStep<T, R>} awaiting its arguments.
     *
     * @throws NullArgumentException If {@code function} is {@code null}.
     */
    static <T extends AutoCloseable, R> ArgStep<T, R> ofResource(
            CheckedFunction<T, R, ? extends Throwable> function
    ) throws NullArgumentException {
        return new ArgFuncStep<>(function, TryResource::new);
    }

    /**
     * Entry point for describing a fallible computation over two
     * {@link AutoCloseable} resources.
     * <p>
     * Binds the {@code function} to be executed and returns a
     * {@link BiArgFuncStep}, an intermediate container that can be used to declare
     * the resources that the {@code function} will be supplied, or to declare
     * finally logic.
     * <p>
     * This entry leads down to a {@link TryBiResource} implementation of
     * {@code Try}, which mirrors the functionality of a try with resources
     * statement.
     *
     * @see TryBiResource
     * @see BiArgFuncStep
     * @see BiArgFuncStep#withArgs(CheckedSupplier, CheckedSupplier)
     * @see BiArgFuncStep#withFinally(CheckedRunnable)
     *
     * @param <T> The first {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <U> The second {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param function The fallible computation to describe.
     *
     * @return A {@code ArgFuncStep<T, R>} awaiting its arguments.
     *
     * @throws NullArgumentException If {@code function} is {@code null}.
     */
    static <T extends AutoCloseable, U extends AutoCloseable, R> BiArgStep<T, U, R> ofResources(
            CheckedBiFunction<T, U, R, ? extends Throwable> function
    ) throws NullArgumentException {
        return new BiArgFuncStep<>(function, TryBiResource::new);
    }

    /**
     * Entry point for describing a fallible single-argument consuming
     * computation.
     * <p>
     * Binds the {@code consumer} to be executed and returns a
     * {@link ArgFuncStep}, an intermediate container that can be used to declare
     * the argument that the {@code consumer} will be supplied, or to declare
     * finally logic.
     * <p>
     * While a {@link CheckedConsumer} typically does not produce a result. To
     * enable reasoning about the outcome of the computation, the {@link Try}
     * resulting from this path will return a {@link Right} of {@link Unit} on
     * success of its {@link #execute()}.
     *
     * @see ArgFuncStep
     * @see ArgFuncStep#withArg(CheckedSupplier)
     * @see ArgFuncStep#withFinally(CheckedRunnable)
     *
     * @param <T> The input type of the {@code consumer}.
     * @param consumer The fallible computation to describe.
     *
     * @return A {@code ArgFuncStep<T, Unit>} awaiting its argument.
     *
     * @throws NullArgumentException If {@code consumer} is {@code null}.
     */
    static <T> ArgStep<T, Unit> consume(
            CheckedConsumer<T, ? extends Throwable> consumer
    ) throws NullArgumentException {
        requireNonNull(consumer, "consumer");
        return new ArgFuncStep<>(t -> {
            consumer.accept(t);
            return Unit.INSTANCE;
        }, TryValue::new);
    }

    /**
     * Entry point for describing a fallible two-argument consuming
     * computation.
     * <p>
     * Binds the {@code consumer} to be executed and returns a
     * {@link BiArgFuncStep}, an intermediate container that can be used to declare
     * the arguments that the {@code consumer} will be supplied, or to declare
     * finally logic.
     * <p>
     * While a {@link CheckedBiConsumer} typically does not produce a result. To
     * enable reasoning about the outcome of the computation, the {@link Try}
     * resulting from this path will return a {@link Right} of {@link Unit} on
     * success of its {@link #execute()}.
     *
     * @see BiArgFuncStep
     * @see BiArgFuncStep#withArgs(CheckedSupplier, CheckedSupplier)
     * @see BiArgFuncStep#withFinally(CheckedRunnable)
     *
     * @param <T> The first argument type of the {@code function}.
     * @param <U> The second argument type of the {@code function}.
     * @param consumer The fallible computation to describe.
     *
     * @return A {@code BiArgFuncStep<T, U, Unit>} awaiting its arguments.
     *
     * @throws NullArgumentException If {@code consumer} is {@code null}.
     */
    static <T, U> BiArgStep<T, U, Unit> consume(
            CheckedBiConsumer<T, U, ? extends Throwable> consumer
    ) {
        requireNonNull(consumer, "consumer");
        return new BiArgFuncStep<>((t, u) -> {
            consumer.accept(t, u);
            return Unit.INSTANCE;
        }, TryBiValue::new);
    }

    /**
     * Entry point for describing a fallible consuming computation over a single
     * {@link AutoCloseable} resource.
     * <p>
     * Binds the {@code consumer} to be executed and returns a
     * {@link ArgFuncStep}, an intermediate container that can be used to declare
     * the argument that the {@code consumer} will be supplied, or to declare
     * finally logic.
     * <p>
     * This entry leads down to a {@link TryResource} implementation of
     * {@code Try}, which mirrors the functionality of a try with resources
     * statement.
     * <p>
     * While a {@link CheckedConsumer} typically does not produce a result. To
     * enable reasoning about the outcome of the computation, the {@link Try}
     * resulting from this path will return a {@link Right} of {@link Unit} on
     * success of its {@link #execute()}.
     *
     * @see TryResource
     * @see ArgFuncStep
     * @see ArgFuncStep#withArg(CheckedSupplier)
     * @see ArgFuncStep#withFinally(CheckedRunnable)
     *
     * @param <T> The {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param consumer The fallible computation to describe.
     *
     * @return A {@code ArgFuncStep<T, Unit>} awaiting its argument.
     *
     * @throws NullArgumentException If {@code consumer} is {@code null}.
     */
    static <T extends AutoCloseable> ArgStep<T, Unit> consumeResource(
            CheckedConsumer<T, ? extends Throwable> consumer
    ) {
        requireNonNull(consumer, "consumer");
        return new ArgFuncStep<>(t -> {
            consumer.accept(t);
            return Unit.INSTANCE;
        }, TryResource::new);
    }

    /**
     * Entry point for describing a fallible consuming computation over two
     * {@link AutoCloseable} resources.
     * <p>
     * Binds the {@code consumer} to be executed and returns a
     * {@link BiArgFuncStep}, an intermediate container that can be used to declare
     * the arguments that the {@code consumer} will be supplied, or to declare
     * finally logic.
     * <p>
     * This entry leads down to a {@link TryBiResource} implementation of
     * {@code Try}, which mirrors the functionality of a try with resources
     * statement.
     * <p>
     * While a {@link CheckedBiConsumer} typically does not produce a result. To
     * enable reasoning about the outcome of the computation, the {@link Try}
     * resulting from this path will return a {@link Right} of {@link Unit} on
     * success of its {@link #execute()}.
     *
     * @see TryBiResource
     * @see BiArgFuncStep
     * @see BiArgFuncStep#withArgs(CheckedSupplier, CheckedSupplier)
     * @see BiArgFuncStep#withFinally(CheckedRunnable)
     *
     * @param <T> The first {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <U> The second {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param consumer The fallible computation to describe.
     *
     * @return A {@code BiArgFuncStep<T, U, Unit>} awaiting its arguments.
     *
     * @throws NullArgumentException If {@code consumer} is {@code null}.
     */
    static <T extends AutoCloseable, U extends AutoCloseable> BiArgStep<T, U, Unit> consumeResources(
            CheckedBiConsumer<T, U, ? extends Throwable> consumer
    ) {
        requireNonNull(consumer, "consumer");
        return new BiArgFuncStep<>((t, u) -> {
            consumer.accept(t, u);
            return Unit.INSTANCE;
        }, TryBiResource::new);
    }

    /**
     * Entry point for describing a fallible computation that supplies a value.
     * <p>
     * Binds the {@code supplier} to be executed and returns a {@link TryNoArg}.
     *
     * @see TryNoArg
     * @see TryNoArg#withFinally(CheckedRunnable)
     *
     * @param <R> The result type produced by the {@code supplier}.
     * @param supplier The fallible computation to describe.
     *
     * @return A {@code TryNoArg<R>} awaiting execution.
     *
     * @throws NullArgumentException If {@code supplier} is {@code null}.
     */
    static <R> TryNoArg<R> supply(
            CheckedSupplier<R, ? extends Throwable> supplier
    ) {
        requireNonNull(supplier, "supplier");
        return new TryNoArg<>(supplier);
    }

    /**
     * Entry point for describing a fallible computation.
     * <p>
     * Binds the {@code runnable} to be executed and returns a {@link TryNoArg}.
     * <p>
     * While a {@link CheckedRunnable} typically does not produce a result. To
     * enable reasoning about the outcome of the computation, the {@link TryNoArg}
     * resulting from this path will return a {@link Right} of {@link Unit} on
     * success of its {@link #execute()}.
     *
     * @see TryNoArg
     * @see TryNoArg#withFinally(CheckedRunnable)
     *
     * @param runnable The fallible computation to describe.
     *
     * @return A {@code TryNoArg<Unit>} awaiting execution.
     *
     * @throws NullArgumentException If {@code runnable} is {@code null}.
     */
    static TryNoArg<Unit> run(
            CheckedRunnable<? extends Throwable> runnable
    ) {
        requireNonNull(runnable, "runnable");
        return new TryNoArg<>(() -> {
            runnable.run();
            return Unit.INSTANCE;
        });
    }

    /**
     * A {@link Try} that decorates a {@link #delegate()} with an
     * {@link #finallyAction()} that always runs after it (unless the
     * {@code delegate} propagates an {@link Error}), applying the finalisation
     * semantics documented on {@link TryWithFinally#execute()}.
     *
     * @see TryWithFinally#execute()
     * @see Try#withFinally(CheckedRunnable)
     *
     * @param <R> The result type produced by the delegate.
     */
    sealed interface TryWithFinally<R> extends Try<R> permits TryNoArgWithFinally, TryArgWithFinally, TryBiArgWithFinally {

        /**
         * Return the {@link Try} being decorated by this {@code TryWithFinally}.
         */
        Try<R> delegate();

        /**
         * Return the runnable to be executed as the <em>finally</em> action.
         */
        CheckedRunnable<? extends Throwable> finallyAction();

        /**
         * Runs the {@link Try#execute()} of the {@link #delegate()}, and then
         * applies the {@link #finallyAction()}, mirroring the
         * {@code try {} catch(Throwable x) {} finally {}} semantics.
         * <p>
         * <b>Error immediately terminates the computation:</b><br>
         * If some {@link Error} is thrown when executing the
         * {@link #delegate()}, the {@link #finallyAction()} is ignored and the
         * error is immediately rethrown. When some {@link Error} is thrown
         * during execution of the {@link #finallyAction()}, any previous
         * results get discarded, exceptions are suppressed, and the error is
         * immediately rethrown.
         * <p>
         * <b>ContractViolationException is always thrown:</b><br>
         * If some {@link ContractViolationException} is thrown when executing
         * the {@link #delegate()}, the {@link #finallyAction()} still gets
         * executed, but afterwards the {@link ContractViolationException} is
         * rethrown.
         * <p>
         * <b>Multiple Throwables get suppressed down the chain:</b><br>
         * Unless stated otherwise, between the {@link #delegate()} and
         * {@link #finallyAction()} executions, should multiple
         * {@link Throwable}s be thrown during the process, they will get
         * recorded as suppressed within any subsequent throwables. So multiple
         * exceptions are always captured and reported.
         *
         * @see Try#execute()
         *
         * @return A {@code Right<R>} on normal completion. Otherwise, a
         *         {@code Left<Thrown>} capturing the thrown {@link Throwable}.
         * @throws ContractViolationException If the delegate execution throws
         *         some {@link ContractViolationException}, it is rethrown
         *         rather than captured.
         */
        @Override
        default Either<Thrown, R> execute() throws ContractViolationException {
            Either<Thrown, R> result;
            try {
                result = delegate().execute();
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                result = Either.left(new Thrown(e));
            }

            return switch (result) {
                case Right<Thrown, R>(var funcResult) -> {
                    try {
                        finallyAction().run();
                    } catch (Error xFinally) {
                        throw xFinally;
                    } catch (Throwable xFinally) {
                        yield Either.left(new Thrown(xFinally));
                    }

                    yield Either.right(funcResult);
                }
                case Left<Thrown, R>(Thrown(ContractViolationException xFunc)) -> {
                    try {
                        finallyAction().run();
                    } catch (Error xFinally) {
                        xFinally.addSuppressed(xFunc);
                        throw xFinally;
                    } catch (Throwable xFinally) {
                        xFunc.addSuppressed(xFinally);
                        throw xFunc;
                    }

                    throw xFunc;
                }
                case Left<Thrown, R>(Thrown(var xFunc)) -> {
                    try {
                        finallyAction().run();
                    } catch (Error xFinally) {
                        xFinally.addSuppressed(xFunc);
                        throw xFinally;
                    } catch (Throwable xFinally) {
                        xFinally.addSuppressed(xFunc);
                        yield Either.left(new Thrown(xFinally));
                    }

                    yield Either.left(new Thrown(xFunc));
                }
            };
        }
    }

    /**
     * A {@link Try} that requires a single argument {@code T} to successfully
     * execute.
     *
     * @param <T> The type of the argument.
     * @param <R> The result type produced by the function.
     */
    sealed interface TryArg<T, R> extends Try<R> permits TryArgWithFinally, TryValue, TryResource {

        /**
         * Capture the argument to be supplied to the <em>function</em> executed
         * by this {@link Try}. The {@code arg} will be wrapped in a
         * {@link CheckedSupplier}.
         *
         * @param arg The argument that will be supplied to the
         *            <em>function</em> executed by this {@link Try}.
         *
         * @return Some new {@code TryArg<T, R>}, dependent on implementation.
         *
         * @throws NullArgumentException When {@code arg} is a {@code null}.
         */
        default TryArg<T, R> withArg(T arg) throws NullArgumentException {
            requireNonNull(arg, "arg");
            return withArg(() -> arg);
        }

        /**
         * Capture the {@link CheckedSupplier} that will supply the argument to
         * the <em>function</em> executed by this {@link Try}.
         *
         * @param argSupplier The {@link CheckedSupplier} that will supply the
         *        argument to the <em>function</em> executed by this {@link Try}.
         *
         * @return Some new {@code TryArg<T, R>}, dependent on implementation.
         *
         * @throws NullArgumentException When {@code argSupplier} is a
         *         {@code null}.
         */
        TryArg<T, R> withArg(
                CheckedSupplier<T, ? extends Throwable> argSupplier
        ) throws NullArgumentException;

        /**
         * Capture a {@link CheckedRunnable} that will be executed as the
         * <em>finally</em> statement of this {@link Try}.
         *
         * @param finallyAction The {@link CheckedRunnable} that will be
         *        executed as the <em>finally</em> statement of this {@link Try}.
         *
         * @return New {@code TryArgWithFinally<T, R>}, containing {@code this}
         *         as the <em>delegate</em>, and the {@code finallyAction} to be
         *         executed after the <em>delegate.execute()</em> completes.
         *
         * @throws NullArgumentException When {@code finallyAction} is a
         *         {@code null}.
         */
        default TryArgWithFinally<T, R> withFinally(
                CheckedRunnable<? extends Throwable> finallyAction
        ) throws NullArgumentException {
            return new TryArgWithFinally<>(this, finallyAction);
        }

        /**
         * Return the <em>function</em> being contained by this {@code Try}.
         */
        CheckedFunction<T, R, ? extends Throwable> function();
    }

    /**
     * A builder step for constructing a {@link TryArg}. Represents the step
     * where a {@link CheckedFunction} has been captured to be executed, but
     * the argument passed to the <em>function</em> has not yet been captured.
     *
     * @param <T> The type of the argument.
     * @param <R> The result type produced by the function.
     */
    sealed interface ArgStep<T, R> permits ArgFuncStep, ArgFinallyStep {

        /**
         * Capture the argument to be supplied to the <em>function</em> executed
         * by the {@link Try} being constructed. The {@code arg} will be wrapped
         * in a {@link CheckedSupplier}.
         *
         * @see TryArg
         *
         * @param arg The argument that will be supplied to the
         *            <em>function</em> executed by the {@link Try} under
         *            construction.
         *
         * @return Some new {@code TryArg<T, R>}, dependent on implementation.
         *
         * @throws NullArgumentException When {@code arg} is a {@code null}.
         */
        default TryArg<T, R> withArg(T arg) throws NullArgumentException {
            requireNonNull(arg, "arg");
            return withArg(() -> arg);
        }

        /**
         * Capture the {@link CheckedSupplier} that will supply the argument to
         * the <em>function</em> executed by the {@link Try} being constructed.
         *
         * @param argSupplier The {@link CheckedSupplier} that will supply the
         *        argument to the <em>function</em> executed by the {@link Try}
         *        under constructed.
         *
         * @return Some new {@code TryArg<T, R>}, dependent on implementation.
         *
         * @throws NullArgumentException When {@code argSupplier} is a
         *         {@code null}.
         */
        TryArg<T, R> withArg(
                CheckedSupplier<T, ? extends Throwable> argSupplier
        ) throws NullArgumentException;

        /**
         * Capture a {@link CheckedRunnable} that will be executed as the
         * <em>finally</em> statement of the {@link Try} being constructed.
         *
         * @param finallyAction The {@link CheckedRunnable} that will be
         *        executed as the <em>finally</em> statement of the {@link Try}
         *        under construction.
         *
         * @return New {@code ArgFinallyStep<T, R>}, containing {@code this}
         *         as the <em>delegate</em>, and the {@code finallyAction} to be
         *         passed to the {@link TryArgWithFinally} that results at the
         *         end of this builder chain.
         *
         * @throws NullArgumentException When {@code finallyAction} is a
         *         {@code null}.
         */
        default ArgStep<T, R> withFinally(
                CheckedRunnable<? extends Throwable> finallyAction
        ) throws NullArgumentException {
            return new ArgFinallyStep<>(this, finallyAction);
        }

        /**
         * Return the <em>function</em> being contained by this {@code ArgStep}.
         */
        CheckedFunction<T, R, ? extends Throwable> function();
    }

    /**
     * A two argument version of {@link TryArg}.
     *
     * @see TryArg
     *
     * @param <T> The type of the first argument.
     * @param <U> The type of the second argument.
     * @param <R> The result type produced by the function.
     */
    sealed interface TryBiArg<T, U, R> extends Try<R> permits TryBiArgWithFinally, TryBiValue, TryBiResource {

        /**
         * A two argument version of {@link TryArg#withArg(Object)}.
         *
         * @see TryArg#withArg(Object)
         */
        default TryBiArg<T, U, R> withArgs(T arg0, U arg1) {
            requireNonNull(arg0, "arg0");
            requireNonNull(arg1, "arg1");
            return  withArgs(() -> arg0, () -> arg1);
        }

        /**
         * Capture the {@link CheckedSupplier}s that will supply the arguments
         * to the <em>function</em> executed by this {@link Try}.
         *
         * @param arg0Supplier The {@link CheckedSupplier} that will supply the
         *        first argument to the <em>function</em> executed by this
         *        {@link Try}.
         * @param arg1Supplier The {@link CheckedSupplier} that will supply the
         *        second argument to the <em>function</em> executed by this
         *        {@link Try}.
         *
         * @return Some new {@code TryBiArg<T, U, R>}, dependent on implementation.
         *
         * @throws NullArgumentException When {@code arg0Supplier} or
         *         {@code arg1Supplier} is a {@code null}.
         */
        TryBiArg<T, U, R> withArgs(
                CheckedSupplier<T, ? extends Throwable> arg0Supplier,
                CheckedSupplier<U, ? extends Throwable> arg1Supplier
        );

        /**
         * A two arg type bind version of {@link TryArg#withFinally(CheckedRunnable)}.
         *
         * @see TryArg#withFinally(CheckedRunnable)
         */
        default TryBiArgWithFinally<T, U, R> withFinally(
                CheckedRunnable<? extends Throwable> finallyAction
        ) {
            return new TryBiArgWithFinally<>(this, finallyAction);
        }

        /**
         * Return the <em>function</em> being contained by this {@code TryBiArg}.
         */
        CheckedBiFunction<T, U, R, ? extends Throwable> function();
    }

    /**
     * A two argument version of {@link ArgStep}.
     *
     * @see ArgStep
     */
    sealed interface BiArgStep<T, U, R> permits BiArgFuncStep, BiArgFinallyStep {

        /**
         * A two argument version of {@link ArgStep#withArg(Object)}.
         *
         * @see ArgStep#withArg(Object)
         */
        default TryBiArg<T, U, R> withArgs(T arg0, U arg1) {
            requireNonNull(arg0, "arg0");
            requireNonNull(arg1, "arg1");
            return  withArgs(() -> arg0, () -> arg1);
        }

        /**
         * A two argument version of {@link ArgStep#withArg(CheckedSupplier)}.
         *
         * @see ArgStep#withArg(CheckedSupplier)
         */
        TryBiArg<T, U, R> withArgs(
                CheckedSupplier<T, ? extends Throwable> arg0Supplier,
                CheckedSupplier<U, ? extends Throwable> arg1Supplier
        );

        /**
         * A two arg type bind version of {@link ArgStep#withFinally(CheckedRunnable)}.
         *
         * @see ArgStep#withFinally(CheckedRunnable)
         */
        default BiArgStep<T, U, R> withFinally(
                CheckedRunnable<? extends Throwable> finallyAction
        ) {
            return new BiArgFinallyStep<>(this, finallyAction);
        }

        /**
         * Return the <em>function</em> being contained by this {@code TryBiArg}.
         */
        CheckedBiFunction<T, U, R, ? extends Throwable> function();
    }
}

/**
 * A {@link Try.ArgStep} that captures the {@code function} that will be
 * executed in the try catch of the {@link Try.TryArg} produced at the end of
 * this step chain.
 *
 * @param function The <em>function</em> that will be executed in the try catch
 *        of the {@link Try.TryArg} produced at the end of this step chain.
 * @param instantiator A <em>function</em> that will assemble a specific
 *        {@link Try.TryArg} implementation using the capture {@code function}
 *        and any argument captured later.
 */
record ArgFuncStep<T, R>(
        CheckedFunction<T, R, ? extends Throwable> function,
        BiFunction<
                CheckedFunction<T, R, ? extends Throwable>,
                CheckedSupplier<T, ? extends Throwable>,
                Try.TryArg<T, R>
        > instantiator
) implements Try.ArgStep<T, R> {

    ArgFuncStep {
        requireNonNull(function, "function");
        requireNonNull(instantiator, "instantiator");
    }

    /**
     * {@inheritDoc}
     * <p>
     * The specific type of {@link Try.TryArg} constructed is dependent on the
     * contained {@link #instantiator()}.
     */
    @Override
    public Try.TryArg<T, R> withArg(
            CheckedSupplier<T, ? extends Throwable> argSupplier
    ) {
        requireNonNull(argSupplier, "argSupplier");
        return instantiator.apply(function, argSupplier);
    }
}

/**
 * A {@link Try.ArgStep} that captures the {@code finallyAction} that will be
 * executed after the try catch of the {@code delegate}. This is a special step
 * where the finally action has been provided but the argument has not yet been
 * provided to construct the final {@link Try.TryArg}.
 *
 * @param delegate Contains the {@link Try.ArgStep} that contains the
 *        <em>function</em> needed to build the final {@link Try.TryArg}.
 * @param finallyAction A {@link CheckedRunnable} to be executed after the
 *        try catch of the <em>function</em> contained in the {@code delegate}.
 */
record ArgFinallyStep<T, R>(
        Try.ArgStep<T, R> delegate,
        CheckedRunnable<? extends Throwable> finallyAction
) implements Try.ArgStep<T, R> {

    ArgFinallyStep {
        requireNonNull(delegate, "delegate");
        requireNonNull(finallyAction, "finallyAction");
    }

    /**
     * {@inheritDoc}
     * <p>
     * The specific type of {@link Try.TryArg} constructed is dependent on what
     * the {@code delegate} produces.
     */
    @Override
    public Try.TryArg<T, R> withArg(
            CheckedSupplier<T, ? extends Throwable> argSupplier
    ) {
        var delegate = delegate().withArg(argSupplier);
        return new TryArgWithFinally<>(delegate, finallyAction);
    }

    /**
     * Extract the function contained in the {@code delegate}.
     */
    @Override
    public CheckedFunction<T, R, ? extends Throwable> function() {
        return delegate.function();
    }
}

/**
 * A two argument version of {@link ArgFuncStep}.
 *
 * @see ArgFuncStep
 */
record BiArgFuncStep<T, U, R>(
        CheckedBiFunction<T, U, R, ? extends Throwable> function,
        TriFunction<
                CheckedBiFunction<T, U, R, ? extends Throwable>,
                CheckedSupplier<T, ? extends Throwable>,
                CheckedSupplier<U, ? extends Throwable>,
                Try.TryBiArg<T, U, R>
        > instantiator
) implements Try.BiArgStep<T, U, R> {

    BiArgFuncStep {
        requireNonNull(function, "function");
        requireNonNull(instantiator, "instantiator");
    }

    /**
     * A two argument version of {@link ArgFuncStep#withArg(CheckedSupplier)}.
     *
     * @see ArgFuncStep#withArg(CheckedSupplier)
     */
    @Override
    public Try.TryBiArg<T, U, R> withArgs(
            CheckedSupplier<T, ? extends Throwable> arg0Supplier,
            CheckedSupplier<U, ? extends Throwable> arg1Supplier
    ) {
        return instantiator.apply(function, arg0Supplier, arg1Supplier);
    }
}

/**
 * A two argument version of {@link ArgFinallyStep}.
 *
 * @see ArgFinallyStep
 */
record BiArgFinallyStep<T, U, R>(
        Try.BiArgStep<T, U, R> delegate,
        CheckedRunnable<? extends Throwable> finallyAction
) implements Try.BiArgStep<T, U, R> {

    BiArgFinallyStep {
        requireNonNull(delegate, "delegate");
        requireNonNull(finallyAction, "finallyAction");
    }

    /**
     * A two argument version of {@link ArgFinallyStep#withArg(CheckedSupplier)}.
     *
     * @see ArgFinallyStep#withArg(CheckedSupplier)
     */
    @Override
    public Try.TryBiArg<T, U, R> withArgs(
            CheckedSupplier<T, ? extends Throwable> arg0Supplier,
            CheckedSupplier<U, ? extends Throwable> arg1Supplier
    ) {
        var delegate = delegate().withArgs(arg0Supplier, arg1Supplier);
        return new TryBiArgWithFinally<>(delegate, finallyAction);
    }

    /**
     * Extract the function contained in the {@code delegate}.
     */
    @Override
    public CheckedBiFunction<T, U, R, ? extends Throwable> function() {
        return delegate.function();
    }
}