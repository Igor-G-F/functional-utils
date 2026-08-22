package io.github.igorgf.control;

import io.github.igorgf.CloseableInputStream;
import io.github.igorgf.GivenWhenThen;
import io.github.igorgf.GivenWhenThenGenerator;
import io.github.igorgf.UncloseableInputStream;
import io.github.igorgf.function.CheckedBiFunction;
import io.github.igorgf.function.CheckedSupplier;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(GivenWhenThenGenerator.class)
class TryBiResourceTest {

    private static final CheckedBiFunction<CloseableInputStream, CloseableInputStream, Integer, RuntimeException> FUNC =
            (s0, s1) -> s0.read() + s1.read();
    private static final CheckedSupplier<CloseableInputStream, ? extends Throwable> RES_SUP = () -> new CloseableInputStream("Test".getBytes());

    @Test
    @GivenWhenThen(
            given = "new TryBiResource",
            when = "\"function\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryBiResource_FunctionIsNull() {
        var result = assertThrows(NullArgumentException.class,() -> {
            new TryBiResource<>(
                    null,
                    RES_SUP,
                    RES_SUP
            );
        });
        assertEquals("Contract violation. Argument \"function\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "new TryBiResource",
            when = "\"res0Supplier\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryBiResource_Res0SupplierIsNull() {
        var result = assertThrows(NullArgumentException.class, () -> {
            new TryBiResource<>(
                    FUNC,
                    null,
                    RES_SUP
            );
        });
        assertEquals("Contract violation. Argument \"res0Supplier\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "new TryBiResource",
            when = "\"res1Supplier\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryBiResource_Res1SupplierIsNull() {
        var result = assertThrows(NullArgumentException.class, () -> {
            new TryBiResource<>(
                    FUNC,
                    RES_SUP,
                    null
            );
        });
        assertEquals("Contract violation. Argument \"res1Supplier\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"res0Supplier\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_Res0SupplierReturnsNull() {
        var testTry = new TryBiResource<>(
                FUNC,
                () -> null,
                RES_SUP
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. \"res0Supplier\" supplied a \"null\" value.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"res1Supplier\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_Res1SupplierReturnsNull() {
        var testTry = new TryBiResource<>(
                FUNC,
                RES_SUP,
                () -> null
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. \"res1Supplier\" supplied a \"null\" value.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns a \"null\"", "and resources are closed"},
            then = "throws NullResultException"
    )
    void execute_FunctionReturnsNull() {
        var res0 = new CloseableInputStream("Test".getBytes());
        var res1 = new CloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                (_, _) -> null,
                () -> res0,
                ()  -> res1
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
        assertTrue(res0.isClosed());
        assertTrue(res1.isClosed());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns a \"null\"", "and resource 0 fails to close"},
            then = "throws NullResultException with suppressed IOException"
    )
    void execute_FunctionReturnsNull_Resource0FailsToClose() {
        var res0 = new UncloseableInputStream("Test".getBytes());
        var res1 = new CloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                (_, _) -> null,
                () -> res0,
                ()  -> res1
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
        assertEquals("Failed to close: " + res0, result.getSuppressed()[0].getMessage());
        assertTrue(res1.isClosed());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns a \"null\"", "and resource 1 fails to close"},
            then = "throws NullResultException with suppressed IOException"
    )
    void execute_FunctionReturnsNull_Resource1FailsToClose() {
        var res0 = new CloseableInputStream("Test".getBytes());
        var res1 = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                (_, _) -> null,
                () -> res0,
                ()  -> res1
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
        assertEquals("Failed to close: " + res1, result.getSuppressed()[0].getMessage());
        assertTrue(res0.isClosed());
    }

    @SuppressWarnings("resource")
    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns a \"null\"", "and resources fail to close"},
            then = "throws NullResultException with suppressed IOExceptions"
    )
    void execute_FunctionReturnsNull_ResourcesFailToClose() {
        var res0 = new UncloseableInputStream("Test".getBytes());
        var res1 = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                (_, _) -> null,
                () -> res0,
                ()  -> res1
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
        assertEquals("Failed to close: " + res1, result.getSuppressed()[0].getMessage());
        assertEquals("Failed to close: " + res0, result.getSuppressed()[1].getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an Error", "and resources are closed"},
            then = "throws Error"
    )
    void execute_FunctionThrowsError() {
        var res0 = new CloseableInputStream("Test".getBytes());
        var res1 = new CloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                (_, _) -> { throw new Error(); },
                () -> res0,
                ()  -> res1
        );

        assertThrows(Error.class, testTry::execute);
        assertTrue(res0.isClosed());
        assertTrue(res1.isClosed());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an Error", "and resource 0 fails to close"},
            then = "throws Error with suppressed IOException"
    )
    void execute_FunctionThrowsError_Resource0FailsToClose() {
        var res0 = new UncloseableInputStream("Test".getBytes());
        var res1 = new CloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                (_, _) -> { throw new Error(); },
                () -> res0,
                ()  -> res1
        );

        var result = assertThrows(Error.class, testTry::execute);
        assertEquals("Failed to close: " + res0, result.getSuppressed()[0].getMessage());
        assertTrue(res1.isClosed());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an Error", "and resource 1 fails to close"},
            then = "throws Error with suppressed IOException"
    )
    void execute_FunctionThrowsError_Resource1FailsToClose() {
        var res0 = new CloseableInputStream("Test".getBytes());
        var res1 = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                (_, _) -> { throw new Error(); },
                () -> res0,
                ()  -> res1
        );

        var result = assertThrows(Error.class, testTry::execute);
        assertEquals("Failed to close: " + res1, result.getSuppressed()[0].getMessage());
        assertTrue(res0.isClosed());
    }

