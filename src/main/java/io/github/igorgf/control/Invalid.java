package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.List;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * The invalid, <em>left</em> side, extension of {@code Validation<E, T>}.
 * Describes the shared behaviours of <em>failed</em> validation checks.
 *
 * @see Validation
 * @see Valid
 * @see Accumulated
 * @see Critical
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <E> The error type.
 * @param <T> The validation target type.
 */
public sealed interface Invalid<E, T> extends Validation<E, T> permits Accumulated, Critical {

    /**
     * The severity of an {@link Invalid}: whether it short-circuits
     * {@link Validation#combine} or participates in error accumulation.
     */
    enum Severity {
        /**
         * Participates in error accumulation alongside other invalid results.
         *
         * @see Accumulated
         * */
        ACCUMULATED,
        /**
         * Short-circuits any subsequent {@code combine}.
         *
         * @see Critical
         * */
        CRITICAL
    }

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
