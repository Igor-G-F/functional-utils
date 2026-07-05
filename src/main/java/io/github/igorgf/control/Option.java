package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedRunnable;
import io.github.igorgf.function.CheckedSupplier;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A sum type representing the presence or absence of a value, as a null-safe
 * alternative to {@link java.util.Optional}.
 * <p>
 * {@code Option} is:
 * <ul>
 *   <li>
 *       A <b>functor</b>: {@link #map} transforms the contained value.
 *   </li>
 *   <li>
 *       A <b>monad</b>: {@link #flatMap} chains operations that may not
 *       produce a value, short-circuiting on {@link Empty}.
 *   </li>
 * </ul>
 *
 * @apiNote
 * Unlike {@link java.util.Optional}, {@code Option} is a sealed type with
 * explicit {@link Present} and {@link Empty} cases, improving the pattern
 * matching experience.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <T> the contained value type
 */
public sealed interface Option<T> {

    // Construction
    static <T> Option<T> of(T value) {
        return new Present<>(value);
    }

    static <T> Option<T> ofNullable(T value) {
        return value == null ? new Empty<>() : new Present<>(value);
    }

    static <T> Option<T> empty() {
        return new Empty<>();
    }

    // Monad
    <U, X extends Exception> Option<U> map(
            CheckedFunction<? super T, ? extends U, ? extends X> mapper
    ) throws X;

    <U, X extends Exception> Option<U> flatMap(
            CheckedFunction<? super T, ? extends Option<? extends U>, ? extends X> mapper
    ) throws X;

    <U, X extends Exception> U fold(
            CheckedFunction<? super T, ? extends U, ? extends X> presentMapper,
            CheckedSupplier<? extends U, ? extends X> emptySupplier
    ) throws X;

    // Convenience
    boolean isPresent();

    boolean isEmpty();

    <X extends Exception> void ifPresent(
            CheckedConsumer<? super T, ? extends X> action
    ) throws X;

    <X extends Exception> void ifEmpty(
            CheckedRunnable<? extends X> emptyAction
    ) throws X;

    <X extends Exception> void ifPresentOrElse(
            CheckedConsumer<? super T, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X;

    Option<T> filter(Predicate<? super T> predicate);

    @SuppressWarnings("unchecked")
    default <X extends Exception> Option<T> or(
            CheckedSupplier<? extends Option<? extends T>, ? extends X> supplier
    ) throws X {
        return isPresent() ? this : (Option<T>) Objects.requireNonNull(supplier.get());
    }

    T orElse(T other);

    <X extends Exception> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) throws X;

    <X extends Exception> T orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) throws X;

    T orThrow() throws EmptyValueException;

}

record Present<T>(T value) implements Option<T> {

    Present { Objects.requireNonNull(value); }

    @Override
    public <U, X extends Exception> Option<U> map(
            CheckedFunction<? super T, ? extends U, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return new Present<>(mapper.apply(this.value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U, X extends Exception> Option<U> flatMap(
            CheckedFunction<? super T, ? extends Option<? extends U>, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return (Option<U>) mapper.apply(this.value);
    }

    @Override
    public <U, X extends Exception> U fold(
            CheckedFunction<? super T, ? extends U, ? extends X> presentMapper,
            CheckedSupplier<? extends U, ? extends X> emptySupplier
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
            CheckedConsumer<? super T, ? extends X> action
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
            CheckedConsumer<? super T, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X {
        Objects.requireNonNull(action);
        action.accept(this.value);
    }

    @Override
    public Option<T> filter(
            Predicate<? super T> predicate
    ) {
        Objects.requireNonNull(predicate);
        return predicate.test(this.value) ? this : new Empty<>();
    }

    @Override
    public T orElse(T other) {
        return this.value;
    }

    @Override
    public <X extends Exception> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) {
        return this.value;
    }

    @Override
    public <X extends Exception> T orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) {
        return this.value;
    }

    @Override
    public T orThrow() {
        return this.value;
    }
}

record Empty<T>() implements Option<T> {

    @Override
    public <U, X extends Exception> Option<U> map(
            CheckedFunction<? super T, ? extends U, ? extends X> mapper
    ) {
        return new Empty<>();
    }

    @Override
    public <U, X extends Exception> Option<U> flatMap(
            CheckedFunction<? super T, ? extends Option<? extends U>, ? extends X> mapper
    ) {
        return new Empty<>();
    }

    @Override
    public <U, X extends Exception> U fold(
            CheckedFunction<? super T, ? extends U, ? extends X> presentMapper,
            CheckedSupplier<? extends U, ? extends X> emptySupplier
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
            CheckedConsumer<? super T, ? extends X> action
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
            CheckedConsumer<? super T, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X {
        Objects.requireNonNull(emptyAction);
        emptyAction.run();
    }

    @Override
    public Option<T> filter(
            Predicate<? super T> predicate
    ) {
        return this;
    }

    @Override
    public T orElse(T other) {
        return other;
    }

    @Override
    public <X extends Exception> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) throws X {
        Objects.requireNonNull(supplier);
        return supplier.get();
    }

    @Override
    public <X extends Exception> T orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) throws X {
        throw exceptionSupplier.get();
    }

    @Override
    public T orThrow() throws EmptyValueException {
        throw new EmptyValueException("Option is " + this.getClass().getCanonicalName());
    }
}