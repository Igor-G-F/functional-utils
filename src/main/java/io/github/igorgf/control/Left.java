package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * The left {@code L} value implementation of {@code Either<L, R>}.
 * <p>
 * Contains a NEVER {@code null} value {@code L}.
 *
 * @see Either
 * @see Right
 *
 * @param value The contained value {@code L}. Never {@code null}.
 * @param <L> The contained value type.
 * @param <R> The left value type.
 */
public record Left<L, R>(L value) implements Either<L, R> {

    public Left { requireNonNull(value, "value"); }

    /**
     * {@inheritDoc}
     *
     * @return {@code Left<S, T>} with the value {@code S} returned by the
     *         {@code leftMapper}
     *
     * @throws X1 {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> bimap(
            CheckedFunction<? super L, ? extends S, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1, NullArgumentException, NullResultException {
        requireNonNull(leftMapper, "leftMapper");
        var result = requireNonNullResult(leftMapper, value);
        return new Left<>(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S, X extends Exception> Either<S, R> mapLeft(
            CheckedFunction<? super L, ? extends S, ? extends X> leftMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(leftMapper, "leftMapper");
        var result = requireNonNullResult(leftMapper, value);
        return new Left<>(result);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The cast in this method is provably safe as only the left value is
     * contained here.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, X extends Exception> Either<L, S> mapRight(
            CheckedFunction<? super R, ? extends S, ? extends X> rightMapper
    ) {
        return (Either<L, S>) this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Either<S, T>} returned by the {@code leftMapper}.
     *
     * @throws X1 {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> biflatMap(
            CheckedFunction<? super L, ? extends Either<S, T>, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends Either<S, T>, ? extends X2> rightMapper
    ) throws X1, NullArgumentException, NullResultException {
        requireNonNull(leftMapper, "leftMapper");
        return requireNonNullResult(leftMapper, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S, X extends Exception> Either<S, R> flatMapLeft(
            CheckedFunction<? super L, ? extends Either<S, R>, ? extends X> leftMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(leftMapper, "leftMapper");
        return requireNonNullResult(leftMapper, value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The cast in this method is provably safe as only the left value is
     * contained here.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, X extends Exception> Either<L, S> flatMapRight(
            CheckedFunction<? super R, ? extends Either<L, S>, ? extends X> rightMapper
    ) {
        return (Either<L, S>) this;
    }

    /**
     * Converts {@code this} {@link Left} into a {@code Right<R, L>} containing
     * {@link #value()}.
     *
     * @return A new {@code Right<R, L>} containing {@link #value()}.
     */
    @Override
    public Either<R, L> swap() {
        return Either.right(value);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code T} returned by the {@code leftMapper}.
     *
     * @throws X1 {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super L, ? extends T, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1 {
        requireNonNull(leftMapper, "leftMapper");
        return requireNonNullResult(leftMapper, value);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    public boolean isLeft() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isRight() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see Present
     *
     * @return A new {@code Present<L>}.
     */
    @Override
    public Option<L> getLeft() {
        return Option.of(this.value);
    }

    /**
     * {@inheritDoc}
     *
     * @see Empty
     *
     * @return A new {@code Empty<R>}.
     */
    @Override
    public Option<R> getRight() {
        return Option.empty();
    }

}
