package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.Objects;

/**
 * A {@link Try} whose described computation operates on one or more
 * {@link AutoCloseable} resources that are opened immediately before the
 * computation runs and closed immediately after, mirroring the semantics of a
 * <em>try with resources</em> statement.
 * <p>
 * Like every {@link Try}, a {@code TryWithResources} is <b>lazy</b>: the
 * resource {@link CheckedSupplier suppliers} and the computation are only
 * invoked when {@link #execute()} is called. On execution each resource is
 * acquired from its supplier, the computation is applied to the open
 * resource(s), and the resources are closed in the reverse order of
 * acquisition, whether the computation completes normally or throws.
 * <p>
 * <b>Failure capture:</b><br>
 * Consistent with {@link Try}, every {@link Throwable} raised while acquiring a
 * resource, running the computation, or closing a resource is captured within a
 * {@link Thrown} contained in a {@link Left}, <em>except</em> for
 * {@link Error}, which is rethrown and propagates out of {@link #execute()}.
 * If the computation throws and a subsequent
 * {@link AutoCloseable#close()} also throws, the close throwable is attached to
 * the primary throwable via {@link Throwable#addSuppressed(Throwable)}, exactly
 * as a <em>try with resources</em> statement would arrange. If the computation
 * <b>succeeds</b> but a {@link AutoCloseable#close()} throws a
 * non-{@link Error} {@link Throwable}, that close throwable becomes the
 * captured failure and the would-be result is discarded.
 * <p>
 * <b>Staged construction:</b><br>
 * Construction proceeds in stages just like {@link Try}, with an extra stage for
 * supplying the resource(s): {@link #of(CheckedFunction)} (or
 * {@link #of(CheckedBiFunction)}) records the computation and returns an
 * intermediate step; the step then binds the resource supplier(s) to yield an
 * executable {@code Try}:
 * <pre>{@code
 *     Either<Thrown, String> firstLine = TryWithResources
 *             .of((BufferedReader r) -> r.readLine())
 *             .withResource(() -> Files.newBufferedReader(path))
 *             .execute();
 * }</pre>
 * For a computation over two resources, supply both via
 * {@link TryBiFunctionWithResourcesStep#withResources(CheckedSupplier, CheckedSupplier)};
 * they are opened in the given order and closed in reverse:
 * <pre>{@code
 *     Either<Thrown, Long> bytesCopied = TryWithResources
 *             .of((InputStream in, OutputStream out) -> in.transferTo(out))
 *             .withResources(
 *                     () -> Files.newInputStream(source),
 *                     () -> Files.newOutputStream(target))
 *             .execute();
 * }</pre>
 * The bound resource type must be {@link AutoCloseable}; the supplier itself may
 * throw while opening the resource, and any such {@link Throwable} is captured
 * on {@link #execute()} in the usual way.
 *
 * @see Try
 * @see Thrown
 * @see AutoCloseable
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <R> the result type produced on normal completion
 */
public sealed interface TryWithResources<R> extends Try<R> permits TryFunctionWithResource, TryBiFunctionWithResources {

    /**
     * Entry point for describing a fallible computation over a single
     * {@link AutoCloseable} resource.
     * <p>
     * Records the {@code function} to be executed and returns a
     * {@link TryFunctionWithResourceStep}. The resource must then be supplied
     * via {@link TryFunctionWithResourceStep#withResource(CheckedSupplier)} to
     * produce an executable {@code Try}. The {@code function} is not invoked
     * here.
     *
     * @see TryFunctionWithResourceStep
     *
     * @param <T> The {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The fallible computation to describe.
     *
     * @return A {@code TryFunctionWithResourceStep<T, R>} awaiting its resource
     *         supplier.
     *
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <T extends AutoCloseable, R> TryFunctionWithResourceStep<T, R> of(
            CheckedFunction<T, R, Throwable> function
    ) {
        Objects.requireNonNull(function);
        return new TryFunctionWithResourceStep<>(function);
    }

    /**
     * Entry point for describing a fallible computation over two
     * {@link AutoCloseable} resources.
     * <p>
     * Records the {@code function} to be executed and returns a
     * {@link TryBiFunctionWithResourcesStep}. The resources must then be
     * supplied via
     * {@link TryBiFunctionWithResourcesStep#withResources(CheckedSupplier, CheckedSupplier)}
     * to produce an executable {@code Try}. The {@code function} is not invoked
     * here.
     *
     * @see TryBiFunctionWithResourcesStep
     *
     * @param <T> The first {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <U> The second {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The fallible computation to describe.
     *
     * @return A {@code TryBiFunctionWithResourcesStep<T, U, R>} awaiting its
     *         resource suppliers.
     *
     * @throws NullPointerException If {@code function} is {@code null}.
     */
    static <T extends AutoCloseable, U extends AutoCloseable, R> TryBiFunctionWithResourcesStep<T, U, R> of(
            CheckedBiFunction<T, U, R, Throwable> function
    ) {
        Objects.requireNonNull(function);
        return new TryBiFunctionWithResourcesStep<>(function);
    }

