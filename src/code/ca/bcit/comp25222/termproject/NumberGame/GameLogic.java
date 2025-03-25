package ca.bcit.comp25222.termproject.NumberGame;

/**
 * <p>The GameLogic interface defines the core methods required
 * for managing the game state, including starting a new game,
 * generating numbers, placing numbers on the board, checking
 * win/lose conditions, and updating the score at game end.</p>
 *
 * @author Braeden Duval
 */
public interface GameLogic
{

    /**
     * Resets the game state and initializes a new game.
     * This method should clear the board and prepare the game for play.
     */
    void startNewGame();

    /**
     * Generates and returns the next number to be placed on the board.
     *
     * @return the next number to be placed, or -1 if no more numbers are available.
     */
    int generateNextNumber();

    /**
     * Attempts to place the next number at the specified index on the game board.
     *
     * @param index the index where the number should be placed.
     * @return {@code true} if the number was successfully placed, {@code false} otherwise.
     */
    boolean placeNumber(int index);

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

    /**
     * Updates the score at the end of the game.
     *
     * @param didWin {@code true} if the game was won, {@code false} otherwise.
     */
    void updateScoreOnGameEnd(boolean didWin);
}
