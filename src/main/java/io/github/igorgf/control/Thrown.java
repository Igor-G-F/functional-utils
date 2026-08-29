package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;

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
     * The strategy used to match {@link #captured()} against a candidate
     * throwable type.
     */
    public enum MatchStrategy {
        /** Matches by assignability, mirroring how a {@code catch} clause matches. */
        ASSIGNABLE,
        /** Matches only when the candidate type equals the captured throwable's exact runtime class. */
        EXACT
    }


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
     * {@code recoveryMapper}, turning the {@code this} into a usable
     * value {@code T}.
     * <p>
     * This is the primary escape hatch from a {@code ThrownX}, converting the
     * captured exception back into a value the caller can continue processing
     * with.
     *
     * @param <T> The recovered value type.
     * @param <X2> The checked exception type {@code recovery} may throw.
     * @param recoveryMapper Consumes {@code this} and produces a recovery value.
     *
     * @return The value produced by {@code recovery}.
     *
     * @throws X2 If {@code recovery} throws it.
     * @throws NullArgumentException If {@code recovery} is {@code null}.
     * @throws NullResultException If {@code recovery} returns {@code null}.
     */
    public <T, X2 extends Throwable> T recover(
            CheckedFunction<? super Thrown<X>, ? extends T, ? extends X2> recoveryMapper
    ) throws X2, NullArgumentException, NullResultException {
        requireNonNull(recoveryMapper, "recoveryMapper");
        return requireNonNullResult(recoveryMapper, this);
    }

    /**
     * Elevates {@code this} into an {@link Either}, delegating to {@code mapper}
     * when {@link #captured()} is an instance of {@code matchTarget}. Otherwise,
     * preserves {@code this} unchanged as a {@code Left}.
     * <p>
     * Matching logic is determined by {@code matchStrategy}. Always matches if
     * {@code matchTargets} contains 0 elements.
     *
     * @see #recover(CheckedFunction)
     * @see MatchStrategy
     *
     * @param <T> The throwable type {@code mapper} operates on.
     * @param <S> The right hand value type {@code mapper} may produce.
     * @param <X2> The checked exception type {@code mapper} may throw.
     * @param matchStrategy Determines the matching logic.
     * @param mapper Consumes {@code this}, narrowed to {@code Thrown<T>}, and
     *        produces an {@link Either}.
     * @param matchTarget The type {@link #captured()} must be an instance of.
     *
     * @return {@code mapper}'s result, when {@code matchTarget} matches. <br>
     *         {@code Either.left(this)}, unchanged, otherwise.
     *
     * @throws X2 If {@code mapper} runs and throws it.
     * @throws NullArgumentException If {@code mapper} or {@code matchTarget}
     *         is {@code null}.
     * @throws NullResultException If {@code mapper} returns {@code null}.
     */
    @SuppressWarnings("unchecked")
    public <T extends X, S, X2 extends Throwable> Either<Thrown<X>, S> toEither(
            MatchStrategy matchStrategy,
            CheckedFunction<? super Thrown<T>, ? extends Either<Thrown<X>, S>, ? extends X2> mapper,
            Class<T> matchTarget
    ) throws X2, NullArgumentException, NullResultException {
        requireNonNull(matchStrategy, "matchStrategy");
        requireNonNull(mapper, "mapper");
        requireNonNull(matchTarget, "matchTarget");
        if (matches(matchStrategy, matchTarget)) {
            return requireNonNullResult(mapper, (Thrown<T>) this);
        }
        return Either.left(this);
    }

    /**
     * Overload of {@link #toEither(MatchStrategy, CheckedFunction, Class)}.
     * <p>
     * Unlike {@link #toEither(MatchStrategy, CheckedFunction, Class)}, this
     * always matches by {@link MatchStrategy#ASSIGNABLE}.
     *
     * @see #toEither(MatchStrategy, CheckedFunction, Class) 
     * @see MatchStrategy
     *
     * @param <T> The throwable type {@code mapper} operates on.
     * @param <S> The right hand value type {@code mapper} may produce.
     * @param <X2> The checked exception type {@code mapper} may throw.
     * @param mapper Consumes {@code this}, narrowed to {@code Thrown<T>}, and
     *        produces an {@link Either}.
     * @param matchTarget The type {@link #captured()} must be an instance of.
     *
     * @return {@code mapper}'s result, when {@code matchTarget} matches. <br>
     *         {@code Either.left(this)}, unchanged, otherwise.
     *
     * @throws X2 If {@code mapper} runs and throws it.
     * @throws NullArgumentException If {@code mapper} or {@code matchTarget}
     *         is {@code null}.
     * @throws NullResultException If {@code mapper} returns {@code null}.
     */
    public <T extends X, S, X2 extends Throwable> Either<Thrown<X>, S> toEither(
            CheckedFunction<? super Thrown<T>, ? extends Either<Thrown<X>, S>, ? extends X2> mapper,
            Class<T> matchTarget
    ) throws X2, NullArgumentException, NullResultException {
        return toEither(MatchStrategy.ASSIGNABLE, mapper, matchTarget);
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
     * an instance of one of the given {@code matchTargets} types, then returns
     * {@code this} for chaining.
     * <p>
     * Matching logic is determined by {@code matchStrategy}. Always matches if
     * {@code matchTargets} contains 0 elements.
     * <p>
     * This uses a generic {@link Throwable} handler, to bind the handler to a
     * concrete type use {@link #handle(MatchStrategy, CheckedConsumer, Class)}.
     *
     * @see #handle(CheckedConsumer, Class[])
     * @see MatchStrategy
     *
     * @param <X2> The checked exception type the {@code handler} may throw.
     * @param matchStrategy Determines the matching logic.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param matchTargets The throwable types to match, empty always matches.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X2 If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code matchStrategy}, {@code handler}, 
     *         the {@code matchTargets} array, or any element of it is {@code null}.
     */
    @SafeVarargs
    public final <X2 extends Throwable> Thrown<X> handle(
            MatchStrategy matchStrategy,
            CheckedConsumer<X, X2> handler,
            Class<? extends X>... matchTargets
    ) throws X2, NullArgumentException {
        requireNonNull(matchStrategy, "matchStrategy");
        requireNonNull(handler, "handler");
        requireNonNull(matchTargets, "matchTargets");
        if (matchTargets.length == 0 || matches(matchStrategy, matchTargets)) {
            handler.accept(captured);
        }
        return this;
    }

    /**
     * Overload of {@link #handle(MatchStrategy, CheckedConsumer, Class[])}.
     * <p>
     * Unlike {@link #handle(MatchStrategy, CheckedConsumer, Class[])}, this
     * always matches by {@link MatchStrategy#ASSIGNABLE}.
     * 
     * @see #handle(MatchStrategy, CheckedConsumer, Class[])
     * 
     * @param <X2> The checked exception type the {@code handler} may throw.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param matchTargets The throwable types to match, empty always matches.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X2 If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code handler}, the {@code matchTargets}
     *         array, or any element of it is {@code null}.
     */
    @SafeVarargs
    public final <X2 extends Throwable> Thrown<X> handle(
            CheckedConsumer<X, X2> handler,
            Class<? extends X>... matchTargets
    ) throws X2, NullArgumentException {
        return  handle(MatchStrategy.ASSIGNABLE, handler, matchTargets);
    }

    /**
     * Invokes {@code handler} with the {@link #captured()} throwable when it is
     * an instance of the given {@code matchTarget}, then returns {@code this}
     * for chaining.
     * <p>
     * Matching logic is determined by {@code matchStrategy}.
     * <p>
     * Unlike {@link #handle(CheckedConsumer, Class[])}, the {@code handler}
     * is bound to a specific exception type {@code T}.
     *
     * @see #handle(CheckedConsumer, Class)
     * @see MatchStrategy
     *
     * @param <T> The throwable type the {@code handler} consumes.
     * @param <X2> The checked exception type the {@code handler} may throw.
     * @param matchStrategy Determines the matching logic.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param matchTarget The type the wrapped throwable must be an instance
     *        of.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X2 If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code matchStrategy}, {@code handler}
     *         or {@code matchTarget} is {@code null}.
     */
    public <T extends X, X2 extends Throwable> Thrown<X> handle(
            MatchStrategy matchStrategy,
            CheckedConsumer<T, X2> handler,
            Class<T> matchTarget
    ) throws X2, NullArgumentException {
        requireNonNull(matchStrategy, "matchStrategy");
        requireNonNull(handler, "handler");
        requireNonNull(matchTarget, "matchTarget");
        if (matches(matchStrategy, matchTarget)) {
            handler.accept(matchTarget.cast(this.captured));
        }
        return this;
    }

    /**
     * Overload of {@link #handle(MatchStrategy, CheckedConsumer, Class)}.
     * <p>
     * Unlike {@link #handle(MatchStrategy, CheckedConsumer, Class)}, this
     * always matches by {@link MatchStrategy#ASSIGNABLE}.
     *
     * @see #handle(MatchStrategy, CheckedConsumer, Class)
     *
     * @param <T> The throwable type the {@code handler} consumes.
     * @param <X2> The checked exception type the {@code handler} may throw.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param matchTarget The type the wrapped throwable must be an instance
     *        of.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X2 If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code handler} or {@code matchTarget}
     *         is {@code null}.
     */
    public <T extends X, X2 extends Throwable> Thrown<X> handle(
            CheckedConsumer<T, X2> handler,
            Class<T> matchTarget
    ) throws X2, NullArgumentException {
        return handle(MatchStrategy.ASSIGNABLE, handler, matchTarget);
    }
    // endregion ———————————————— Handlers ———————————————————

    // region ——————————————————— Rethrows ———————————————————
    /**
     * Rethrows {@link #captured()} when it is an instance of one of
     * {@code matchTargets} or when no arguments are
     * given. Otherwise, returns {@code this} for chaining.
     * <p>
     * Matching logic is determined by {@code matchStrategy}.
     *
     * @see #rethrow(Class[])
     * @see MatchStrategy
     *
     * @param matchTargets The types to match, empty matches everything.
     * @param matchStrategy Determines the matching logic.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws X If {@link #captured()} matches.
     * @throws NullArgumentException If {@code matchStrategy} or
     *         {@code matchTargets}, or any element is {@code null}.
     */
    @SafeVarargs
    public final Thrown<X> rethrow(
            MatchStrategy matchStrategy,
            Class<? extends X>... matchTargets
    ) throws X, NullArgumentException {
        requireNonNull(matchStrategy, "matchStrategy");
        requireNonNull(matchTargets, "matchTargets");
        if (matchTargets.length == 0 || matches(matchStrategy, matchTargets)) {
            throw captured;
        }
        return this;
    }

    /**
     * Overload of {@link #rethrow(MatchStrategy, Class[])}.
     * <p>
     * Unlike {@link #rethrow(MatchStrategy, Class[])}, this always matches by
     * {@link MatchStrategy#ASSIGNABLE}. Empty {@code matchTargets} always
     * throws {@code X}.
     *
     * @see #rethrow(MatchStrategy, Class[])
     *
     * @param matchTargets The types to match, empty matches everything.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws X If {@link #captured()} matches.
     * @throws NullArgumentException If {@code matchTargets}, or any element is
     *         {@code null}.
     */
    @SafeVarargs
    public final Thrown<X> rethrow(
            Class<? extends X>... matchTargets
    ) throws X, NullArgumentException {
        return rethrow(MatchStrategy.ASSIGNABLE, matchTargets);
    }

    /**
     * Rethrows {@link #captured()}, narrowed to {@code T}, when it is an
     * instance of {@code matchTarget}. Otherwise, returns {@code this}.
     * <p>
     * Unlike {@link #rethrow(MatchStrategy, Class[])}, the declared exception
     * is narrowed from {@code X} to the more specific {@code T}.
     * <p>
     * Matching logic is determined by {@code matchStrategy}.
     *
     * @see #rethrow(Class)
     * @see MatchStrategy
     *
     * @param <T> The throwable type to match and rethrow.
     * @param matchTarget The type {@link #captured()} must be an instance of.
     * @param matchStrategy Determines the matching logic.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws T If {@link #captured()} matches.
     * @throws NullArgumentException If {@code matchStrategy} or
     *         {@code matchTarget} is {@code null}.
     */
    public <T extends X> Thrown<X> rethrow(
            MatchStrategy matchStrategy,
            Class<T> matchTarget
    ) throws T, NullArgumentException {
        requireNonNull(matchStrategy, "matchStrategy");
        requireNonNull(matchTarget, "matchTarget");
        if (matches(matchStrategy, matchTarget)) {
            throw matchTarget.cast(this.captured);
        }
        return this;
    }

    /**
     * Overload of {@link #rethrow(MatchStrategy, Class)}, this always uses
     * a {@link MatchStrategy#ASSIGNABLE}.
     *
     * @see #rethrow(MatchStrategy, Class)
     *
     * @param <T> The throwable type to match and rethrow.
     * @param matchTarget The type {@link #captured()} must be an instance of.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws T If {@link #captured()} matches.
     * @throws NullArgumentException If {@code matchTarget} is {@code null}.
     */
    public <T extends X> Thrown<X> rethrow(
            Class<T> matchTarget
    ) throws T, NullArgumentException {
        return rethrow(MatchStrategy.ASSIGNABLE, matchTarget);
    }

    /**
     * Rethrows {@link #captured()}, without declaring or requiring the caller to
     * handle it, when it matches of one of {@code matchTargets}, or when no
     * arguments are given. Always matches on empty {@code matchTargets}.
     * Otherwise, returns {@code this}.
     * <p>
     * This bypasses checked-exception enforcement via an unchecked cast,
     * calling this method is always an explicit, deliberate act.
     *
     * @see #rethrowSneaky(Class[])
     * @see MatchStrategy
     *
     * @param matchStrategy Determines the matching logic.
     * @param matchTargets The types to match, including subtypes; empty matches
     *        everything.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws NullArgumentException If {@code matchStrategy} or
     *         {@code matchTargets}, or any element is {@code null}.
     */
    @SafeVarargs
    public final Thrown<X> rethrowSneaky(
            MatchStrategy matchStrategy,
            Class<? extends Throwable>... matchTargets
    ) throws NullArgumentException {
        requireNonNull(matchStrategy, "matchStrategy");
        requireNonNull(matchTargets, "matchTargets");
        if (matchTargets.length == 0 || matches(matchStrategy, matchTargets)) {
            rethrowSneaky(this.captured);
        }
        return this;
    }

    /**
     * Overload of {@link #rethrowSneaky(MatchStrategy, Class[])},
     * <p>
     * Unlike {@link #rethrowSneaky(MatchStrategy, Class[])}, this always
     * matches by {@link MatchStrategy#ASSIGNABLE}. Empty {@code matchTargets}
     * always throws {@code X}.
     * <p>
     * This bypasses checked-exception enforcement via an unchecked cast,
     * calling this method is always an explicit, deliberate act.
     *
     * @see #rethrowSneaky(Class[])
     *
     * @param matchTargets The types to match, including subtypes; empty matches
     *        everything.
     *
     * @return {@code this}, when the throwable does not match.
     *
     * @throws NullArgumentException If {@code matchTargets} or any element is
     *         {@code null}.
     */
    @SafeVarargs
    public final Thrown<X> rethrowSneaky(
            Class<? extends Throwable>... matchTargets
    ) throws NullArgumentException {
        return rethrowSneaky(MatchStrategy.ASSIGNABLE, matchTargets);
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
     * Reports whether {@link #captured()} is assignable from the given type
     * {@code matchType}.
     */
    private boolean matches(MatchStrategy matchStrategy, Class<?> matchType) {
        return switch (matchStrategy) {
            case ASSIGNABLE -> matchType.isInstance(this.captured);
            case EXACT -> matchType.equals(this.captured.getClass());
        };
    }

    /**
     * Reports whether {@link #captured()} is assignable from the given types
     * {@code matchTargets}.
     */
    private boolean matches(MatchStrategy matchStrategy, Class<?>... matchTargets) {
        var i = 0;
        for (Class<?> type : matchTargets) {
            requireNonNull(type, "matchTargets[" + i + "]");
            if (matches(matchStrategy, type)) {
                return true;
            }
            ++i;
        }
        return false;
    }
}