    @SuppressWarnings("resource")
    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an Error", "and resources fail to close"},
            then = "throws Error with suppressed IOExceptions"
    )
    void execute_FunctionThrowsError_ResourcesFailToClose() {
        var res0 = new UncloseableInputStream("Test".getBytes());
        var res1 = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                (_, _) -> { throw new Error(); },
                () -> res0,
                ()  -> res1
        );

        var result = assertThrows(Error.class, testTry::execute);
        assertEquals("Failed to close: " + res1, result.getSuppressed()[0].getMessage());
        assertEquals("Failed to close: " + res0, result.getSuppressed()[1].getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an X", "and resources are closed"},
            then = "returns Left of X"
    )
    void execute_FunctionThrowsX() {
        var res0 = new CloseableInputStream("Test".getBytes());
        var res1 = new CloseableInputStream("Test".getBytes());
        var x = new Exception("test me");
        var testTry = new TryBiResource<>(
                (_, _) -> { throw x; },
                () -> res0,
                ()  -> res1
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals(x, containedX);
        assertTrue(res0.isClosed());
        assertTrue(res1.isClosed());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an X", "and resource 0 fails to close"},
            then = "returns Left of X with suppressed IOException"
    )
    void execute_FunctionThrowsX_Resource0FailsToClose() {
        var res0 = new UncloseableInputStream("Test".getBytes());
        var res1 = new CloseableInputStream("Test".getBytes());
        var x = new Exception("test me");
        var testTry = new TryBiResource<>(
                (_, _) -> { throw x; },
                () -> res0,
                ()  -> res1
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals(x, containedX);
        assertEquals("Failed to close: " + res0, containedX.getSuppressed()[0].getMessage());
        assertTrue(res1.isClosed());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an X", "and resource 1 fails to close"},
            then = "returns Left of X with suppressed IOException"
    )
    void execute_FunctionThrowsX_Resource1FailsToClose() {
        var res0 = new CloseableInputStream("Test".getBytes());
        var res1 = new UncloseableInputStream("Test".getBytes());
        var x = new Exception("test me");
        var testTry = new TryBiResource<>(
                (_, _) -> { throw x; },
                () -> res0,
                ()  -> res1
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals(x, containedX);
        assertEquals("Failed to close: " + res1, containedX.getSuppressed()[0].getMessage());
        assertTrue(res0.isClosed());
    }

    @SuppressWarnings("resource")
    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an X", "and resources fail to close"},
            then = "returns Left of X with suppressed IOExceptions"
    )
    void execute_FunctionThrowsX_ResourcesFailToClose() {
        var res0 = new UncloseableInputStream("Test".getBytes());
        var res1 = new UncloseableInputStream("Test".getBytes());
        var x = new Exception("test me");
        var testTry = new TryBiResource<>(
                (_, _) -> { throw x; },
                () -> res0,
                ()  -> res1
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals(x, containedX);
        assertEquals("Failed to close: " + res1, containedX.getSuppressed()[0].getMessage());
        assertEquals("Failed to close: " + res0, containedX.getSuppressed()[1].getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns T", "and resources are closed"},
            then = "returns Right of T"
    )
    void execute_FunctionReturns() {
        var res0 = new CloseableInputStream("Test".getBytes());
        var res1 = new CloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                FUNC,
                () -> res0,
                ()  -> res1
        );

        var result = testTry.execute();

        assertEquals(Either.right(168), result);
        assertTrue(res0.isClosed());
        assertTrue(res1.isClosed());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns T", "and resource 0 fails to close"},
            then = "returns Left of IOException"
    )
    void execute_FunctionReturns_Resource0FailsToClose() {
        var res0 = new UncloseableInputStream("Test".getBytes());
        var res1 = new CloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                FUNC,
                () -> res0,
                ()  -> res1
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals("Failed to close: " + res0, containedX.getMessage());
        assertTrue(res1.isClosed());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns T", "and resource 1 fails to close"},
            then = "returns Left of IOException"
    )
    void execute_FunctionReturns_Resource1FailsToClose() {
        var res0 = new CloseableInputStream("Test".getBytes());
        var res1 = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                FUNC,
                () -> res0,
                ()  -> res1
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals("Failed to close: " + res1, containedX.getMessage());
        assertTrue(res0.isClosed());
    }

    @SuppressWarnings("resource")
    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns T", "and resources fail to close"},
            then = "returns Left of IOException with suppressed IOException"
    )
    void execute_FunctionReturns_ResourcesFailToClose() {
        var res0 = new UncloseableInputStream("Test".getBytes());
        var res1 = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryBiResource<>(
                FUNC,
                () -> res0,
                ()  -> res1
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals("Failed to close: " + res1, containedX.getMessage());
        assertEquals("Failed to close: " + res0, containedX.getSuppressed()[0].getMessage());
    }

}