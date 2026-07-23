package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;

import java.util.Objects;
import java.util.function.Consumer;


/**
 * An immutable container for a {@link Throwable} captured by a {@link Try}
 * during {@link Try#execute()}, turning a thrown control flow event into an
 * inspectable value. A {@code Thrown} is the {@code Left} payload of the
 * {@link Either} that every {@link Try} produces on failure.
 * <p>
 * <b>{@code Thrown} features:</b>
 * <ul>
 *   <li>
 *       Is <b>null safe</b>: a {@code Thrown} can never wrap a {@code null}
 *       {@link Throwable}, and every handler, supplier, and type argument
 *       passed to its methods is rejected when {@code null} with a
 *       {@link NullPointerException}.
 *   </li>
 *   <li>
 *       Is <b>fluent</b>: the inspection methods ({@link #handle},
 *       {@link #rethrow}, {@link #handleChecked}, {@link #rethrowChecked})
 *       return {@code this} when they do not throw, so reactions to distinct
 *       throwable types can be chained.
 *   </li>
 *   <li>
 *       Offers <b>checked and unchecked</b> reaction paths: the plain
 *       {@link #handle}/{@link #rethrow} operate through unchecked functional
 *       interfaces, while {@link #handleChecked}/{@link #rethrowChecked}
 *       preserve and propagate a declared checked exception type {@code X}.
 *   </li>
 * </ul>
 * <p>
 * <b>Type matching:</b><br>
 * All selective methods match the wrapped throwable by <em>assignability</em>
 * (subtype aware), exactly as a {@code catch} clause would: a filter of
 * {@code IOException.class} matches a wrapped
 * {@link java.io.FileNotFoundException}. The varargs methods additionally treat
 * an <b>empty</b> filter as &quot;match anything&quot;.
 * <p>
 * <b>Relationship to {@link Error}:</b><br>
 * A {@code Thrown} never wraps an {@link Error}. The canonical constructor
 * rejects one with an {@link IllegalArgumentException}, upholding the same
 * stance as {@link Try#execute()}, which rethrows {@link Error} rather than
 * capturing it: an {@link Error} is unrecoverable and must propagate. The
 * checked reaction methods are additionally bounded to {@link Exception} and so
 * cannot target an {@link Error} in any case.
 *
 * @see Try
 * @see Either
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param thrown The captured throwable; never {@code null}; never {@link Error}.
 */
public record Thrown(Throwable thrown) {

    /**
     * Rejects {@link Error}: a {@code Thrown} models a <em>recoverable</em>
     * failure captured as a value, and an {@link Error} signals an
     * unrecoverable condition that must propagate. This mirrors
     * {@link Try#execute()}, which rethrows {@link Error} rather than capturing
     * it, and guarantees that no {@code Thrown} anywhere ever wraps an
     * {@link Error}.
     *
     * @param thrown the captured throwable; must not be an {@link Error}.
     *
     * @throws NullPointerException If {@code thrown} is {@code null}.
     * @throws IllegalArgumentException If {@code thrown} is an {@link Error}.
     */
    public Thrown {
        Objects.requireNonNull(thrown);
        if (thrown instanceof Error) {
            throw new IllegalArgumentException(
                    "Thrown cannot wrap an Error; Errors are unrecoverable and "
                            + "must propagate: " + thrown
            );
        }
    }

    /**
     * Returns the captured {@link Throwable}. Alias for the generated record
     * accessor {@link #thrown()}.
     *
     * @return The wrapped throwable; never {@code null}.
     */
    public Throwable get() {
        return this.thrown;
    }

    /**
     * Reports whether the captured throwable is an unchecked exception.
     *
     * @return {@code true} if the wrapped throwable is a
     *         {@link RuntimeException}; {@code false} otherwise.
     */
    public boolean isRuntimeException() {
        return this.thrown instanceof RuntimeException;
    }

    /**
     * Reports whether the captured throwable is a checked exception, that is an
     * {@link Exception} that is not a {@link RuntimeException}.
     *
     * @return {@code true} if the wrapped throwable is a checked exception;
     *         {@code false} otherwise.
     */
    public boolean isCheckedException() {
        return this.thrown instanceof Exception
                && !(this.thrown instanceof RuntimeException);
    }

    /**
     * Invokes {@code handler} with the captured throwable when it matches one
     * of the given {@code throwables} types, then returns {@code this} for
     * chaining.
     * <p>
     * Matching is by assignability: the handler fires if the wrapped throwable
     * is an instance of any listed type. Passing <b>no</b> types matches every
     * throwable. This method never throws a checked exception; use
     * {@link #handleChecked(CheckedConsumer, Class)} for a handler that must
     * declare one.
     *
     * @param handler The action to run against the throwable on a match.
     * @param throwables The throwable types to match; empty matches all.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws NullPointerException If {@code handler}, the {@code throwables}
     *         array, or any element of it is {@code null}.
     */
    @SafeVarargs
    public final Thrown handle(
            Consumer<Throwable> handler,
            Class<? extends Throwable>... throwables
    ) {
        Objects.requireNonNull(handler);
        if (matches(throwables)) {
            handler.accept(thrown);
        }
        return this;
    }

