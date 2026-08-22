package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.Collection;

/**
 * Contains helper methods for {@link io.github.igorgf.control} flow types.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
final class ControlUtils {

    private ControlUtils() {
        // hidden
    }

    /**
     * @throws NullArgumentException If {@code obj} is a {@code null}.
     */
    static <T> T requireNonNull(T obj, String fieldName) throws NullArgumentException {
        if (obj == null) {
            throw new NullArgumentException(fieldName);
        }
        return obj;
    }

    /**
     * @throws NullArgumentException If {@code col} is a {@code null}.
     * @throws EmptyArgumentException If {@code col} is empty.
     */
    static <T, U extends Collection<T>> U requireNonEmpty(
            U col, String fieldName
    ) throws EmptyArgumentException, NullArgumentException {
        requireNonNull(col, fieldName);
        if (col.isEmpty()) {
            throw new EmptyArgumentException(fieldName);
        }
        return col;
    }

    /**
     * @throws NullResultException If {@code supplier} returns a {@code null}.
     */
    static <T, X extends Throwable> T requireNonNullResult(
            CheckedSupplier<T, X> supplier,  String reference
    ) throws X, NullResultException {
        T result = supplier.get();
        if (result == null) {
            throw new NullResultException("\"" + reference + "\" supplied a \"null\" value.");
        }
        return result;
    }

    /**
     * @throws NullResultException If {@code function} returns a {@code null}.
     */
    static <T, R, X extends Throwable> R requireNonNullResult(
            CheckedFunction<T, R, X> function, T param
    ) throws X, NullResultException {
        R result = function.apply(param);
        if (result == null) {
            throw new NullResultException();
        }
        return result;
    }

    /**
     * @throws NullResultException If {@code function} returns a {@code null}.
     */
    static <T, U, R, X extends Throwable> R requireNonNullResult(
            CheckedBiFunction<T, U, R, X> function, T param, U param2
    ) throws X, NullResultException {
        R result = function.apply(param, param2);
        if (result == null) {
            throw new NullResultException();
        }
        return result;
    }

}
