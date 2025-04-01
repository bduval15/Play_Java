package ca.bcit.comp25222.termproject.resourcerouter.managers;

import java.util.Objects;

/**
 * Represents an entry in the high score list for the game.
 * <p>
 * Each ScoreEntry is an immutable object containing a numerical score and the associated player's name.
 * Instances of ScoreEntry are compared in descending order by score (i.e., a higher score is considered "less"
 * in order than a lower score when sorting, so that the highest scores appear first).
 * </p>
 * <p>
 * The class provides functionality to create a ScoreEntry either from explicit score and player name values,
 * or by parsing a string in a specific format. The expected string format for loading from storage is:
 * <blockquote>
 *     score|playerName
 * </blockquote>
 * where the score is an integer and the player name is a string. If the player name is missing, null, or blank,
 * a default name ("Player") is used.
 * </p>
 * <p>
 * Key features include:
 * <ul>
 *   <li>
 *     <strong>Immutability:</strong> Both the score and the player's name are final and set during construction,
 *     ensuring that a ScoreEntry instance cannot be modified after creation.
 *   </li>
 *   <li>
 *     <strong>Comparison:</strong> The {@link #compareTo(ScoreEntry)} method orders ScoreEntry objects in descending order,
 *     which is useful for displaying high scores with the highest score first.
 *   </li>
 *   <li>
 *     <strong>Persistence:</strong> The {@link #toSaveString()} method provides a standardized string representation
 *     for saving ScoreEntry objects to a file, while the static method {@link #fromSaveString(String)} is used to reconstruct
 *     a ScoreEntry from its string representation.
 *   </li>
 *   <li>
 *     <strong>Equality and Hashing:</strong> The {@link #equals(Object)} and {@link #hashCode()} methods are implemented
 *     based on both the score and the player's name, ensuring that two ScoreEntry objects are considered equal only if
 *     both fields match.
 *   </li>
 *   <li>
 *     <strong>String Representation:</strong> The {@link #toString()} method returns a human-readable representation
 *     in the format "playerName : score", which is useful for debugging and display purposes.
 *   </li>
 * </ul>
 * </p>
 * <p>
 * In summary, ScoreEntry is a fundamental data structure for managing and persisting high scores in the game,
 * encapsulating both the score value and the associated player identity, and providing all necessary methods for
 * comparison, serialization, and display.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class ScoreEntry
             implements Comparable<ScoreEntry>
{

    private static final String DEFAULT_PLAYER_NAME        = "Player";
    private static final int    ZERO_INDEX                 = 0;
    private static final int    SPLIT_VALUE                = 2;
    private static final int    FIRST_SPLIT_VALUE          = 1;

    private final String    playerName;
    private final int       score;

    /**
     * Constructs a new ScoreEntry.
     *
     * @param score      the score
     * @param playerName the player's name; if null or blank, defaults to "Player"
     */
    public ScoreEntry(final int score,
                      final String playerName)
    {
        this.score = score;

        final String tempName;
        if (playerName == null || playerName.isBlank())
        {
            tempName = DEFAULT_PLAYER_NAME;
        }
        else
        {
            tempName = playerName.trim();
        }
        this.playerName = tempName;
    }

    /**
     * Creates a ScoreEntry from a saved string.
     *
     * @param line the string containing the score and player name separated by a '|'
     * @return a ScoreEntry object, or null if the string is null, blank, or malformed.
     */
    public static ScoreEntry fromSaveString(final String line)
    {
        if (line == null || line.isBlank())
        {
            return null;
        }

        final String[] parts;
        parts = line.split("\\|", SPLIT_VALUE);

        try
        {
            final int parsedScore;
            final String parsedName;

            parsedScore = Integer.parseInt(parts[ZERO_INDEX].trim());

            if (parts.length > FIRST_SPLIT_VALUE)
            {
                parsedName = parts[FIRST_SPLIT_VALUE].trim();
            }
            else
            {
                parsedName = DEFAULT_PLAYER_NAME;
            }

            final ScoreEntry entry;
            entry = new ScoreEntry(parsedScore, parsedName);

            return entry;
        }
        catch (final Exception e)
        {
            System.err.println("Warning: Malformed score line: " + line);
            return null;
        }
    }

    /**
     * Returns the score.
     *
     * @return the score as an int.
     */
    public int getScore()
    {
        final int result;
        result = score;
        return result;
    }

    /**
     * Returns a string formatted for saving to a file.
     *
     * @return a string in the format "score|playerName".
     */
    public String toSaveString()
    {
        final String result;
        result = score + "|" + playerName;
        return result;
    }

    /**
     * Compares this ScoreEntry with the specified ScoreEntry for order.
     * The comparison is done in descending order (highest score first).
     *
     * @param other the ScoreEntry to be compared.
     * @return a negative integer, zero, or a positive integer as this score
     *         is greater than, equal to, or less than the specified score.
     */
    @Override
    public int compareTo(final ScoreEntry other)
    {
        final int result;
        result = Integer.compare(other.score, this.score);
        return result;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(final Object o)
    {
        final boolean result;
        if (this == o)
        {
            result = true;
        }
        else if (!(o instanceof ScoreEntry))
        {
            result = false;
        }
        else
        {
            final ScoreEntry that;
            that = (ScoreEntry) o;
            result = (this.score == that.score) && this.playerName.equals(that.playerName);
        }
        return result;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode()
    {
        final int result;
        result = Objects.hash(score, playerName);
        return result;
    }

    /**
     * Returns a string representation of this ScoreEntry.
     *
     * @return a string in the format "playerName : score".
     */
    @Override
    public String toString()
    {
        final String result;
        result = String.format("%s : %d", playerName, score);
        return result;
    }
}
