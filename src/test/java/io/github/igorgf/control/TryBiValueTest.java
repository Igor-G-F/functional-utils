package io.github.igorgf.control;

import io.github.igorgf.GivenWhenThen;
import io.github.igorgf.GivenWhenThenGenerator;
import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedRunnable;
import io.github.igorgf.function.CheckedSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayNameGeneration(GivenWhenThenGenerator.class)
class TryBiValueTest {

    private static final CheckedBiFunction<Integer, Long, String, RuntimeException> FUNC =
            (s0, s1) -> String.valueOf(s0 + s1);


    @Test
    @GivenWhenThen(
            given = "new TryBiValue",
            when = "\"function\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryBiValue_FunctionIsNull() {
        var result = assertThrows(NullArgumentException.class,() -> {
            new TryBiValue<>(
                    null,
                    () -> 7,
                    () -> 7L
            );
        });
        assertEquals("Contract violation. Argument \"function\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "new TryBiValue",
            when = "\"arg0Supplier\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryBiValue_Arg0SupplierIsNull() {
        var result = assertThrows(NullArgumentException.class, () -> {
            new TryBiValue<>(
                    FUNC,
                    null,
                    () -> 7L
            );
        });
        assertEquals("Contract violation. Argument \"arg0Supplier\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "new TryBiValue",
            when = "\"arg1Supplier\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryBiValue_Arg1SupplierIsNull() {
        var result = assertThrows(NullArgumentException.class, () -> {
            new TryBiValue<>(
                    FUNC,
                    () -> 7,
                    null
            );
        });
        assertEquals("Contract violation. Argument \"arg1Supplier\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"arg0Supplier\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_Arg0SupplierReturnsNull() {
        var testTry = new TryBiValue<>(
                FUNC,
                () -> null,
                () -> 7L
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. \"arg0Supplier\" supplied a \"null\" value.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"arg1Supplier\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_Arg1SupplierReturnsNull() {
        var testTry = new TryBiValue<>(
                FUNC,
                () -> 7,
                () -> null
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. \"arg1Supplier\" supplied a \"null\" value.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_FunctionReturnsNull() {
        var testTry = new TryBiValue<>(
                (_, _) -> null,
                () -> 7,
                () -> 7L
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" throws an Error",
            then = "throws Error"
    )
    void execute_FunctionThrowsError() {
        var testTry = new TryBiValue<>(
                (_, _) -> {
                    throw new Error();
                },
                () -> 7,
                () -> 7L
        );

        assertThrows(Error.class, testTry::execute);
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" throws X",
            then = "returns Left of X"
    )
    void execute_FunctionThrowsX() {
        var exception = new Exception("test me");
        var testTry = new TryBiValue<>(
                (_, _) -> {
                    throw exception;
                },
                () -> 7,
                () -> 7L
        );

        var result = testTry.execute();

        assertEquals(Either.left(new Thrown(exception)), result);
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" returns T",
            then = "returns Right of T"
    )
    void execute_FunctionReturns() {
        var testTry = new TryBiValue<>(
                FUNC,
                () -> 7,
                () -> 7L
        );

        var result = testTry.execute();

        assertEquals(Either.right("14"), result);
    }
}