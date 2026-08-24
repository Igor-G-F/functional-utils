package io.github.igorgf.control;

import io.github.igorgf.function.CheckedFunction;

import java.util.List;

import static io.github.igorgf.control.ControlUtils.requireNonNull;
import static io.github.igorgf.control.ControlUtils.requireNonNullResult;

/**
 * The invalid, <em>left</em> side, implementation of {@code Validation<E, T>},
 * inheriting shared <em>failed</em> validation behaviour from
 * {@code Invalid<E, T>}.
 * <p>
 * Contains a major validation {@link #error} {@code E}, short-circuiting the
 * validation chain. {@code Critical} bypasses every subsequent
 * {@link Validation#combine(Validation)} or its overload, discarding any
 * previously accumulated errors or any subsequent errors, and only preserving
 * its own contained {@link #error}.
 *
 * @see Validation
 * @see Invalid
 * @see Accumulated
 * @see Valid
 *
 * @param error The contained validation error value {@code E}. Never
 *        {@code null}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 *
 * @param <E> The contained error type.
 * @param <T> The validation target type.
 */
public record Critical<E, T>(E error) implements Invalid<E, T> {

    public Critical {
        requireNonNull(error, "error");
    }

    /**
     * {@inheritDoc}
     * <p>
     * The unchecked cast from {@code this} to {@code Invalid<E, U>} is
     * provably safe because an {@code Invalid} does not contain any value
     * {@code T}, it only contains errors {@code E}.
     *
     * @return {@code this} as {@code Invalid<E, U>}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <S, U> Invalid<E, U> combineInvalid(Invalid<E, S> other) {
        return (Invalid<E, U>) this;
    }

    /**
     * {@inheritDoc}
     *
     * @return New {@code Critical<U, T>} containing the result of
     * {@code errorMapper}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Validation<U, T> mapError(
            CheckedFunction<? super E, ? extends U, ? extends X> errorMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(errorMapper,  "errorMapper");
        var result = requireNonNullResult(errorMapper, error);
        return new Critical<>(result);
    }

    /**
     * {@inheritDoc}
     *
     * @return New {@code Critical<U, T>} containing the result of
     * {@code errorMapper}.
     *
     * @throws X {@inheritDoc}
     * @throws NullArgumentException {@inheritDoc}
     * @throws NullResultException {@inheritDoc}
     */
    @Override
    public <U, X extends Throwable> Validation<U, T> foldErrors(
            CheckedFunction<? super List<E>, ? extends U, ? extends X> errorMapper
    ) throws X, NullArgumentException, NullResultException {
        requireNonNull(errorMapper,  "errorMapper");
        var result = requireNonNullResult(errorMapper, getErrors());
        return new Critical<>(result);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false}
     */
    @Override
    public boolean isAccumulated() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true}
     */
    @Override
    public boolean isCritical() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code List<E>} containing one element {@link #error}.
     */
    @Override
    public List<E> getErrors() {
        return List.of(error);
    }
}
