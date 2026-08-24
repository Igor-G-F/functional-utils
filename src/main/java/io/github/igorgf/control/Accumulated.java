package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;

import java.util.ArrayList;
import java.util.List;

import static io.github.igorgf.control.ControlUtils.*;

/**
 * The invalid, <em>left</em> side, implementation of {@code Validation<E, T>},
 * inheriting shared <em>failed</em> validation behavior from
 * {@code Invalid<E, T>}.
 * <p>
 * Contains an accumulation of validation {@link #errors} in a {@code List<E>},
 * preserving errors of any {@code Accumulated} passed into
 * {@link Validation#combine(Validation)} or its overloads.
 *
 * @see Validation
 * @see Invalid
 * @see Critical
 * @see Valid
 *
 * @param errors The contained list of accumulated errors {@code E}. Never
 *        {@code null} or empty.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <E> The contained error type.
 * @param <T> The validation target type.
 */
public record Accumulated<E, T>(List<E> errors) implements Invalid<E, T> {

    public Accumulated {
        requireNonEmpty(errors, "errors");
        errors = List.copyOf(errors);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code other} to {@code Invalid<E, U>} is
     * provably safe because a {@code Critical} does not contain any value
     * {@code S}, it only contains error {@code E}.
     *
     * @throws NullArgumentException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, U> Invalid<E, U> combineInvalid(Invalid<E, S> other) throws NullArgumentException {
        requireNonNull(other, "other");
        return switch (other) {
            case Critical<E, S> c -> (Invalid<E, U>) c;
            case Accumulated<E, S>(var e) -> {
                var combined = new ArrayList<E>(this.errors.size() + e.size());
                combined.addAll(this.errors);
                combined.addAll(e);
                yield new Accumulated<>(combined);
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return New {@code Accumulated<U, T>}, accumulating the results of
     * {@code errorMapper} applied to each {@link #errors} element.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Validation<U, T> mapError(
            CheckedFunction<? super E, ? extends U, ? extends X> errorMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(errorMapper, "errorMapper");
        List<U> mapped = new ArrayList<>(this.errors.size());
        for (E error : this.errors) {
            var result = requireNonNullResult(errorMapper, error);
            mapped.add(result);
        }
        return new Accumulated<>(mapped);
    }

    /**
     * {@inheritDoc}
     *
     * @return New {@code Accumulated<U, T>} containing the result of
     * {@code errorMapper}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Validation<U, T> foldErrors(
            CheckedFunction<? super List<E>, ? extends U, ? extends X> errorMapper
    ) throws X {
        requireNonNull(errorMapper, "errorMapper");
        var result = requireNonNullResult(errorMapper, this.errors);
        return new Accumulated<>(List.of(result));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    public boolean isAccumulated() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isCritical() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link #errors}.
     */
    @Override
    public List<E> getErrors() {
        return this.errors;
    }
}
