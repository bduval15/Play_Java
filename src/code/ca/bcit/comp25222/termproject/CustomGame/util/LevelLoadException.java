package ca.bcit.comp25222.termproject.CustomGame.util; // CHANGE THIS

/**
 * Custom Exception (Course Concept)
 * Used for errors during level file loading and parsing.
 */
public class LevelLoadException extends Exception { // Inherits from Exception

  public LevelLoadException(String message) {
    super(message);
  }

  public LevelLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
