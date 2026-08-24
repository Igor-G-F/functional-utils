package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.github.igorgf.control.ControlUtils.*;

/**
 * A disjoint union (sum type) representing one of three distinct states:
 * <ul>
 *     <li>
 *         {@code Valid<T>}: A validated value.
 *     </li>
 *     <li>
 *         {@code Accumulated<E>}: A {@code List<E>} of accumulated validation
 *         errors.
 *     </li>
 *     <li>
 *         {@code Critical<E>}: A single major error that short circuits the
 *         validation process.
 *     </li>
 * </ul>
 * <b>{@code Validation} features:</b>
 * <ul>
 *   <li>
 *       Is <b>null safe</b>: {@link Valid} or {@link Invalid} can never contain
 *       a null value, {@code Validation} does not expose any null value entry
 *       or exit. The entire API rejects {@code null} at every boundary:
 *       constructors, mapping functions, and suppliers all throw appropriate
 *       {@link ContractViolationException}s on {@code null}.
 *   </li>
 *   <li>
 *       An <b>applicative functor</b>: {@link #combine(Validation)} joins
 *       multiple independent validations and <em>accumulates all errors</em>.
 *   </li>
 *   <li>
 *       A <b>monad (right biased)</b>: {@link #then(CheckedFunction)}
 *       transforms the contained target into a new {@code Validation} when
 *       this is {@link Valid}.
 *   </li>
 *   <li>
 *       A <b>functor (right biased)</b>: {@link #mapTarget(CheckedFunction)}
 *       transforms the contained target when this is {@link Valid}.
 *   </li>
 *   <li>
 *       Is <b>exception fluent:</b> Operations use the checked function aware
 *       functional interfaces from {@link io.github.igorgf.function}, to ensure
 *       checked exception propagation support.
 *   </li>
 * </ul>
 * <p>
 * <b>Alternative Types:</b><br>
 * For sequential, non-accumulative error handling, use {@link Either}. <br>
 * For single value presence or absence handling, use {@link Option}.
 *
 * @see Valid
 * @see Invalid
 * @see Accumulated
 * @see Critical
 * @see io.github.igorgf.function
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <E> the error element/container type
 * @param <T> the validated type
 */
public sealed interface Validation<E, T> permits Valid, Invalid {

    /**
     * Factory method for creating a new {@code Valid<E, T>} instance.
     * Containing the validation target {@code T}.
     *
     * @see Valid
     *
     * @param <T> The type of the object being validated.
     * @param <E> The error type reference.
     * @param target The validation target.
     *
     * @return A new {@code Valid<E, T>} instance.
     *
     * @throws NullArgumentException If {@code value} is {@code null}.
     */
    static <E, T> Validation<E, T> valid(T target) throws NullArgumentException { 
        return new Valid<>(target); 
    }

    /**
     * Factory method for creating a new {@code Accumulated<E, T>} instance.
     * Containing the validation error {@code E}.
     *
     * @see Accumulated
     *
     * @param <T> The type of the object being validated.
     * @param <E> The error type reference.
     * @param error The validation error information.
     *
     * @return A new {@code Accumulated<E, T>} instance.
     *
     * @throws NullArgumentException If {@code error} is {@code null}.
     */
    static <E, T> Validation<E, T> accumulated(E error) throws NullArgumentException {
        requireNonNull(error, "error");
        return new Accumulated<>(List.of(error));
    }

    /**
     * Factory method for creating a new {@code Accumulated<E, T>} instance.
     * Containing the validation errors {@code List<E>}.
     * <p>
     * {@link EmptyArgumentException} is thrown when the provided {@code errors} is
     * empty. A {@link Accumulated#errors()} can never contain null or empty values.
     * A checked exception is an intentional design choice to force the user
     * handle empty cases at call point.
     *
     * @see Accumulated
     *
     * @param <T> The type of the object being validated.
     * @param <E> The error type reference.
     * @param errors The validation errors information.
     *
     * @return A new {@code Accumulated<E, T>} instance.
     *
     * @throws EmptyArgumentException If {@code errors} is empty.
     * @throws NullArgumentException If {@code errors} is {@code null}.
     */
    static <E, T> Validation<E, T> accumulated(List<E> errors) throws NullArgumentException, EmptyArgumentException {
        return new Accumulated<>(errors);
    }

    /**
     * Factory method for creating a new {@code Critical<E, T>} instance.
     * Containing the critical validation error {@code E}.
     * <p>
     * {@link Critical} is a short circuit that bypasses any further error
     * accumulation.
     *
     * @see Critical
     *
     * @param <T> The type of the object being validated.
     * @param <E> The error type reference.
     * @param error The validation error information.
     *
     * @return A new {@code Critical<E, T>} instance.
     *
     * @throws NullArgumentException If {@code error} is {@code null}.
     */
    static <E, T> Validation<E, T> critical(E error) throws NullArgumentException {
        return new Critical<>(error);
    }

