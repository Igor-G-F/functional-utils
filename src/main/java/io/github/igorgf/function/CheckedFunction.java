package io.github.igorgf.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedFunction<T, R, X extends Exception> {

    R apply(T t) throws X;

    default <V> CheckedFunction<V, R, X> compose(
            CheckedFunction<? super V, ? extends T, X> before
    ) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> CheckedFunction<T, V, X> andThen(
            CheckedFunction<? super R, ? extends V, X> after
    ) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T, X extends Exception> CheckedFunction<T, T, X> identity() {
        return t -> t;
    }

}
