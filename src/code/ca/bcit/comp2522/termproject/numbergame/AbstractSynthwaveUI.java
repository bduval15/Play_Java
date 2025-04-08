package ca.bcit.comp2522.termproject.numbergame;

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
 * @see Gameplay
 *
 * @author Braeden Duval
 * @version 1.0
 */

abstract class AbstractSynthwaveUI
{

    /**
     * Constructs and initializes the user interface components specific to the synthwave theme.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Create and layout primary UI elements such as buttons, labels, and score indicators.</li>
     *   <li>Initialize styling elements consistent with the synthwave theme.</li>
     *   <li>Set up any event listeners required for user interactions.</li>
     * </ul>
     * </p>
     */
    abstract void buildUI();

    /**
     * Updates the main informational label in the user interface with the provided message.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Ensure the message clearly communicates the current game status to the player.</li>
     *   <li>Update the label visually without disrupting the ongoing gameplay.</li>
     * </ul>
     * </p>
     *
     * @param message the message string to be displayed in the main information area.
     */
    abstract void updateInfoLabel(final String message);

    /**
     * Resets the game board components to their initial state.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Clear any placed elements or previous game states from the UI components.</li>
     *   <li>Re-enable interactive components necessary to start a new game.</li>
     *   <li>Visually prepare the game board for fresh gameplay.</li>
     * </ul>
     * </p>
     */
    abstract void resetGameBoard();

    /**
     * Disables all interactive controls in the user interface, typically after the game has ended.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Prevent further player interaction until a new game is initiated.</li>
     *   <li>Clearly indicate visually that controls are inactive (e.g., grayed-out buttons).</li>
     * </ul>
     * </p>
     */
    abstract void disableGameControls();

    /**
     * Displays a dialog box or overlay indicating the game has ended, accompanied by a custom message.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Show the provided message clearly and prominently.</li>
     *   <li>Include options for the player to acknowledge the message and possibly start a new game.</li>
     *   <li>Ensure the dialog is styled consistently with the overall synthwave aesthetic.</li>
     * </ul>
     * </p>
     *
     * @param message the message to be shown in the game-over dialog.
     */
    abstract void showGameOverDialog(final String message);

    /**
     * Updates the display of the player's score within the user interface.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Clearly present the updated score information to the player.</li>
     *   <li>Ensure the score is displayed prominently and updates dynamically.</li>
     *   <li>Maintain styling consistency with the synthwave theme.</li>
     * </ul>
     * </p>
     *
     * @param scoreMessage a formatted string detailing the current score information.
     */
    abstract void updateScoreDisplay(final String scoreMessage);
}