    /**
     * A convenience factory method for creating a {@code Validation<E, T>}
     * based on the result of {@code value} being tested against
     * {@code predicate}.
     *
     * @see Valid
     * @see Accumulated
     * @see Critical
     *
     * @param <T> The type of the object being validated.
     * @param <E> The error type reference.
     * @param value The value to be validated.
     * @param predicate The condition tested against {@code value}.
     * @param errorMapper Error provider should the result be {@link Invalid}.
     * @param critical If {@code true} then an {@link Invalid} result will be of
     *        type {@code Critical<E, T>}. If {@code false} the result type will
     *        be {@code Accumulated<E, T>}.
     *
     * @return {@code Valid<E, T>}, containing {@code value}, when
     *         {@code predicate} returns {@code true}. <br>
     *         {@code Accumulated<E, T>}, containing result of
     *         {@code errorMapper}, when {@code predicate} returns {@code false}
     *         and {@code critical} is {@code false}. <br>
     *         {@code Critical<E, T>}, containing result of {@code errorMapper},
     *         when {@code predicate} returns {@code false} and {@code critical}
     *         is {@code true}.
     *         
     * @throws NullArgumentException If any of the supplied method arguments are
     *         a {@code null}.
     */
    static <E, T> Validation<E, T> validate(
            T value,
            Predicate<? super T> predicate,
            Function<? super T, ? extends E> errorMapper,
            boolean critical
    ) throws NullArgumentException {
        requireNonNull(value, "value");
        requireNonNull(predicate, "predicate");
        if (predicate.test(value)) {
            return valid(value);
        }

        requireNonNull(errorMapper, "errorMapper");
        var error = errorMapper.apply(value);
        requireNonNull(critical, "critical");
        return critical ? critical(error) : accumulated(error);
    }

    /**
     * A convenience factory method overload for
     * {@link #validate(Object, Predicate, Function, boolean)}.
     * <p>
     * This method always assumes that any error is {@link Accumulated}.
     *
     * @see Valid
     * @see Accumulated
     * @see #validate(Object, Predicate, Function, boolean)
     *
     * @param <T> The type of the object being validated.
     * @param <E> The error type reference.
     * @param value The value to be validated.
     * @param predicate The condition tested against {@code value}.
     * @param errorMapper Error provider should the result be
     *        {@link Accumulated}.
     *
     * @return {@code Valid<E, T>}, containing {@code value}, when
     *         {@code predicate} returns {@code true}. <br>
     *         {@code Accumulated<E, T>}, containing result of
     *         {@code errorMapper}, when {@code predicate} returns
     *         {@code false}.
     *
     * @throws NullArgumentException If any of the supplied method arguments are
     *         a {@code null}.
     */
    static <E, T> Validation<E, T> validate(
            T value,
            Predicate<? super T> predicate,
            Function<? super T, ? extends E> errorMapper
    ) {
        return validate(value, predicate, errorMapper, false);
    }

    /**
     * The <b>applicative functor</b> operation of {@link Validation}. Combines
     * {@code this} with some {@code other} validation {@code Validation<E, S>}.
     * <p>
     * Scenarios:
     * <ul>
     *     <li>
     *         When {@code this} is a {@link Valid} and {@code other} is
     *         {@link Valid}, uses the {@code combiner} to map {@code this}
     *         contained target and the {@code other} contained target,
     *         producing a new {@code Valid<E, U>}.
     *     </li>
     *     <li>
     *         When {@code this} is a {@link Valid} and {@code other} is some
     *         {@link Invalid}, returns {@code other} as
     *         {@code Validation<E, U>}. Preserving the contained errors, and
     *         binding to the new target type {@code U}.
     *     </li>
     *     <li>
     *         When {@code this} is a {@link Accumulated} and {@code other} is
     *         {@link Valid}, returns {@code this} as {@code Validation<E, U>},
     *         binding to the new target type {@code U}.
     *     </li>
     *     <li>
     *         When {@code this} is a {@link Accumulated} and {@code other} is
     *         {@link Accumulated}, combines errors from {@code this} and
     *         {@code other} into a new {@code Accumulated<E, U>}. Preserving
     *         the contained errors, and binding to the new target type
     *         {@code U}.
     *     </li>
     *     <li>
     *        When {@code this} is a {@link Accumulated} and {@code other} is
     *        {@link Critical}, returns {@code other} as
     *        {@code Validation<E, U>}. Preserving the contained errors, and
     *        binding to the new target type {@code U}.
     *     </li>
     *     <li>
     *        When {@code this} is a {@link Critical}, ignores {@code other},
     *        returns {@code this} as {@code Validation<E, U>}. Preserving the
     *        contained error, and binding to the new target type {@code U}.
     *     </li>
     * </ul>
     * <p>
     * The {@code combiner} doesn't have to be concerned with handling
     * {@code null} contained values as {@link Valid#target()} can never be
     * {@code null}.
     * <p>
     * Checked exceptions thrown by {@code combiner} are propagated, see
     * {@link Validation} class documentation for details.
     *
     * @see Valid#combine(Validation, CheckedBiFunction)
     * @see Invalid#combine(Validation, CheckedBiFunction)
     *
     * @param <S> The type of object validated by {@code other}.
     * @param <U> The target type of the returned {@link Validation}.
     * @param other {@link Validation} to combine with {@code this}.
     * @param combiner Used to map {@code this} contained target and the
     *        {@code other} contained target, when both are {@link Valid}.
     *
     * @return {@code Valid<E, U>}, {@code Accumulated<E, U>}, or
     *         {@code Critical<E, U>}. See scenarios above for details.
     *
     * @throws X If the {@code combiner} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code other} or {@code combiner} is a 
     *         {@code null}.
     * @throws NullResultException If {@code combiner} returns a {@code null}.
     */
    <S, U, X extends Throwable> Validation<E, U> combine(
            Validation<E, S> other,
            CheckedBiFunction<? super T, ? super S, ? extends U, ? extends X> combiner
    ) throws X, NullArgumentException, NullResultException;

