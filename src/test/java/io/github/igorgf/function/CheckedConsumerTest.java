package io.github.igorgf.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckedConsumerTest {

    @Test
    @DisplayName("Given accept where X is RuntimeException. When consumer success. Then returns value, does not require handling X.")
    void AcceptXIsRuntimeException_ConsumerReturns_ReturnsValueAndXIsStealthy() {
        var i = new AtomicInteger(1);
        CheckedConsumer<AtomicInteger, RuntimeException> func = AtomicInteger::getAndIncrement;
        func.accept(i);
        assertEquals(2, i.get());
    }

    @Test
    @DisplayName("Given accept where X is RuntimeException. When consumer throws X. Then does not require handling X, and X is propagated.")
    void AcceptXIsRuntimeException_ConsumerThrows_XThrown() {
        final CheckedConsumer<Integer, RuntimeException> func =
                _ -> { throw new RuntimeException(); };
        assertThrows(RuntimeException.class, () -> func.accept(7));
    }

    @Test
    @DisplayName("Given accept where X is Error. When consumer success. Then returns value, does not require handling X.")
    void AcceptXIsError_ConsumerReturns_ReturnsValueAndXIsStealthy() {
        var i = new AtomicInteger(1);
        CheckedConsumer<AtomicInteger, Error> func = AtomicInteger::getAndIncrement;
        func.accept(i);
        assertEquals(2, i.get());
    }

    @Test
    @DisplayName("Given accept where X is Error. When consumer throws X. Then does not require handling X, and X is propagated.")
    void AcceptXIsError_ConsumerThrows_XThrown() {
        final CheckedConsumer<Integer, Error> func =
                _ -> { throw new Error(); };
        assertThrows(Error.class, () -> func.accept(7));
    }

    @Test
    @DisplayName("Given accept where X is Exception. When consumer success. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void AcceptXIsException_ConsumerReturns_ReturnsValueAndXMustBeHandled() throws Exception {
        var i = new AtomicInteger(1);
        CheckedConsumer<AtomicInteger, Exception> func = AtomicInteger::getAndIncrement;
        func.accept(i);
        assertEquals(2, i.get());
    }

    @Test
    @DisplayName("Given accept where X is Exception. When consumer throws X. Then X is propagated, requires handling X.")
    void AcceptXIsException_ConsumerThrows_XThrown() {
        final CheckedConsumer<Integer, Exception> func =
                _ -> { throw new Exception(); };
        // assertThrows swallows X
        assertThrows(Exception.class, () -> func.accept(7));
    }

    @Test
    @DisplayName("Given accept where X is Throwable. When consumer success. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void AcceptXIsThrowable_ConsumerReturns_ReturnsValueAndXMustBeHandled() throws Throwable {
        var i = new AtomicInteger(1);
        CheckedConsumer<AtomicInteger, Throwable> func = AtomicInteger::getAndIncrement;
        func.accept(i);
        assertEquals(2, i.get());
    }

    @Test
    @DisplayName("Given accept where X is Throwable. When consumer throws X. Then X is propagated, requires handling X.")
    void AcceptXIsThrowable_ConsumerThrows_XThrown() {
        final CheckedConsumer<Integer, Throwable> func =
                _ -> { throw new Throwable(); };
        // assertThrows swallows X
        assertThrows(Throwable.class, () -> func.accept(7));
    }

}