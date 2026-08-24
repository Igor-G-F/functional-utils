package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;

import java.util.Objects;

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

    public Left { Objects.requireNonNull(value); }

    /**
     * {@inheritDoc}
     *
     * @return {@code Left<S, T>} with the value {@code S} returned by the
     *         {@code leftMapper}
     *
     * @throws X1 {@inheritDoc}
     * @throws NullPointerException If the {@code leftMapper} is {@code null}
     *         or returns a {@code null}.
     */
    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> bimap(
            CheckedFunction<? super L, ? extends S, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1 {
        Objects.requireNonNull(leftMapper);
        return new Left<>(leftMapper.apply(value));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Either<S, T>} returned by the {@code leftMapper}.
     *
     * @throws X1 {@inheritDoc}
     * @throws NullPointerException If the {@code leftMapper} is {@code null}
     *         or returns a {@code null}.
     */
    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> biflatMap(
            CheckedFunction<? super L, ? extends Either<S, T>, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends Either<S, T>, ? extends X2> rightMapper
    ) throws X1 {
        Objects.requireNonNull(leftMapper);
        return Objects.requireNonNull(leftMapper.apply(this.value));
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
     * @throws NullPointerException If the {@code leftMapper} is {@code null}
     *         or returns a {@code null}.
     */
    @Override
    public <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super L, ? extends T, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1 {
        Objects.requireNonNull(leftMapper);
        return leftMapper.apply(value);
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
