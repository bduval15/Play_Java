package ca.bcit.comp2522.termproject.numbergame;

/**
 * The Gameplay interface defines the core operations required to manage the state and progression
 * of a Number Game. Implementers of this interface are responsible for initializing new games,
 * generating and placing numbers on the game board, updating scores when the game ends, and checking
 * for win or game-over conditions.
 * <p>
 * The key responsibilities include:
 * <ul>
 *   <li>{@link #startNewGame()} – Resetting the game state and initializing a new game.</li>
 *   <li>{@link #updateScoreOnGameEnd(boolean)} – Updating the game score based on the outcome of the game.</li>
 *   <li>{@link #placeNumber(int)} – Placing the next number on the game board at a specified index.</li>
 *   <li>{@link #generateNextNumber()} – Generating and returning the next number to be placed on the board.</li>
 *   <li>{@link #checkIfGameOver()} – Determining whether no valid moves remain.</li>
 *   <li>{@link #checkIfWin()} – Checking whether the win condition has been met.</li>
 * </ul>
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

interface Gameplay
{

    /**
     * Resets and initializes the state of the game board and any associated game logic.
     * <p>
     * Implementations should ensure the following:
     * <ul>
     *   <li>Clear any existing game data or state.</li>
     *   <li>Reset any counters or trackers used during gameplay.</li>
     *   <li>Generate an initial set of numbers or game elements for placement.</li>
     *   <li>Prepare the user interface (if applicable) to reflect a new game state.</li>
     * </ul>
     * </p>
     */
    void startNewGame();

    /**
     * Updates the player's score upon game completion, based on whether the player won or lost.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Increment or decrement scores appropriately based on the game outcome.</li>
     *   <li>Persist the updated scores to maintain score history (if required).</li>
     *   <li>Provide feedback to the player about their new score and the outcome of the game.</li>
     * </ul>
     * </p>
     *
     * @param didWin {@code true} if the player won the game, {@code false} otherwise.
     */
    void updateScoreOnGameEnd(final boolean didWin);

    /**
     * Attempts to place the next available number at the specified index on the game board.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Check if the specified index is within valid bounds of the game board.</li>
     *   <li>Determine if the placement at the specified index is valid based on game rules.</li>
     *   <li>Update the game board state with the placed number if valid.</li>
     *   <li>Handle and report invalid placements appropriately.</li>
     * </ul>
     * </p>
     *
     * @param index the index position on the game board where the number should be placed.
     */
    void placeNumber(final int index);

    /**
     * Generates and returns the next number to be placed on the game board.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Manage an internal sequence or randomization logic to provide the next number.</li>
     *   <li>Return -1 if there are no further numbers available to generate.</li>
     *   <li>Ensure consistency and fairness in number generation logic according to game rules.</li>
     * </ul>
     * </p>
     *
     * @return the next number to place on the board, or {@code -1} if no numbers remain.
     */
    int generateNextNumber();


    /**
     * Determines if the game has reached a state with no valid moves remaining.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Check the current state of the game board for possible placements.</li>
     *   <li>Identify if future moves or placements are blocked or impossible.</li>
     *   <li>Return {@code true} if no valid placements or actions remain, ending the game.</li>
     * </ul>
     * </p>
     *
     * @return {@code true} if the game is over (no valid moves), {@code false} otherwise.
     */
    boolean checkIfGameOver();

    /**
     * Checks if the player has successfully achieved the win condition defined by the game.
     * <p>
     * Implementations should:
     * <ul>
     *   <li>Evaluate the game board or game state to identify if all win conditions are satisfied.</li>
     *   <li>Return {@code true} immediately upon satisfying the winning criteria.</li>
     *   <li>Clearly document what constitutes a "win" condition for implementers.</li>
     * </ul>
     * </p>
     *
     * @return {@code true} if the win condition has been met, {@code false} otherwise.
     */
    boolean checkIfWin();
}
