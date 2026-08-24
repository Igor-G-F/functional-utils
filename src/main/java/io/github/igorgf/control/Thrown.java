package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;

import java.util.function.Predicate;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * An immutable container for some {@code captured} {@link Throwable}, enabling
 * fluid reasoning about, decorating, or recovering from exceptions.
 * <p>
 * <b>{@code Thrown} features:</b>
 * <ul>
 *   <li>
 *       Is <b>null safe</b>: a {@code Thrown} can never wrap a {@code null}
 *       value. The entire API otherwise rejects {@code null} at every
 *       boundary: constructors, functions args and results, all throw
 *       appropriate {@link ContractViolationException}s on {@code null}.
 *   </li>
 *   <li>
 *       A <b>functor</b>: {@link #map} transforms the contained value.
 *   </li>
 *   <li>
 *       Is <b>exception fluent:</b> Operations use the checked function aware
 *       functional interfaces from {@link io.github.igorgf.function}, to ensure
 *       checked exception propagation support.
 *   </li>
 * </ul>
 * <p>
 * <b>Relationship to {@link Error}:</b><br>
 * A {@code Thrown} never wraps an {@link Error}. The canonical constructor
 * rejects one with an {@link ContractViolationException}, an {@link Error} is
 * considered unrecoverable and must propagate.
 * <p>
 * <b>Alternative Types:</b><br>
 * For sequential error-handling with short-circuiting, use {@link Either}. <br>
 * For accumulating of multiple validation errors, use {@link Validation}.
 *
 * @see io.github.igorgf.function
 *
 * @param captured The captured throwable, never {@code null} or {@link Error}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <X> The {@link Throwable} type being capture.
 */
public record Thrown<X extends Throwable>(X captured)  {

    /**
     * <b>Rejects {@link Error}:</b> {@code Thrown} models a <em>recoverable</em>
     * failure captured as a value, and an {@link Error} signals an
     * unrecoverable condition that must propagate.
     *
     * @param captured the captured throwable; must not be an {@link Error}.
     *
     * @throws NullArgumentException If {@code thrown} is {@code null}.
     * @throws ContractViolationException If {@code thrown} is an {@link Error}.
     */
    public Thrown {
        requireNonNull(captured, "captured");
        if (captured instanceof Error) {
            throw new ContractViolationException(
                    "Thrown cannot wrap an Error. Errors are unrecoverable and "
                            + "must propagate: " + captured
            );
        }
    }

    /**
     * Factory method for creating a new {@code Thrown<X>} instance.
     *
     * @param captured The exception to be contained.
     * @param <X> The exception type being contained.
     */
    public static <X extends Throwable> Thrown<X> of(X captured) {
        return new Thrown<X>(captured);
    }

    // region ——————————————————— Transformation ———————————————————
    /**
     * Transforms the {@link #captured()} throwable into a different throwable
     * type {@code T}, returning a new {@code ThrownX<T>} wrapping the result.
     * <p>
     * The returned {@code ThrownX<T>} is subject to the same canonical
     * constructor invariants as any other {@code ThrownX}: if {@code mapper}
     * produces an {@link Error}, construction fails with a
     * {@link ContractViolationException}.
     *
     * @param <T> The output throwable type.
     * @param <X2> The checked exception type {@code mapper} may throw.
     * @param mapper Transforms the {@link #captured()} throwable into {@code Y}.
     *
     * @return A new {@code ThrownX<T>} wrapping the mapped throwable.
     *
     * @throws X2 If {@code mapper} throws it.
     * @throws NullArgumentException If {@code mapper} is {@code null}.
     * @throws NullResultException If {@code mapper} returns {@code null}.
     * @throws ContractViolationException If {@code mapper} returns an
     *         {@link Error}.
     */
    public <T extends Throwable, X2 extends Throwable> Thrown<T> map(
            CheckedFunction<? super X, ? extends T, ? extends X2> mapper
    ) throws X2, NullArgumentException, NullResultException {
        requireNonNull(mapper, "mapper");
        var mapped = requireNonNullResult(mapper, this.captured);
        return Thrown.of(mapped);
    }

    /**
     * Recovers from the {@link #captured()} throwable by applying
     * {@code recovery}, turning the failure into a usable value {@code T}.
     * <p>
     * This is the primary escape hatch from a {@code ThrownX}, converting the
     * captured exception back into a value the caller can continue processing
     * with.
     *
     * @param <T> The recovered value type.
     * @param <X2> The checked exception type {@code recovery} may throw.
     * @param recoveryMapper Consumes {@link #captured()} and produces a recovery value.
     *
     * @return The value produced by {@code recovery}.
     *
     * @throws X2 If {@code recovery} throws it.
     * @throws NullArgumentException If {@code recovery} is {@code null}.
     * @throws NullResultException If {@code recovery} returns {@code null}.
     */
    public <T, X2 extends Throwable> T recover(
            CheckedFunction<? super X, ? extends T, ? extends X2> recoveryMapper
    ) throws X2, NullArgumentException, NullResultException {
        requireNonNull(recoveryMapper, "recoveryMapper");
        return requireNonNullResult(recoveryMapper, this.captured);
    }
    // endregion ———————————————— Transformation ———————————————————

    // region ——————————————————— Inspection ———————————————————
    /**
     * Reports whether the captured throwable is an unchecked exception.
     *
     * @return {@code true} if the wrapped throwable is a
     *         {@link RuntimeException}; {@code false} otherwise.
     */
    public boolean isRuntimeException() {
        return this.captured instanceof RuntimeException;
    }

    /**
     * Reports whether the captured throwable is a checked exception, that is an
     * {@link Throwable} that is not a {@link RuntimeException}.
     * <p>
     * This method never returns {@code true} for an {@link Error}, because
     * {@code Thrown} rejects {@code Error} at construction time and can
     * never wrap {@code Error}.
     *
     * @return {@code true} if the wrapped throwable is a checked exception;
     *         {@code false} otherwise.
     */
    public boolean isCheckedException() {
        return !(this.captured instanceof RuntimeException);
    }
    // endregion ———————————————— Inspection ———————————————————

    // region ——————————————————— Handlers ———————————————————
    /**
     * Invokes {@code handler} with the {@link #captured()} throwable when it is
     * an instance of one of the given {@code throwables} types, then returns
     * {@code this} for chaining.
     * <p>
     * Matching is by assignability. For exact-class-only matching, use
     * {@link #handleExact(CheckedConsumer, Class[])}.
     * <p>
     * This uses a generic {@link Throwable} handler, to bind the handler to a
     * concrete type use {@link #handle(CheckedConsumer, Class)}.
     *
     * @see #handle(CheckedConsumer, Class)
     * @see #handleExact(CheckedConsumer, Class[])
     *
     * @param <X2> The checked exception type the {@code handler} may throw.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param throwables The throwable types to match, empty always matches.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X2 If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code handler}, the {@code throwables}
     *         array, or any element of it is {@code null}.
     */
    @SafeVarargs
    public final <X2 extends Throwable> Thrown<X> handle(
            CheckedConsumer<X, X2> handler,
            Class<? extends X>... throwables
    ) throws X2, NullArgumentException {
        requireNonNull(handler, "handler");
        requireNonNull(throwables, "throwables");
        if (throwables.length == 0 || matchesAssignable(throwables)) {
            handler.accept(captured);
        }
        return this;
    }

    /**
     * Invokes {@code handler} with the {@link #captured()} throwable when it is
     * an instance of the given {@code exceptionType}, then returns {@code this}
     * for chaining.
     * <p>
     * Matching is by assignability. For exact-class-only matching, use
     * {@link #handleExact(CheckedConsumer, Class)}.
     * <p>
     * Unlike {@link #handle(CheckedConsumer, Class[])}, the {@code handler}
     * is bound to a specific exception type {@code T}.
     *
     * @see #handleExact(CheckedConsumer, Class)
     * @see #handle(CheckedConsumer, Class[])
     *
     * @param <T> The throwable type the {@code handler} consumes.
     * @param <X2> The checked exception type the {@code handler} may throw.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param exceptionType The type the wrapped throwable must be an instance
     *        of.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X2 If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code handler} or {@code exceptionType}
     *         is {@code null}.
     */
    public <T extends X, X2 extends Throwable> Thrown<X> handle(
            CheckedConsumer<T, X2> handler,
            Class<T> exceptionType
    ) throws X2, NullArgumentException {
        requireNonNull(handler, "handler");
        requireNonNull(exceptionType, "exceptionType");
        if (exceptionType.isInstance(this.captured)) {
            handler.accept(exceptionType.cast(this.captured));
        }
        return this;
    }

    /**
     * Invokes {@code handler} with the {@link #captured()} throwable when its
     * exact runtime class matches one of the given {@code throwables} types,
     * then returns {@code this} for chaining.
     * <p>
     * Matching is by exact class equality, a filter for {@code IOException}
     * does <b>not</b> match a captured {@code FileNotFoundException}. For
     * subtype-inclusive matching, use {@link #handle(CheckedConsumer, Class[])}.
     * <p>
     * This uses a generic {@link Throwable} handler, to bind the handler to a
     * concrete type use {@link #handleExact(CheckedConsumer, Class)}.
     *
     * @see #handleExact(CheckedConsumer, Class)
     * @see #handle(CheckedConsumer, Class[])
     *
     * @param <X2> The checked exception type the {@code handler} may throw.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param throwables The throwable types to match, empty always matches.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X2 If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code handler}, the {@code throwables}
     *         array, or any element of it is {@code null}.
     */
    @SafeVarargs
    public final <X2 extends Throwable> Thrown<X> handleExact(
            CheckedConsumer<X, X2> handler,
            Class<? extends X>... throwables
    ) throws X2, NullArgumentException {
        requireNonNull(handler, "handler");
        requireNonNull(throwables, "throwables");
        if (throwables.length == 0 || matchesExact(throwables)) {
            handler.accept(captured);
        }
        return this;
    }

    /**
     * Invokes {@code handler} with the {@link #captured()} throwable when its
     * exact runtime class matches the given {@code exceptionType}, then returns
     * {@code this} for chaining.
     * <p>
     * Matching is by exact class equality, a filter for {@code IOException}
     * does <b>not</b> match a captured {@code FileNotFoundException}. For
     * subtype-inclusive matching, use {@link #handle(CheckedConsumer, Class)}.
     * <p>
     * Unlike {@link #handle(CheckedConsumer, Class[])}, the {@code handler}
     * is bound to a specific exception type {@code T}.
     *
     * @see #handleExact(CheckedConsumer, Class[])
     * @see #handle(CheckedConsumer, Class)
     *
     * @param <T> The throwable type the {@code handler} consumes.
     * @param <X2> The checked exception type the {@code handler} may throw.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param exceptionType The type the wrapped throwable must be an instance
     *        of.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X2 If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code handler} or {@code exceptionType}
     *         is {@code null}.
     */
    public <T extends X, X2 extends Throwable> Thrown<X> handleExact(
            CheckedConsumer<T, X2> handler,
            Class<T> exceptionType
    ) throws X2, NullArgumentException {
        requireNonNull(handler, "handler");
        requireNonNull(exceptionType, "exceptionType");
        if (exceptionType.equals(this.captured.getClass())) {
            handler.accept(exceptionType.cast(this.captured));
        }
        return this;
    }
    // endregion ———————————————— Handlers ———————————————————

    // region ——————————————————— Rethrows ———————————————————
    /**
     * Rethrows {@link #captured()} when its exact runtime class matches one of
     * {@code throwables}, or when no arguments are given (matches everything).
     * Otherwise, returns {@code this} for chaining.
     * <p>
     * Matching is by exact class equality, a filter for {@code IOException}
     * does <b>not</b> match a captured {@code FileNotFoundException}. For
     * subtype-inclusive matching, use {@link #rethrow(Class[])}.
     *
     * @see #rethrow(Class[])
     *
     * @param throwables The exact types to match; empty matches everything.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws X If {@link #captured()} matches.
     * @throws NullArgumentException If {@code throwables} or any element is
     *         {@code null}.
     */
    @SafeVarargs
    public final Thrown<X> rethrowExact(
            Class<? extends X>... throwables
    ) throws X, NullArgumentException {
        requireNonNull(throwables, "throwables");
        if (throwables.length == 0 || matchesExact(throwables)) {
            throw captured;
        }
        return this;
    }

    /**
     * Rethrows {@link #captured()}, narrowed to {@code T}, when its exact
     * runtime class equals {@code throwable}. Otherwise, returns {@code this}.
     * <p>
     * Unlike {@link #rethrowExact(Class[])}, the declared exception is narrowed
     * from {@code X} to the more specific {@code T}.
     * <p>
     * Matching is by exact class equality, a filter for {@code IOException}
     * does <b>not</b> match a captured {@code FileNotFoundException}. For
     * subtype-inclusive matching, use {@link #rethrow(Class)}.
     *
     * @see #rethrow(Class)
     *
     * @param <T> The exact throwable type to match and rethrow.
     * @param throwable The exact type {@link #captured()} must equal.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws T If {@link #captured()} matches.
     * @throws NullArgumentException If {@code throwable} is {@code null}.
     */
    public <T extends X> Thrown<X> rethrowExact(
            Class<T> throwable
    ) throws T, NullArgumentException {
        requireNonNull(throwable, "throwable");
        if (throwable.equals(this.captured.getClass())) {
            throw throwable.cast(this.captured);
        }
        return this;
    }

    /**
     * Rethrows {@link #captured()} when it is an instance of one of
     * {@code throwables} (matching subtypes too), or when no arguments are
     * given. Otherwise, returns {@code this} for chaining.
     * <p>
     * Matching is by assignability, this mirrors how a {@code catch} clause
     * matches. For exact-class-only matching, use {@link #rethrowExact(Class[])}.
     *
     * @see #rethrowExact(Class[])
     *
     * @param throwables The types to match, including subtypes; empty matches
     *        everything.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws X If {@link #captured()} matches.
     * @throws NullArgumentException If {@code throwables} or any element is
     *         {@code null}.
     */
    @SafeVarargs
    public final Thrown<X> rethrow(
            Class<? extends X>... throwables
    ) throws X, NullArgumentException {
        requireNonNull(throwables, "throwables");
        if (throwables.length == 0 || matchesAssignable(throwables)) {
            throw captured;
        }
        return this;
    }

    /**
     * Rethrows {@link #captured()}, narrowed to {@code T}, when it is an
     * instance of {@code throwable}. Otherwise, returns {@code this}.
     * <p>
     * Unlike {@link #rethrow(Class[])}, the declared exception is narrowed from
     * {@code X} to the more specific {@code T}.
     * <p>
     * Matching is by assignability, this mirrors how a {@code catch} clause
     * matches. For exact-class-only matching, use {@link #rethrowExact(Class)}.
     *
     * @see #rethrowExact(Class)
     *
     * @param <T> The throwable type to match and rethrow.
     * @param throwable The type {@link #captured()} must be an instance of.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws T If {@link #captured()} matches.
     * @throws NullArgumentException If {@code throwable} is {@code null}.
     */
    public <T extends X> Thrown<X> rethrow(
            Class<T> throwable
    ) throws T, NullArgumentException {
        requireNonNull(throwable, "throwable");
        if (throwable.isInstance(this.captured)) {
            throw throwable.cast(this.captured);
        }
        return this;
    }

    /**
     * Rethrows {@link #captured()}, without declaring or requiring the caller to
     * handle it, when its exact runtime class matches one of {@code throwables},
     * or when no arguments are given. Otherwise, returns {@code this}.
     * <p>
     * This bypasses checked-exception enforcement via an unchecked cast, see
     * {@link #rethrowSneaky(Class[])} for the assignability-matching counterpart.
     * Calling this method is always an explicit, deliberate act.
     *
     * @see #rethrowSneaky(Class[])
     * @see #rethrowExact(Class[])
     *
     * @param throwables The exact types to match; empty matches everything.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws NullArgumentException If {@code throwables} or any element is
     *         {@code null}.
     */
    @SafeVarargs
    public final Thrown<X> rethrowExactSneaky(
            Class<? extends Throwable>... throwables
    ) throws NullArgumentException {
        requireNonNull(throwables, "throwables");
        if (throwables.length == 0 || matchesExact(throwables)) {
            rethrowSneaky(this.captured);
        }
        return this;
    }

    /**
     * Rethrows {@link #captured()}, without declaring or requiring the caller to
     * handle it, when it is an instance of one of {@code throwables}, or when no
     * arguments are given. Otherwise, returns {@code this}.
     * <p>
     * This bypasses checked-exception enforcement via an unchecked cast, see
     * {@link #rethrowExactSneaky(Class[])} for the exact-match counterpart.
     * Calling this method is always an explicit, deliberate act.
     *
     * @see #rethrowExactSneaky(Class[])
     * @see #rethrow(Class[])
     *
     * @param throwables The types to match, including subtypes; empty matches
     *        everything.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws NullArgumentException If {@code throwables} or any element is
     *         {@code null}.
     */
    @SafeVarargs
    public final Thrown<X> rethrowSneaky(
            Class<? extends Throwable>... throwables
    ) throws NullArgumentException {
        requireNonNull(throwables, "throwables");
        if (throwables.length == 0 || matchesAssignable(throwables)) {
            rethrowSneaky(this.captured);
        }
        return this;
    }
    // endregion ———————————————— Rethrows ———————————————————

    /**
     * Returns the captured {@link Throwable}. Alias for the generated record
     * accessor {@link #captured()}.
     *
     * @return The wrapped throwable; never {@code null}.
     */
    public X get() {
        return this.captured;
    }

    /**
     * Wrapper to trick the compiler to allow throwing a checked exception,
     * without forcing the caller to handle it.
     */
    @SuppressWarnings("unchecked")
    private <T extends Throwable> void rethrowSneaky(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Reports whether {@link #captured()} is an exact instance of one of the
     * given types {@code throwables}.
     *
     * @param throwables The types to test against.
     *
     * @return {@code true} if the wrapped throwable matches; {@code false}
     *         otherwise.
     *
     * @throws NullArgumentException If the array or any element is {@code null}.
     */
    private boolean matchesExact(Class<?>[] throwables) {
        var i = 0;
        for (Class<?> type : throwables) {
            requireNonNull(type, "throwables[" + i + "]");
            if (type.equals(this.captured.getClass())) {
                return true;
            }
            ++i;
        }
        return false;
    }

    /**
     * Reports whether {@link #captured()} is assignable from any of the given
     * types {@code throwables}.
     *
     * @param throwables The types to test against.
     *
     * @return {@code true} if the wrapped throwable matches; {@code false}
     *         otherwise.
     *
     * @throws NullArgumentException If the array or any element is {@code null}.
     */
    private boolean matchesAssignable(Class<?>[] throwables) {
        var i = 0;
        for (Class<?> type : throwables) {
            requireNonNull(type, "throwables[" + i + "]");
            if (type.isInstance(this.captured)) {
                return true;
            }
            ++i;
        }
        return false;
    }
}
