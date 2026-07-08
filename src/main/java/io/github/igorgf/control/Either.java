package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.Objects;

/**
 * A disjoint union (sum type) representing exactly one of two possible
 * outcomes: {@code Left<L>} or {@code Right<R>}. This is <b>NOT</b> a
 * "pair type". {@code Either} is a general purpose XOR type for reasoning about
 * two mutually exclusive results.
 * <p>
 * <b>{@code Either} features:</b>
 * <ul>
 *   <li>
 *       Is <b>null safe</b>: {@link Left} or {@link Right} can never contain a
 *       null value, {@code Either} does not expose any null value entry or
 *       exit. The entire API rejects {@code null} at every boundary:
 *       constructors, mapping functions, and suppliers all throw
 *       {@link NullPointerException} on {@code null}.
 *   </li>
 *   <li>
 *       A <b>bi-functor</b>: {@link #bimap} transforms both {@link Left} and
 *       {@link Right} values independently.
 *   </li>
 *   <li>
 *       <b>bi-functor monadic chaining</b>: {@link #biflatMap} chains
 *       operations that produce {@code Either}, on both {@link Left} and
 *       {@link Right} values independently.
 *   </li>
 *   <li>
 *       A <b>catamorphism</b>: {@link #fold} collapses both possible states
 *       ({@link Left} and {@link Right}) into a single value.
 *   </li>
 * </ul>
 * <p>
 * <b>Exception Handling:</b><br>
 * Methods accepting a {@link CheckedFunction} or {@link CheckedSupplier}
 * propagate checked exceptions transparently through a generic
 * {@code X extends Exception} parameter. The compiler will only require
 * handling if checked exceptions are explicitly declared or thrown in the
 * lambda body. Unchecked exceptions ({@link RuntimeException}) propagate
 * normally and require no declaration. Example of checked exception handling:
 * <pre>{@code
 *     try {
 *         Either.left(7).bimap(n -> {
 *             if (n < 10) throw new Exception();
 *             return n * n;
 *         }, o -> o);
 *     } catch (Exception e) {
 *         // e has to be handled as it propagated from map
 *     }
 * }</pre>
 * While throwing unchecked exceptions does not require explicit handling:
 * <pre>{@code
 *     Either.left(7).bimap(n -> {
 *         if (n < 10) throw new RuntimeException();
 *         return n * n;
 *     }, o -> o);
 * }</pre>
 * <b>Alternative Types:</b><br>
 * For accumulating multiple validation errors, use {@link Validation}. <br>
 * For single value presence or absence handling, use {@link Option}.
 *
 * @see Left
 * @see Right
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <L> the Left value type
 * @param <R> the Right value type
 */
public sealed interface Either<L, R> permits Right, Left {

