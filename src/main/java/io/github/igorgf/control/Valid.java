package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.List;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

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
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <E> The error type.
 * @param <T> The contained validation target type.
 */
public record Valid<E, T>(T target) implements Validation<E, T> {

    public Valid {
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
