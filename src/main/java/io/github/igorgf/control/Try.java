package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedRunnable;

import java.util.Objects;

public sealed interface Try<R> permits TryWithResources, TryFunction, TryBiFunction, WithFinally {

    Either<Thrown, R> execute();

    static <T, R> TryFunctionStep<T, R> of(
            CheckedFunction<T, R, Exception> function
    ) {
        Objects.requireNonNull(function);
        return new TryFunctionStep<>(function);
    }

    static <T, U, R> TryBiFunctionStep<T, U, R> of(
            CheckedBiFunction<T, U, R, Exception> function
    ) {
        Objects.requireNonNull(function);
        return new TryBiFunctionStep<>(function);
    }

    default Try<R> withFinally(
            CheckedRunnable<Exception> action
    ) {
        Objects.requireNonNull(action);
        return new WithFinally<>(this, action);
    }

    record TryFunctionStep<T, R>(
            CheckedFunction<T, R, Exception> function
    ) {
        public Try<R> withParam(T param) {
            Objects.requireNonNull(param);
            return new TryFunction<>(function, param);
        }
    }

    record TryBiFunctionStep<T, U, R>(
            CheckedBiFunction<T, U, R, Exception> function
    ) {
        public Try<R> withParams(T param, U param2) {
            Objects.requireNonNull(param);
            Objects.requireNonNull(param2);
            return new TryBiFunction<>(function, param, param2);
        }
    }

}

record TryFunction<T, R>(
        CheckedFunction<T, R, Exception> function,
        T param
) implements Try<R> {
    @Override
    public Either<Thrown, R> execute() {
        try {
            return Either.right(function.apply(param));
        } catch (Exception e) {
            return Either.left(new Thrown(e));
        }
    }
}

record TryBiFunction<T, U, R>(
        CheckedBiFunction<T, U, R, Exception> function,
        T param,
        U param2
) implements Try<R> {
    @Override
    public Either<Thrown, R> execute() {
        try {
            return Either.right(function.apply(param, param2));
        } catch (Exception e) {
            return Either.left(new Thrown(e));
        }
    }
}

record WithFinally<R>(
        Try<R> delegate,
        CheckedRunnable<Exception> action
) implements Try<R> {
    @Override
    public Either<Thrown, R> execute() {
        Either<Thrown, R> result = delegate.execute();
        try {
            action.run();
        } catch (Exception e) {
            return switch (result) {
                case Right<Thrown, R>(_) -> Either.left(new Thrown(e));
                case Left<Thrown, R>(var thrown) -> {
                    thrown.get().addSuppressed(e);
                    yield result;
                }
            };
        }
        return result;
    }
}