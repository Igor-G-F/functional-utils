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
 * The value absence representing implementation of {@link Option}.
 *
 * @see Option
 * @see Present
 *
 * @param <T> The value type.
 */
public record Empty<T>() implements Option<T> {

    /**
     * @return {@code Empty<U>}
     */
    @Override
    public <U, X extends Throwable> Option<U> map(
            CheckedFunction<? super T, ? extends U, ? extends X> mapper
    ) {
        return new Empty<>();
    }

    /**
     * @return {@code Empty<U>}
     */
    @Override
    public <U, X extends Throwable> Option<U> flatMap(
            CheckedFunction<? super T, ? extends Option<? extends U>, ? extends X> mapper
    ) {
        return new Empty<>();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code U} from the {@code emptySupplier}.
     *
     * @throws X2 {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X1 extends Throwable, X2 extends Throwable> U fold(
            CheckedFunction<? super T, ? extends U, ? extends X1> presentMapper,
            CheckedSupplier<? extends U, ? extends X2> emptySupplier
    ) throws X2, NullResultException, NullArgumentException {
        requireNonNull(emptySupplier, "emptySupplier");
        return requireNonNullResult(emptySupplier, "emptySupplier");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isPresent() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * Do nothing because {@code this} is a {@link Empty}.
     */
    @Override
    public <X extends Throwable> void ifPresent(
            CheckedConsumer<? super T, ? extends X> action
    ) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     */
    @Override
    public <X extends Throwable> void ifEmpty(
            CheckedRunnable<? extends X> emptyAction
    ) throws X, NullArgumentException {
        requireNonNull(emptyAction, "emptyAction");
        emptyAction.run();
    }

    /**
     * {@inheritDoc}
     *
     * @throws X If the {@code emptyAction} throws a checked exception, it is
     *         propagated to the caller.
     * @throws NullArgumentException If {@code emptyAction} is {@code null}.
     */
    @Override
    public <X extends Throwable> void ifPresentOrElse(
            CheckedConsumer<? super T, ? extends X> action,
            CheckedRunnable<? extends X> emptyAction
    ) throws X, NullArgumentException {
        requireNonNull(emptyAction, "emptyAction");
        emptyAction.run();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code this} which is an instance of {@code Empty<T>}
     */
    @Override
    public Option<T> filter(
            Predicate<? super T> predicate
    ) {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code other}
     *
     * @throws NullArgumentException If {@code other} is {@code null}.
     */
    @Override
    public T orElse(T other) throws NullArgumentException {
        return requireNonNull(other, "other");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code other}
     */
    @Override
    public T orElseNullable(T other) {
        return other;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code T} provided by the {@code supplier}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <X extends Throwable> T orElseGet(
            CheckedSupplier<? extends T, ? extends X> supplier
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(supplier, "supplier");
        return requireNonNullResult(supplier, "supplier");
    }

    /**
     * Always throws {@code X}, because {@code this} is a {@link Empty}.
     * 
     * @throws X Because {@code this} is a {@link Empty}.
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <X extends Throwable> T orElseThrow(
            Supplier<? extends X> exceptionSupplier
    ) throws X {
        requireNonNull(exceptionSupplier, "exceptionSupplier");
        throw requireNonNullResult(exceptionSupplier::get, "exceptionSupplier");
    }

    /**
     * Always throws {@link EmptyResultException}, because {@code this} is a
     * {@link Empty}.
     * 
     * @throws EmptyResultException Because {@code this} is a {@link Empty}.
     */
    @Override
    public T orThrow() throws EmptyResultException {
        throw new EmptyResultException();
    }
}
