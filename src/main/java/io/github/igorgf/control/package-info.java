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
 * and results by throwing some
 * {@link io.github.igorgf.control.ContractViolationException}. This
 * is a precondition violation (a bug in the caller), not a domain result, and
 * is therefore not represented in the return type. The functional types in this
 * package are null safe by construction and result, once a value is wrapped in
 * the above listed types it is guaranteed non-null. Also, the resutls of any
 * computations performed by these types are guaranteed non-null.
 * <p>
 * <b>Exception policy:</b> Members of this package use the checked function
 * aware functional interfaces from {@link io.github.igorgf.function}, to ensure
 * checked exception propagation support.
 * </p>
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
package io.github.igorgf.control;