    /**
     * The <b>applicative functor</b> operation of {@link Validation}. Similar
     * to {@link #combine(Validation, CheckedBiFunction)}.
     * <p>
     * Unlike {@link #combine(Validation, CheckedBiFunction)}, this method
     * preserves the contained target {@code T} in {@code this}, when
     * {@code this} and {@code other} are both {@link Valid}.
     *
     * @see Valid#combine(Validation)
     * @see Invalid#combine(Validation)
     * @see #combine(Validation, CheckedBiFunction)
     *
     * @param <S> The type of object validated by {@code other}.
     * @param other {@link Validation} to combine with {@code this}.
     *
     * @return {@code Valid<E, T>}, {@code Accumulated<E, T>}, or
     *         {@code Critical<E, T>}. See description above for details.
     *
     * @throws NullArgumentException If {@code other} is {@code null}.
     */
    <S> Validation<E, T> combine(
            Validation<E, S> other
    ) throws  NullArgumentException;

    /**
     * The <b>monad (right biased)</b> operation of {@link Validation}.
     * {@code validator} is only executed when {@code this} is {@link Valid},
     * allowing for lazy evaluation of dependent validations.
     * <p>
     * The {@code validator} doesn't have to be concerned with handling
     * {@code null} contained values as {@link Valid#target()} can never be
     * {@code null}.
     * <p>
     * Checked exceptions thrown by {@code validator} are propagated, see
     * {@link Validation} class documentation for details.
     *
     * @see Valid#then(CheckedFunction)
     * @see Invalid#then(CheckedFunction)
     *
     * @param <S> The target type of the returned {@link Validation}.
     * @param validator Used to supply a {@code Validation<E, S>} when
     *        {@code this} is {@link Valid}.
     *
     * @return {@code Validation<E, S>} produced by {@code validator} when
     *         {@code this} is {@link Valid}. Otherwise, returns {@code this} as
     *         {@code Validation<E, S>}, preserving errors and binding to the
     *         new target type {@code S}
     *
     * @throws X If the {@code validator} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code validator} is a {@code null}. 
     * @throws NullResultException If {@code validator} returns a {@code null}. 
     */
    <S, X extends Throwable> Validation<E, S> then(
            CheckedFunction<? super T, ? extends Validation<E, S>, ? extends X> validator
    ) throws X, NullArgumentException, NullResultException;

    /**
     * Allows item by item conversion of any contained errors from type
     * {@code E} to type {@code U}, when {@code this} is {@link Invalid}. When
     * {@code this} is {@link Valid} the conversion is not applied as there are
     * no errors, but the new error type binding {@code U} is preserved.
     * <p>
     * The {@code errorMapper} doesn't have to be concerned with handling
     * {@code null} or empty errors as {@link Accumulated#errors()} and
     * {@link Critical#error()} can never be {@code null} or empty.
     * <p>
     * Checked exceptions thrown by {@code errorMapper} are propagated, see
     * {@link Validation} class documentation for details.
     *
     * @see Valid#mapError(CheckedFunction)
     * @see Accumulated#mapError(CheckedFunction)
     * @see Critical#mapError(CheckedFunction)
     *
     * @param <U> The new error type.
     * @param errorMapper Used to map errors from type {@code E} to type
     *        {@code U}.
     *
     * @return {@code Valid<U, T>} when {@code this} is {@link Valid},
     *         preserving the contained target {@code T} and the new error type
     *         binding {@code U}. <br>
     *         New {@code Invalid<U, T>} when {@code this} is some
     *         {@link Invalid}. Containing errors converted to new type
     *         {@code U}, and preserving the target type {@code T}.
     *
     * @throws X If the {@code errorMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code errorMapper} is a {@code null}. 
     * @throws NullResultException If {@code errorMapper} returns a {@code null}. 
     */
    <U, X extends Throwable> Validation<U, T> mapError(
            CheckedFunction<? super E, ? extends U, ? extends X> errorMapper
    ) throws X, NullArgumentException, NullResultException;

