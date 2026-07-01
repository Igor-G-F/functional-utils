package io.github.igorgf.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ValidationBuilder<E> {

    private final List<Supplier<E>> checks = new ArrayList<>();

    ValidationBuilder() { /* hidden constructor */ }

    public ValidationBuilder<E> check(
            final Validation<E, ?> validation
    ) {
        Objects.requireNonNull(validation);
        switch (validation) {
            case Valid<E, ?>(_) -> { }
            case Invalid<E, ?>(var errors) -> { for (E error : errors) checks.add(() -> error); }
        }
        return this;
    }

    public <T> ValidationBuilder<E> check(
            final T value,
            final Predicate<? super T> predicate,
            final Supplier<? extends E> errorSupplier
    ) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(errorSupplier);
        checks.add(() -> predicate.test(value) ? null : errorSupplier.get());
        return this;
    }

    public <T> ValidationBuilder<E> check(
            final T value,
            final Predicate<? super T> predicate,
            final Function<? super T, ? extends E> errorMapper
    ) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(errorMapper);
        checks.add(() -> predicate.test(value) ? null : errorMapper.apply(value));
        return this;
    }

    public <R> Validation<E, R> validate(
            Supplier<? extends R> resultSupplier
    ) {
        Objects.requireNonNull(resultSupplier);
        List<E> errors = collectErrors();
        return errors.isEmpty()
                ? Validation.valid(resultSupplier.get())
                : Validation.invalid(errors);
    }

    private List<E> collectErrors() {
        var seen = new java.util.LinkedHashSet<E>();
        for (var check : checks) {
            final E error = check.get();
            if (error != null) seen.add(error);
        }
        return List.copyOf(seen);
    }

}
