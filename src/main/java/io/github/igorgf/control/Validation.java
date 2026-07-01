package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public sealed interface Validation<E, R> {

    // Constructors

    static <E, R> Validation<E, R> valid(R value) { return new Valid<>(value); }

    static <E, R> Validation<E, R> invalid(E error) {
        Objects.requireNonNull(error);
        return new Invalid<>(List.of(error));
    }

    static <E, R> Validation<E, R> invalid(List<E> errors) {
        Objects.requireNonNull(errors);
        if (errors.isEmpty()) throw new EmptyValueException("errors param cannot be empty");
        return new Invalid<>(errors);
    }

    static <E> ValidationBuilder<E> builder() { return new ValidationBuilder<>(); }

    // Composition: monadic

    // bimap: "transform errors and value"
    <E2, R2, X1 extends Exception, X2 extends Exception> Validation<E2, R2> bimap(
            CheckedFunction<? super List<E>, ? extends List<E2>, ? extends X1> errorMapper,
            CheckedFunction<? super R, ? extends R2, ? extends X2> valueMapper
    ) throws X1, X2;

    // flatMap: "decide next step based on value"
    <R2, X extends Exception> Validation<E, R2> flatMap(
            CheckedFunction<? super R, ? extends Validation<E, R2>, ? extends X> mapper
    ) throws X;

    // biflatMap: "decide next step based on either side"
    <E2, R2, X1 extends Exception, X2 extends Exception> Validation<E2, R2> biflatMap(
            CheckedFunction<? super List<E>, ? extends Validation<E2, R2>, ? extends X1> errorMapper,
            CheckedFunction<? super R, ? extends Validation<E2, R2>, ? extends X2> valueMapper
    ) throws X1, X2;

    // Composition: applicative

    <R2, R3> Validation<E, R3> combine(
            Validation<E, R2> other,
            BiFunction<R, R2, R3> combiner
    );

    <R2> Validation<E, R> combine(
            Validation<E, R2> other
    );

    // Elimination

    <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super List<E>, ? extends T, ? extends X1> invalidMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> validMapper
    ) throws X1, X2;

    <X extends Exception> void ifValid(
            CheckedConsumer<? super R, ? extends X> action
    ) throws X;

    <X extends Exception> void ifInvalid(
            CheckedConsumer<? super List<E>, ? extends X> action
    ) throws X;

    // Extraction

    Either<List<E>, R> toEither();

    Option<R> get();

    List<E> getErrors();

    <X extends Exception> R orElseGet(
            CheckedSupplier<? extends R, ? extends X> supplier
    ) throws X;

    <X extends Exception> Validation<E, R> or(
            CheckedSupplier<? extends Validation<E, R>, ? extends X> supplier
    ) throws X;

    // Inspection

    boolean isValid();
    boolean isInvalid();

}

record Valid<E, R>(R value) implements Validation<E, R> {

    Valid { Objects.requireNonNull(value); }

    @Override
    public <E2, R2, X1 extends Exception, X2 extends Exception> Validation<E2, R2> bimap(
            CheckedFunction<? super List<E>, ? extends List<E2>, ? extends X1> errorMapper,
            CheckedFunction<? super R, ? extends R2, ? extends X2> valueMapper
    ) throws X2 {
        Objects.requireNonNull(valueMapper);
        return new Valid<>(valueMapper.apply(value));
    }

    @Override
    public <R2, X extends Exception> Validation<E, R2> flatMap(
            CheckedFunction<? super R, ? extends Validation<E, R2>, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return mapper.apply(value);
    }

    @Override
    public <E2, R2, X1 extends Exception, X2 extends Exception> Validation<E2, R2> biflatMap(
            CheckedFunction<? super List<E>, ? extends Validation<E2, R2>, ? extends X1> errorMapper,
            CheckedFunction<? super R, ? extends Validation<E2, R2>, ? extends X2> valueMapper
    ) throws X2 {
        Objects.requireNonNull(valueMapper);
        return valueMapper.apply(value);
    }

