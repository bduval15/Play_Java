package ca.bcit.comp2522.termproject.resourcerouter.util;

import ca.bcit.comp2522.termproject.resourcerouter.managers.GameController;

/**
 * Represents an object that can be updated on each frame or simulation tick within the game loop.
 * <p>
 * This interface provides a contract for any game element that requires regular updates over time
 * (e.g., logic for resource production, processing, movement, animation, or UI feedback).
 * Implementing this interface allows consistent handling of game state transitions and resets.
 * </p>
 *
 * <p>
 * Typical implementers include:
 * <ul>
 *     <li>{@code SourceNode} – Produces resources to be consumed.</li>
 *     <li>{@code ProcessorNode} – Consumes and processes resources.</li>
 *     <li>{@code SinkNode} – Validates and consumes delivered resources.</li>
 *     <li>Other timed or interactive game elements.</li>
 * </ul>
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public interface Updatable
{

    /**
     * Updates the state of the object based on elapsed time.
     * <p>
     * This method is called automatically by the {@link GameController}'s game loop at fixed intervals
     * to simulate progression of time or events.
     * It is used to manage internal timers, animate transitions, or perform resource checks/transfers.
     * </p>
     *
     * @param deltaTime  the amount of time passed since the last update (in seconds).
     *                   Useful for time-based logic or animations.
     * @param controller the {@link GameController} providing access to shared game state
     *                   and control interfaces such as simulation status, scoring, and UI feedback.
     */
    void update(double deltaTime,
                GameController controller);

    /**
     * Resets the internal state of this object to its initial/default configuration.
     * <p>
     * Called when restarting a level, resetting the simulation, or recovering from an error state.
     * It should undo any transient or active states like timers, flags, or temporary buffers.
     * </p>
     */
    void resetState();
}
