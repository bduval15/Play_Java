package ca.bcit.comp25222.termproject.resourcerouter.util;

/**
 * Enumerates the possible states of the game during its lifecycle.
 * <p>
 * This enum is used throughout the application to manage and track the overall game flow. Each constant represents a distinct
 * phase or mode in which the game can operate, and transitions between these states trigger corresponding UI updates and
 * gameplay logic. It provides a clear and centralized way to control the game's behavior, ensuring that all components respond
 * appropriately to state changes.
 * </p>
 * <p>
 * The game states include:
 * <ul>
 *   <li><strong>MENU:</strong> The game is in the main menu, where the player can start a new game, view high scores, or access other options.</li>
 *   <li><strong>TUTORIAL:</strong> The game is presenting tutorial content, providing instructions or guidance to the player.</li>
 *   <li><strong>LOADING:</strong> The game is loading level data and initializing game elements. This state indicates that resources are being prepared.</li>
 *   <li><strong>PLAYING:</strong> The game is actively running, and the player is engaged in gameplay. All simulation and game logic updates occur in this state.</li>
 *   <li><strong>LEVEL_COMPLETE:</strong> The current level has been successfully completed. This state is used to display level completion feedback and transition to the next level.</li>
 *   <li><strong>GAME_OVER:</strong> The game session has ended, either due to failure conditions or the completion of the session. In this state, the game may prompt for high score entries and final statistics.</li>
 *   <li><strong>LEVEL_TRANSITION:</strong> The game is in the process of transitioning between levels. Certain animations or effects might continue during this phase.</li>
 *   <li><strong>HIGH_SCORES:</strong> The game is displaying the high scores screen, showing the best scores from previous sessions.</li>
 * </ul>
 * </p>
 *
 * @see ca.bcit.comp25222.termproject.resourcerouter.managers.GameController
 * @author Braeden Duval
 * @version 1.0
 */

public enum GameState
{
    MENU,
    TUTORIAL,
    LOADING,
    PLAYING,
    LEVEL_COMPLETE,
    GAME_OVER,
    LEVEL_TRANSITION, HIGH_SCORES
}