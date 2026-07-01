package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;

import java.util.Objects;

public sealed interface Either<L, R> {

    // Construction
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    // Monad
    <R2, X extends Exception> Either<L, R2> map(
            CheckedFunction<? super R, ? extends R2, ? extends X> mapper
    ) throws X;

    <R2, X extends Exception> Either<L, R2> flatMap(
            CheckedFunction<? super R, ? extends Either<L, R2>, ? extends X> mapper
    ) throws X;

    <L2, R2, X1 extends Exception, X2 extends Exception> Either<L2, R2> bimap(
            CheckedFunction<? super L, ? extends L2, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends R2, ? extends X2> rightMapper
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
    public <R2, X extends Exception> Either<L, R2> map(
            CheckedFunction<? super R, ? extends R2, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return Either.right(mapper.apply(value));
    }

    @Override
    public <R2, X extends Exception> Either<L, R2> flatMap(
            CheckedFunction<? super R, ? extends Either<L, R2>, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return mapper.apply(value);
    }

    @Override
    public <L2, R2, X1 extends Exception, X2 extends Exception> Either<L2, R2> bimap(
            CheckedFunction<? super L, ? extends L2, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends R2, ? extends X2> rightMapper
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
    public <R2, X extends Exception> Either<L, R2> map(
            CheckedFunction<? super R, ? extends R2, ? extends X> mapper
    ) {
        return (Either<L, R2>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R2, X extends Exception> Either<L, R2> flatMap(
            CheckedFunction<? super R, ? extends Either<L, R2>, ? extends X> mapper
    ) {
        return (Either<L, R2>) this;
    }

    @Override
    public <L2, R2, X1 extends Exception, X2 extends Exception> Either<L2, R2> bimap(
            CheckedFunction<? super L, ? extends L2, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends R2, ? extends X2> rightMapper
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