    /**
     * Allows collapsing contained errors into a new type {@code U}, when
     * {@code this} is {@link Invalid}. When {@code this} is {@link Valid} the
     * fold is not applied as there are no errors, but the new error type
     * binding {@code U} is preserved.
     * <p>
     * The {@code errorMapper} doesn't have to be concerned with handling
     * {@code null} or empty errors as {@link Accumulated#errors()} and
     * {@link Critical#error()} can never be {@code null} or empty.
     * <p>
     * Checked exceptions thrown by {@code errorMapper} are propagated, see
     * {@link Validation} class documentation for details.
     *
     * @see Valid#foldErrors(CheckedFunction)
     * @see Accumulated#foldErrors(CheckedFunction)
     * @see Critical#foldErrors(CheckedFunction)
     *
     * @param <U> The new error type.
     * @param errorMapper Used to collapse errors into a new type {@code U}.
     *
     * @return {@code Valid<U, T>} when {@code this} is {@link Valid},
     *         preserving the contained target {@code T} and the new error type
     *         binding {@code U}. <br>
     *         New {@code Invalid<U, T>} when {@code this} is some
     *         {@link Invalid}. Containing error produced by {@code errorMapper}.
     *
     * @throws X If the {@code errorMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code errorMapper} is a {@code null}. 
     * @throws NullResultException If {@code errorMapper} returns a {@code null}. 
     */
    <U, X extends Throwable> Validation<U, T> foldErrors(
            CheckedFunction<? super List<E>, ? extends U, ? extends X> errorMapper
    ) throws X,  NullArgumentException, NullResultException;

    /**
     * The <b>functor (right biased)</b> operation of {@link Validation}. Allows
     * conversion of contained target from type {@code T} to type {@code S},
     * when {@code this} is {@link Valid}. When {@code this} is {@link Invalid}
     * the conversion is not applied, but the new target type binding {@code S}
     * is preserved.
     * <p>
     * The {@code mapper} doesn't have to be concerned with handling
     * {@code null} as {@link Valid#target()} can never be {@code null}.
     * <p>
     * Checked exceptions thrown by {@code mapper} are propagated, see
     * {@link Validation} class documentation for details.
     *
     * @see Valid#mapTarget(CheckedFunction)
     * @see Invalid#mapTarget(CheckedFunction)
     *
     * @param <S> The new target type.
     * @param mapper Used to map target from type {@code T} to type {@code S}.
     *
     * @return {@code Valid<E, S>} when {@code this} is {@link Valid},
     *         containing the new target {@code S} and preserving the error type
     *         binding. <br>
     *         {@code Invalid<E, S>} when {@code this} is some {@link Invalid}.
     *         Preserving contained errors, and binding the new target type
     *         {@code S}.
     *
     * @throws X If the {@code mapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code mapper} is a {@code null}. 
     * @throws NullResultException If {@code mapper} returns a {@code null}. 
     */
    <S, X extends Throwable> Validation<E, S> mapTarget(
            CheckedFunction<? super T, ? extends S, ? extends X> mapper
    ) throws X, NullArgumentException, NullResultException;

    /**
     * Allows setting a new target {@code S}, when {@code this} is a
     * {@link Valid}. When {@code this} is some {@link Invalid} the
     * {@code targetSupplier} is not applied, but the new target type binding
     * {@code S} is preserved.
     * <p>
     * Checked exceptions thrown by {@code targetSupplier} are propagated, see
     * {@link Validation} class documentation for details.
     *
     * @see Valid#newTarget(CheckedSupplier)
     * @see Invalid#newTarget(CheckedSupplier)
     *
     * @param <S> The new target type.
     * @param targetSupplier Used to provide new target {@code S}.
     *
     * @return {@code Valid<E, S>} when {@code this} is {@link Valid},
     *         containing the new target {@code S} and preserving the error type
     *         binding. <br>
     *         {@code Invalid<E, S>} when {@code this} is some {@link Invalid}.
     *         Preserving contained errors, and binding the new target type
     *         {@code S}.
     *
     * @throws X If the {@code targetSupplier} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code targetSupplier} is a {@code null}.
     * @throws NullResultException If {@code targetSupplier} returns a {@code null}.
     */
    <S, X extends Throwable> Validation<E, S> newTarget(
            CheckedSupplier<? extends S, ? extends X> targetSupplier
    ) throws X, NullArgumentException, NullResultException;

