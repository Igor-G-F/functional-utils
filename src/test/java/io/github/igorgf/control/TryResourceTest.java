package io.github.igorgf.control;

import io.github.igorgf.CloseableInputStream;
import io.github.igorgf.GivenWhenThen;
import io.github.igorgf.GivenWhenThenGenerator;
import io.github.igorgf.UncloseableInputStream;
import io.github.igorgf.function.CheckedFunction;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(GivenWhenThenGenerator.class)
class TryResourceTest {

    private static final CheckedFunction<CloseableInputStream, Integer, RuntimeException> FUNC =
            CloseableInputStream::read;

    @Test
    @GivenWhenThen(
            given = "new TryResource",
            when = "\"function\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryResource_FunctionIsNull() throws IOException {
        var stream = new CloseableInputStream("Test".getBytes());
        var result = assertThrows(NullArgumentException.class,() -> {
            new TryResource<>(
                    null,
                    () -> stream
            );
        });
        stream.close();
        assertEquals("Contract violation. Argument \"function\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "new TryResource",
            when = "\"resSupplier\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void TryResource_ArgSupplierIsNull() {
        var result = assertThrows(NullArgumentException.class, () -> {
            new TryResource<>(
                    FUNC,
                    null
            );
        });
        assertEquals("Contract violation. Argument \"resSupplier\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = "\"resSupplier\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void execute_ArgSupplierReturnsNull() {
        var testTry = new TryResource<>(
                FUNC,
                () -> null
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. \"resSupplier\" supplied a \"null\" value.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns a \"null\"", "and resource is closed"},
            then = "throws NullResultException"
    )
    void execute_FunctionReturnsNull() {
        var stream = new CloseableInputStream("Test".getBytes());
        var testTry = new TryResource<>(
                _ -> null,
                () -> stream
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
        assertTrue(stream.isClosed());
    }

    @SuppressWarnings("resource")
    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns a \"null\"", "and resource fails to close"},
            then = "throws NullResultException with suppressed IOException"
    )
    void execute_FunctionReturnsNull_ResourceFailsToClose() {
        var stream = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryResource<>(
                _ -> null,
                () -> stream
        );

        var result = assertThrows(NullResultException.class, testTry::execute);
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
        assertEquals("Failed to close: " + stream, result.getSuppressed()[0].getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an Error", "and resource is closed"},
            then = "throws Error"
    )
    void execute_FunctionThrowsError() {
        var stream = new CloseableInputStream("Test".getBytes());
        var testTry = new TryResource<>(
                _ -> { throw new Error(); },
                () -> stream
        );

        assertThrows(Error.class, testTry::execute);
        assertTrue(stream.isClosed());
    }

    @SuppressWarnings("resource")
    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws an Error", "and resource fails to close"},
            then = "throws Error with suppressed IOException"
    )
    void execute_FunctionThrowsError_ResourceFailsToClose() {
        var stream = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryResource<>(
                _ -> { throw new Error(); },
                () -> stream
        );

        var result = assertThrows(Error.class, testTry::execute);
        assertEquals("Failed to close: " + stream, result.getSuppressed()[0].getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws X", "and resource is closed"},
            then = "returns Left of X"
    )
    void execute_FunctionThrowsX() {
        var stream = new CloseableInputStream("Test".getBytes());
        var x = new Exception("test me");
        var testTry = new TryResource<>(
                _ -> { throw x; },
                () -> stream
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals(x, containedX);
        assertTrue(stream.isClosed());
    }

    @SuppressWarnings("resource")
    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" throws X", "and resource fails to close"},
            then = "returns Left of X with suppressed IOException"
    )
    void execute_FunctionThrowsX_ResourceFailsToClose() {
        var stream = new UncloseableInputStream("Test".getBytes());
        var x = new Exception("test me");
        var testTry = new TryResource<>(
                _ -> { throw x; },
                () -> stream
        );

        var result = testTry.execute();

        var containedX = result.getLeft().orThrow().get();
        assertEquals(x, containedX);
        assertEquals("Failed to close: " + stream, containedX.getSuppressed()[0].getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns T", "and resource is closed"},
            then = "returns Right of T"
    )
    void execute_FunctionReturns() {
        var stream = new CloseableInputStream("Test".getBytes());
        var testTry = new TryResource<>(
                FUNC,
                () -> stream
        );

        var result = testTry.execute();

        assertEquals(Either.right(84), result);
        assertTrue(stream.isClosed());
    }

    @SuppressWarnings("resource")
    @Test
    @GivenWhenThen(
            given = "execute()",
            when = {"\"function\" returns T", "and resource fails to close"},
            then = "returns Left of IOException"
    )
    void execute_FunctionReturns_ResourceFailsToClose() {
        var stream = new UncloseableInputStream("Test".getBytes());
        var testTry = new TryResource<>(
                FUNC,
                () -> stream
        );

        var result = testTry.execute();

        var containedIOX = result.getLeft().orThrow().get();
        assertEquals("Failed to close: " + stream, containedIOX.getMessage());
    }

}