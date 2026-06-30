package io.github.igorgf.control;

import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedFunction;
import io.github.igorgf.function.CheckedSupplier;

import java.util.Objects;

public sealed interface TryWithResources<R> extends Try<R> {

    static <T extends AutoCloseable, R> TryFunctionWithResourceStep<T, R> of(
            CheckedFunction<T, R, Exception> function
    ) {
        Objects.requireNonNull(function);
        return new TryFunctionWithResourceStep<>(function);
    }

    static <T extends AutoCloseable, U extends AutoCloseable, R> TryBiFunctionWithResourcesStep<T, U, R> of(
            CheckedBiFunction<T, U, R, Exception> function
    ) {
        Objects.requireNonNull(function);
        return new TryBiFunctionWithResourcesStep<>(function);
    }

    record TryFunctionWithResourceStep<T extends AutoCloseable, R>(
            CheckedFunction<T, R, Exception> function
    ) {
        public Try<R> withResource(CheckedSupplier<T, Exception> supplier) {
            Objects.requireNonNull(supplier);
            return new TryFunctionWithResource<>(function, supplier);
        }
    }

    record TryBiFunctionWithResourcesStep<T extends AutoCloseable, U extends AutoCloseable, R>(
            CheckedBiFunction<T, U, R, Exception> function
    ) {
        public Try<R> withResources(
                CheckedSupplier<T, Exception> supplier,
                CheckedSupplier<U, Exception> supplier2
        ) {
            Objects.requireNonNull(supplier);
            Objects.requireNonNull(supplier2);
            return new TryBiFunctionWithResources<>(function, supplier, supplier2);
        }
    }

}

record TryBiFunctionWithResources<T extends AutoCloseable, U extends AutoCloseable, R>(
        CheckedBiFunction<T, U, R, Exception> function,
        CheckedSupplier<T, Exception> supplier,
        CheckedSupplier<U, Exception> supplier2
) implements TryWithResources<R> {
    @Override
    public Either<Thrown, R> execute() {
        try (
                T resource = supplier.get();
                U resource2 = supplier2.get()
        ) {
            return Either.right(function.apply(resource, resource2));
        } catch (Exception e) {
            return Either.left(new Thrown(e));
        }
    }
}

record TryFunctionWithResource<T extends AutoCloseable, R>(
        CheckedFunction<T, R, Exception> function,
        CheckedSupplier<T, Exception> supplier
) implements TryWithResources<R> {
    @Override
    public Either<Thrown, R> execute() {
        try (T resource = supplier.get()) {
            return Either.right(function.apply(resource));
        } catch (Exception e) {
            return Either.left(new Thrown(e));
        }
    }
}