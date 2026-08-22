package io.github.igorgf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CloseableInputStream implements AutoCloseable {
    protected boolean closed = false;
    protected final ByteArrayInputStream delegate;

    public CloseableInputStream(byte[] data) {
        this.delegate = new ByteArrayInputStream(data);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        closed = true;
    }

    public int read() {
        return delegate.read();
    }

    public boolean isClosed() {
        return closed;
    }
}