package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedRunnable;
import io.github.igorgf.function.CheckedSupplier;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * The value presence representing implementation of {@link Option}.
 * <p>
 * Contains a NEVER {@code null} value.
 *
 * @see Option
 * @see Empty
 *
 * @param value The contained value. Never {@code null}.
 * @param <T> The contained value type.
 */
public record Present<T>(T value) implements Option<T> {

    public Present {
        requireNonNull(value, "value");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Present<U>}
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Option<U> map(
            CheckedFunction<? super T, ? extends U, ? extends X> mapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(mapper,  "mapper");
        var result = requireNonNullResult(mapper, value);
        return new Present<>(result);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code Option<? extends U>} to {@code Option<U>}
     * is provably safe because {@code Option} is covariant in its type
     * parameter, it only produces values of {@code U}, never consumes them.
     *
     * @return {@code Present<U>}
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <U, X extends Throwable> Option<U> flatMap(
            CheckedFunction<? super T, ? extends Option<? extends U>, ? extends X> mapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(mapper, "mapper");
        var result = requireNonNullResult(mapper, value);
        return (Option<U>) result;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code U} from the {@code presentMapper}.
     *
     * @throws X1 {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X1 extends Throwable, X2 extends Throwable> U fold(
            CheckedFunction<? super T, ? extends U, ? extends X1> presentMapper,
            CheckedSupplier<? extends U, ? extends X2> emptySupplier
    ) throws X1, NullArgumentException, NullResultException {
        requireNonNull(presentMapper, "presentMapper");
        return requireNonNullResult(presentMapper, value);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    public boolean isPresent() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public <X extends Throwable> void ifPresent(
            CheckedConsumer<? super T, ? extends X> action
    ) throws X, NullArgumentException {
        requireNonNull(action, "action");
        action.accept(this.value);
    }

    /**
     * Do nothing because {@code this} is a {@link Present}.
     */
    @Override
    public <X extends Throwable> void ifEmpty(
            CheckedRunnable<? extends X> emptyAction
    ) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @throws X If the {@code action} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public <X extends Throwable> void ifPresentOrElse(
            CheckedConsumer<? super T, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X, NullArgumentException {
        requireNonNull(action, "action");
        action.accept(this.value);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public Option<T> filter(
            Predicate<? super T> predicate
    ) throws NullArgumentException {
        requireNonNull(predicate, "predicate");
        return predicate.test(this.value) ? this : new Empty<>();
    }

    /**
     * @return Contained value.
     */
    @Override
    public T orElse(T other) {
        return this.value;
    }

    /**
     * @return Contained value.
     */
    @Override
    public T orElseNullable(T other) {
        return this.value;
    }

    /**
     * @return Contained value.
     */
    @Override
    public <X extends Throwable> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) {
        return this.value;
    }

    /**
     * @return Contained value.
     */
    @Override
    public <X extends Throwable> T orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) {
        return this.value;
    }

    /**
     * @return Contained value.
     */
    @Override
    public T orThrow() {
        return this.value;
    }
}
