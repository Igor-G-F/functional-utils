package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;

import java.util.Objects;

/**
 * A disjoint union (sum type) representing exactly one of two possible
 * outcomes: {@code Left<L>} or {@code Right<R>}. This is <b>NOT</b> a
 * "pair type". {@code Either} is a general purpose XOR type for reasoning about
 * two mutually exclusive results.
 *
 * <p>
 * {@code Either} is:
 * <ul>
 *   <li>
 *       <b>right biased</b>: operations like {@link #map}, {@link #flatMap},
 *       etc. target the {@code Right<R>} value.
 *   </li>
 *   <li>
 *       A <b>functor</b>: {@link #map} transforms the {@code Right<R>} value,
 *       preserving the context.
 *   </li>
 *   <li>
 *       A <b>monad</b>: {@link #flatMap} chains operations that produce
 *       {@code Either}, short-circuiting on the first {@link Left}. For
 *       accumulating multiple independent errors rather than short-circuiting,
 *       use {@link Validation}.
 *   </li>
 *   <li>
 *       A <b>bi-functor</b>: {@link #bimap} transforms both {@link Left} and
 *       {@link Right} values independently.
 *   </li>
 * </ul>
 * <p>
 * {@link #fold} is the catamorphism for {@code Either}, it collapses both
 * possible cases into a single value by providing a handler for each.
 *
 * @apiNote
 * By convention, when one side represents an undesirable outcome
 * (error, exception, null, etc.), it is placed on the {@code Left<L>}, this is
 * a common functional usage pattern, it is not a semantic requirement.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <L> the Left value type
 * @param <R> the Right value type
 */
public sealed interface Either<L, R> permits Right, Left {

    // Construction
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    // Monad
    <T, X extends Exception> Either<L, T> map(
            CheckedFunction<? super R, ? extends T, ? extends X> mapper
    ) throws X;

    <T, X extends Exception> Either<L, T> flatMap(
            CheckedFunction<? super R, ? extends Either<L, T>, ? extends X> mapper
    ) throws X;

    <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> bimap(
            CheckedFunction<? super L, ? extends S, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1, X2;

    <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super L, ? extends T, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1, X2;

    // Convenience
    Either<R, L> swap();

    <X extends Exception> void ifLeft(
            CheckedConsumer<? super L, ? extends X> action
    ) throws X;

    <X extends Exception> void ifRight(
            CheckedConsumer<? super R, ? extends X> action
    ) throws X;

    boolean isLeft();

    boolean isRight();

    Option<L> getLeft();

    Option<R> getRight();

}

record Right<L, R>(R value) implements Either<L, R> {

    Right { Objects.requireNonNull(value); }

    @Override
    public <T, X extends Exception> Either<L, T> map(
            CheckedFunction<? super R, ? extends T, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return Either.right(mapper.apply(value));
    }

    @Override
    public <T, X extends Exception> Either<L, T> flatMap(
            CheckedFunction<? super R, ? extends Either<L, T>, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return mapper.apply(value);
    }

    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> bimap(
            CheckedFunction<? super L, ? extends S, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X2 {
        Objects.requireNonNull(rightMapper);
        return Either.right(rightMapper.apply(value));
    }

    @Override
    public Either<R, L> swap() {
        return Either.left(value);
    }

    @Override
    public <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super L, ? extends T, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X2 {
        Objects.requireNonNull(rightMapper);
        return rightMapper.apply(value);
    }

    @Override
    public <X extends Exception> void ifLeft(
            CheckedConsumer<? super L, ? extends X> action
    ) {
        // do nothing
    }

    @Override
    public <X extends Exception> void ifRight(
            CheckedConsumer<? super R, ? extends X> action
    ) throws X {
        Objects.requireNonNull(action);
        action.accept(value);
    }

    @Override
    public boolean isLeft() {
        return false;
    }

    @Override
    public boolean isRight() {
        return true;
    }

    @Override
    public Option<L> getLeft() {
        return Option.empty();
    }

    @Override
    public Option<R> getRight() {
        return Option.of(this.value);
    }

}

record Left<L, R>(L value) implements Either<L, R> {

    Left { Objects.requireNonNull(value); }

    @SuppressWarnings("unchecked")
    @Override
    public <T, X extends Exception> Either<L, T> map(
            CheckedFunction<? super R, ? extends T, ? extends X> mapper
    ) {
        return (Either<L, T>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, X extends Exception> Either<L, T> flatMap(
            CheckedFunction<? super R, ? extends Either<L, T>, ? extends X> mapper
    ) {
        return (Either<L, T>) this;
    }

    @Override
    public <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> bimap(
            CheckedFunction<? super L, ? extends S, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1 {
        Objects.requireNonNull(leftMapper);
        return Either.left(leftMapper.apply(value));
    }

    @Override
    public Either<R, L> swap() {
        return Either.right(value);
    }

    @Override
    public <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super L, ? extends T, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1 {
        Objects.requireNonNull(leftMapper);
        return leftMapper.apply(value);
    }

    @Override
    public <X extends Exception> void ifLeft(
            CheckedConsumer<? super L, ? extends X> action
    ) throws X {
        Objects.requireNonNull(action);
        action.accept(value);
    }

    @Override
    public <X extends Exception> void ifRight(
            CheckedConsumer<? super R, ? extends X> action
    ) {
        // do nothing
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public Option<L> getLeft() {
        return Option.of(this.value);
    }

    @Override
    public Option<R> getRight() {
        return Option.empty();
    }

}
