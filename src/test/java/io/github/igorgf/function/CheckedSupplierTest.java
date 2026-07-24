package io.github.igorgf.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckedSupplierTest {

    @Test
    @DisplayName("Given get where X is RuntimeException. When supplier returns. Then returns value, does not require handling X.")
    void GetXIsRuntimeException_SupplierReturns_ReturnsValueAndXIsStealthy() {
        CheckedSupplier<Integer, RuntimeException> func = () -> 2;
        assertEquals(2, func.get());
    }

    @Test
    @DisplayName("Given get where X is RuntimeException. When supplier throws X. Then does not require handling X, and X is propagated.")
    void GetXIsRuntimeException_SupplierThrows_XThrown() {
        final CheckedSupplier<Integer, RuntimeException> func =
                () -> { throw new RuntimeException(); };
        assertThrows(RuntimeException.class, func::get);
    }

    @Test
    @DisplayName("Given get where X is Error. When supplier returns. Then returns value, does not require handling X.")
    void GetXIsError_SupplierReturns_ReturnsValueAndXIsStealthy() {
        CheckedSupplier<Integer, Error> func = () -> 2;
        assertEquals(2, func.get());
    }

    @Test
    @DisplayName("Given get where X is Error. When supplier throws X. Then does not require handling X, and X is propagated.")
    void GetXIsError_SupplierThrows_XThrown() {
        final CheckedSupplier<Integer, Error> func =
                () -> { throw new Error(); };
        assertThrows(Error.class, func::get);
    }

    @Test
    @DisplayName("Given get where X is Exception. When supplier returns. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void GetXIsException_SupplierReturns_ReturnsValueAndXMustBeHandled() throws Exception {
        CheckedSupplier<Integer, Exception> func = () -> 2;
        assertEquals(2, func.get());
    }

    @Test
    @DisplayName("Given get where X is Exception. When supplier throws X. Then X is propagated, requires handling X.")
    void GetXIsException_SupplierThrows_XThrown() {
        final CheckedSupplier<Integer, Exception> func =
                () -> { throw new Exception(); };
        // assertThrows swallows X
        assertThrows(Exception.class, func::get);
    }

    @Test
    @DisplayName("Given get where X is Throwable. When supplier returns. Then returns value, requires handling X.")
    // The test here is whether the throws clause is required in the caller context
    void GetXIsThrowable_SupplierReturns_ReturnsValueAndXMustBeHandled() throws Throwable {
        CheckedSupplier<Integer, Throwable> func = () -> 2;
        assertEquals(2, func.get());
    }

    @Test
    @DisplayName("Given get where X is Throwable. When supplier throws X. Then X is propagated, requires handling X.")
    void GetXIsThrowable_SupplierThrows_XThrown() {
        final CheckedSupplier<Integer, Throwable> func =
                () -> { throw new Throwable(); };
        // assertThrows swallows X
        assertThrows(Throwable.class, func::get);
    }

}