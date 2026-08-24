package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedRunnable;
import io.github.igorgf.function.CheckedSupplier;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * A sum type representing the presence or absence of a value, as a null-safe
 * alternative to {@link java.util.Optional}. Unlike {@link java.util.Optional}, 
 * {@code Option} is a sealed type with explicit {@link Present} and 
 * {@link Empty} cases, it propagates checked exceptions from mapping functions, 
 * and does not accept {@code null} values.
 * <p>
 * <b>{@code Option} features:</b>
 * <ul>
 *   <li>
 *       Is <b>null safe</b>: {@link Present} can never contain a null value,
 *       {@code Option} exposes only one {@code null} accepting entry
 *       {@link #ofNullable(Object)}, and one exit
 *       {@link #orElseNullable(Object)}, for interoperation with null-returning
 *       Java APIs. The entire API otherwise rejects {@code null} at every
 *       boundary: constructors, mapping functions, and suppliers all throw
 *       appropriate {@link ContractViolationException}s on {@code null}.
 *   </li>
 *   <li>
 *       A <b>functor</b>: {@link #map} transforms the contained value.
 *   </li>
 *   <li>
 *       A <b>monad</b>: {@link #flatMap} chains operations that may not
 *       produce a value, short-circuiting on {@link Empty}.
 *   </li>
 *   <li>
 *       A <b>catamorphism</b>: {@link #fold} collapses both possible states
 *       ({@link Present} and {@link Empty}) into a single value.
 *   </li>
 *   <li>
 *       Is <b>exception fluent:</b> Operations use the checked function aware
 *       functional interfaces from {@link io.github.igorgf.function}, to ensure
 *       checked exception propagation support.
 *   </li>
 * </ul>
 * <p>
 * <b>Alternative Types:</b><br>
 * For sequential error-handling with short-circuiting, use {@link Either}. <br>
 * For accumulating multiple validation errors, use {@link Validation}.
 *
 * @see Present
 * @see Empty
 * @see io.github.igorgf.function
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> the contained value type
 */
public sealed interface Option<T> permits Present, Empty {

    /**
     * Factory method for creating a new {@code Present<T>} instance.
     *
     * @see Present
     *
     * @param <T> The type of the object being contained.
     * @param value the object to be contained.
     *
     * @return A new {@code Present<T>} instance.
     *
     * @throws NullArgumentException If {@code value} is {@code null}.
     */
    static <T> Option<T> of(T value) throws NullArgumentException {
        return new Present<>(value);
    }

    /**
     * Factory method for creating a new {@code Present<T>} instance. Supporting
     * {@code null} values being supplied. A null {@code value} will instead
     * return a new {@code Empty<T>}, containing the type information but no
     * {@code value}.
     *
     * @see Present
     * @see Empty
     *
     * @param <T> The type of the object being contained.
     * @param value The object to be contained {@code nullable}.
     *
     * @return A new {@code Present<T>} or {@code Empty<T>} instance.
     */
    static <T> Option<T> ofNullable(T value) {
        return value == null ? new Empty<>() : new Present<>(value);
    }

    /**
     * Factory method for creating a new {@code Empty<T>} instance, containing
     * the type information but no value.
     * <p>
     * Due to type erasure, {@link Empty} instances carry no runtime type
     * information. Two {@link Empty} instances are equal by
     * {@link Empty#equals(Object)} (as records with no components) regardless
     * of their type parameter. Therefore:
     * <pre>{@code
     *     var oString = Option.<String>empty();
     *     var oInt = Option.<Integer>empty();
     *     //noinspection EqualsBetweenInconvertibleTypes
     *     assert oString.equals(oInt);
     * }</pre>
     * <p>
     * Use {@link #isEmpty()} or {@link #isPresent()} for emptiness checks.
     * <p>
     * Do not use {@code ==} for comparison, as each {@link Empty} is a distinct
     * object instance.
     *
     * @see Empty
     *
     * @param <T> The missing type being represented.
     *
     * @return A new {@code Empty<T>} instance.
     */
    static <T> Option<T> empty() {
        return new Empty<>();
    }

    /**
     * The <b>functor</b> operation of {@link Option}. Transforms the contained
     * value using the provided mapping function.
     * <p>
     * When {@code this} is a {@code Present<T>} then the mapping is applied to
     * the contained value, and contained within a new {@code Present<U>},
     * allowing for convenient chaining. Otherwise, this method returns a new
     * {@code Empty<U>}.
     * <p>
     * When the mapping has a chance of returning a {@code null} use
     * {@link #flatMap(CheckedFunction)} instead, to return a {@code Empty<U>}
     * for {@code null} result cases. If the mapping returns a {@code null}
     * this method throws a {@link NullResultException}.
     * <p>
     * The {@code mapper} doesn't have to be concerned with handling
     * {@code null} values as {@link Present#value()} is never {@code null}.
     * <p>
     * Checked exceptions thrown by {@code mapper} are propagated, see
     * {@link Option} class documentation for details.
     *
     * @see Present#map(CheckedFunction)
     * @see Empty#map(CheckedFunction)
     *
     * @param <U> The type being contained after mapping.
     * @param mapper The mapping function to apply to the contained value, if
     *               present.
     *
     * @return {@code Present<U>} if {@code this} is a {@link Present}. New
     *         {@code Empty<U>} if {@code this} is a {@link Empty}.
     *
     * @throws X If the {@code mapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code mapper} is a {@code null}.
     * @throws NullResultException If {@code mapper} returns a {@code null}.
     */
    <U, X extends Throwable> Option<U> map(
            CheckedFunction<? super T, ? extends U, ? extends X> mapper
    ) throws X, NullArgumentException, NullResultException;

    /**
     * The <b>monad</b> operation of {@link Option}. Transforms the contained
     * value using the provided mapping function.
     * <p>
     * This method is similar to {@link #map(CheckedFunction)}, but the mapping
     * function is one whose result is already an {@code Option<? extends U>},
     * and if invoked, {@code flatMap} does not wrap it within an additional
     * {@code Option}.
     * <p>
     * If the mapping returns a {@code null} then this method throws a
     * {@link NullResultException}. The mapper must always return a
     * {@code Option<? extends U>}.
     * <p>
     * The {@code mapper} doesn't have to be concerned with handling
     * {@code null} values as {@link Present#value()} is never {@code null}.
     * <p>
     * Checked exceptions thrown by {@code mapper} are propagated, see
     * {@link Option} class documentation for details.
     *
     * @see Option#map(CheckedFunction)
     * @see Present#flatMap(CheckedFunction)
     * @see Empty#flatMap(CheckedFunction)
     *
     * @param <U> The type being contained after mapping.
     * @param mapper The mapping function to apply to the contained value, if
     *        present.
     *
     * @return {@code Option<U>} if {@code this} is a {@link Present}. New
     *         {@code Empty<U>} if {@code this} is a {@link Empty}.
     *
     * @throws X If the {@code mapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code mapper} is a {@code null}.
     * @throws NullResultException If {@code mapper} returns a {@code null}.
     */
    <U, X extends Throwable> Option<U> flatMap(
            CheckedFunction<? super T, ? extends Option<? extends U>, ? extends X> mapper
    ) throws X, NullArgumentException, NullResultException;

    /**
     * The <b>catamorphism</b> operation of {@link Option}. Transforms and
     * extracts the contained value using the provided mapping function when
     * {@code this} is a {@link Present}. Supplies a value when {@code this} is
     * a {@link Empty}.
     * <p>
     * If the mapper or the supplier return a {@code null} then this method
     * throws a {@link NullResultException}. The mapper and supplier must
     * always return a {@code U}. When the mapping has a chance of returning
     * a {@code null} use {@link #flatMap(CheckedFunction)} instead, to return
     * a {@code Option<U>}, to then handle the result using
     * {@link Option#orElseGet(CheckedSupplier)}.
     * <p>
     * The {@code presentMapper} doesn't have to be concerned with handling
     * {@code null} values as {@link Present#value()} is never {@code null}.
     * <p>
     * Checked exceptions thrown by {@code presentMapper} or
     * {@code emptySupplier} are propagated, see {@link Option} class
     * documentation for details.
     *
     * @see Present#fold(CheckedFunction, CheckedSupplier)
     * @see Empty#fold(CheckedFunction, CheckedSupplier)
     *
     * @param <U> The type being contained after mapping.
     * @param presentMapper The mapping function to apply to the contained
     *        value, if {@code this} is a {@link Present}.
     * @param emptySupplier The supplying function to provide a replacement
     *        value, if {@code this} is a {@link Empty}.
     *
     * @return {@code U} from the {@code presentMapper} if {@code this} is a
     *         {@link Present}. {@code U} from the {@code emptySupplier} if
     *         {@code this} is a {@link Empty}.
     *
     * @throws X1 If the {@code presentMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws X2 If the {@code emptySupplier} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code presentMapper} or
     *         {@code emptySupplier} is a {@code null}.
     * @throws NullResultException If {@code presentMapper} or
     *         {@code emptySupplier} return a {@code null}.
     */
    <U, X1 extends Throwable, X2 extends Throwable> U fold(
            CheckedFunction<? super T, ? extends U, ? extends X1> presentMapper,
            CheckedSupplier<? extends U, ? extends X2> emptySupplier
    ) throws X1, X2, NullArgumentException, NullResultException;

    /**
     * Is {@code this} a {@link Present}.
     *
     * @see #isEmpty()
     *
     * @return {@code true} if {@code this} is a {@link Present}, otherwise
     *         {@code false}
     */
    boolean isPresent();

    /**
     * Is {@code this} a {@link Empty}.
     *
     * @see #isPresent()
     *
     * @return {@code true} if {@code this} is a {@link Empty}, otherwise
     *         {@code false}
     */
    boolean isEmpty();

    /**
     * If {@code this} is a {@link Present}, performs the given action with the
     * value, otherwise does nothing.
     * <p>
     * The {@code action} doesn't have to be concerned with handling
     * {@code null} values as {@link Present#value()} is never {@code null}.
     * <p>
     * Checked exceptions thrown by {@code action} are propagated, see
     * {@link Option} class documentation for details.
     *
     * @param action the action to be performed, if {@code this} is a
     *        {@link Present}
     *
     * @throws X If the {@code action} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code action} is {@code null}.
     */
    <X extends Throwable> void ifPresent(
            CheckedConsumer<? super T, ? extends X> action
    ) throws X, NullArgumentException;

    /**
     * If {@code this} is a {@link Empty}, performs the given action, otherwise
     * does nothing.
     * <p>
     * Checked exceptions thrown by {@code emptyAction} are propagated, see
     * {@link Option} class documentation for details.
     *
     * @param emptyAction the action to be performed, if {@code this} is a
     *        {@link Empty}
     *
     * @throws X If the {@code emptyAction} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code emptyAction} is {@code null}.
     */
    <X extends Throwable> void ifEmpty(
            CheckedRunnable<? extends X> emptyAction
    ) throws X, NullArgumentException;

    /**
     * If {@code this} is a {@link Present}, performs the given action with
     * value, otherwise performs the empty case action.
     * <p>
     * The {@code action} doesn't have to be concerned with handling
     * {@code null} values as {@link Present#value()} is never {@code null}.
     * <p>
     * Checked exceptions thrown by {@code action} or {@code emptyAction} are
     * propagated, see {@link Option} class documentation for details.
     *
     * @param action the action to be performed, if {@code this} is a
     *        {@link Present}
     * @param emptyAction the action to be performed, if {@code this} is a
     *        {@link Empty}
     *
     * @throws X If the {@code action} or {@code emptyAction} throw a checked
     *         exception, it is propagated to the caller.
     * @throws NullArgumentException If {@code action} or {@code emptyAction} are
     *         {@code null} during the lazy evaluation.
     */
    <X extends Throwable> void ifPresentOrElse(
            CheckedConsumer<? super T, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X, NullArgumentException;

    /**
     * Facilitates conditional short-circuiting. If {@code this} is a
     * {@link Present}, and the value matches the given predicate, returns a
     * {@code this}, otherwise returns a new {@code Empty<T>}.
     * <p>
     * The {@code predicate} doesn't have to be concerned with handling
     * {@code null} values as {@link Present#value()} is never {@code null}.
     *
     * @param predicate The predicate to apply to a value, if {@code this} is a
     *        {@link Present}
     *
     * @return {@code Present<T>} if {@code this} is a {@link Present}, and the
     *         value matches the given predicate. Otherwise {@code Empty<T>}.
     *
     * @throws NullArgumentException If the {@code predicate} is {@code null}.
     */
    Option<T> filter(Predicate<? super T> predicate);

    /**
     * If {@code this} is a {@link Present}, returns {@code this} describing the
     * value, otherwise uses the {@code supplier} to return a new
     * {@code Option<T>}.
     * <p>
     * Checked exceptions thrown by {@code supplier} are propagated, see
     * {@link Option} class documentation for details.
     * <p>
     * The unchecked cast from {@code Option<? extends T>} to {@code Option<T>}
     * is provably safe because {@code Option} is covariant in its type
     * parameter, it only produces values of {@code T}, never consumes them.
     *
     * @param supplier Used to provide the replacement {@link Option} when
     *        {@code this} is a {@link Empty}.
     *
     * @return {@code Present<T>} if {@code this} is a {@link Present}.
     *         Otherwise {@code Option<T>} provided by the {@code supplier}.
     *
     * @throws X If the {@code supplier} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If the {@code supplier} is {@code null}
     *         during lazy evaluation.
     * @throws NullResultException If {@code supplier} returns a {@code null}.
     */
    @SuppressWarnings("unchecked")
    default <X extends Throwable> Option<T> or(
            CheckedSupplier<? extends Option<? extends T>, ? extends X> supplier
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(supplier, "supplier");
        return isPresent() ? this : (Option<T>) requireNonNullResult(supplier, "supplier");
    }

    /**
     * If {@code this} is a {@link Present} returns contained value. Otherwise,
     * returns {@code other}.
     * <p>
     * Never returns {@code null}, as {@link Present#value()} and {@code other}
     * are never {@code null}. For {@code null} result support use
     * {@link #orElseNullable(Object)}.
     *
     * @see #orElseNullable(Object)
     *
     * @param other A non-null {@code T} to be returned if {@code this} is a
     *        {@link Empty}.
     *
     * @return Value {@code T} when {@code this} is a {@link Present}.
     *         {@code other} when {@code this} is a {@link Empty}.
     *
     * @throws NullArgumentException If {@code this} is a {@link Empty} and
     *         {@code other} is {@code null}.
     */
    T orElse(T other) throws NullArgumentException;

    /**
     * If {@code this} is a {@link Present} returns contained value. Otherwise,
     * returns {@code other}, which may be {@code null}.
     * <p>
     * This is the sole nullable exit point in the {@link Option} API. The
     * entire API otherwise rejects {@code null} at every boundary:
     * constructors, mapping functions, and suppliers all throw some
     * {@link ContractViolationException} on {@code null}. This method, alongside
     * {@link #ofNullable(Object)} as its nullable entry counterpart, forms the
     * deliberate and exclusive pair of escape hatches for interoperation with
     * null-returning Java APIs. Prefer {@link #orElse(Object)} in all other
     * cases.
     *
     * @see #ofNullable(Object)
     * @see #orElse(Object)
     *
     * @param other {@code T} to be returned if {@code this} is a
     *        {@link Empty}, may be {@code null}.
     *
     * @return Value {@code T} when {@code this} is a {@link Present}.
     *         {@code other} when {@code this} is a {@link Empty}.
     */
    T orElseNullable(T other);

    /**
     * If {@code this} is a {@link Present}, returns contained value. Otherwise,
     * uses the {@code supplier} to return an instance of {@code T}.
     * <p>
     * Never returns {@code null}, as {@link Present#value()} and
     * {@code supplier} return values are never {@code null}.
     * <p>
     * Checked exceptions thrown by {@code supplier} are propagated, see
     * {@link Option} class documentation for details.
     *
     * @param supplier Used to provide the replacement {@code T} when
     *        {@code this} is a {@link Empty}.
     *
     * @return Contained value {@code T} if {@code this} is a {@link Present}.
     *         Otherwise {@code T} provided by the {@code supplier}.
     *
     * @throws X If the {@code supplier} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If the {@code supplier} is {@code null}
     *         during lazy evaluation.
     * @throws NullResultException If {@code supplier} returns a {@code null}.
     */
    <X extends Throwable> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) throws X, NullArgumentException, NullResultException;

    /**
     * If {@code this} is a {@link Present}, returns contained value. Otherwise,
     * uses the {@code supplier} to get an exception, then throws it.
     * <p>
     * Never returns {@code null}, as {@link Present#value()} is never
     * {@code null}.
     * <p>
     * Checked exceptions thrown by {@code exceptionSupplier} are propagated,
     * see {@link Option} class documentation for details.
     *
     * @param exceptionSupplier Used to provide the exception to throw when
     *        {@code this} is a {@link Empty}.
     *
     * @return Contained value {@code T} if {@code this} is a {@link Present}.
     *
     * @throws X If {@code this} is a {@link Empty}.
     * @throws NullArgumentException If the {@code supplier} is {@code null}
     *         during lazy evaluation.
     * @throws NullResultException If {@code supplier} returns a {@code null}.
     */
    <X extends Throwable> T orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) throws X, NullArgumentException, NullResultException;

    /**
     * If {@code this} is a {@link Present}, returns contained value. Otherwise,
     * throws a {@link EmptyResultException}.
     * <p>
     * Never returns {@code null}, as {@link Present#value()} and {@code other}
     * are never {@code null}.
     *
     * @return Contained value {@code T} if {@code this} is a {@link Present}.
     *
     * @throws EmptyResultException If {@code this} is a {@link Empty}.
     */
    T orThrow() throws EmptyResultException;

}