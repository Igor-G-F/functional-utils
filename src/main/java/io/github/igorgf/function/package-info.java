/**
 * Checked exception aware functional interfaces.
 * <p>
 * These interfaces mirror the standard {@link java.util.function} interfaces
 * but declare throwable via a generic {@code X extends Throwable}
 * parameter, enabling precise checked exception propagation.
 * <p>
 * Standard functional interfaces declare no {@code throws} clause, so any
 * lambda that throws a checked exception cannot be passed as one, the compiler
 * rejects it and requires a {@code try/catch} block. Example:
 * <pre>{@code
 *     // given this method
 *     <T> T extract(Option<T> option) throws Exception {
 *         return option.orElseThrow(() -> new Exception("option is empty"));
 *     }
 *
 *     // the following code is illegal, as "this::extract" throws an Exception
 *     int example() {
 *         var opt = Option.<Integer>empty();
 *         Function<Option<Integer>, Integer> extractor = this::extract;
 *         return extractor.apply(opt);
 *     }
 *
 *     // therefore the following would be required, obscuring the exception
 *     int example() {
 *         var opt = Option.<Integer>empty();
 *         Function<Option<Integer>, Integer> extractor = option -> {
 *             try {
 *                 return extract(option);
 *             } catch (Exception e) {
 *                 throw new RuntimeException(e);
 *             }
 *         };
 *         return extractor.apply(opt);
 *     }
 * }</pre>
 * In e.g. {@link io.github.igorgf.function.CheckedFunction} the exception type
 * is a <em>type parameter</em>, fixed by the caller. A method that accepts a
 * {@code CheckedFunction} can mirror that {@code X} in its own {@code throws}
 * clause, propagating the exact declared exception rather than forcing a
 * {@code try/catch}. Example:
 * <pre>{@code
 *     // given this method
 *     <T> T extract(Option<T> option) throws Exception {
 *         return option.orElseThrow(() -> new Exception("option is empty"));
 *     }
 *
 *     // the checked exception can be propagated
 *     int example() throws Exception {
 *         var opt = Option.<Integer>empty();
 *         CheckedFunction<Option<Integer>, Integer, Exception>
 *             extractor = this::extract;
 *         return extractor.apply(opt);
 *     }
 *
 *     // however given unchecked exceptions or no exceptions
 *     <T> T extractUnchecked(Option<T> option) throws RuntimeException {
 *         return option.orElseThrow(() -> new RuntimeException("option is empty"));
 *     }
 *
 *     // this behaves exactly like java.util.function.Function
 *     int exampleUnchecked() {
 *         var opt = Option.<Integer>empty();
 *         // the X param is always required therefore RuntimeException can be
 *         // provided to stop propagation
 *         CheckedFunction<Option<Integer>, Integer, RuntimeException>
 *                 extractor = this::extractUnchecked;
 *         return extractor.apply(opt);
 *     }
 * }</pre>
 * <p>
 * The bound {@code X extends Throwable} permits any throwable: a checked
 * exception, an unchecked {@link java.lang.RuntimeException}, or an
 * {@link java.lang.Error}; leaving the policy of what to accept to each call
 * site.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
package io.github.igorgf.function;