    /**
     * Rethrows the captured throwable, wrapped as an unchecked exception, when
     * it matches one of the given {@code throwables} types; otherwise returns
     * {@code this} for chaining.
     * <p>
     * Matching is by assignability, and passing <b>no</b> types matches every
     * throwable. When the wrapped throwable is already a
     * {@link RuntimeException} it is rethrown as is; otherwise it is wrapped
     * in a {@link RuntimeException}, following {@link #getAsUnchecked()}.
     *
     * @param throwables The throwable types to match; empty matches all.
     *
     * @return {@code this}, when the throwable does not match and is not thrown.
     *
     * @throws RuntimeException If the wrapped throwable matches; the original
     *         if it is already unchecked, otherwise a wrapper around it.
     * @throws NullPointerException If the {@code throwables} array or any
     *         element of it is {@code null}.
     */
    @SafeVarargs
    public final Thrown rethrow(
            Class<? extends Throwable>... throwables
    ) {
        if (matches(throwables)) {
            throw getAsUnchecked();
        }
        return this;
    }

    /**
     * Invokes {@code handler} with the captured throwable when it is an
     * instance of {@code exceptionType}, then returns {@code this} for
     * chaining.
     * <p>
     * Unlike {@link #handle(Consumer, Class[])}, the {@code handler} may
     * declare a checked exception {@code X}, which propagates to the caller if
     * the handler throws it.
     *
     * @param <T> The throwable type the handler consumes.
     * @param <X> The checked exception type the handler may throw.
     * @param handler The action to run against the throwable on a match.
     * @param exceptionType The type the wrapped throwable must be an instance
     *        of.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X If the {@code handler} runs and throws it.
     * @throws NullPointerException If {@code handler} or {@code throwableType}
     *         is {@code null}.
     */
    public <T extends Exception, X extends Exception> Thrown handleChecked(
            CheckedConsumer<T, X> handler,
            Class<T> exceptionType
    ) throws X {
        Objects.requireNonNull(handler);
        Objects.requireNonNull(exceptionType);
        if (exceptionType.isInstance(this.thrown)) {
            handler.accept(exceptionType.cast(this.thrown));
        }
        return this;
    }

    /**
     * Rethrows the captured throwable, preserving its original checked type,
     * when it is an instance of {@code exceptionType}; otherwise returns
     * {@code this} for chaining.
     * <p>
     * Unlike {@link #rethrow(Class[])}, the throwable is rethrown as is (cast
     * to {@code X}) rather than wrapped, so its declared checked type
     * propagates to the caller.
     *
     * @param <X> The checked exception type to match and rethrow.
     * @param exceptionType The type the wrapped throwable must be an instance
     *        of.
     *
     * @return {@code this}, when the throwable does not match and is not
     *         thrown.
     *
     * @throws X If the wrapped throwable is an instance of
     *         {@code exceptionType}.
     * @throws NullPointerException If {@code exceptionType} is {@code null}.
     */
    public <X extends Exception> Thrown rethrowChecked(
            Class<X> exceptionType
    ) throws X {
        Objects.requireNonNull(exceptionType);
        if (exceptionType.isInstance(this.thrown)) {
            throw exceptionType.cast(this.thrown);
        }
        return this;
    }

    /**
     * Returns the captured throwable as an unchecked exception, without throwing
     * it.
     *
     * @return The wrapped throwable itself if it is already a
     *         {@link RuntimeException}, otherwise a new {@link RuntimeException}
     *         wrapping it as its cause.
     */
    public RuntimeException getAsUnchecked() {
        return this.thrown instanceof RuntimeException runtime
                ? runtime
                : new RuntimeException(this.thrown);
    }

    /**
     * Rethrows the captured throwable as an unchecked exception, unconditionally.
     *
     * @throws RuntimeException Always; the original if the wrapped throwable is
     *         already unchecked, otherwise a wrapper around it, per
     *         {@link #getAsUnchecked()}.
     */
    public void rethrowUnchecked() {
        throw getAsUnchecked();
    }

    /**
     * Reports whether the wrapped throwable is an instance of the given types.
     * An empty array matches unconditionally.
     *
     * @param throwables The types to test against; empty matches all.
     *
     * @return {@code true} if the wrapped throwable matches; {@code false}
     *         otherwise.
     *
     * @throws NullPointerException If the array or any element is {@code null}.
     */
    private boolean matches(
            Class<? extends Throwable>[] throwables
    ) {
        if (throwables.length == 0) {
            return true;
        }
        for (Class<? extends Throwable> type : throwables) {
            Objects.requireNonNull(type);
            if (type.isInstance(this.thrown)) {
                return true;
            }
        }
        return false;
    }
}
