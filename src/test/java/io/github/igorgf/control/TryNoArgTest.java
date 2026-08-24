package io.github.igorgf.control;

import io.github.igorgf.GivenWhenThen;
import io.github.igorgf.GivenWhenThenGenerator;
import io.github.igorgf.function.CheckedSupplier;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(GivenWhenThenGenerator.class)
class TryNoArgTest {
    
    private static final CheckedSupplier<Integer, RuntimeException> FUNC = () -> 7;

    @Test
    @GivenWhenThen(
            given = "new TryNoArg",
            when = "\"function\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryNoArg_FunctionIsNull() {
        var result = assertThrows(NullArgumentException.class,() -> new TryNoArg<>(null));
        assertEquals("Contract violation. Argument \"function\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_FunctionReturnsNull() {
        var testTry = new TryNoArg<>(() -> null);

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. \"function\" supplied a \"null\" value.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" throws an Error",
            then = "throws Error"
    )
    void execute_FunctionThrowsError() {
        var testTry = new TryNoArg<>(() -> { throw new Error(); });

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
        var testTry = new TryNoArg<>(() -> { throw exception; });

        var result = testTry.execute();

        assertEquals(Either.left(Thrown.of(exception)), result);
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"function\" returns T",
            then = "returns Right of T"
    )
    void execute_FunctionReturns() {
        var testTry = new TryNoArg<>(FUNC);

        var result = testTry.execute();

        assertEquals(Either.right(7), result);
    }
}