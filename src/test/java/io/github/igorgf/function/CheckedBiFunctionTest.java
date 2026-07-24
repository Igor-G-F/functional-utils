package io.github.igorgf.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckedBiFunctionTest {

    @Test
    @DisplayName("Given apply where X is RuntimeException. When bifunction returns. Then returns value, does not require handling X.")
    void ApplyXIsRuntimeException_BiFunctionReturns_ReturnsValueAndXIsStealthy() {
        CheckedBiFunction<Integer, Integer, Integer, RuntimeException> func = Integer::sum;
        assertEquals(4, func.apply(1, 3));
    }

    @Test
    @DisplayName("Given apply where X is RuntimeException. When bifunction throws X. Then does not require handling X, and X is propagated.")
    void ApplyXIsRuntimeException_BiFunctionThrows_XThrown() {
        final CheckedBiFunction<Integer, Integer, Integer, RuntimeException> func =
                (_, _) -> { throw new RuntimeException(); };
        assertThrows(RuntimeException.class, () -> func.apply(1, 3));
    }

    @Test
    @DisplayName("Given apply where X is Error. When bifunction returns. Then returns value, does not require handling X.")
    void ApplyXIsError_BiFunctionReturns_ReturnsValueAndXIsStealthy() {
        CheckedBiFunction<Integer, Integer, Integer, Error> func = Integer::sum;
        assertEquals(4, func.apply(1, 3));
    }

    @Test
    @DisplayName("Given apply where X is Error. When bifunction throws X. Then does not require handling X, and X is propagated.")
    void ApplyXIsError_BiFunctionThrows_XThrown() {
        final CheckedBiFunction<Integer, Integer, Integer, Error> func =
                (_, _) -> { throw new Error(); };
        assertThrows(Error.class, () -> func.apply(1, 3));
    }

    @Test
    @DisplayName("Given apply where X is Exception. When bifunction success. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void ApplyXIsException_BiFunctionReturns_ReturnsValueAndXMustBeHandled() throws Exception {
        CheckedBiFunction<Integer, Integer, Integer, Exception> func = Integer::sum;
        assertEquals(4, func.apply(1, 3));
    }

    @Test
    @DisplayName("Given apply where X is Exception. When bifunction throws X. Then X is propagated, requires handling X.")
    void ApplyXIsException_BiFunctionThrows_XThrown() {
        final CheckedBiFunction<Integer, Integer, Integer, Exception> func =
                (_, _) -> { throw new Exception(); };
        // assertThrows swallows X
        assertThrows(Exception.class, () -> func.apply(1, 3));
    }

    @Test
    @DisplayName("Given apply where X is Throwable. When bifunction success. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void ApplyXIsThrowable_BiFunctionReturns_ReturnsValueAndXMustBeHandled() throws Throwable {
        CheckedBiFunction<Integer, Integer, Integer, Throwable> func = Integer::sum;
        assertEquals(4, func.apply(1, 3));
    }

    @Test
    @DisplayName("Given apply where X is Throwable. When bifunction throws X. Then X is propagated, requires handling X.")
    void ApplyXIsThrowable_BiFunctionThrows_XThrown() {
        final CheckedBiFunction<Integer, Integer, Integer, Throwable> func =
                (_, _) -> { throw new Throwable(); };
        // assertThrows swallows X
        assertThrows(Throwable.class, () -> func.apply(1, 3));
    }

}