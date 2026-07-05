/**
 * Checked functional interfaces for use with the control flow types in
 * {@link io.github.igorgf.control}.
 * <p>
 * These interfaces mirror the standard {@link java.util.function} interfaces
 * but declare checked exceptions via a generic {@code X extends Exception}
 * parameter, enabling precise checked exception tracking through
 * {@link io.github.igorgf.control.Try} and related types.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
package io.github.igorgf.function;
