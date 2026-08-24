package io.github.igorgf.control;

import io.github.igorgf.GivenWhenThen;
import io.github.igorgf.GivenWhenThenGenerator;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(GivenWhenThenGenerator.class)
class TryTest {

    //TODO: add tests for factories and steps

    @Nested
    class TryWithFinallyTest {

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" returns Right of R", "and finally success"},
                then = "returns Right of R"
        )
        void execute_DelegateReturnsRight() {
            var finallyRan = new AtomicBoolean(false);
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> 7),
                    () -> finallyRan.set(true)
            );

            var result = myTry.execute();
            assertEquals(Either.right(7), result);
            assertTrue(finallyRan.get());
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" returns Right of R", "and finally throws Error"},
                then = "throws Error"
        )
        void execute_DelegateReturnsRight_FinallyThrowsError() {
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> 7),
                    () -> { throw new Error(); }
            );

            assertThrows(Error.class, myTry::execute);
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" returns Right of R", "and finally throws X"},
                then = "returns Left of X"
        )
        void execute_DelegateReturnsRight_FinallyThrowsX() {
            var x = new Exception("Hello");
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> 7),
                    () -> { throw x; }
            );

            var result = myTry.execute();
            assertEquals(Either.left(Thrown.of(x)), result);
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = "\"delegate.execute()\" throws Error",
                then = "throws Error"
        )
        void execute_DelegateThrowsError() {
            var finallyRan = new AtomicBoolean(false);
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> { throw new Error(); }),
                    () -> finallyRan.set(true)
            );

            assertThrows(Error.class, myTry::execute);
            assertFalse(finallyRan.get());
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" returns Left of X", "and finally success"},
                then = "returns Left of X"
        )
        void execute_DelegateReturnsLeft() {
            var x = new Exception("Hello");
            var finallyRan = new AtomicBoolean(false);
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> { throw x; }),
                    () -> finallyRan.set(true)
            );

            var result = myTry.execute();
            assertEquals(Either.left(Thrown.of(x)), result);
            assertTrue(finallyRan.get());
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" returns Left of X", "and finally throws Error"},
                then = "throws Error with suppressed X"
        )
        void execute_DelegateReturnsLeft_FinallyThrowsError() {
            var x = new Exception("Hello");
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> { throw x; }),
                    () -> { throw new Error(); }
            );

            var result = assertThrows(Error.class, myTry::execute);
            assertEquals(x, result.getSuppressed()[0]);
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" returns Left of X", "and finally throws X2"},
                then = "returns Left of X2 with suppressed X"
        )
        void execute_DelegateReturnsLeft_FinallyThrowsX2() {
            var x = new Exception("Hello");
            var x2 = new Exception("World");
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> { throw x; }),
                    () -> { throw x2; }
            );

            var result = myTry.execute();
            var caughtX = result.getLeft().orThrow().get();
            assertEquals(x2, caughtX);
            assertEquals(x, caughtX.getSuppressed()[0]);
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" throws ContractViolationException", "and finally success"},
                then = "throws ContractViolationException"
        )
        void execute_DelegateThrowsContractViolationException() {
            var cvx = new ContractViolationException("TEST");
            var finallyRan = new AtomicBoolean(false);
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> { throw cvx; }),
                    () -> finallyRan.set(true)
            );

            var result = assertThrows(ContractViolationException.class, myTry::execute);
            assertEquals(cvx, result);
            assertTrue(finallyRan.get());
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" throws ContractViolationException", "and finally throws Error"},
                then = "throws Error with suppressed ContractViolationException"
        )
        void execute_DelegateThrowsContractViolationException_FinallyThrowsError() {
            var cvx = new ContractViolationException("TEST");
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> { throw cvx; }),
                    () -> { throw new Error(); }
            );

            var result = assertThrows(Error.class, myTry::execute);
            assertEquals(cvx, result.getSuppressed()[0]);
        }

        @Test
        @GivenWhenThen(
                given = "execute()",
                when = {"\"delegate.execute()\" throws ContractViolationException", "and finally throws X"},
                then = "throws ContractViolationException with suppressed X"
        )
        void execute_DelegateThrowsContractViolationException_FinallyThrowsX() {
            var cvx = new ContractViolationException("TEST");
            var x = new Exception("Hello");
            var myTry = new TryNoArgWithFinally<>(
                    Try.supply(() -> { throw cvx; }),
                    () -> { throw x; }
            );

            var result = assertThrows(ContractViolationException.class, myTry::execute);
            assertEquals(cvx, result);
            assertEquals(x, result.getSuppressed()[0]);
        }
    }

    @Nested
    class TryArgTest {

    }

    @Nested
    class TryBiArgTest {

    }

    @Nested
    class ArgStepTest {

    }

    @Nested
    class BiArgStepTest {

    }

    @Nested
    class ArgFuncStepTest {

    }

    @Nested
    class ArgFinallyStepTest {

    }

    @Nested
    class BiArgFuncStepTest {

    }

    @Nested
    class BiArgFinallyStepTest {

    }

}