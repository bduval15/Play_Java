package ca.bcit.comp25222.termproject.CustomGame.util;

import ca.bcit.comp25222.termproject.CustomGame.managers.GameController;

/**
 * Represents an element that can be updated within the game loop.
 * This interface is implemented by any class whose state needs to be
 * periodically updated (e.g., game nodes, animations, etc.).
 *
 * @author Braeden Duval
 * @version 1.0
 */
public interface Updatable
{
    /**
     * Updates the state of the object.
     *
     * @param deltaTime  the time elapsed since the last update in seconds.
     * @param controller a reference to the GameController for potential interactions.
     */
    void update(double deltaTime, GameController controller);

    /**
     * Resets the state of the object to its initial conditions.
     */
    void resetState();
}
