package io.github.igorgf.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckedFunctionTest {

    @Test
    @DisplayName("Given apply where X is RuntimeException. When function returns. Then returns value, does not require handling X.")
    void ApplyXIsRuntimeException_FunctionReturns_ReturnsValueAndXIsStealthy() {
        CheckedFunction<Integer, Integer, RuntimeException> func = i -> ++i;
        assertEquals(2, func.apply(1));
    }

    @Test
    @DisplayName("Given apply where X is RuntimeException. When function throws X. Then does not require handling X, and X is propagated.")
    void ApplyXIsRuntimeException_FunctionThrows_XThrown() {
        final CheckedFunction<Integer, Integer, RuntimeException> func =
                _ -> { throw new RuntimeException(); };
        assertThrows(RuntimeException.class, () -> func.apply(1));
    }

    @Test
    @DisplayName("Given apply where X is Error. When function returns. Then returns value, does not require handling X.")
    void ApplyXIsError_FunctionReturns_ReturnsValueAndXIsStealthy() {
        CheckedFunction<Integer, Integer, Error> func = i -> ++i;
        assertEquals(2, func.apply(1));
    }

    @Test
    @DisplayName("Given apply where X is Error. When function throws X. Then does not require handling X, and X is propagated.")
    void ApplyXIsError_FunctionThrows_XThrown() {
        final CheckedFunction<Integer, Integer, Error> func =
                _ -> { throw new Error(); };
        assertThrows(Error.class, () -> func.apply(1));
    }

    @Test
    @DisplayName("Given apply where X is Exception. When function returns. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void ApplyXIsException_FunctionReturns_ReturnsValueAndXMustBeHandled() throws Exception {
        CheckedFunction<Integer, Integer, Exception> func = i -> ++i;
        assertEquals(2, func.apply(1));
    }

    @Test
    @DisplayName("Given apply where X is Exception. When function throws X. Then X is propagated, requires handling X.")
    void ApplyXIsException_FunctionThrows_XThrown() {
        final CheckedFunction<Integer, Integer, Exception> func =
                _ -> { throw new Exception(); };
        // assertThrows swallows X
        assertThrows(Exception.class, () -> func.apply(1));
    }

    @Test
    @DisplayName("Given apply where X is Throwable. When function returns. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void ApplyXIsThrowable_FunctionReturns_ReturnsValueAndXMustBeHandled() throws Throwable {
        CheckedFunction<Integer, Integer, Throwable> func = i -> ++i;
        assertEquals(2, func.apply(1));
    }

    @Test
    @DisplayName("Given apply where X is Throwable. When function throws X. Then X is propagated, requires handling X.")
    void ApplyXIsThrowable_FunctionThrows_XThrown() {
        final CheckedFunction<Integer, Integer, Throwable> func =
                _ -> { throw new Throwable(); };
        // assertThrows swallows X
        assertThrows(Throwable.class, () -> func.apply(1));
    }

    @Test
    @DisplayName("Given identity where X is RuntimeException. Then returns identity function, does not require handling X.")
    // An identity function returns the input of the function
    void IdentityXIsRuntimeException_ReturnsIdentityFunctionAndXIsStealthy() {
        final CheckedFunction<Integer, Integer, RuntimeException> func = CheckedFunction.identity();
        assertEquals(1, func.apply(1));
    }

    @Test
    @DisplayName("Given identity where X is Error. Then returns identity function, does not require handling X.")
    void IdentityXIsError_ReturnsIdentityFunctionAndXIsStealthy() {
        final CheckedFunction<Integer, Integer, Error> func = CheckedFunction.identity();
        assertEquals(1, func.apply(1));
    }

    @Test
    @DisplayName("Given identity where X is Exception. Then returns identity function, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void IdentityXIsException_ReturnsIdentityFunctionAndXMustBeHandled() throws Exception {
        final CheckedFunction<Integer, Integer, Exception> func = CheckedFunction.identity();
        assertEquals(1, func.apply(1));
    }

    @Test
    @DisplayName("Given identity where X is Throwable. Then returns identity function, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void IdentityXIsThrowable_ReturnsIdentityFunctionAndXMustBeHandled() throws Throwable {
        final CheckedFunction<Integer, Integer, Throwable> func = CheckedFunction.identity();
        assertEquals(1, func.apply(1));
    }

}