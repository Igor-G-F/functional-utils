package io.github.igorgf.control;

/**
 * The singleton inhabitant of the unit type, representing the absence of a
 * meaningful value. The null safe analogue of {@link Void}.
 * <p>
 * Unlike {@link Void}, whose sole reference is {@code null}, {@code Unit} holds
 * a real, non-null instance, so it can be stored in null safe containers such
 * as {@link Either} and {@link Try} without violating their no null contracts.
 * <p>
 * Used as the result type of computations that produce no value but may still
 * succeed or fail, such as those described by
 * {@link io.github.igorgf.function.CheckedRunnable} or
 * {@link io.github.igorgf.function.CheckedConsumer}.
 *
 * @author Igor Flakiewicz
 * @since 1.0.0
 */
public final class Unit {

    /** The sole instance of {@link Unit}. */
    public static final Unit INSTANCE = new Unit();

    private Unit() {
        // singleton
    }

    @Override
    public String toString() {
        return "Unit";
    }
}