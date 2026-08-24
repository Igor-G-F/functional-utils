package io.github.igorgf.control;

import io.github.igorgf.GivenWhenThen;
import io.github.igorgf.GivenWhenThenGenerator;
import io.github.igorgf.function.CheckedFunction;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DisplayNameGeneration(GivenWhenThenGenerator.class)
class TryValueTest {

    private static final CheckedFunction<Integer, Integer, RuntimeException> FUNC = i -> ++i;

    @Test
    @GivenWhenThen(
            given = "new TryValue",
            when = "\"function\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryValue_FunctionIsNull() {
        var result = assertThrows(NullArgumentException.class,() -> new TryValue<>(
                null,
                () -> 7
        ));
        assertEquals("Contract violation. Argument \"function\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "new TryValue",
            when = "\"argSupplier\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryValue_ArgSupplierIsNull() {
        var result = assertThrows(NullArgumentException.class, () -> new TryValue<>(
                FUNC,
                null
        ));
        assertEquals("Contract violation. Argument \"argSupplier\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"argSupplier\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_ArgSupplierReturnsNull() {
        var testTry = new TryValue<>(
                FUNC,
                () -> null
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. \"argSupplier\" supplied a \"null\" value.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_FunctionReturnsNull() {
        var testTry = new TryValue<>(
                _ -> null,
                () -> 7
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
        var testTry = new TryValue<>(
                _ -> {
                    throw new Error();
                },
                () -> 7
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
        var x = new Exception("test me");
        var testTry = new TryValue<>(
                _ -> {
                    throw x;
                },
                () -> 7
        );

        var result = testTry.execute();

        assertEquals(Either.left(Thrown.of(x)), result);
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" returns T",
            then = "returns Right of T"
    )
    void execute_FunctionReturns() {
        var testTry = new TryValue<>(
                FUNC,
                () -> 7
        );

        var result = testTry.execute();

        assertEquals(Either.right(8), result);
    }
}