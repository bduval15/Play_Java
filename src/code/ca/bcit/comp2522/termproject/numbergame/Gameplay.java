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

public interface Gameplay
{

    /**
     * Resets the game state and initializes a new game.
     * This method should clear the board and prepare the game for play.
     */
    void startNewGame();

    /**
     * Updates the score at the end of the game.
     *
     * @param didWin {@code true} if the game was won, {@code false} otherwise.
     */
    void updateScoreOnGameEnd(boolean didWin);

    /**
     * Attempts to place the next number at the specified index on the game board.
     *
     * @param index the index where the number should be placed.
     */
    void placeNumber(int index);

    /**
     * Generates and returns the next number to be placed on the board.
     *
     * @return the next number to be placed, or -1 if no more numbers are available.
     */
    int generateNextNumber();

    /**
     * Checks if the game is over by determining if there are no valid moves remaining.
     *
     * @return {@code true} if the game is over, {@code false} otherwise.
     */
    boolean checkIfGameOver();

    /**
     * Checks if the win condition for the game has been met.
     *
     * @return {@code true} if the game has been won, {@code false} otherwise.
     */
    boolean checkIfWin();
}
