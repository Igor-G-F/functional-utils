package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A disjoint union (sum type) representing a validated value {@code Valid<T>}
 * or an accumulated {@code List<E>} of errors contained by {@code Invalid<E>}.
 * This is <b>NOT</b> a "pair type". {@code Validation} is a special case XOR
 * type for reasoning about accumulation of distinct errors.
 * <p>
 * {@code Validation} is:
 * <ul>
 *   <li>
 *       <b>right biased</b>: operations like {@link #flatMap} target the
 *       {@code Valid<T>} validated value.
 *   </li>
 *   <li>
 *       A <b>monad</b>: {@link #flatMap} chains dependent validations,
 *       short-circuiting on the first {@link Invalid}. For sequential error
 *       handling with short-circuiting use {@link Either}.
 *   </li>
 *   <li>
 *       A <b>bi-functor</b>: {@link #bimap} transforms both {@link Valid} and
 *       {@link Invalid} results independently.
 *   </li>
 *   <li>
 *       An <b>applicative functor</b>: {@link #combine} evaluates multiple
 *       independent validations and <em>accumulates all errors</em>, unlike
 *       {@link #flatMap} which short-circuits.
 *   </li>
 * </ul>
 *
 * @apiNote
 * Use {@code Validation} when you want to collect all validation errors
 * (e.g. form validation) rather than stopping at the first failure. For
 * sequential, non-accumulative error handling with short-circuiting use
 * {@link Either}.
 *
 * @param <E> the error element/container type
 * @param <T> the validated result type
 */
public sealed interface Validation<E, T> permits Valid, Invalid {

    // Constructors

    static <E, T> Validation<E, T> valid(T value) { return new Valid<>(value); }

    static <E, T> Validation<E, T> invalid(E error) {
        Objects.requireNonNull(error);
        return new Invalid<>(List.of(error));
    }

    static <E, T> Validation<E, T> invalid(List<E> errors) throws EmptyValueException {
        Objects.requireNonNull(errors);
        if (errors.isEmpty()) throw new EmptyValueException("errors cannot be empty");
        return new Invalid<>(errors);
    }

    static <E> ValidationBuilder<E> builder() { return new ValidationBuilder<>(); }

    // Composition: monadic

    // flatMap: "decide next step based on value"
    <S, X extends Exception> Validation<E, S> flatMap(
            CheckedFunction<? super T, ? extends Validation<E, S>, ? extends X> mapper
    ) throws X;

    // bimap: "transform errors and value"
    <U, S, X1 extends Exception, X2 extends Exception> Validation<U, S> bimap(
            CheckedFunction<? super List<E>, ? extends List<U>, ? extends X1> errorMapper,
            CheckedFunction<? super T, ? extends S, ? extends X2> valueMapper
    ) throws X1, X2;

    // biflatMap: "decide next step based on either side"
    <U, S, X1 extends Exception, X2 extends Exception> Validation<U, S> biflatMap(
            CheckedFunction<? super List<E>, ? extends Validation<U, S>, ? extends X1> errorMapper,
            CheckedFunction<? super T, ? extends Validation<U, S>, ? extends X2> valueMapper
    ) throws X1, X2;

    // Composition: applicative

    <S, U> Validation<E, U> combine(
            Validation<E, S> other,
            BiFunction<T, S, U> combiner
    );

    <S> Validation<E, T> combine(
            Validation<E, S> other
    );

    // Elimination

    <U, X1 extends Exception, X2 extends Exception> U fold(
            CheckedFunction<? super List<E>, ? extends U, ? extends X1> invalidMapper,
            CheckedFunction<? super T, ? extends U, ? extends X2> validMapper
    ) throws X1, X2;

    <X extends Exception> void ifValid(
            CheckedConsumer<? super T, ? extends X> action
    ) throws X;

    <X extends Exception> void ifInvalid(
            CheckedConsumer<? super List<E>, ? extends X> action
    ) throws X;

    // Extraction

    Either<List<E>, T> toEither();

    Option<T> get();

    List<E> getErrors();

    <X extends Exception> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) throws X;

    <X extends Exception> Validation<E, T> or(
            CheckedSupplier<? extends Validation<E, T>, ? extends X> supplier
    ) throws X;

    // Inspection

    boolean isValid();
    boolean isInvalid();

}

record Valid<E, T>(T value) implements Validation<E, T> {

    Valid { Objects.requireNonNull(value); }

    @Override
    public <U, S, X1 extends Exception, X2 extends Exception> Validation<U, S> bimap(
            CheckedFunction<? super List<E>, ? extends List<U>, ? extends X1> errorMapper,
            CheckedFunction<? super T, ? extends S, ? extends X2> valueMapper
    ) throws X2 {
        Objects.requireNonNull(valueMapper);
        return new Valid<>(valueMapper.apply(value));
    }

    @Override
    public <S, X extends Exception> Validation<E, S> flatMap(
            CheckedFunction<? super T, ? extends Validation<E, S>, ? extends X> mapper
    ) throws X {
        Objects.requireNonNull(mapper);
        return mapper.apply(value);
    }

    @Override
    public <U, S, X1 extends Exception, X2 extends Exception> Validation<U, S> biflatMap(
            CheckedFunction<? super List<E>, ? extends Validation<U, S>, ? extends X1> errorMapper,
            CheckedFunction<? super T, ? extends Validation<U, S>, ? extends X2> valueMapper
    ) throws X2 {
        Objects.requireNonNull(valueMapper);
        return valueMapper.apply(value);
    }

    @Override
    public <S, U> Validation<E, U> combine(
            Validation<E, S> other,
            BiFunction<T, S, U> combiner
    ) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(combiner);
        return switch (other) {
            case Valid<E, S>(var otherValue) -> new Valid<>(combiner.apply(this.value, otherValue));
            case Invalid<E, S>(var errors) -> new Invalid<>(errors);
        };
    }

    @Override
    public <S> Validation<E, T> combine(
            Validation<E, S> other
    ) {
        Objects.requireNonNull(other);
        return switch (other) {
            case Valid<E, S>(_) -> this;
            case Invalid<E, S>(var errors) -> new Invalid<>(errors);
        };
    }


    @Override
    public <U, X1 extends Exception, X2 extends Exception> U fold(
            CheckedFunction<? super List<E>, ? extends U, ? extends X1> invalidMapper,
            CheckedFunction<? super T, ? extends U, ? extends X2> validMapper
    ) throws X2 {
        Objects.requireNonNull(validMapper);
        return validMapper.apply(this.value);
    }

    @Override
    public <X extends Exception> void ifValid(
            CheckedConsumer<? super T, ? extends X> action
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
    public Either<List<E>, T> toEither() {
        return Either.right(this.value);
    }

    @Override
    public Option<T> get() {
        return Option.of(this.value);
    }

    @Override
    public List<E> getErrors() {
        return List.of();
    }

    @Override
    public <X extends Exception> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) {
        return this.value;
    }

    @Override
    public <X extends Exception> Validation<E, T> or(
            CheckedSupplier<? extends Validation<E, T>, ? extends X> supplier
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

record Invalid<E, T>(List<E> errors) implements Validation<E, T> {

    @Override
    public <U, S, X1 extends Exception, X2 extends Exception> Validation<U, S> bimap(
            CheckedFunction<? super List<E>, ? extends List<U>, ? extends X1> errorMapper,
            CheckedFunction<? super T, ? extends S, ? extends X2> valueMapper
    ) throws X1 {
        Objects.requireNonNull(errorMapper);
        return new Invalid<>(errorMapper.apply(errors));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S, X extends Exception> Validation<E, S> flatMap(
            CheckedFunction<? super T, ? extends Validation<E, S>, ? extends X> mapper
    ) {
        return (Validation<E, S>) this;
    }

    @Override
    public <U, S, X1 extends Exception, X2 extends Exception> Validation<U, S> biflatMap(
            CheckedFunction<? super List<E>, ? extends Validation<U, S>, ? extends X1> errorMapper,
            CheckedFunction<? super T, ? extends Validation<U, S>, ? extends X2> valueMapper
    ) throws X1 {
        Objects.requireNonNull(errorMapper);
        return errorMapper.apply(this.errors);
    }

    @Override
    public <S, U> Validation<E, U> combine(
            Validation<E, S> other,
            BiFunction<T, S, U> combiner
    ) {
        Objects.requireNonNull(other);
        return switch (other) {
            case Valid<E, S>(_) -> new Invalid<>(this.errors);
            case Invalid<E, S>(var otherErrors) -> new Invalid<>(merge(otherErrors, this.errors));
        };
    }

    @Override
    public <S> Validation<E, T> combine(
            Validation<E, S> other
    ) {
        Objects.requireNonNull(other);
        return switch (other) {
            case Valid<E, S>(_) -> this;
            case Invalid<E, S>(var otherErrors) -> new Invalid<>(merge(otherErrors, this.errors));
        };
    }

    private static <E> List<E> merge(List<E> a, List<E> b) {
        var combined = new ArrayList<E>(a.size() + b.size());
        combined.addAll(a);
        combined.addAll(b);
        return List.copyOf(combined);
    }

    @Override
    public <U, X1 extends Exception, X2 extends Exception> U fold(
            CheckedFunction<? super List<E>, ? extends U, ? extends X1> invalidMapper,
            CheckedFunction<? super T, ? extends U, ? extends X2> validMapper
    ) throws X1 {
        Objects.requireNonNull(invalidMapper);
        return invalidMapper.apply(this.errors);
    }

    @Override
    public <X extends Exception> void ifValid(
            CheckedConsumer<? super T, ? extends X> action
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
    public Either<List<E>, T> toEither() {
        return Either.left(this.errors);
    }

    @Override
    public Option<T> get() {
        return Option.empty();
    }

    @Override
    public List<E> getErrors() {
        return this.errors;
    }

    @Override
    public <X extends Exception> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) throws X {
        Objects.requireNonNull(supplier);
        return supplier.get();
    }

    @Override
    public <X extends Exception> Validation<E, T> or(
            CheckedSupplier<? extends Validation<E, T>, ? extends X> supplier
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