    /**
     * Allows recovery when {@code this} is some {@link Invalid}. Useful for
     * recovering from a {@link Critical}, either converting it into a
     * {@link Accumulated} or providing a new {@link Valid}.
     * <p>
     * The {@code mapper} doesn't have to be concerned with handling
     * {@code null} or empty errors as {@link Accumulated#errors()} and
     * {@link Critical#error()} can never be {@code null} or empty.
     * <p>
     * Checked exceptions thrown by {@code mapper} are propagated, see
     * {@link Validation} class documentation for details.
     *
     * @see Valid#recover(CheckedFunction)
     * @see Invalid#recover(CheckedFunction)
     *
     * @param mapper Used to consume potential errors and providing a new
     *        {@code Validation<E, T>}.
     *
     * @return {@code this} when {@code this} is {@link Valid}. <br>
     *         {@code mapper} result when {@code this} is some {@link Invalid}.
     *
     * @throws X If the {@code mapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code mapper} is a {@code null}.
     * @throws NullResultException If {@code mapper} returns a {@code null}.
     */
    <X extends Throwable> Validation<E, T> recover(
            CheckedFunction<? super List<E>, ? extends Validation<E, T>, ? extends X> mapper
    ) throws X, NullArgumentException, NullResultException;

    /**
     * Is {@code this} a {@link Valid}.
     *
     * @see #isInvalid()
     * @see #isAccumulated()
     * @see #isCritical()
     *
     * @return {@code true} if {@code this} is a {@link Valid}, otherwise
     *         {@code false}
     */
    boolean isValid();

    /**
     * Is {@code this} a {@link Invalid}.
     *
     * @see #isValid()
     * @see #isAccumulated()
     * @see #isCritical()
     *
     * @return {@code true} if {@code this} is a {@link Accumulated} or
     *         {@link Critical}, otherwise {@code false}.
     */
    boolean isInvalid();

    /**
     * Is {@code this} a {@link Accumulated}.
     *
     * @see #isValid()
     * @see #isInvalid()
     * @see #isCritical()
     *
     * @return {@code true} if {@code this} is a {@link Accumulated}, otherwise
     *         {@code false}
     */
    boolean isAccumulated();

    /**
     * Is {@code this} a {@link Critical}.
     *
     * @see #isValid()
     * @see #isInvalid()
     * @see #isAccumulated()
     *
     * @return {@code true} if {@code this} is a {@link Critical}, otherwise
     *         {@code false}
     */
    boolean isCritical();

    /**
     * Projects {@code this} into an instance of {@link Either}.
     * <p>
     * When {@code this} is a {@link Valid}, the result is a new {@link Right}
     * containing the target value {@code T}. Otherwise, returns a new
     * {@link Left} containing the errors {@code List<E>}.
     * <p>
     * {@link Either} contains various utility functions that enable lazy
     * reasoning about its contents.
     *
     * @see Either
     *
     * @return If {@code this} is a {@link Valid}, returns a {@code Right<T>}.
     *         Otherwise, returns a {@code Left<List<E>>}.
     */
    Either<List<E>, T> toEither();

    /**
     * Projects {@code this} into an instance of {@link Option}.
     * <p>
     * When {@code this} is a {@link Valid}, the result is a new {@link Present}
     * representation of {@link Option}, preserving the contained target value.
     * Otherwise, returns a new {@link Empty} only preserving the type
     * constraint {@code T}.
     *
     * @see Option
     * @see #getErrors()
     *
     * @return If {@code this} is a {@link Valid}, returns a {@code Present<T>}.
     *         Otherwise, returns a {@code Empty<T>}.
     */
    Option<T> get();

    /**
     * Projects {@code this} into a {@code List<E>}.
     * <p>
     * When {@code this} is a {@link Invalid}, the result is a new
     * {@code List<E>} preserving the contained errors. Otherwise, returns an
     * empty {@code List<E>}.
     *
     * @see #get()
     *
     * @return {@code List<E>}, that is empty when {@code this} is
     *         {@link Valid}, or contains errors when {@code this} is
     *         {@link Invalid}.
     */
    List<E> getErrors();

}

/**
 * The validation target {@code T} value, <em>right</em> side, implementation of
 * {@code Validation<E, T>}. Representing a {@link #target} object that
 * <em>passed</em> some validation checks.
 * <p>
 * Contains a validation target value {@code T}.
 *
 * @see Validation
 * @see Invalid
 * @see Accumulated
 * @see Critical
 *
 * @param target The contained validation target value {@code T}. Never
 *        {@code null}.
 * @param <E> The error type.
 * @param <T> The contained validation target type.
 */
record Valid<E, T>(T target) implements Validation<E, T> {