    @Override
    public <R2, R3> Validation<E, R3> combine(
            Validation<E, R2> other,
            BiFunction<R, R2, R3> combiner
    ) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(combiner);
        return switch (other) {
            case Valid<E, R2>(var otherValue) -> new Valid<>(combiner.apply(this.value, otherValue));
            case Invalid<E, R2>(var errors) -> new Invalid<>(errors);
        };
    }

    @Override
    public <R2> Validation<E, R> combine(
            Validation<E, R2> other
    ) {
        Objects.requireNonNull(other);
        return switch (other) {
            case Valid<E, R2>(_) -> this;
            case Invalid<E, R2>(var errors) -> new Invalid<>(errors);
        };
    }


    @Override
    public <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super List<E>, ? extends T, ? extends X1> invalidMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> validMapper
    ) throws X2 {
        Objects.requireNonNull(validMapper);
        return validMapper.apply(this.value);
    }

    @Override
    public <X extends Exception> void ifValid(
            CheckedConsumer<? super R, ? extends X> action
    ) throws X {
        Objects.requireNonNull(action);
        action.accept(this.value);
    }

    @Override
    public <X extends Exception> void ifInvalid(
            CheckedConsumer<? super List<E>, ? extends X> action
    ) {
        // do nothing
    }

    @Override
    public Either<List<E>, R> toEither() {
        return Either.right(this.value);
    }

    @Override
    public Option<R> get() {
        return Option.of(this.value);
    }

    @Override
    public List<E> getErrors() {
        return List.of();
    }

    @Override
    public <X extends Exception> R orElseGet(
            CheckedSupplier<? extends R, ? extends X> supplier
    ) {
        return this.value;
    }

    @Override
    public <X extends Exception> Validation<E, R> or(
            CheckedSupplier<? extends Validation<E, R>, ? extends X> supplier
    ) {
        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isInvalid() {
        return false;
    }
}

record Invalid<E, R>(List<E> errors) implements Validation<E, R> {

    @Override
    public <E2, R2, X1 extends Exception, X2 extends Exception> Validation<E2, R2> bimap(
            CheckedFunction<? super List<E>, ? extends List<E2>, ? extends X1> errorMapper,
            CheckedFunction<? super R, ? extends R2, ? extends X2> valueMapper
    ) throws X1 {
        Objects.requireNonNull(errorMapper);
        return new Invalid<>(errorMapper.apply(errors));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R2, X extends Exception> Validation<E, R2> flatMap(
            CheckedFunction<? super R, ? extends Validation<E, R2>, ? extends X> mapper
    ) {
        return (Validation<E, R2>) this;
    }

    @Override
    public <E2, R2, X1 extends Exception, X2 extends Exception> Validation<E2, R2> biflatMap(
            CheckedFunction<? super List<E>, ? extends Validation<E2, R2>, ? extends X1> errorMapper,
            CheckedFunction<? super R, ? extends Validation<E2, R2>, ? extends X2> valueMapper
    ) throws X1 {
        Objects.requireNonNull(errorMapper);
        return errorMapper.apply(this.errors);
    }

    @Override
    public <R2, R3> Validation<E, R3> combine(
            Validation<E, R2> other,
            BiFunction<R, R2, R3> combiner
    ) {
        Objects.requireNonNull(other);
        return switch (other) {
            case Valid<E, R2>(_) -> new Invalid<>(this.errors);
            case Invalid<E, R2>(var otherErrors) -> new Invalid<>(merge(otherErrors, this.errors));
        };
    }

    @Override
    public <R2> Validation<E, R> combine(
            Validation<E, R2> other
    ) {
        Objects.requireNonNull(other);
        return switch (other) {
            case Valid<E, R2>(_) -> this;
            case Invalid<E, R2>(var otherErrors) -> new Invalid<>(merge(otherErrors, this.errors));
        };
    }

    private static <E> List<E> merge(List<E> a, List<E> b) {
        var combined = new ArrayList<E>(a.size() + b.size());
        combined.addAll(a);
        combined.addAll(b);
        return List.copyOf(combined);
    }

    @Override
    public <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super List<E>, ? extends T, ? extends X1> invalidMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> validMapper
    ) throws X1 {
        Objects.requireNonNull(invalidMapper);
        return invalidMapper.apply(this.errors);
    }

    @Override
    public <X extends Exception> void ifValid(
            CheckedConsumer<? super R, ? extends X> action
    ) {
        // do nothing
    }

    @Override
    public <X extends Exception> void ifInvalid(
            CheckedConsumer<? super List<E>, ? extends X> action
    ) throws X {
        Objects.requireNonNull(action);
        action.accept(this.errors);
    }

    @Override
    public Either<List<E>, R> toEither() {
        return Either.left(this.errors);
    }

    @Override
    public Option<R> get() {
        return Option.empty();
    }

    @Override
    public List<E> getErrors() {
        return this.errors;
    }

    @Override
    public <X extends Exception> R orElseGet(
            CheckedSupplier<? extends R, ? extends X> supplier
    ) throws X {
        Objects.requireNonNull(supplier);
        return supplier.get();
    }

    @Override
    public <X extends Exception> Validation<E, R> or(
            CheckedSupplier<? extends Validation<E, R>, ? extends X> supplier
    ) throws X {
        Objects.requireNonNull(supplier);
        return supplier.get();
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isInvalid() {
        return true;
    }

}