    /**
     * An intermediate stage produced by
     * {@link TryWithResources#of(CheckedFunction)} that holds a described
     * single resource {@code function} awaiting its resource supplier.
     *
     * @see TryWithResources#of(CheckedFunction)
     *
     * @param <T> The {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The described fallible computation.
     */
    record TryFunctionWithResourceStep<T extends AutoCloseable, R>(
            CheckedFunction<T, R, Throwable> function
    ) {
        /**
         * Binds the resource {@code supplier} to the described function,
         * producing an executable {@code Try}.
         * <p>
         * The {@code supplier} is invoked on {@link Try#execute()} to open the
         * resource, which is closed automatically after the computation
         * completes. The {@code supplier} itself is not invoked here.
         *
         * @param supplier The factory that opens the {@link AutoCloseable}
         *        resource on execution.
         *
         * @return A new executable {@code Try<R>}.
         *
         * @throws NullPointerException If {@code supplier} is {@code null}.
         */
        public Try<R> withResource(CheckedSupplier<T, Throwable> supplier) {
            Objects.requireNonNull(supplier);
            return new TryFunctionWithResource<>(function, supplier);
        }
    }

    /**
     * An intermediate stage produced by
     * {@link TryWithResources#of(CheckedBiFunction)} that holds a described
     * two resource {@code function} awaiting its resource suppliers.
     *
     * @see TryWithResources#of(CheckedBiFunction)
     *
     * @param <T> The first {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <U> The second {@link AutoCloseable} resource type consumed by the
     *        {@code function}.
     * @param <R> The result type produced by the {@code function}.
     * @param function The described fallible computation.
     */
    record TryBiFunctionWithResourcesStep<T extends AutoCloseable, U extends AutoCloseable, R>(
            CheckedBiFunction<T, U, R, Throwable> function
    ) {
        /**
         * Binds the resource {@code supplier}s to the described function,
         * producing an executable {@code Try}.
         * <p>
         * On {@link Try#execute()} the resources are opened in the order given
         * ({@code supplier} then {@code supplier2}) and closed in the reverse
         * order after the computation completes. Neither supplier is invoked
         * here.
         *
         * @param supplier The factory that opens the first {@link AutoCloseable}
         *        resource on execution.
         * @param supplier2 The factory that opens the second
         *        {@link AutoCloseable} resource on execution.
         *
         * @return A new executable {@code Try<R>}.
         *
         * @throws NullPointerException If {@code supplier} or {@code supplier2}
         *         is {@code null}.
         */
        public Try<R> withResources(
                CheckedSupplier<T, Throwable> supplier,
                CheckedSupplier<U, Throwable> supplier2
        ) {
            Objects.requireNonNull(supplier);
            Objects.requireNonNull(supplier2);
            return new TryBiFunctionWithResources<>(function, supplier, supplier2);
        }
    }

}

/**
 * A {@link TryWithResources} describing the application of a two argument
 * {@link CheckedBiFunction} to two {@link AutoCloseable} resources opened by
 * {@code supplier} and {@code supplier2} on execution.
 *
 * @param <T> The first {@link AutoCloseable} resource type.
 * @param <U> The second {@link AutoCloseable} resource type.
 * @param <R> The result type produced by the function.
 * @param function The fallible computation to run on execution.
 * @param supplier The factory opening the first resource on execution.
 * @param supplier2 The factory opening the second resource on execution.
 */
record TryBiFunctionWithResources<T extends AutoCloseable, U extends AutoCloseable, R>(
        CheckedBiFunction<T, U, R, Throwable> function,
        CheckedSupplier<T, Throwable> supplier,
        CheckedSupplier<U, Throwable> supplier2
) implements TryWithResources<R> {
    @Override
    public Either<Thrown, R> execute() {
        try (
                T resource = supplier.get();
                U resource2 = supplier2.get()
        ) {
            return Either.right(function.apply(resource, resource2));
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            return Either.left(new Thrown(e));
        }
    }
}

/**
 * A {@link TryWithResources} describing the application of a single argument
 * {@link CheckedFunction} to an {@link AutoCloseable} resource opened by
 * {@code supplier} on execution.
 *
 * @param <T> The {@link AutoCloseable} resource type.
 * @param <R> The result type produced by the function.
 * @param function The fallible computation to run on execution.
 * @param supplier The factory opening the resource on execution.
 */
record TryFunctionWithResource<T extends AutoCloseable, R>(
        CheckedFunction<T, R, Throwable> function,
        CheckedSupplier<T, Throwable> supplier
) implements TryWithResources<R> {
    @Override
    public Either<Thrown, R> execute() {
        try (T resource = supplier.get()) {
            return Either.right(function.apply(resource));
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            return Either.left(new Thrown(e));
        }
    }
}