    Valid {
        requireNonNull(target, "target");
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code Invalid<E, S>} to
     * {@code Validation<E, U>} is provably safe because the {@code Invalid}
     * does not contain any value {@code S}, it only contains errors {@code E}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, U, X extends Throwable> Validation<E, U> combine(
            Validation<E, S> other,
            CheckedBiFunction<? super T, ? super S, ? extends U, ? extends X> combiner
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(other, "other");
        return switch (other) {
            case Valid<E, S>(var otherValue) -> {
                requireNonNull(combiner, "combiner");
                var result = requireNonNullResult(combiner, this.target, otherValue);
                yield new Valid<>(result);
            }
            case Invalid<E, S> e -> (Validation<E, U>) e;
        };
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code Invalid<E, S>} to
     * {@code Validation<E, T>} is provably safe because the {@code Invalid}
     * does not contain any value {@code S}, it only contains errors {@code E}.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S> Validation<E, T> combine(
            Validation<E, S> other
    ) throws NullArgumentException {
        requireNonNull(other, "other");
        return switch (other) {
            case Valid<E, S>(_) -> this;
            case Invalid<E, S> e -> (Validation<E, T>) e;
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Validation<E, S>} produced by {@code validator}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <S, X extends Throwable> Validation<E, S> then(
            CheckedFunction<? super T, ? extends Validation<E, S>, ? extends X> validator
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(validator, "validator");
        return requireNonNullResult(validator, target);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code this} to {@code Validation<U, T>} is
     * provably safe because {@code this} does not contain any error value
     * {@code E}, it only contains the validation target {@code T}.
     *
     * @return {@code Valid<U, T>} containing {@link #target}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <U, X extends Throwable> Validation<U, T> mapError(
            CheckedFunction<? super E, ? extends U, ? extends X> errorMapper
    ) {
        return (Validation<U, T>) this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code this} to {@code Validation<U, T>} is
     * provably safe because {@code this} does not contain any error value
     * {@code E}, it only contains the validation target {@code T}.
     *
     * @return {@code Valid<U, T>} containing {@link #target}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <U, X extends Throwable> Validation<U, T> foldErrors(
            CheckedFunction<? super List<E>, ? extends U, ? extends X> errorMapper
    ) {
        return (Validation<U, T>) this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Valid<E, S>} containing the new target {@code S}
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <S, X extends Throwable> Validation<E, S> mapTarget(
            CheckedFunction<? super T, ? extends S, ? extends X> mapper
    ) throws X {
        requireNonNull(mapper,  "mapper");
        var result = requireNonNullResult(mapper, target);
        return new Valid<>(result);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Valid<E, S>} containing the new target {@code S}
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <S, X extends Throwable> Validation<E, S> newTarget(
            CheckedSupplier<? extends S, ? extends X> targetSupplier
    ) throws X {
        requireNonNull(targetSupplier, "targetSupplier");
        var result = requireNonNullResult(targetSupplier, "targetSupplier");
        return new Valid<>(result);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code this}
     */
    @Override
    public <X extends Throwable> Validation<E, T> recover(
            CheckedFunction<? super List<E>, ? extends Validation<E, T>, ? extends X> mapper
    ) {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Right<T>} containing {@link #target}.
     */
    @Override
    public Either<List<E>, T> toEither() {
        return Either.right(this.target);
    }

    /**
     * {@inheritDoc}
     *
     * @see Present
     *
     * @return {@code Present<T>} containing {@link #target}.
     */
    @Override
    public Option<T> get() {
        return Option.of(this.target);
    }

    /**
     * {@inheritDoc}
     *
     * @return Empty list.
     */
    @Override
    public List<E> getErrors() {
        return List.of();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isInvalid() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isAccumulated() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isCritical() {
        return false;
    }
}

/**
 * The invalid, <em>left</em> side, extension of {@code Validation<E, T>}.
 * Describes the shared behaviors of <em>failed</em> validation checks.
 *
 * @see Validation
 * @see Valid
 * @see Accumulated
 * @see Critical
 *
 * @param <E> The error type.
 * @param <T> The validation target type.
 */
sealed interface Invalid<E, T> extends Validation<E, T> permits Accumulated, Critical {

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code this} to {@code Validation<E, U>} is
     * provably safe because an {@code Invalid} does not contain any value
     * {@code S}, it only contains errors {@code E}.
     *
     * @see #combineInvalid(Invalid)
     * @see Accumulated#combineInvalid(Invalid)
     * @see Critical#combineInvalid(Invalid)
     *
     * @return {@code this} as {@code Validation<E, U>} when {@code other} is a
     *         {@link Valid}. <br>
     *         Result of {@link #combineInvalid(Invalid)} when {@code other} is
     *         some {@link Invalid}.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    default <S, U, X extends Throwable> Validation<E, U> combine(
            Validation<E, S> other,
            CheckedBiFunction<? super T, ? super S, ? extends U, ? extends X> combiner
    ) throws NullArgumentException {
        requireNonNull(other, "other");
        return switch (other) {
            case Valid<E, S>(_) -> (Validation<E, U>) this;
            case Invalid<E, S> e -> this.combineInvalid(e);
        };
    }

    /**
     * {@inheritDoc}
     *
     * @see #combineInvalid(Invalid)
     * @see Accumulated#combineInvalid(Invalid)
     * @see Critical#combineInvalid(Invalid)
     *
     * @return {@code this} when {@code other} is a {@link Valid}. <br>
     *         Result of {@link #combineInvalid(Invalid)} when {@code other} is
     *         some {@link Invalid}.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    default <S> Validation<E, T> combine(
            Validation<E, S> other
    ) throws NullArgumentException {
        requireNonNull(other, "other");
        return switch (other) {
            case Valid<E, S>(_) -> this;
            case Invalid<E, S> e -> this.combineInvalid(e);
        };
    }

    /**
     * The <b>applicative functor</b> operation of {@link Invalid}. Similar
     * to {@link Invalid#combine(Validation)}. Used to explicitly combine two
     * {@link Invalid} validations.
     * <p>
     * Scenarios:
     * <ul>
     *     <li>
     *         When {@code this} is a {@link Accumulated} and {@code other} is
     *         {@link Accumulated}, combines errors from {@code this} and
     *         {@code other} into a new {@code Accumulated<E, U>}. Preserving
     *         the contained errors, and binding to the new target type
     *         {@code U}.
     *     </li>
     *     <li>
     *        When {@code this} is a {@link Accumulated} and {@code other} is
     *        {@link Critical}, returns {@code other} as {@code Invalid<E, U>}.
     *        Preserving the contained error, and binding the new target type
     *        {@code U}.
     *     </li>
     *     <li>
     *        When {@code this} is a {@link Critical}, ignores {@code other},
     *        returns {@code this} as {@code Invalid<E, U>}. Preserving the
     *        contained error, and binding the new target type {@code U}.
     *     </li>
     * </ul>
     *
     * @see Invalid#combine(Validation)
     * @see Accumulated#combineInvalid(Invalid)
     * @see Critical#combineInvalid(Invalid)
     *
     * @param <S> The type of object validated by {@code other}.
     * @param <U> The validation target type to bind going forward.
     * @param other {@link Invalid} to combine with {@code this}.
     *
     * @return {@code Invalid<E, U>} based on scenarios outlined above.
     *
     * @throws NullArgumentException If {@code other} is {@code null}.
     */
    <S, U> Invalid<E, U> combineInvalid(Invalid<E, S> other);

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code this} to {@code Validation<E, S>} is
     * provably safe because the {@code this} does not contain any value
     * {@code S}, it only contains errors {@code E}.
     *
     * @return {@code this} as {@code Validation<E, S>}, preserving errors and
     *         binding to the new target type {@code S}
     */
    @SuppressWarnings("unchecked")
    @Override
    default <S, X extends Throwable> Validation<E, S> then(
            CheckedFunction<? super T, ? extends Validation<E, S>, ? extends X> validator
    ) {
        return (Validation<E, S>) this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code this} to {@code Validation<E, S>} is
     * provably safe because the {@code this} does not contain any value
     * {@code S}, it only contains errors {@code E}.
     *
     * @return {@code this} as {@code Validation<E, S>}, preserving errors and
     *         binding to the new target type {@code S}
     */
    @SuppressWarnings("unchecked")
    @Override
    default <S, X extends Throwable> Validation<E, S> mapTarget(
            CheckedFunction<? super T, ? extends S, ? extends X> mapper
    ) {
        return (Validation<E, S>) this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code this} to {@code Validation<E, S>} is
     * provably safe because the {@code this} does not contain any value
     * {@code S}, it only contains errors {@code E}.
     *
     * @return {@code this} as {@code Validation<E, S>}, preserving errors and
     *         binding to the new target type {@code S}
     */
    @SuppressWarnings("unchecked")
    @Override
    default <S, X extends Throwable> Validation<E, S> newTarget(
            CheckedSupplier<? extends S, ? extends X> targetSupplier
    ) {
        return (Validation<E, S>) this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code mapper} result.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    default <X extends Throwable> Validation<E, T> recover(
            CheckedFunction<? super List<E>, ? extends Validation<E, T>, ? extends X> mapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(mapper, "mapper");
        return requireNonNullResult(mapper, getErrors());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Left<List<E>>} containing the errors.
     */
    @Override
    default Either<List<E>, T> toEither() {
        return Either.left(getErrors());
    }

    /**
     * {@inheritDoc}
     *
     * @see Empty
     *
     * @return {@code Empty<T>}.
     */
    @Override
    default Option<T> get() {
        return Option.empty();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    default boolean isValid() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    default boolean isInvalid() {
        return true;
    }
}

/**
 * The invalid, <em>left</em> side, implementation of {@code Validation<E, T>},
 * inheriting shared <em>failed</em> validation behavior from
 * {@code Invalid<E, T>}.
 * <p>
 * Contains a major validation {@link #error} {@code E}, short-circuiting the
 * validation chain. {@code Critical} bypasses every subsequent
 * {@link Validation#combine(Validation)} or its overload, discarding any
 * previously accumulated errors or any subsequent errors, and only preserving
 * its own contained {@link #error}.
 *
 * @see Validation
 * @see Invalid
 * @see Accumulated
 * @see Valid
 *
 * @param error The contained validation error value {@code E}. Never
 *        {@code null}.
 * @param <E> The contained error type.
 * @param <T> The validation target type.
 */
record Critical<E, T>(E error) implements Invalid<E, T> {

    Critical {
        requireNonNull(error, "error");
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code this} to {@code Invalid<E, U>} is
     * provably safe because an {@code Invalid} does not contain any value
     * {@code T}, it only contains errors {@code E}.
     *
     * @return {@code this} as {@code Invalid<E, U>}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, U> Invalid<E, U> combineInvalid(Invalid<E, S> other) {
        return (Invalid<E, U>) this;
    }

    /**
     * {@inheritDoc}
     *
     * @return New {@code Critical<U, T>} containing the result of
     * {@code errorMapper}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Validation<U, T> mapError(
            CheckedFunction<? super E, ? extends U, ? extends X> errorMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(errorMapper,  "errorMapper");
        var result = requireNonNullResult(errorMapper, error);
        return new Critical<>(result);
    }

    /**
     * {@inheritDoc}
     *
     * @return New {@code Critical<U, T>} containing the result of
     * {@code errorMapper}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Validation<U, T> foldErrors(
            CheckedFunction<? super List<E>, ? extends U, ? extends X> errorMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(errorMapper,  "errorMapper");
        var result = requireNonNullResult(errorMapper, getErrors());
        return new Critical<>(result);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isAccumulated() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    public boolean isCritical() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code List<E>} containing one element {@link #error}.
     */
    @Override
    public List<E> getErrors() {
        return List.of(error);
    }
}

/**
 * The invalid, <em>left</em> side, implementation of {@code Validation<E, T>},
 * inheriting shared <em>failed</em> validation behavior from
 * {@code Invalid<E, T>}.
 * <p>
 * Contains an accumulation of validation {@link #errors} in a {@code List<E>},
 * preserving errors of any {@code Accumulated} passed into
 * {@link Validation#combine(Validation)} or its overloads.
 *
 * @see Validation
 * @see Invalid
 * @see Critical
 * @see Valid
 *
 * @param errors The contained list of accumulated errors {@code E}. Never
 *        {@code null} or empty.
 * @param <E> The contained error type.
 * @param <T> The validation target type.
 */
record Accumulated<E, T>(List<E> errors) implements Invalid<E, T> {

    Accumulated {
        requireNonEmpty(errors, "errors");
        errors = List.copyOf(errors);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code other} to {@code Invalid<E, U>} is
     * provably safe because a {@code Critical} does not contain any value
     * {@code S}, it only contains error {@code E}.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, U> Invalid<E, U> combineInvalid(Invalid<E, S> other) throws NullArgumentException {
        requireNonNull(other, "other");
        return switch (other) {
            case Critical<E, S> c -> (Invalid<E, U>) c;
            case Accumulated<E, S>(var e) -> {
                var combined = new ArrayList<E>(this.errors.size() + e.size());
                combined.addAll(this.errors);
                combined.addAll(e);
                yield new Accumulated<>(combined);
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return New {@code Accumulated<U, T>}, accumulating the results of
     * {@code errorMapper} applied to each {@link #errors} element.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Validation<U, T> mapError(
            CheckedFunction<? super E, ? extends U, ? extends X> errorMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(errorMapper, "errorMapper");
        List<U> mapped = new ArrayList<>(this.errors.size());
        for (E error : this.errors) {
            var result = requireNonNullResult(errorMapper, error);
            mapped.add(result);
        }
        return new Accumulated<>(mapped);
    }

    /**
     * {@inheritDoc}
     *
     * @return New {@code Accumulated<U, T>} containing the result of
     * {@code errorMapper}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Validation<U, T> foldErrors(
            CheckedFunction<? super List<E>, ? extends U, ? extends X> errorMapper
    ) throws X {
        requireNonNull(errorMapper, "errorMapper");
        var result = requireNonNullResult(errorMapper, this.errors);
        return new Accumulated<>(List.of(result));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    public boolean isAccumulated() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isCritical() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link #errors}.
     */
    @Override
    public List<E> getErrors() {
        return this.errors;
    }
}