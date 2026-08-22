package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;

import java.util.Objects;

import static io.github.igorgf.control.ControlUtils.requireNonNull;


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
 *       passed to its methods is rejected when {@code null} with some
 *       {@link ContractViolationException}.
 *   </li>
 *   <li>
 *       Is <b>fluent</b>: the inspection methods:
 *       {@link #handle(CheckedConsumer, Class[])},
 *       {@link #handle(CheckedConsumer, Class)}, {@link #rethrow(Class[])}, and
 *       {@link #rethrow(Class)}, return {@code this} when they do not throw, so
 *       reactions to distinct throwable types can be chained.
 *   </li>
 * </ul>
 * <p>
 * <b>Relationship to {@link Error}:</b><br>
 * A {@code Thrown} never wraps an {@link Error}. The canonical constructor
 * rejects one with an {@link IllegalArgumentException}, upholding the same
 * stance as {@link Try#execute()}, which rethrows {@link Error} rather than
 * capturing it: an {@link Error} is unrecoverable and must propagate.
 *
 * @see Try
 * @see Either
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param captured The captured throwable, never {@code null} or {@link Error}.
 */
public record Thrown(Throwable captured) {

    /**
     * Rejects {@link Error}: a {@code Thrown} models a <em>recoverable</em>
     * failure captured as a value, and an {@link Error} signals an
     * unrecoverable condition that must propagate. This mirrors
     * {@link Try#execute()}, which rethrows {@link Error} rather than capturing
     * it, and guarantees that no {@code Thrown} anywhere ever wraps an
     * {@link Error}.
     *
     * @param captured the captured throwable; must not be an {@link Error}.
     *
     * @throws NullArgumentException If {@code thrown} is {@code null}.
     * @throws IllegalArgumentException If {@code thrown} is an {@link Error}.
     */
    public Thrown {
        requireNonNull(captured, "captured");
        if (captured instanceof Error) {
            throw new IllegalArgumentException(
                    "Thrown cannot wrap an Error. Errors are unrecoverable and "
                            + "must propagate: " + captured
            );
        }
    }

    /**
     * Returns the captured {@link Throwable}. Alias for the generated record
     * accessor {@link #captured()}.
     *
     * @return The wrapped throwable; never {@code null}.
     */
    public Throwable get() {
        return this.captured;
    }

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

    /**
     * Invokes {@code handler} with the {@link #captured()} throwable when it
     * matches one of the given {@code throwables} types, then returns
     * {@code this} for chaining.
     * <p>
     * Matching is by class type, the handler fires when {@link #captured()} is
     * the same class as one of the {@code throwables}. Passing <b>no</b> types
     * matches every throwable.
     * <p>
     * This uses a generic {@link Throwable} handler, to bind the handler to a
     * concrete type use {@link #handle(CheckedConsumer, Class)}.
     *
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param throwables The throwable types to match, empty always matches.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code handler}, the {@code throwables}
     *         array, or any element of it is {@code null}.
     */
    @SafeVarargs
    public final <X extends Throwable> Thrown handle(
            CheckedConsumer<Throwable, X> handler,
            Class<? extends Throwable>... throwables
    ) throws X, NullArgumentException {
        requireNonNull(handler, "handler");
        requireNonNull(throwables, "throwables");
        if (throwables.length == 0 || matches(throwables)) {
            handler.accept(captured);
        }
        return this;
    }

    /**
     * Invokes {@code handler} with the {@link #captured()} throwable when it is
     * a class of {@code exceptionType}, then returns {@code this} for chaining.
     * <p>
     * Unlike {@link #handle(CheckedConsumer, Class[])}, the {@code handler}
     * is bound to a specific exception type {@code T}.
     *
     * @param <T> The throwable type the {@code handler} consumes.
     * @param <X> The checked exception type the {@code handler} may throw.
     * @param handler The action to run against the {@link #captured()} on a
     *        match.
     * @param exceptionType The type the wrapped throwable must be an instance
     *        of.
     *
     * @return {@code this}, to allow chaining further reactions.
     *
     * @throws X If the {@code handler} runs and throws it.
     * @throws NullArgumentException If {@code handler} or {@code exceptionType}
     *         is {@code null}.
     */
    public <T extends Throwable, X extends Throwable> Thrown handle(
            CheckedConsumer<T, X> handler,
            Class<T> exceptionType
    ) throws X, NullArgumentException {
        requireNonNull(handler, "handler");
        requireNonNull(exceptionType, "exceptionType");
        if (exceptionType.equals(this.captured.getClass())) {
            handler.accept(exceptionType.cast(this.captured));
        }
        return this;
    }

    /**
     * Rethrows the {@link #captured()} throwable as an unchecked exception,
     * when it matches one of the given {@code throwables} types, or when no
     * arguments provided. Otherwise, returns {@code this} for chaining.
     * <p>
     * Matching is by assignability, and passing <b>no</b> types matches every
     * throwable.
     *
     * @param throwables The throwable types to match, empty matches all.
     *
     * @return {@code this}, when the throwable does not match or no types
     *         passed.
     *
     * @throws NullArgumentException If the {@code throwables} array or any
     *         element of it is {@code null}.
     */
    @SafeVarargs
    public final Thrown rethrow(
            Class<? extends Throwable>... throwables
    ) throws NullArgumentException {
        requireNonNull(throwables, "throwables");
        if (throwables.length == 0 || matches(throwables)) {
            rethrowGeneric(this.captured);
        }
        return this;
    }

    /**
     * Rethrows the captured throwable, preserving its original checked type,
     * when it is an instance of {@code throwable}; otherwise returns
     * {@code this} for chaining.
     * <p>
     * Unlike {@link #rethrow(Class[])}, the throwable is rethrown as is (cast
     * to {@code X}) rather than wrapped, so its declared checked type
     * propagates to the caller.
     *
     * @param <X> The checked exception type to match and rethrow.
     * @param throwable The type the wrapped throwable must be an instance
     *        of.
     *
     * @return {@code this}, when the throwable does not match and is not
     *         thrown.
     *
     * @throws X If the wrapped throwable is an instance of
     *         {@code throwable}.
     * @throws NullArgumentException If {@code throwable} is {@code null}.
     */
    public <X extends Throwable> Thrown rethrow(
            Class<X> throwable
    ) throws X, NullArgumentException {
        requireNonNull(throwable, "throwable");
        if (throwable.equals(this.captured.getClass())) {
            throw throwable.cast(this.captured);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrowGeneric(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Reports whether the wrapped throwable is an instance of the given types.
     *
     * @param throwables The types to test against.
     *
     * @return {@code true} if the wrapped throwable matches; {@code false}
     *         otherwise.
     *
     * @throws NullArgumentException If the array or any element is {@code null}.
     */
    private boolean matches(
            Class<? extends Throwable>[] throwables
    ) {
        var i = 0;
        for (Class<? extends Throwable> type : throwables) {
            requireNonNull(type, "throwables[" + i + "]" );
            if (type.equals(this.captured.getClass())) {
                return true;
            }
            ++i;
        }
        return false;
    }
}
