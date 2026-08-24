package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;

import java.util.Objects;

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

    public Right { Objects.requireNonNull(value); }

    /**
     * {@inheritDoc}
     *
     * @return {@code Right<S, T>} with the value {@code T} returned by the
     *         {@code rightMapper}
     *
     * @throws X2 {@inheritDoc}
     * @throws NullPointerException If the {@code rightMapper} is {@code null}
     *         or returns a {@code null}.
     */
    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> bimap(
            CheckedFunction<? super L, ? extends S, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X2 {
        Objects.requireNonNull(rightMapper);
        return Either.right(rightMapper.apply(value));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Either<S, T>} returned by the {@code rightMapper}.
     *
     * @throws X2 {@inheritDoc}
     * @throws NullPointerException If the {@code rightMapper} is {@code null}
     *         or returns a {@code null}.
     */
    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> biflatMap(
            CheckedFunction<? super L, ? extends Either<S, T>, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends Either<S, T>, ? extends X2> rightMapper
    ) throws X2 {
        Objects.requireNonNull(rightMapper);
        return Objects.requireNonNull(rightMapper.apply(this.value));
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
     * @throws NullPointerException If the {@code rightMapper} is {@code null}
     *         or returns a {@code null}.
     */
    @Override
    public <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super L, ? extends T, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X2 {
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
