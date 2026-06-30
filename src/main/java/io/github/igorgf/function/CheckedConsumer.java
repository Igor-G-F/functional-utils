package io.github.igorgf.function;

import java.util.Objects;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {

    void accept(T t) throws E;

    default <X extends Exception> CheckedConsumer<T, Exception> andThen(CheckedConsumer<? super T, ? extends X> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}