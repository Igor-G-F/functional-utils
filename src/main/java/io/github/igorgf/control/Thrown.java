package io.github.igorgf.control;

import io.github.igorgf.function.CheckedConsumer;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public record Thrown(Exception thrown) {

    public Thrown {
        Objects.requireNonNull(thrown, "Thrown exception cannot be null");
    }

    public Exception get() {
        return thrown;
    }

    public boolean isRuntimeException() {
        return thrown instanceof RuntimeException;
    }

    public boolean isCheckedException() {
        return !(thrown instanceof RuntimeException);
    }

    @SafeVarargs
    public final Thrown handle(Consumer<Exception> handler, Class<? extends Exception>... exceptions) {
        if (exceptions.length == 0 || Set.of(exceptions).contains(thrown.getClass())) {
            handler.accept(thrown);
        }
        return this;
    }

    @SafeVarargs
    public final Thrown rethrow(Class<? extends Exception>... exceptions) {
        if (exceptions.length == 0 || Set.of(exceptions).contains(thrown.getClass())) {
            throw new RuntimeException(thrown);
        }
        return this;
    }

    public <T extends Exception, X extends Exception> Thrown handleChecked(CheckedConsumer<T, X> handler, Class<T> exceptionType) throws X {
        if (exceptionType.isInstance(this.thrown)) {
            handler.accept(exceptionType.cast(this.thrown));
        }
        return this;
    }

    public <T extends Exception> Thrown rethrowChecked(Class<T> exceptionType) throws T {
        if (exceptionType.isAssignableFrom(this.thrown.getClass())) {
            throw exceptionType.cast(this.thrown);
        }
        return this;
    }
}
