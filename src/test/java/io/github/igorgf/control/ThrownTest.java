package io.github.igorgf.control;

import org.junit.jupiter.api.DisplayName;
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

class ThrownTest {

    @Test
    @DisplayName("Given new Thrown. When captured is null. Then throws NPE.")
    void Create_CapturedIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Thrown(null));
    }

    @Test
    @DisplayName("Given new Thrown. When captured is Error. Then throws IllegalArgumentException.")
    void Create_CapturedIsError_ThrowsIllegalArgumentException() {
        final var error = new Error();
        var e = assertThrows(IllegalArgumentException.class, () -> new Thrown(error));
        assertEquals("Thrown cannot wrap an Error. Errors are " +
                "unrecoverable and must propagate: " + error, e.getMessage());
    }

    @Test
    @DisplayName("Given Get. Then returns contained captured exception.")
    void Get_ReturnsCaptured() {
        final var e = new Exception("Test Me!");
        final var thrown = new Thrown(e);

        assertEquals(e, thrown.get());
    }

    @DisplayName("Given IsRuntimeException.")
    @ParameterizedTest(name = "When captured is {0}. Then returns {1}.")
    @FieldSource("IsRuntimeException_Args")
    void IsRuntimeException(Throwable captured, boolean expected) {
        var thrown = new Thrown(captured);
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

    @DisplayName("Given IsCheckedException.")
    @ParameterizedTest(name = "When captured is {0}. Then returns {1}.")
    @MethodSource("IsCheckedException_Args")
    void IsCheckedException(Throwable captured, boolean expected) {
        var thrown = new Thrown(captured);
        assertEquals(expected, thrown.isCheckedException());
    }

    @Test
    @DisplayName("Given HandleParams. When handler is null. Then throws NPE.")
    void HandleParams_HandlerNull_ThrowsNPE() {
        var thrown = new Thrown(new Throwable());
        assertThrows(NullPointerException.class, () -> thrown.handle(null));
    }

    @Test
    @DisplayName("Given HandleParams. When throwables is null. Then throws NPE.")
    void HandleParams_ThrowablesNull_ThrowsNPE() {
        var thrown = new Thrown(new Throwable());
        assertThrows(NullPointerException.class, () -> thrown.handle(
                _ -> {},
                (Class<? extends Throwable>[]) null
        ));
    }

    @Test
    @DisplayName("Given HandleParams. When matched. Then handler receives captured.")
    void HandleParams_Matched_HandlerSeesThrown() {
        var spy = new AtomicReference<Throwable>();
        var captured = new RuntimeException("Hello World!");
        var thrown = new Thrown(captured);

        assertEquals(thrown, thrown.handle(spy::set, RuntimeException.class, Throwable.class));
        assertEquals(captured, spy.get());
    }

    @DisplayName("Given HandleParams.")
    @ParameterizedTest(name = "When captured {0} matching on {1}. {argumentSetName}")
    @FieldSource("HandleParams_Args")
    <X extends Throwable> void HandleParams(
            X captured,
            Class<? extends Throwable>[] matchingOn,
            boolean shouldHandlerRun
    ) {
        var handlerRan = new AtomicBoolean(false);
        var thrown = new Thrown(captured);
        var result = thrown.handle(
                _ -> handlerRan.set(true), matchingOn
        );
        assertEquals(thrown, result);
        assertEquals(shouldHandlerRun, handlerRan.get());
    }

    static final class CustomThrowable extends Throwable {}

    private static final List<Arguments> HandleParams_Args = List.of(
            // always match when no args
            argumentSet("Then handler ran.", new IOException(), new Class<?>[] {}, true),
            // no match on superclass
            argumentSet("Then no match, handler ignored.",new IOException(), new Class<?>[] {
                    Exception.class, Throwable.class
            }, false),
            argumentSet("Then no match, handler ignored.",new CustomThrowable(), new Class<?>[] {
                    Throwable.class
            }, false),
            argumentSet("Then no match, handler ignored.",new IllegalArgumentException(), new Class<?>[] {
                    Exception.class, Throwable.class, RuntimeException.class
            }, false),
            // Exact match
            argumentSet("Then matched, handler ran.",new CustomThrowable(), new Class<?>[] {
                    CustomThrowable.class
            }, true),
            argumentSet("Then matched, handler ran.",new IOException(), new Class<?>[] {
                    IOException.class
            }, true),
            // match last element
            argumentSet("Then matched, handler ran.",new IllegalArgumentException(), new Class<?>[] {
                    Exception.class, Throwable.class, RuntimeException.class, IllegalArgumentException.class
            }, true),
            // match on first element, ignore rest
            argumentSet("Then matched, handler ran.",new IOException(), new Class<?>[] {
                    IOException.class, IllegalArgumentException.class, IllegalStateException.class
            }, true),
            // match on middle element
            argumentSet("Then matched, handler ran.",new IllegalArgumentException(), new Class<?>[] {
                    IOException.class, IllegalArgumentException.class, IllegalStateException.class
            }, true)
    );

    @Test
    @DisplayName("Given HandleOne. When handler is null. Then throws NPE.")
    void HandleOne_HandlerNull_ThrowsNPE() {
        var thrown = new Thrown(new Throwable());
        assertThrows(NullPointerException.class,
                () -> thrown.handle(null, Throwable.class)
        );
    }

    @Test
    @DisplayName("Given HandleOne. When throwable is null. Then throws NPE.")
    void HandleOne_ThrowableNull_ThrowsNPE() {
        var thrown = new Thrown(new Exception());
        assertThrows(NullPointerException.class,
                () -> thrown.handle((Exception _) -> {}, null
        ));
    }

    @Test
    @DisplayName("Given HandleOne. When matched. Then handler receives captured.")
    void HandleOne_Matched_HandlerReceivesCaptured() {
        var captured = new AtomicReference<IOException>();
        var throwable = new IOException("Hello World!");
        var thrown = new Thrown(throwable);

        assertEquals(thrown, thrown.handle(captured::set, IOException.class));
        assertEquals(throwable, captured.get());
    }

    @Test
    @DisplayName("Given HandleOne. Then checked throwable requires handling.")
    void HandleOne_CheckedPropagated() {
        var thrown = new Thrown(new IOException());

        // unchecked handler throw does not require handling
        thrown.handle(e -> { throw e; }, IllegalArgumentException.class);

        try {
            // checked handler throw requires handling
            thrown.handle(e -> { throw e; }, IOException.class);
        } catch (IOException e) {
            assertEquals(IOException.class, e.getClass());
        }
    }

    @DisplayName("Given HandleOne.")
    @ParameterizedTest(name = "When captured {0} matching {1}. {argumentSetName}")
    @FieldSource("HandleOne_Args")
    <X extends Throwable> void HandleOne(
            X captured,
            Class<? extends Throwable> matchingOn,
            boolean shouldHandlerRun
    ) {
        var handlerRan = new AtomicBoolean(false);
        var thrown = new Thrown(captured);

        var result = thrown.handle(
                _ -> handlerRan.set(true), matchingOn
        );

        assertEquals(thrown, result);
        assertEquals(shouldHandlerRun, handlerRan.get());
    }

    private static final List<Arguments> HandleOne_Args = List.of(
            // no match on superclass
            argumentSet("Then no match, handler ignored.",new IOException(), Exception.class, false),
            argumentSet("Then no match, handler ignored.",new CustomThrowable(), Throwable.class, false),
            argumentSet("Then no match, handler ignored.",new IllegalArgumentException(), Exception.class, false),
            // no match on subclass
            argumentSet("Then no match, handler ignored.",new Exception(), RuntimeException.class, false),
            // Exact match
            argumentSet("Then matched, handler ran.",new CustomThrowable(), CustomThrowable.class, true),
            argumentSet("Then matched, handler ran.",new IOException(), IOException.class, true)
    );

    @Test
    @DisplayName("Given RethrowParams. When throwables is null. Then throws NPE.")
    void RethrowParams_ThrowablesNull_ThrowsNPE() {
        var thrown = new Thrown(new Throwable());
        assertThrows(NullPointerException.class, () -> thrown.rethrow(
                (Class<? extends Throwable>[]) null
        ));
    }

    @DisplayName("Given RethrowParams.")
    @ParameterizedTest(name = "When captured {0} matching on {1}, {argumentSetName}")
    @FieldSource("RethrowParams_Args")
    <X extends Throwable> void RethrowParams_Args(
            X captured,
            Class<? extends Throwable>[] matchingOn
    ) {
        var thrown = new Thrown(captured);

        // the possible branches are: an exception being thrown, or the Thrown container returns itself
        Supplier<Either<Throwable, Thrown>> result = () -> {
            try {
                return Either.right(thrown.rethrow(matchingOn));
            } catch (RuntimeException e) {
                return Either.left(e);
            }
        };

        switch (result.get()) {
            // has throwable as cause
            case Left<Throwable, Thrown> (Throwable x) when x.getCause() != null -> {
                assertEquals(RuntimeException.class, x.getClass());
                assertEquals(captured, x.getCause());
            }
            // throwable thrown directly
            case Left<Throwable, Thrown> (Throwable x) -> assertEquals(captured, x);
            // no match
            case Right<Throwable, Thrown> (Thrown t) -> assertEquals(thrown, t);
        }
    }

    private static final List<Arguments> RethrowParams_Args = List.of(
            // empty throwables
            argumentSet("is unchecked. Then throws IllegalArgumentException.", new IllegalArgumentException(), new Class<?>[] {}),
            argumentSet("is unchecked. Then throws RuntimeException.", new RuntimeException(), new Class<?>[] {}),
            argumentSet("is checked. Then throws RuntimeException with cause Exception.", new Exception(), new Class<?>[] {}),
            argumentSet("is checked. Then throws RuntimeException with cause Throwable.", new Throwable(), new Class<?>[] {}),
            // no match on superclass or subclass
            argumentSet("is unchecked. Then returns Thrown.", new RuntimeException(), new Class<?>[] {
                    IllegalArgumentException.class, Exception.class, Throwable.class
            }),
            argumentSet("is checked. Then returns Thrown.", new Exception(), new Class<?>[] {
                    IOException.class, Throwable.class
            }),
            argumentSet("is checked. Then returns Thrown.", new Exception(), new Class<?>[] {
                    IOException.class, Throwable.class
            }),
            // handles direct children of Throwable
            argumentSet("extends Throwable. Then returns Thrown.", new CustomThrowable(), new Class<?>[] {
                    Throwable.class
            }),
            // exact match
            argumentSet("is unchecked. Then throws IllegalArgumentException.", new IllegalArgumentException(), new Class<?>[] {
                    IllegalArgumentException.class
            }),
            argumentSet("is checked. Then throws RuntimeException with cause IOException.", new IOException(), new Class<?>[] {
                    IOException.class
            }),
            // match last element
            argumentSet("is unchecked. Then throws thrown.",new IllegalArgumentException(), new Class<?>[] {
                    Exception.class, Throwable.class, RuntimeException.class, IllegalArgumentException.class
            }),
            argumentSet("is checked. Then throws RuntimeException with cause IOException.",new IOException(), new Class<?>[] {
                    Exception.class, Throwable.class, RuntimeException.class, IOException.class
            }),
            // match on first element, ignore rest
            argumentSet("is unchecked. Then throws thrown.",new RuntimeException(), new Class<?>[] {
                    RuntimeException.class, IllegalArgumentException.class, IllegalStateException.class
            }),
            argumentSet("is checked. Then throws RuntimeException with cause IOException.",new IOException(), new Class<?>[] {
                    IOException.class, Exception.class, Throwable.class
            }),
            // match on middle element
            argumentSet("is unchecked. Then throws thrown.",new IllegalArgumentException(), new Class<?>[] {
                    IOException.class, IllegalArgumentException.class, IllegalStateException.class
            }),
            argumentSet("is checked. Then throws RuntimeException with cause IOException.", new IOException(), new Class<?>[] {
                    IOException.class, IOException.class, IllegalStateException.class
            })
    );

    @Test
    @DisplayName("Given RethrowOne. When throwable is null. Then throws NPE.")
    void RethrowOne_ThrowableNull_ThrowsNPE() {
        var thrown = new Thrown(new Throwable());
        assertThrows(NullPointerException.class, () -> thrown.rethrow(
                (Class<? extends Throwable>) null
        ));
    }

    @Test
    @DisplayName("Given RethrowOne. Then checked throwable requires handling.")
    void RethrowOne_CheckedPropagated() {
        var thrown = new Thrown(new IOException());

        // unchecked match target does not require handling
        thrown.rethrow(IllegalArgumentException.class);

        try {
            // checked match target  requires handling
            thrown.rethrow(IOException.class);
        } catch (IOException e) {
            assertEquals(IOException.class, e.getClass());
        }
    }

    @DisplayName("Given RethrowOne.")
    @ParameterizedTest(name = "When captured {0} matching on {1}, {argumentSetName}")
    @FieldSource("RethrowOne_Args")
    <X extends Throwable> void RethrowOne_Args(
            X captured,
            Class<? extends Throwable> matchingOn
    ) {
        var thrown = new Thrown(captured);

        // the possible branches are: an exception being thrown, or the Thrown container returns itself
        Supplier<Either<Throwable, Thrown>> result = () -> {
            try {
                return Either.right(thrown.rethrow(matchingOn));
            } catch (Throwable e) {
                return Either.left(e);
            }
        };

        switch (result.get()) {
            // throwable thrown
            case Left<Throwable, Thrown> (Throwable x) -> assertEquals(captured, x);
            // no match
            case Right<Throwable, Thrown> (Thrown t) -> assertEquals(thrown, t);
        }
    }

    private static final List<Arguments> RethrowOne_Args = List.of(
            // no match on superclass or subclass
            argumentSet("is unchecked. Then returns Thrown.",
                    new RuntimeException(), IllegalArgumentException.class),
            argumentSet("is checked. Then returns Thrown.",
                    new Exception(), Throwable.class),
            // handles direct children of Throwable
            argumentSet("extends Throwable. Then returns Thrown.",
                    new CustomThrowable(), Throwable.class),
            // exact match
            argumentSet("is unchecked. Then throws IllegalArgumentException.",
                    new IllegalArgumentException(), IllegalArgumentException.class),
            argumentSet("is checked. Then throws RuntimeException with cause IOException.",
                    new IOException(), IOException.class)
    );

    @DisplayName("Given GetAsUnchecked.")
    @ParameterizedTest(name = "When captured is {0}. {argumentSetName}")
    @FieldSource("GetAsUnchecked_Args")
    void GetAsUnchecked(Throwable captured) {
        var thrown = new Thrown(captured);

        //noinspection ThrowableNotThrown
        switch (thrown.getAsUnchecked()) {
            case RuntimeException e when e.getCause() != null -> assertEquals(captured, e.getCause());
            case RuntimeException e -> assertEquals(captured, e);
        }
    }

    private static final List<Arguments> GetAsUnchecked_Args = List.of(
            argumentSet("Then returns RuntimeException with cause Exception.", new Exception()),
            argumentSet("Then returns captured.", new RuntimeException()),
            argumentSet("Then returns RuntimeException with cause Throwable.", new Throwable()),
            argumentSet("Then returns RuntimeException with cause IOException.",new IOException()),
            argumentSet("Then returns captured.", new IllegalArgumentException())
    );
}