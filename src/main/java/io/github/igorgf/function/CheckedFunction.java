package io.github.igorgf.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedFunction<T, R, E extends Exception> {

        R apply(T t) throws E;

        default <V> CheckedFunction<V, R, E> compose(CheckedFunction<? super V, ? extends T, E> before) {
            Objects.requireNonNull(before);
            return (V v) -> apply(before.apply(v));
        }

        default <V> CheckedFunction<T, V, E> andThen(CheckedFunction<? super R, ? extends V, E> after) {
            Objects.requireNonNull(after);
            return (T t) -> after.apply(apply(t));
        }

    }
