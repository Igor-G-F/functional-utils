package io.github.igorgf.function;

@FunctionalInterface
public interface CheckedRunnable<E extends Exception> {

    void run() throws E;

}