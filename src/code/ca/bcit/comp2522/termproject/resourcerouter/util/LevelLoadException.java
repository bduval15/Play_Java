package ca.bcit.comp2522.termproject.resourcerouter.util;

/**
 * LevelLoadException is a custom exception used to signal errors encountered during the loading and parsing
 * of level configuration files in the Resource Router game.
 * <p>
 * This exception extends the standard {@link Exception} class and provides a mechanism for reporting
 * specific issues related to level file processing, such as malformed input, missing tokens, or invalid values.
 * It supports both a detailed error message and an optional underlying cause, making it easier to diagnose
 * problems during level initialization.
 * </p>
 * <p>
 * Typical use cases for throwing a LevelLoadException include:
 * <ul>
 *   <li>Encountering an improperly formatted line in the level file.</li>
 *   <li>Missing required configuration directives (e.g., node definitions or time limits).</li>
 *   <li>Failing to parse numerical values from the file.</li>
 *   <li>Any other unexpected condition that prevents a level from being loaded correctly.</li>
 * </ul>
 * </p>
 * <p>
 * Catching this exception allows the game to handle level loading errors gracefully, inform the user,
 * and take corrective actions such as falling back to default configurations or aborting the level load.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class LevelLoadException extends Exception
{

  /**
   * Constructs a new LevelLoadException with the specified detail message.
   *
   * @param message the detail message that explains the reason for the exception.
   */
  public LevelLoadException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new LevelLoadException with the specified detail message and cause.
   *
   * @param message the detail message that explains the reason for the exception.
   * @param cause the underlying cause of this exception,
   *              which can be retrieved later by the {@link #getCause()} method.
   */
  public LevelLoadException(final String message,
                            final Throwable cause)
  {
    super(message, cause);
  }
}
