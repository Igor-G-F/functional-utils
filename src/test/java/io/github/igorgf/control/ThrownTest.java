package io.github.igorgf.control;

import io.github.igorgf.GivenWhenThen;
import io.github.igorgf.GivenWhenThenGenerator;
import io.github.igorgf.control.Thrown.MatchStrategy;
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
import java.util.stream.Stream;

import static io.github.igorgf.control.Thrown.MatchStrategy.ASSIGNABLE;
import static io.github.igorgf.control.Thrown.MatchStrategy.EXACT;
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

        var result = thrown.recover(x -> x.get().getMessage() + " World!");

        assertEquals("Hello World!", result);
    }

    @Test
    @GivenWhenThen(
            given = "toEither()",
            when = "\"matchStrategy\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void toEither_MatchStrategyIsNull() {
        var thrown = Thrown.of(new Exception());

        var result = assertThrows(NullArgumentException.class, () -> thrown.toEither(
                null,
                Either::left,
                Exception.class
        ));
        assertEquals("Contract violation. Argument \"matchStrategy\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "toEither()",
            when = "\"mapper\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void toEither_MapperIsNull() {
        var thrown = Thrown.of(new Exception());

        var result = assertThrows(NullArgumentException.class, () -> thrown.toEither(
                ASSIGNABLE,
                null,
                Exception.class
        ));
        assertEquals("Contract violation. Argument \"mapper\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "toEither()",
            when = "\"matchTarget\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void toEither_MatchTargetIsNull() {
        var thrown = Thrown.of(new Exception());

        var result = assertThrows(NullArgumentException.class, () -> thrown.toEither(
                ASSIGNABLE,
                Either::left,
                null
        ));
        assertEquals("Contract violation. Argument \"matchTarget\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "toEither()",
            when = "\"handler\" result is a \"null\"",
            then = "throws NullResultException"
    )
    void toEither_HandlerResultIsNull() {
        var thrown = Thrown.of(new Exception());

        var result = assertThrows(NullResultException.class, () -> thrown.toEither(
                EXACT,
                _ -> null,
                Exception.class
        ));
        assertEquals("Contract violation. Function result is a \"null\".", result.getMessage());
    }

    @DisplayName("Given toEither(), no match.")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then returns Left or \"this\".")
    @FieldSource("noMatch_Args")
    void toEither_NoMatch(
            MatchStrategy matchStrategy,
            Throwable captured,
            Class<? extends Throwable> matchingOn
    ) {
        var handlerRan = new AtomicBoolean(false);
        var thrown = Thrown.of(captured);

        var result = thrown.toEither(
                matchStrategy,
                _ -> { throw new RuntimeException("This should never happen"); },
                matchingOn
        );

        assertEquals(Either.left(thrown), result);
        assertFalse(handlerRan.get());
    }

    @DisplayName("Given toEither().")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then \"mapper\" ran.")
    @FieldSource("match_Args")
    <X extends Throwable> void toEither_Match(
            MatchStrategy matchStrategy,
            X captured,
            Class<? extends X> matchingOn
    ) {
        var thrown = Thrown.of(captured);
        var result = thrown.toEither(
                matchStrategy,
                t -> Either.right("Good Stuff!" + t.get()),
                matchingOn
        );
        assertEquals(Either.right("Good Stuff!" + captured), result);
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

    // region ——————————————————— handle() Tests ———————————————————
    @Test
    @GivenWhenThen(
            given = "handle()",
            when = "\"matchStrategy\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handle_MatchStrategyNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(
                null, _ -> {}, IOException.class
        ));

        assertEquals("Contract violation. Argument \"matchStrategy\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handle()",
            when = "\"handler\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handle_HandlerNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(
                ASSIGNABLE, null, IOException.class
        ));

        assertEquals("Contract violation. Argument \"handler\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handle()",
            when = "\"matchTarget\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handle_ExceptionTypeNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(
                ASSIGNABLE,
                _ -> {},
                (Class<? extends Throwable>) null
        ));

        assertEquals("Contract violation. Argument \"matchTarget\" is null.", result.getMessage());
    }

    @DisplayName("Given handle(), no match.")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then handler not ran.")
    @FieldSource("noMatch_Args")
    void handle_NoMatch(
            MatchStrategy matchStrategy,
            Throwable captured,
            Class<? extends Throwable> matchingOn
    ) {
        var handlerRan = new AtomicBoolean(false);
        var thrown = Thrown.of(captured);

        var result = thrown.handle(
                matchStrategy,
                _ -> handlerRan.set(true),
                matchingOn
        );

        assertEquals(thrown, result);
        assertFalse(handlerRan.get());
    }

    @DisplayName("Given handle().")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then handler ran.")
    @FieldSource("match_Args")
    <X extends Throwable> void handle_Match(
            MatchStrategy matchStrategy,
            X captured,
            Class<? extends X> matchingOn
    ) {
        var handlerSpy = new AtomicReference<Throwable>();
        var thrown = Thrown.of(captured);
        var result = thrown.handle(
                matchStrategy, handlerSpy::set, matchingOn
        );
        assertEquals(thrown, result);
        assertEquals(captured, handlerSpy.get());
    }

    @Test
    @GivenWhenThen(
            given = "handle() parameterized",
            when = "\"matchStrategy\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleParams_MatchStrategyNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(
                null, _ -> {}, IOException.class, IllegalArgumentException.class
        ));

        assertEquals("Contract violation. Argument \"matchStrategy\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handle() parameterized",
            when = "\"handler\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleParams_HandlerNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(EXACT, null));

        assertEquals("Contract violation. Argument \"handler\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "handle() parameterized",
            when = "\"matchTargets\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void handleParams_ThrowablesNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.handle(
                ASSIGNABLE,
                _ -> {},
                (Class<? extends Throwable>[]) null
        ));

        assertEquals("Contract violation. Argument \"matchTargets\" is null.", result.getMessage());
    }

    @DisplayName("Given handle() parameterized, no match.")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then handler not ran.")
    @FieldSource("paramsNoMatch_Args")
    void handleParams_NoMatch(
            MatchStrategy matchStrategy,
            Throwable captured,
            Class<? extends Throwable>[] matchingOn
    ) {
        var handlerRan = new AtomicBoolean(false);
        var thrown = Thrown.of(captured);

        var result = thrown.handle(
                matchStrategy,
                _ -> handlerRan.set(true),
                matchingOn
        );

        assertEquals(thrown, result);
        assertFalse(handlerRan.get());
    }

    @DisplayName("Given handle() parameterized.")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then handler ran.")
    @FieldSource("paramsMatch_Args")
    <X extends Throwable> void handleParams_Match(
            MatchStrategy matchStrategy,
            X captured,
            Class<? extends X>[] matchingOn
    ) {
        var handlerSpy = new AtomicReference<Throwable>();
        var thrown = Thrown.of(captured);
        var result = thrown.handle(
                matchStrategy, handlerSpy::set, matchingOn
        );
        assertEquals(thrown, result);
        assertEquals(captured, handlerSpy.get());
    }
    // endregion ———————————————— handle() Tests ———————————————————

    // region ——————————————————— rethrow() Tests ———————————————————
    @Test
    @GivenWhenThen(
            given = "rethrow()",
            when = "\"matchStrategy\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void rethrow_MatchStrategyNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.rethrow(
                null, IOException.class
        ));

        assertEquals("Contract violation. Argument \"matchStrategy\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "rethrow()",
            when = "\"matchTarget\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void rethrow_ExceptionTypeNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.rethrow(
                ASSIGNABLE,
                (Class<? extends Throwable>) null
        ));

        assertEquals("Contract violation. Argument \"matchTarget\" is null.", result.getMessage());
    }

    @DisplayName("Given rethrow(), no match.")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then returns \"this\".")
    @FieldSource("noMatch_Args")
    void rethrow_NoMatch(
            MatchStrategy matchStrategy,
            Throwable captured,
            Class<? extends Throwable> matchingOn
    ) throws Throwable {
        var thrown = Thrown.of(captured);

        var result = thrown.rethrow(
                matchStrategy,
                matchingOn
        );

        assertEquals(thrown, result);
    }

    @DisplayName("Given rethrow().")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then \"captured\" is thrown.")
    @FieldSource("match_Args")
    <X extends Throwable> void rethrow_Match(
            MatchStrategy matchStrategy,
            X captured,
            Class<? extends X> matchingOn
    ) {
        var thrown = Thrown.of(captured);

        assertThrows(matchingOn, () -> thrown.rethrow(
                matchStrategy, matchingOn
        ));
    }

    @Test
    @GivenWhenThen(
            given = "rethrow() parameterized",
            when = "\"matchStrategy\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void rethrowParams_MatchStrategyNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.rethrow(
                (MatchStrategy) null, IOException.class, IllegalArgumentException.class
        ));

        assertEquals("Contract violation. Argument \"matchStrategy\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "rethrow() parameterized",
            when = "\"matchTargets\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void rethrowParams_ThrowablesNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.rethrow(
                ASSIGNABLE,
                (Class<? extends Throwable>[]) null
        ));

        assertEquals("Contract violation. Argument \"matchTargets\" is null.", result.getMessage());
    }

    @DisplayName("Given rethrow() parameterized, no match.")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then returns \"this\".")
    @FieldSource("paramsNoMatch_Args")
    void rethrowParams_NoMatch(
            MatchStrategy matchStrategy,
            Throwable captured,
            Class<? extends Throwable>[] matchingOn
    ) throws Throwable {
        var thrown = Thrown.of(captured);

        var result = thrown.rethrow(
                matchStrategy,
                matchingOn
        );

        assertEquals(thrown, result);
    }

    @DisplayName("Given rethrow() parameterized.")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then throws \"captured\".")
    @FieldSource("paramsMatch_Args")
    <X extends Throwable> void rethrowParams_Match(
            MatchStrategy matchStrategy,
            X captured,
            Class<? extends X>[] matchingOn
    ) {
        var thrown = Thrown.of(captured);
        assertThrows(captured.getClass(), () -> thrown.rethrow(
                matchStrategy, matchingOn
        ));
    }
    // endregion ———————————————— rethrow() Tests ———————————————————

    // region ——————————————————— rethrowSneaky() Tests ———————————————————
    @Test
    @GivenWhenThen(
            given = "rethrowSneaky()",
            when = "\"matchStrategy\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void rethrowSneaky_MatchStrategyNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.rethrowSneaky(
                (MatchStrategy) null, IOException.class, IllegalArgumentException.class
        ));

        assertEquals("Contract violation. Argument \"matchStrategy\" is null.", result.getMessage());
    }

    @Test
    @GivenWhenThen(
            given = "rethrowSneaky()",
            when = "\"matchTargets\" is a \"null\"",
            then = "throws NullArgumentException"
    )
    void rethrowSneaky_ThrowablesNull() {
        var thrown = Thrown.of(new Throwable());

        var result = assertThrows(NullArgumentException.class, () -> thrown.rethrowSneaky(
                ASSIGNABLE,
                (Class<? extends Throwable>[]) null
        ));

        assertEquals("Contract violation. Argument \"matchTargets\" is null.", result.getMessage());
    }

    @DisplayName("Given rethrowSneaky(), no match.")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then returns \"this\".")
    @FieldSource("paramsNoMatch_Args")
    void rethrowSneaky_NoMatch(
            MatchStrategy matchStrategy,
            Throwable captured,
            Class<? extends Throwable>[] matchingOn
    ) {
        var thrown = Thrown.of(captured);

        var result = thrown.rethrowSneaky(
                matchStrategy,
                matchingOn
        );

        assertEquals(thrown, result);
    }

    @DisplayName("Given rethrowSneaky().")
    @ParameterizedTest(name = "When \"matchStrategy\" is {0}, and {argumentSetName}. Then throws \"captured\".")
    @FieldSource("paramsMatch_Args")
    <X extends Throwable> void rethrowSneaky_Match(
            MatchStrategy matchStrategy,
            X captured,
            Class<? extends X>[] matchingOn
    ) {
        var thrown = Thrown.of(captured);
        assertThrows(captured.getClass(), () -> thrown.rethrow(
                matchStrategy, matchingOn
        ));
    }
    // endregion ———————————————— rethrowSneaky() Tests ———————————————————

    static final class CustomThrowable extends Throwable {}

    private static final List<Arguments> noMatch_Args = List.of(
            argumentSet("\"matchTarget\" is a superclass",
                    EXACT, new IllegalArgumentException(), RuntimeException.class
            ),
            argumentSet("\"matchTarget\" is a subclass",
                    EXACT, new Exception(), RuntimeException.class
            ),
            argumentSet("\"matchTarget\" is not a superclass",
                    ASSIGNABLE, new IllegalArgumentException(), IOException.class
            ),
            argumentSet("\"matchTarget\" is a subclass.",
                    ASSIGNABLE, new Exception(), RuntimeException.class
            )
    );

    private static final List<Arguments> match_Args = List.of(
            argumentSet("\"matchTarget\" is a superclass",
                    ASSIGNABLE, new IllegalArgumentException(), RuntimeException.class
            ),
            argumentSet("\"captured\" extends Throwable, and \"matchTarget\" is Throwable.class",
                    ASSIGNABLE, new CustomThrowable(), Throwable.class
            ),
            argumentSet("\"matchTarget\" is an exact match",
                    ASSIGNABLE, new IOException(), IOException.class
            ),
            argumentSet("\"matchTarget\" is an exact match",
                    EXACT, new IOException(), IOException.class
            )
    );

    private static final List<Arguments> paramsNoMatch_Args = List.of(
            argumentSet("\"matchTargets\" contains superclass",
                    EXACT,
                    new IllegalArgumentException(),
                    new Class<?>[] { RuntimeException.class, Exception.class }
            ),
            argumentSet("\"matchTargets\" contains subclass.",
                    EXACT,
                    new Exception(),
                    new Class<?>[] { RuntimeException.class, IOException.class }
            ),
            argumentSet("\"matchTargets\" no match",
                    ASSIGNABLE,
                    new IllegalArgumentException(),
                    new Class<?>[] { IOException.class, CustomThrowable.class }
            ),
            argumentSet("\"matchTargets\" contains subclass",
                    ASSIGNABLE,
                    new Exception(),
                    new Class<?>[] { RuntimeException.class, IOException.class }
            )
    );

    private static final List<Arguments> paramsMatch_Args = List.of(
            //assignable
            argumentSet("\"matchTargets\" is empty",
                    ASSIGNABLE,
                    new IOException(),
                    new Class<?>[] {}
            ),
            argumentSet("\"matchTargets\" contains superclass",
                    ASSIGNABLE,
                    new IllegalArgumentException(),
                    new Class<?>[] { NullPointerException.class, Exception.class, IOException.class }
            ),
            argumentSet("\"matchTargets\" contains Throwable",
                    ASSIGNABLE,
                    new CustomThrowable(),
                    new Class<?>[] { IllegalArgumentException.class, Throwable.class }
            ),
            argumentSet("\"matchTargets\" contains exact match",
                    ASSIGNABLE,
                    new IOException(),
                    new Class<?>[] { IOException.class, IllegalArgumentException.class }
            ),
            //exact
            argumentSet("\"matchTargets\" is empty",
                    EXACT,
                    new IOException(),
                    new Class<?>[] {}
            ),
            argumentSet("match on first element",
                    EXACT,
                    new IOException(),
                    new Class<?>[] { IOException.class, IllegalArgumentException.class, NullPointerException.class }
            ),
            argumentSet("match on middle element",
                    EXACT,
                    new CustomThrowable(),
                    new Class<?>[] { IllegalArgumentException.class, CustomThrowable.class, RuntimeException.class }
            ),
            argumentSet("match on last element",
                    EXACT,
                    new IOException(),
                    new Class<?>[] { IllegalArgumentException.class, IOException.class }
            )
    );
}