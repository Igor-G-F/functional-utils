package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedRunnable;
import io.github.igorgf.function.CheckedSupplier;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface Option<E> {

    // Construction
    static <E> Option<E> of(E value) {
        return new Present<>(value);
    }

    static <E> Option<E> ofNullable(E value) {
        return value == null ? new Empty<>() : new Present<>(value);
    }

    static <E> Option<E> empty() {
        return new Empty<>();
    }

    // Monad
    <F, X extends Exception> Option<F> map(
            CheckedFunction<? super E, ? extends F, ? extends X> mapper
    ) throws X;

    <F, X extends Exception> Option<F> flatMap(
            CheckedFunction<? super E, ? extends Option<? extends F>, ? extends X> mapper
    ) throws X;

    <F, X extends Exception> F fold(
            CheckedFunction<? super E, ? extends F, ? extends X> presentMapper,
            CheckedSupplier<? extends F, ? extends X> emptySupplier
    ) throws X;

    // Convenience
    boolean isPresent();

    boolean isEmpty();

    <X extends Exception> void ifPresent(
            CheckedConsumer<? super E, ? extends X> action
    ) throws X;

    <X extends Exception> void ifEmpty(
            CheckedRunnable<? extends X> emptyAction
    ) throws X;

    <X extends Exception> void ifPresentOrElse(
            CheckedConsumer<? super E, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X;

    Option<E> filter(Predicate<? super E> predicate);

    @SuppressWarnings("unchecked")
    default <X extends Exception> Option<E> or(
            CheckedSupplier<? extends Option<? extends E>, ? extends X> supplier
    ) throws X {
        return isPresent() ? this : (Option<E>) Objects.requireNonNull(supplier.get());
    }

    E orElse(E other);

    <X extends Exception> E orElseGet(
            CheckedSupplier<? extends E, ? extends X> supplier
    ) throws X;

    <X extends Exception> E orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) throws X;

    E orThrow() throws EmptyValueException;

}

record Present<E>(E value) implements Option<E> {

    Present { Objects.requireNonNull(value); }

    @Override
    public <F, X extends Exception> Option<F> map(
            CheckedFunction<? super E, ? extends F, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return new Present<>(mapper.apply(this.value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F, X extends Exception> Option<F> flatMap(
            CheckedFunction<? super E, ? extends Option<? extends F>, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return (Option<F>) mapper.apply(this.value);
    }

    @Override
    public <F, X extends Exception> F fold(
            CheckedFunction<? super E, ? extends F, ? extends X> presentMapper,
            CheckedSupplier<? extends F, ? extends X> emptySupplier
    ) throws X {
        Objects.requireNonNull(presentMapper);
        return presentMapper.apply(this.value);
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public <X extends Exception> void ifPresent(
            CheckedConsumer<? super E, ? extends X> action
    ) throws X {
        Objects.requireNonNull(action);
        action.accept(this.value);
    }

    @Override
    public <X extends Exception> void ifEmpty(
            CheckedRunnable<? extends X> emptyAction
    ) {
        // do nothing
    }

    @Override
    public <X extends Exception> void ifPresentOrElse(
            CheckedConsumer<? super E, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X {
        Objects.requireNonNull(action);
        action.accept(this.value);
    }

    @Override
    public Option<E> filter(
            Predicate<? super E> predicate
    ) {
        Objects.requireNonNull(predicate);
        return predicate.test(this.value) ? this : new Empty<>();
    }

    @Override
    public E orElse(E other) {
        return this.value;
    }

    @Override
    public <X extends Exception> E orElseGet(
            CheckedSupplier<? extends E, ? extends X> supplier
    ) {
        return this.value;
    }

    @Override
    public <X extends Exception> E orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) {
        return this.value;
    }

    @Override
    public E orThrow() {
        return this.value;
    }
}

record Empty<E>() implements Option<E> {

    @Override
    public <F, X extends Exception> Option<F> map(
            CheckedFunction<? super E, ? extends F, ? extends X> mapper
    ) {
        return new Empty<>();
    }

    @Override
    public <F, X extends Exception> Option<F> flatMap(
            CheckedFunction<? super E, ? extends Option<? extends F>, ? extends X> mapper
    ) {
        return new Empty<>();
    }

    @Override
    public <F, X extends Exception> F fold(
            CheckedFunction<? super E, ? extends F, ? extends X> presentMapper,
            CheckedSupplier<? extends F, ? extends X> emptySupplier
    ) throws X {
        Objects.requireNonNull(emptySupplier);
        return emptySupplier.get();
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public <X extends Exception> void ifPresent(
            CheckedConsumer<? super E, ? extends X> action
    ) {
        // do nothing
    }

    @Override
    public <X extends Exception> void ifEmpty(
            CheckedRunnable<? extends X> emptyAction
    ) throws X {
        Objects.requireNonNull(emptyAction);
        emptyAction.run();
    }

    @Override
    public <X extends Exception> void ifPresentOrElse(
            CheckedConsumer<? super E, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X {
        Objects.requireNonNull(emptyAction);
        emptyAction.run();
    }

    @Override
    public Option<E> filter(
            Predicate<? super E> predicate
    ) {
        return this;
    }

    @Override
    public E orElse(E other) {
        return other;
    }

    @Override
    public <X extends Exception> E orElseGet(
            CheckedSupplier<? extends E, ? extends X> supplier
    ) throws X {
        Objects.requireNonNull(supplier);
        return supplier.get();
    }

    @Override
    public <X extends Exception> E orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) throws X {
        throw exceptionSupplier.get();
    }

    @Override
    public E orThrow() throws EmptyValueException {
        throw new EmptyValueException("Option is " + this.getClass().getCanonicalName());
    }
}