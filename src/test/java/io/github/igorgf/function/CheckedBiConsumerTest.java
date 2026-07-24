package io.github.igorgf.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckedBiConsumerTest {

    @Test
    @DisplayName("Given accept where X is RuntimeException. When bi-consumer success. Then returns value, does not require handling X.")
    void AcceptXIsRuntimeException_BiConsumerReturns_ReturnsValueAndXIsStealthy() {
        var i = new AtomicInteger(1);
        CheckedBiConsumer<AtomicInteger, AtomicInteger, RuntimeException> func = (i1, i2) -> {
            i1.getAndIncrement();
            i2.getAndIncrement();
        };
        func.accept(i, i);
        assertEquals(3, i.get());
    }

    @Test
    @DisplayName("Given accept where X is RuntimeException. When bi-consumer throws X. Then does not require handling X, and X is propagated.")
    void AcceptXIsRuntimeException_BiConsumerThrows_XThrown() {
        final CheckedBiConsumer<Integer, Integer, RuntimeException> func =
                (_, _) -> { throw new RuntimeException(); };
        assertThrows(RuntimeException.class, () -> func.accept(7, 7));
    }

    @Test
    @DisplayName("Given accept where X is Error. When bi-consumer success. Then returns value, does not require handling X.")
    void AcceptXIsError_BiConsumerReturns_ReturnsValueAndXIsStealthy() {
        var i = new AtomicInteger(1);
        CheckedBiConsumer<AtomicInteger, AtomicInteger, Error> func = (i1, i2) -> {
            i1.getAndIncrement();
            i2.getAndIncrement();
        };
        func.accept(i, i);
        assertEquals(3, i.get());
    }

    @Test
    @DisplayName("Given accept where X is Error. When bi-consumer throws X. Then does not require handling X, and X is propagated.")
    void AcceptXIsError_BiConsumerThrows_XThrown() {
        final CheckedBiConsumer<Integer, Integer, Error> func =
                (_, _) -> { throw new Error(); };
        assertThrows(Error.class, () -> func.accept(7, 7));
    }

    @Test
    @DisplayName("Given accept where X is Exception. When bi-consumer success. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void AcceptXIsException_BiConsumerReturns_ReturnsValueAndXMustBeHandled() throws Exception {
        var i = new AtomicInteger(1);
        CheckedBiConsumer<AtomicInteger, AtomicInteger, Exception> func = (i1, i2) -> {
            i1.getAndIncrement();
            i2.getAndIncrement();
        };
        func.accept(i, i);
        assertEquals(3, i.get());
    }

    @Test
    @DisplayName("Given accept where X is Exception. When bi-consumer throws X. Then X is propagated, requires handling X.")
    void AcceptXIsException_BiConsumerThrows_XThrown() {
        final CheckedBiConsumer<Integer, Integer, Exception> func =
                (_, _) -> { throw new Exception(); };
        // assertThrows swallows X
        assertThrows(Exception.class, () -> func.accept(7, 7));
    }

    @Test
    @DisplayName("Given accept where X is Throwable. When bi-consumer success. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void AcceptXIsThrowable_BiConsumerReturns_ReturnsValueAndXMustBeHandled() throws Throwable {
        var i = new AtomicInteger(1);
        CheckedBiConsumer<AtomicInteger, AtomicInteger, Throwable> func = (i1, i2) -> {
            i1.getAndIncrement();
            i2.getAndIncrement();
        };
        func.accept(i, i);
        assertEquals(3, i.get());
    }

    @Test
    @DisplayName("Given accept where X is Throwable. When bi-consumer throws X. Then X is propagated, requires handling X.")
    void AcceptXIsThrowable_BiConsumerThrows_XThrown() {
        final CheckedBiConsumer<Integer, Integer, Throwable> func =
                (_, _) -> { throw new Throwable(); };
        // assertThrows swallows X
        assertThrows(Throwable.class, () -> func.accept(7, 7));
    }

}