    /**
     * Factory method for creating a new {@code Left<L, R>} instance.
     *
     * @see Left
     *
     * @param <L> The type of the object being contained.
     * @param <R> The type reference for a possible {@link Right}.
     * @param value The object to be contained.
     *
     * @return A new {@code Left<L, R>} instance.
     *
     * @throws NullPointerException If {@code value} is {@code null}.
     */
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Factory method for creating a new {@code RightLeft<L, R>} instance.
     *
     * @see Right
     *
     * @param <L> The type reference for a possible {@link Left}.
     * @param <R> The type of the object being contained.
     * @param value The object to be contained.
     *
     * @return A new {@code RightLeft<L, R>} instance.
     *
     * @throws NullPointerException If {@code value} is {@code null}.
     */
    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    /**
     * The <b>bi-functor</b> operation of {@link Either}. Transforms the right
     * value using the provided {@code rightMapper} function, or the left value
     * using the provided {@code leftMapper} function.
     * <p>
     * When {@code this} is a {@link Right} then the {@code rightMapper} is
     * applied to the contained value, and contained within a new
     * {@code Right<S, T>}. When {@code this} is a {@link Left} then the
     * {@code leftMapper} is applied to the contained value, and contained
     * within a new {@code Left<S, T>}, allowing for convenient chaining.
     * <p>
     * The {@code rightMapper} and {@code leftMapper} don't have to be concerned
     * with handling {@code null} contained values as {@link Right#value()} or
     * {@link Left#value()} can never be {@code null}.
     * <p>
     * Checked exceptions thrown by {@code rightMapper} or {@code leftMapper}
     * are propagated, see {@link Either} class documentation for details.
     *
     * @see Right#bimap(CheckedFunction, CheckedFunction)
     * @see Left#bimap(CheckedFunction, CheckedFunction)
     *
     * @param <S> The {@link Left} type being contained after mapping.
     * @param <T> The {@link Right} type being contained after mapping.
     * @param leftMapper The mapping function to apply to the contained value,
     *        if {@code this} is a {@link Left}.
     * @param rightMapper The mapping function to apply to the contained value,
     *        if {@code this} is a {@link Right}.
     *
     * @return New {@code Right<S, T>} if {@code this} is a {@link Right}. New
     *         {@code Left<S, T>} if {@code this} is a {@link Left}.
     *
     * @throws X1 If the {@code leftMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws X2 If the {@code rightMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullPointerException If {@code leftMapper} or {@code rightMapper}
     *         is {@code null} during lazy evaluation. Or if either mapping
     *         returns a {@code null}.
     */
    <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> bimap(
            CheckedFunction<? super L, ? extends S, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1, X2;

    /**
     * The <b>bi-functor monadic chaining</b> operation of {@link Either}.
     * Transforms the right value using the provided {@code rightMapper}
     * function, or the left value using the provided {@code leftMapper}
     * function.
     * <p>
     * This method is similar to {@link #bimap(CheckedFunction, CheckedFunction)},
     * but the mapping functions are ones whose result is already an
     * {@code ? extends Either<S, T>}, and if invoked, {@code biflatMap} does
     * not wrap it within an additional {@code Either}.
     * <p>
     * If either mapping returns a {@code null} then this method throws a
     * {@link NullPointerException}. Each mapper must always return a
     * {@code ? extends Either<S, T>}.
     * <p>
     * The {@code rightMapper} and {@code leftMapper} don't have to be concerned
     * with handling {@code null} contained values as {@link Right#value()} or
     * {@link Left#value()} can never be {@code null}.
     * <p>
     * Checked exceptions thrown by {@code rightMapper} or {@code leftMapper}
     * are propagated, see {@link Either} class documentation for details.
     *
     * @see Either#bimap(CheckedFunction, CheckedFunction)
     * @see Right#biflatMap(CheckedFunction, CheckedFunction)
     * @see Left#biflatMap(CheckedFunction, CheckedFunction)
     *
     * @param <S> The {@link Left} type being contained after mapping.
     * @param <T> The {@link Right} type being contained after mapping.
     * @param leftMapper The mapping function to apply to the contained value,
     *        if {@code this} is a {@link Left}.
     * @param rightMapper The mapping function to apply to the contained value,
     *        if {@code this} is a {@link Right}.
     *
     * @return New {@code Either<S, T>} returned by the {@code leftMapper} or
     *         the {@code rightMapper}.
     *
     * @throws X1 If the {@code leftMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws X2 If the {@code rightMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullPointerException If {@code leftMapper} or {@code rightMapper}
     *         is {@code null} during lazy evaluation. Or if either mapping
     *         returns a {@code null}.
     */
    <S, T, X1 extends Exception, X2 extends Exception> Either<S, T> biflatMap(
            CheckedFunction<? super L, ? extends Either<S, T>, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends Either<S, T>, ? extends X2> rightMapper
    ) throws X1, X2;

    /**
     * The <b>catamorphism</b> operation of {@link Either}. Collapses both
     * possible states ({@link Left} and {@link Right}) into a single value.
     * <p>
     * This method is similar to {@link #bimap(CheckedFunction, CheckedFunction)},
     * but forgoes the re-containing of the mapping results, and returns a
     * concrete value {@code T} instead.
     * <p>
     * The {@code rightMapper} and {@code leftMapper} don't have to be concerned
     * with handling {@code null} contained values as {@link Right#value()} or
     * {@link Left#value()} can never be {@code null}.
     * <p>
     * Checked exceptions thrown by {@code rightMapper} or {@code leftMapper}
     * are propagated, see {@link Either} class documentation for details.
     *
     * @see Either#bimap(CheckedFunction, CheckedFunction)
     * @see Present#fold(CheckedFunction, CheckedSupplier)
     * @see Empty#fold(CheckedFunction, CheckedSupplier)
     *
     * @param <T> The result type after mapping.
     * @param leftMapper The mapping function to apply to the contained value,
     *        if {@code this} is a {@link Left}.
     * @param rightMapper The mapping function to apply to the contained value,
     *        if {@code this} is a {@link Right}.
     *
     * @return {@code T} from the {@code leftMapper} or the {@code rightMapper}.
     *
     * @throws X1 If the {@code leftMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws X2 If the {@code rightMapper} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullPointerException If {@code leftMapper} or {@code rightMapper}
     *         is {@code null} during lazy evaluation. Or if either mapping
     *         returns a {@code null}.
     */
    <T, X1 extends Exception, X2 extends Exception> T fold(
            CheckedFunction<? super L, ? extends T, ? extends X1> leftMapper,
            CheckedFunction<? super R, ? extends T, ? extends X2> rightMapper
    ) throws X1, X2;

    /**
     * Swaps the left and right values into their opposite container, preserving
     * the type information.
     * <p>
     * {@code Right<L, R>} becomes a {@code Left<R, L>}, conversely
     * {@code Left<L, R>} becomes a {@code Right<R, L>}. Example:
     * <pre>{@code
     *     Either<Integer, String> expected = Either.left(8);
     *     Either<String, Integer> other = Either.right(8);
     *     assert expected.equals(other.swap());
     * }</pre>
     * <p>
     * {@code swap} is an involution, applying it twice returns the original:
     * <pre>{@code
     *     assert either.swap().swap().equals(either);
     * }</pre>
     * @return A new {@code Either} with left and right swapped,
     *         {@code Either<R, L>}.
     */
    Either<R, L> swap();

    /**
     * Is {@code this} a {@link Left}.
     *
     * @see #isRight()
     *
     * @return {@code true} if {@code this} is a {@link Left}, otherwise
     *         {@code false}
     */
    boolean isLeft();

    /**
     * Is {@code this} a {@link Right}.
     *
     * @see #isLeft()
     *
     * @return {@code true} if {@code this} is a {@link Right}, otherwise
     *         {@code false}
     */
    boolean isRight();

    /**
     * Projects {@code this} into an instance of {@link Option}.
     * <p>
     * When {@code this} is a {@link Left}, the result is a new {@link Present}
     * representation of {@link Option}, preserving the contained value and the
     * type {@code L}. Otherwise, returns a new {@link Empty} only preserving
     * the type constraint {@code L}.
     * <p>
     * This is a more terse and intention revealing equivalent to doing a
     * {@link #fold(CheckedFunction, CheckedFunction)} into an {@link Option}:
     * <pre>{@code
     *     Either<Integer, String> original = Either.left(8);
     *     Option<Integer> folded = original.fold(Option::of, _ -> Option.empty());
     *     assert original.getLeft().equals(folded);
     * }</pre>
     *
     * @see Option
     * @see #getRight()
     *
     * @return If {@code this} is a {@link Left}, returns a {@code Present<L>}.
     *         Otherwise, returns a {@code Empty<L>}.
     */
    Option<L> getLeft();

    /**
     * Projects {@code this} into an instance of {@link Option}.
     * <p>
     * When {@code this} is a {@link Right}, the result is a new {@link Present}
     * representation of {@link Option}, preserving the contained value and the
     * type {@code R}. Otherwise, returns a new {@link Empty} only preserving
     * the type constraint {@code R}.
     * <p>
     * This is a more terse and intention revealing equivalent to doing a
     * {@link #fold(CheckedFunction, CheckedFunction)} into an {@link Option}:
     * <pre>{@code
     *     Either<String, Integer> original = Either.right(8);
     *     Option<Integer> folded = original.fold(_ -> Option.empty(), Option::of);
     *     assert original.getRight().equals(folded);
     * }</pre>
     *
     * @see Option
     * @see #getLeft()
     *
     * @return If {@code this} is a {@link Right}, returns a {@code Present<R>}.
     *         Otherwise, returns a {@code Empty<R>}.
     */
    Option<R> getRight();

}

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
record Right<L, R>(R value) implements Either<L, R> {

    Right { Objects.requireNonNull(value); }

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
record Left<L, R>(L value) implements Either<L, R> {

    Left { Objects.requireNonNull(value); }

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
