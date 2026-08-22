package io.github.igorgf.function;

//TODO: document
@FunctionalInterface
public interface TriFunction<T, U, S, R> {

    R apply(T t, U u, S s);

}
