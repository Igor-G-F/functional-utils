package io.github.igorgf.control;

import io.github.igorgf.GivenWhenThen;
import io.github.igorgf.GivenWhenThenGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

@DisplayNameGeneration(GivenWhenThenGenerator.class)
class ThrownTest {

    // region ——————————————————— Creation Tests ———————————————————
    @Test
    @GivenWhenThen(
            given = "new Thrown",
            when = "captured is a \"null\"",
            then = "throws NullArgumentException"
    )
    void Thrown_CapturedIsNull() {
        assertThrows(NullArgumentException.class, () -> new Thrown<>(null));
    }

    @Test
    @GivenWhenThen(
            given = "new Thrown",
            when = "captured is an Error",
            then = "throws ContractViolationException"
    )
    void Thrown_CapturedIsError() {
        final var error = new Error();
        var e = assertThrows(ContractViolationException.class, () -> new Thrown<>(error));
        assertEquals("Contract violation. Thrown cannot wrap an Error. Errors are " +
                "unrecoverable and must propagate: " + error, e.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "new Thrown",
            then = "contains X"
    )
    void Thrown() {
        final var x = new Exception("Test Me!");
        var thrown = new Thrown<>(x);

        assertEquals(x, thrown.captured());
    }

    @Test
    @GivenWhenThen(
            given = "of()",
            when = "captured is a \"null\"",
            then = "throws NullArgumentException"
    )
    void of_CapturedIsNull() {
        assertThrows(NullArgumentException.class, () -> Thrown.of(null));
    }

    @Test
    @GivenWhenThen(
            given = "of()",
            when = "captured is an Error",
            then = "throws ContractViolationException"
    )
    void of_CapturedIsError() {
        final var error = new Error();
        var e = assertThrows(ContractViolationException.class, () -> Thrown.of(error));
        assertEquals("Contract violation. Thrown cannot wrap an Error. Errors are " +
                "unrecoverable and must propagate: " + error, e.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "of()",
            then = "contains X"
    )
    void of() {
        final var x = new Exception("Test Me!");
        var thrown = Thrown.of(x);

        assertEquals(x, thrown.captured());
    }
    // endregion ———————————————— Creation Tests ———————————————————

    // region ——————————————————— Transformation Tests ———————————————————
    @Test
    @GivenWhenThen(
            given = "map()",
            when = "\"mapper\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void map_MapperIsNull() {
        var thrown = Thrown.of(new Exception());

        var result = assertThrows(NullArgumentException.class, () -> thrown.map(null));
        assertEquals("Contract violation. Argument \"mapper\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "map()",
            when = "\"mapper\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void map_MapperReturnsNull() {
        var thrown = Thrown.of(new Exception());

        var result = assertThrows(NullResultException.class, () -> thrown.map(_ -> null));
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "map()",
            then = "returns new Thrown"
    )
    void map() {
        var thrown = Thrown.of(new Exception("Hello"));

        var result = thrown.map(x -> new IllegalArgumentException(x.getMessage() + " World!"));

        assertInstanceOf(IllegalArgumentException.class, result.get());
        assertEquals("Hello World!", result.get().getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "recover()",
            when = "\"recoveryMapper\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void recover_MapperIsNull() {
        var thrown = Thrown.of(new Exception());

        var result = assertThrows(NullArgumentException.class, () -> thrown.recover(null));
        assertEquals("Contract violation. Argument \"recoveryMapper\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "recover()",
            when = "\"recoveryMapper\" returns a \"null\"",
            then = "throws NullResultException"
    )
    void recover_MapperReturnsNull() {
        var thrown = Thrown.of(new Exception());

        var result = assertThrows(NullResultException.class, () -> thrown.recover(_ -> null));
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "recover()",
            then = "returns new type"
    )
    void recover() {
        var thrown = Thrown.of(new Exception("Hello"));

        var result = thrown.recover(x -> x.getMessage() + " World!");

        assertEquals("Hello World!", result);
    }
    // endregion ———————————————— Transformation Tests ———————————————————

    // region ——————————————————— Inspection Tests ———————————————————
    @Test
    @GivenWhenThen(
            given = "get()",
            then = "returns contained X"
    )
    void get_ReturnsCaptured() {
        final var e = new Exception("Test Me!");
        final var thrown = Thrown.of(e);

        assertEquals(e, thrown.get());
    }

    @DisplayName("Given isRuntimeException().")
    @ParameterizedTest(name = "When captured is {0}. Then returns {1}.")
    @FieldSource("IsRuntimeException_Args")
    void IsRuntimeException(Throwable captured, boolean expected) {
        var thrown = Thrown.of(captured);
        assertEquals(expected, thrown.isRuntimeException());
    }

    private static final List<Arguments> IsRuntimeException_Args = List.of(
            Arguments.of(new Exception(), false),
            Arguments.of(new RuntimeException(), true),
            Arguments.of(new Throwable(), false),
            Arguments.of(new IOException(), false),
            Arguments.of(new IllegalArgumentException(), true)
    );

    static Stream<Arguments> IsCheckedException_Args() {
        return IsRuntimeException_Args.stream().map(arg -> Arguments.of(
                arg.get()[0], !(boolean) arg.get()[1]
        ));
    }

    @DisplayName("Given isCheckedException().")
    @ParameterizedTest(name = "When captured is {0}. Then returns {1}.")
    @MethodSource("IsCheckedException_Args")
    void IsCheckedException(Throwable captured, boolean expected) {
        var thrown = Thrown.of(captured);
        assertEquals(expected, thrown.isCheckedException());
    }
    // endregion ———————————————— Inspection Tests ———————————————————

    // region ——————————————————— handleExact() Tests ———————————————————
    @Test
    @GivenWhenThen(
            given = "handleExact()",
            when = "\"handler\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleExact_HandlerNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handleExact(null, IOException.class));

        assertEquals("Contract violation. Argument \"handler\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handleExact()",
            when = "\"exceptionType\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleExact_ExceptionTypeNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handleExact(
                _ -> {},
                (Class<? extends Throwable>) null
        ));

        assertEquals("Contract violation. Argument \"exceptionType\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handleExact()",
            when = "\"exceptionType\" is an exact match",
            then = "handler ran"
    )
    void handleExact_NoMatch() {
        RuntimeException x = new IllegalArgumentException("Hello");
        var handlerSpy = new AtomicReference<Throwable>();
        var thrown = Thrown.of(x);

        var result = thrown.handleExact(
                handlerSpy::set,
                IllegalArgumentException.class
        );

        assertEquals(thrown, result);
        assertEquals(x, handlerSpy.get());
    }

    @DisplayName("Given handleExact().")
    @ParameterizedTest(name = "{argumentSetName} Then handler NOT ran.")
    @FieldSource("handleExact_Match_Args")
    <X extends Throwable> void handleExact_Match(
            X captured,
            Class<? extends X> matchingOn
    ) {
        var handlerSpy = new AtomicBoolean(false);
        var thrown = Thrown.of(captured);
        var result = thrown.handleExact(
                _ -> handlerSpy.set(true), matchingOn
        );
        assertEquals(thrown, result);
        assertFalse(handlerSpy.get());
    }

    private static final List<Arguments> handleExact_Match_Args = List.of(
            argumentSet("When \"captured\" is unchecked, and \"exceptionType\" is a superclass.",
                    new IllegalArgumentException(), RuntimeException.class
            ),
            argumentSet("When \"captured\" is checked, and \"exceptionType\" is a superclass.",
                    new IOException(), Exception.class
            ),
            argumentSet("When \"captured\" extends Throwable, and \"exceptionType\" is Throwable.class.",
                    new CustomThrowable(), Throwable.class
            ),
            argumentSet("When \"exceptionType\" is a subclass.",
                    new Exception("Hello!"), IllegalArgumentException.class
            ),
            argumentSet("When no match.",
                    new IOException("Hello!"), IllegalArgumentException.class
            )
    );

    @Test
    @GivenWhenThen(
            given = "handleExact() parameterized",
            when = "\"handler\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleExactParams_HandlerNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handleExact(null));

        assertEquals("Contract violation. Argument \"handler\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handleExact() parameterized",
            when = "\"throwables\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleExactParams_ThrowablesNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handleExact(
                _ -> {},
                (Class<? extends Throwable>[]) null
        ));

        assertEquals("Contract violation. Argument \"throwables\" is null.", result.getMessage());
    }

    @DisplayName("Given handleExact() parameterized.")
    @ParameterizedTest(name = "{argumentSetName}")
    @FieldSource("handleExactParams_Match_Args")
    <X extends Throwable> void handleExactParams_Match(
            X captured,
            Class<? extends X>[] matchingOn,
            boolean shouldHandlerRun
    ) {
        var handlerSpy = new AtomicReference<Throwable>();
        var thrown = Thrown.of(captured);
        var result = thrown.handle(
                handlerSpy::set, matchingOn
        );
        assertEquals(thrown, result);
        if (shouldHandlerRun) assertEquals(captured, handlerSpy.get());
    }

    private static final List<Arguments> handleExactParams_Match_Args = List.of(
            argumentSet("When \"throwables\" is empty. Then handler should run.",
                    new IOException(),
                    new Class<?>[] {},
                    true
            ),
            argumentSet("When \"throwables\" contains superclass. Then handler should not run.",
                    new IllegalArgumentException(),
                    new Class<?>[] { NullPointerException.class, Exception.class, IOException.class },
                    false
            ),
            argumentSet("No exact match. Then handler should not run.",
                    new IllegalArgumentException(),
                    new Class<?>[] { NullPointerException.class, ContractViolationException.class, IOException.class },
                    false
            ),
            argumentSet("When exact match on first element. Then handler should run.",
                    new IOException(),
                    new Class<?>[] { IOException.class, IllegalArgumentException.class, NullPointerException.class },
                    true
            ),
            argumentSet("When exact match on middle element. Then handler should run.",
                    new CustomThrowable(),
                    new Class<?>[] { IllegalArgumentException.class, CustomThrowable.class, RuntimeException.class },
                    true
            ),
            argumentSet("When exact match on last element. Then handler should run.",
                    new IOException(),
                    new Class<?>[] { IllegalArgumentException.class, IOException.class },
                    true
            )
    );
    // endregion ———————————————— handleExact() Tests ———————————————————

    // region ——————————————————— handle() Tests ———————————————————
    @Test
    @GivenWhenThen(
            given = "handle()",
            when = "\"handler\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handle_HandlerNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(null, IOException.class));

        assertEquals("Contract violation. Argument \"handler\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handle()",
            when = "\"exceptionType\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handle_ExceptionTypeNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(
                _ -> {},
                (Class<? extends Throwable>) null
        ));

        assertEquals("Contract violation. Argument \"exceptionType\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handle()",
            when = "no match",
            then = "handler not ran"
    )
    void handle_NoMatch() {
        var handlerRan = new AtomicBoolean(false);
        var thrown = Thrown.of(new Throwable());

        var result = thrown.handle(
                _ -> handlerRan.set(true),
                IOException.class
        );

        assertEquals(thrown, result);
        assertFalse(handlerRan.get());
    }

    @DisplayName("Given handle().")
    @ParameterizedTest(name = "{argumentSetName} Then handler ran.")
    @FieldSource("handle_Match_Args")
    <X extends Throwable> void handle_Match(
            X captured,
            Class<? extends X> matchingOn
    ) {
        var handlerSpy = new AtomicReference<Throwable>();
        var thrown = Thrown.of(captured);
        var result = thrown.handle(
                handlerSpy::set, matchingOn
        );
        assertEquals(thrown, result);
        assertEquals(captured, handlerSpy.get());
    }

    private static final List<Arguments> handle_Match_Args = List.of(
            argumentSet("When \"captured\" is unchecked, and \"exceptionType\" is a superclass.",
                    new IllegalArgumentException(), RuntimeException.class
            ),
            argumentSet("When \"captured\" is checked, and \"exceptionType\" is a superclass.",
                    new IOException(), Exception.class
            ),
            argumentSet("When \"captured\" extends Throwable, and \"exceptionType\" is Throwable.class.",
                    new CustomThrowable(), Throwable.class
            ),
            argumentSet("When \"exceptionType\" is an exact match.",
                    new IOException(), IOException.class
            )
    );

    @Test
    @GivenWhenThen(
            given = "handle() parameterized",
            when = "\"handler\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleParams_HandlerNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(null));

        assertEquals("Contract violation. Argument \"handler\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handle() parameterized",
            when = "\"throwables\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleParams_ThrowablesNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(
                _ -> {},
                (Class<? extends Throwable>[]) null
        ));

        assertEquals("Contract violation. Argument \"throwables\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handle() parameterized",
            when = "no match",
            then = "handler not ran"
    )
    void handleParams_NoMatch() {
        var handlerRan = new AtomicBoolean(false);
        var thrown = Thrown.of(new Throwable());

        var result = thrown.handle(
                _ -> handlerRan.set(true),
                IllegalArgumentException.class, IOException.class
        );

        assertEquals(thrown, result);
        assertFalse(handlerRan.get());
    }

    @DisplayName("Given handle() parameterized.")
    @ParameterizedTest(name = "{argumentSetName} Then handler ran.")
    @FieldSource("handleParams_Match_Args")
    <X extends Throwable> void handleParams_Match(
            X captured,
            Class<? extends X>[] matchingOn
    ) {
        var handlerSpy = new AtomicReference<Throwable>();
        var thrown = Thrown.of(captured);
        var result = thrown.handle(
                handlerSpy::set, matchingOn
        );
        assertEquals(thrown, result);
        assertEquals(captured, handlerSpy.get());
    }

    static final class CustomThrowable extends Throwable {}

    private static final List<Arguments> handleParams_Match_Args = List.of(
            argumentSet("When \"throwables\" is empty.",
                    new IOException(),
                    new Class<?>[] {}
            ),
            argumentSet("When \"captured\" is unchecked, and \"throwables\" contains superclass.",
                    new IllegalArgumentException(),
                    new Class<?>[] { NullPointerException.class, Exception.class, IOException.class }
            ),
            argumentSet("When \"captured\" is checked, and \"throwables\" contains superclass.",
                    new IOException(),
                    new Class<?>[] { IllegalArgumentException.class, Exception.class }
            ),
            argumentSet("When \"captured\" extends Throwable, and \"throwables\" contains Throwable.",
                    new CustomThrowable(),
                    new Class<?>[] { IllegalArgumentException.class, Throwable.class }
            ),
            argumentSet("When \"throwables\" contains exact match.",
                    new IOException(),
                    new Class<?>[] { IOException.class, IllegalArgumentException.class }
            )
    );
    // endregion ———————————————— handle() Tests ———————————————————

    //TODO: add rethrow tests

    //TODO: add rethrow exact tests

    //TODO: add rethrowSneaky tests

    //TODO: add rethrowIf tests

}