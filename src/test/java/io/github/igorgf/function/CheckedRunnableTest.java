package io.github.igorgf.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckedRunnableTest {

    @Test
    @DisplayName("Given run where X is RuntimeException. When runnable success. Then does not require handling X.")
    void RunXIsRuntimeException_RunnableReturns_ReturnsValueAndXIsStealthy() {
        var i = new AtomicInteger(1);
        CheckedRunnable<RuntimeException> func = i::getAndIncrement;
        func.run();
        assertEquals(2, i.get());
    }

    @Test
    @DisplayName("Given run where X is RuntimeException. When runnable throws X. Then does not require handling X, and X is propagated.")
    void RunXIsRuntimeException_RunnableThrows_XThrown() {
        final CheckedRunnable<RuntimeException> func = () -> { throw new RuntimeException(); };
        assertThrows(RuntimeException.class, func::run);
    }

    @Test
    @DisplayName("Given run where X is Error. When runnable success. Then does not require handling X.")
    void RunXIsError_RunnableReturns_ReturnsValueAndXIsStealthy() {
        var i = new AtomicInteger(1);
        CheckedRunnable<Error> func = i::getAndIncrement;
        func.run();
        assertEquals(2, i.get());
    }

    @Test
    @DisplayName("Given run where X is Error. When runnable throws X. Then does not require handling X, and X is propagated.")
    void RunXIsError_RunnableThrows_XThrown() {
        final CheckedRunnable<Error> func = () -> { throw new Error(); };
        assertThrows(Error.class, func::run);
    }

    @Test
    @DisplayName("Given run where X is Exception. When runnable success. Then requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void RunXIsException_RunnableReturns_ReturnsValueAndXMustBeHandled() throws Exception {
        var i = new AtomicInteger(1);
        CheckedRunnable<Exception> func = i::getAndIncrement;
        func.run();
        assertEquals(2, i.get());
    }

    @Test
    @DisplayName("Given run where X is Exception. When runnable throws X. Then X is propagated, requires handling X.")
    void RunXIsException_RunnableThrows_XThrown() {
        final CheckedRunnable<Exception> func = () -> { throw new Exception(); };
        // assertThrows swallows X
        assertThrows(Exception.class, func::run);
    }

    @Test
    @DisplayName("Given run where X is Throwable. When runnable success. Then requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void RunXIsThrowable_RunnableReturns_ReturnsValueAndXMustBeHandled() throws Throwable {
        var i = new AtomicInteger(1);
        CheckedRunnable<Throwable> func = i::getAndIncrement;
        func.run();
        assertEquals(2, i.get());
    }

    @Test
    @DisplayName("Given run where X is Throwable. When runnable throws X. Then X is propagated, requires handling X.")
    void RunXIsThrowable_RunnableThrows_XThrown() {
        final CheckedRunnable<Throwable> func = () -> { throw new Throwable(); };
        // assertThrows swallows X
        assertThrows(Throwable.class, func::run);
    }

}