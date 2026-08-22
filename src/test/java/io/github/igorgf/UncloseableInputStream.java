package io.github.igorgf;

import java.io.IOException;

public class UncloseableInputStream extends CloseableInputStream {
    public UncloseableInputStream(byte[] data) {
        super(data);
    }

    @Override
    public void close() throws IOException {
        super.close();
        throw new NoCloseException(this);
    }

    public static final class NoCloseException extends IOException {
        public NoCloseException(AutoCloseable closeable) {
            super("Failed to close: " + closeable);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }
}