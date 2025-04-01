package ca.bcit.comp25222.termproject.numbergame;

/**
 * AbstractSynthwaveUI serves as a base class for constructing and managing the
 * user interface of synthwave-themed games. It defines a set of abstract methods
 * that subclasses must implement to handle all UI-related behaviors, including:
 * <ul>
 *   <li>Building the overall UI layout.</li>
 *   <li>Updating the main informational label.</li>
 *   <li>Resetting the game board components.</li>
 *   <li>Enabling and disabling interactive UI controls.</li>
 *   <li>Displaying game-over dialogs with appropriate messages.</li>
 *   <li>Updating the score display on the interface.</li>
 *   <li>Initiating the game start sequence (which may include animations or resets).</li>
 * </ul>
 *
 * <p>This class helps enforce a clear separation between the UI presentation logic
 * and the underlying game mechanics, promoting encapsulation and modular design.
 * Subclasses (e.g., specific game implementations) will provide concrete behaviors
 * for these methods, ensuring a consistent user experience across different synthwave
 * themed games.</p>
 *
 * @see ca.bcit.comp25222.termproject.numbergame.GameLogic
 *
 * @author Braeden Duval
 * @version 1.0
 */

abstract class AbstractSynthwaveUI
{

    /**
     * Constructs and initializes the user interface components for this instance.
     */
    protected abstract void buildUI();

    /**
     * Updates the main informational label with the provided message.
     *
     * @param message The message to display.
     */
    protected abstract void updateInfoLabel(String message);

    /**
     * Resets the game board UI components (e.g., clears buttons, resets state).
     */
    protected abstract void resetGameBoard();

    /**
     * Disables all interactive UI controls, for example after the game is over.
     */
    protected abstract void disableGameControls();

    /**
     * Enables all interactive UI controls, for example when starting a new game.
     */
    protected abstract void enableGameControls();

    /**
     * Displays a game-over dialog with the specified title and message.
     *
     * @param message The message content.
     */
    protected abstract void showGameOverDialog(final String message);

    /**
     * Updates the score display on the UI.
     *
     * @param scoreMessage The score details to display.
     */
    protected abstract void updateScoreDisplay(final String scoreMessage);
}
