package io.github.igorgf.control;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TryWithResourcesTest {

    private static final byte[] arr = "Hello World".getBytes();

    private static class CloseableInputStream implements AutoCloseable {
        boolean closed = false;
        final ByteArrayInputStream delegate;

        CloseableInputStream(byte[] data) {
            this.delegate = new ByteArrayInputStream(data);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            delegate.close();
        }

        int read() {
            return delegate.read();
        }
    }

    @SuppressWarnings("resource")
    @Nested
    class TryFunctionWithResource {

        @Test
        @DisplayName("Given Try of Function With Resource. When Function returns R. Then Try returns Right of R, and Resource closed.")
        void TryFunctionWithResource_FunctionReturns_ReturnsRightAndResourceClosed() {
            final var stream = new CloseableInputStream(arr);
            var result = TryWithResources.of(CloseableInputStream::read)
                    .withResource(() -> stream)
                    .execute();

            assertEquals(Either.right(72), result);
            assertTrue(stream.closed);
        }

        @Test
        @DisplayName("Given Try of Function With Resource. When Function throws X. Then Try returns Left of Throws of X, and Resource closed.")
        void TryFunctionWithResource_FunctionThrows_ReturnsLeftAndResourceClosed() {
            final var stream = new CloseableInputStream(arr);
            final var exception = new Exception();

            var result = TryWithResources.of(_ -> {
                        throw exception;
                    })
                    .withResource(() -> stream)
                    .execute();

            assertEquals(Either.left(new Thrown(exception)), result);
            assertTrue(stream.closed);
        }
    }

    @SuppressWarnings("resource")
    @Nested
    class TryBiFunctionWithResources {

        @Test
        @DisplayName("Given Try of BiFunction With Resources. When BiFunction returns R. Then Try returns Right of R, and Resources closed.")
        void TryBiFunctionWithResources_BiFunctionReturns_ReturnsRightAndResourcesClosed() {
            final var stream = new CloseableInputStream(arr);
            final var stream2 = new CloseableInputStream(arr);
            var result = TryWithResources.of((CloseableInputStream a, CloseableInputStream b) -> a.read() + b.read())
                    .withResources(() -> stream, () -> stream2)
                    .execute();

            assertEquals(Either.right(144), result);
            assertTrue(stream.closed);
            assertTrue(stream2.closed);
        }

        @Test
        @DisplayName("Given Try of BiFunction With Resources. When BiFunction throws X. Then Try returns Left of Throws of X, and Resources closed.")
        void TryBiFunctionWithResources_BiFunctionThrows_ReturnsLeftAndResourcesClosed() {
            final var stream = new CloseableInputStream(arr);
            final var stream2 = new CloseableInputStream(arr);
            final var exception = new Exception();

            var result = TryWithResources.of((_, _) -> {
                        throw exception;
                    })
                    .withResources(() -> stream, () -> stream2)
                    .execute();

            assertEquals(Either.left(new Thrown(exception)), result);
            assertTrue(stream.closed);
            assertTrue(stream2.closed);
        }
    }
}