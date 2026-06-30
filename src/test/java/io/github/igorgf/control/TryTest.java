package io.github.igorgf.control;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TryTest {

    @Nested
    class TryFunction {

        @Test
        @DisplayName("Given Try of Function. When Function returns R. Then Try returns Right of R.")
        void TryFunction_FunctionReturns_ReturnsRight() {
            var result = Try.of((Integer i) -> ++i)
                    .withParam(7)
                    .execute();

            assertEquals(Either.right(8), result);
        }

        @Test
        @DisplayName("Given Try of Function. When Function throws X. Then Try returns Left of Throws of X.")
        void TryFunction_FunctionThrows_ReturnsLeft() {
            final var exception = new Exception();

            var result = Try.of(_ -> {
                        throw exception;
                    })
                    .withParam(7)
                    .execute();

            assertEquals(Either.left(new Thrown(exception)), result);
        }
    }

    @Nested
    class TryBiFunction {

        @Test
        @DisplayName("Given Try of BiFunction. When BiFunction returns R. Then Try returns Right of R.")
        void TryBiFunction_BiFunctionReturns_ReturnsRight() {
            var result = Try.of(Integer::sum)
                    .withParams(2, 3)
                    .execute();

            assertEquals(Either.right(5), result);
        }

        @Test
        @DisplayName("Given Try of BiFunction. When BiFunction throws X. Then Try returns Left of Throws of X.")
        void TryBiFunction_BiFunctionThrows_ReturnsLeft() {
            final var exception = new Exception();

            var result = Try.of((_, _) -> {
                        throw exception;
                    })
                    .withParams(7, 8)
                    .execute();

            assertEquals(Either.left(new Thrown(exception)), result);
        }
    }

    @Nested
    class TryWithFinallyTest {
        @Test
        @DisplayName("Given Try With Finally. When Function returns R. Then Try returns Right of R, and Finally executed.")
        void TryWithFinally_FunctionReturns_FinallyExecuted() {
            var index = new AtomicInteger(0);

            var result = Try.of((Integer i) -> ++i)
                    .withParam(7)
                    .withFinally(index::getAndIncrement)
                    .execute();

            assertEquals(Either.right(8), result);
            assertEquals(1, index.get());
        }

        @Test
        @DisplayName("Given Try With Finally chain. When Function returns R. Then Try returns Right of R, and Finally chain executed.")
        void TryWithFinallyChain_FunctionReturns_FinallyChainExecuted() {
            var index = new AtomicInteger(0);
            var index2 = new AtomicInteger(0);

            var result = Try.of((Integer i) -> ++i)
                    .withParam(7)
                    .withFinally(index::getAndIncrement)
                    .withFinally(index2::getAndIncrement)
                    .execute();

            assertEquals(Either.right(8), result);
            assertEquals(1, index.get());
            assertEquals(1, index2.get());
        }

        @Test
        @DisplayName("Given Try With Finally. When Function returns, and Finally throws X. Then Try returns Left of Throws of X.")
        void TryWithFinally_FunctionReturnsFinallyThrows_ReturnsLeft() {
            final var exception = new Exception();
            var index = new AtomicInteger(0);

            var result = Try.of(_ -> index.incrementAndGet())
                    .withParam(7)
                    .withFinally(() -> {
                        throw exception;
                    })
                    .execute();

            assertEquals(Either.left(new Thrown(exception)), result);
            assertEquals(1, index.get());
        }

        @Test
        @DisplayName("Given Try With Finally. When Function throws X. Then Try returns Left of Throws of X, and Finally executed.")
        void TryWithFinally_FunctionThrows_FinallyExecuted() {
            final var exception = new Exception();
            var index = new AtomicInteger(0);

            var result = Try.of(_ -> {
                        throw exception;
                    })
                    .withParam(7)
                    .withFinally(index::getAndIncrement)
                    .execute();

            assertEquals(Either.left(new Thrown(exception)), result);
            assertEquals(1, index.get());
        }

        @Test
        @DisplayName("Given Try With Finally. When Function throws X, and Finally throws X2. Then Try returns Left of Throws of X with suppressed X2.")
        void TryWithFinally_FunctionThrowsAndFinallyThrows_ReturnsLeftWithSuppressed() {
            final var exception = new Exception();
            final var exception2 = new Exception();

            var result = Try.of(_ -> {
                        throw exception;
                    })
                    .withParam(7)
                    .withFinally(() -> {
                        throw exception2;
                    })
                    .execute();

            assertEquals(Either.left(new Thrown(exception)), result);
            result.ifLeft(t -> assertTrue(
                    Arrays.stream(t.get().getSuppressed())
                            .toList()
                            .contains(exception2)
            ));
        }
    }
}