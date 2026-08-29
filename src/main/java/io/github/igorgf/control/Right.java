package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;

import java.util.Objects;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * The right {@code R} value implementation of {@code Either<L, R>}.
 * <p>
 * Contains a NEVER {@code null} value {@code R}.
 *
 * @see Either
 * @see Left
 *
 * @param value The contained value {@code R}. Never {@code null}.
 * @param <L> The left value type.
 * @param <R> The contained value type.
 */
public record Right<L, R>(R value) implements Either<L, R> {

    public Right { requireNonNull(value, "value"); }

    /**
     * {@inheritDoc}
     *
     * @return {@code Right<S, T>} with the value {@code T} returned by the
     *         {@code rightMapper}
     *
     * @throws X2 {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> bimap(
            CheckedFunction<? super L, ? extends S, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X2, NullArgumentException, NullResultException {
        requireNonNull(rightMapper, "rightMapper");
        var result = requireNonNullResult(rightMapper, value);
        return new Right<>(result);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The cast in this method is provably safe as only the left value is
     * contained here.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, X extends Exception> Either<S, R> mapLeft(
            CheckedFunction<? super L, ? extends S, ? extends X> leftMapper
    ) {
        return (Either<S, R>) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S, X extends Exception> Either<L, S> mapRight(
            CheckedFunction<? super R, ? extends S, ? extends X> rightMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(rightMapper, "rightMapper");
        var result = requireNonNullResult(rightMapper, value);
        return new Right<>(result);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Either<S, T>} returned by the {@code rightMapper}.
     *
     * @throws X2 {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> biflatMap(
            CheckedFunction<? super L, ? extends Either<S, T>, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends Either<S, T>, ? extends X2> rightMapper
    ) throws X2, NullArgumentException, NullResultException {
        requireNonNull(rightMapper, "rightMapper");
        return requireNonNullResult(rightMapper, value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The cast in this method is provably safe as only the left value is
     * contained here.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, X extends Exception> Either<S, R> flatMapLeft(
            CheckedFunction<? super L, ? extends Either<S, R>, ? extends X> leftMapper
    ) {
        return (Either<S, R>) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S, X extends Exception> Either<L, S> flatMapRight(
            CheckedFunction<? super R, ? extends Either<L, S>, ? extends X> rightMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(rightMapper, "rightMapper");
        return requireNonNullResult(rightMapper, value);
    }

    /**
     * Converts {@code this} {@link Right} into a {@code Left<R, L>} containing
     * {@link #value()}.
     *
     * @return A new {@code Left<R, L>} containing {@link #value()}.
     */
    @Override
    public Either<R, L> swap() {
        return Either.left(value);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code T} returned by the {@code rightMapper}.
     *
     * @throws X2 {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super L, ? extends T, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X2, NullArgumentException, NullResultException {
        Objects.requireNonNull(rightMapper);
        return Objects.requireNonNull(rightMapper.apply(value));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isLeft() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code right}
     */
    @Override
    public boolean isRight() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see Empty
     *
     * @return A new {@code Empty<L>}.
     */
    @Override
    public Option<L> getLeft() {
        return Option.empty();
    }

    /**
     * {@inheritDoc}
     *
     * @see Present
     *
     * @return A new {@code Present<R>}.
     */
    @Override
    public Option<R> getRight() {
        return Option.of(this.value);
    }

}
