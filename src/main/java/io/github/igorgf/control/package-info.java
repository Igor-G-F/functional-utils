/**
 * Core functional control-flow types for error handling and value presence.
 * <p>
 * This package provides sum types (algebraic data types) for representing
 * optional values, disjoint results, and validation outcomes in a null safe,
 * functional style:
 * <ul>
 *   <li>
 *       {@link io.github.igorgf.control.Option} - presence or absence of a
 *       value
 *   </li>
 *   <li>
 *       {@link io.github.igorgf.control.Either} - a disjoint union of two
 *       possible outcomes
 *   </li>
 *   <li>
 *       {@link io.github.igorgf.control.Validation} - a validated value or an
 *       accumulated list of errors
 *   </li>
 *   <li>
 *       {@link io.github.igorgf.control.Try} - a computation that may throw,
 *       with exception handling
 *   </li>
 * </ul>
 * <p>
 * <b>Null policy:</b> All methods in this package reject {@code null} arguments
 * by throwing {@link java.lang.NullPointerException}. This is a precondition
 * violation (a bug in the caller), not a domain result, and is therefore not
 * represented in the return type. The functional types in this package are
 * null safe by construction, once a value is wrapped in the above listed types
 * it is guaranteed non-null.
 * <p>
 * <b>Exception policy:</b> Checked exceptions are used only for precondition
 * violations (e.g. {@code Validation.accumulated(List.of())} throws
 * {@link io.github.igorgf.control.EmptyValueException}).
 * </p>
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
package io.github.igorgf.control;