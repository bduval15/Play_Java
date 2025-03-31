package ca.bcit.comp25222.termproject.CustomGame.managers;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manages the loading and saving of high scores for the Resource Router game.
 * <p>
 * High scores are persisted in a text file whose name is defined by {@value #SCORE_FILENAME}. This file is located
 * in the project resources (or project root) and is automatically created if it does not exist.
 * </p>
 * <p>
 * This class provides functionality to:
 * <ul>
 *   <li>
 *     <strong>Load High Scores:</strong> The {@link #loadHighScores()} method reads the file line-by-line using a
 *     buffered reader, converts each line to a {@link ScoreEntry} object via the {@code ScoreEntry.fromSaveString(String)}
 *     method, filters out any malformed entries, sorts the high scores (using their natural ordering), and returns
 *     the resulting list.
 *   </li>
 *   <li>
 *     <strong>Save High Scores:</strong> A private helper method {@link #saveHighScores(List)} writes a list of
 *     {@link ScoreEntry} objects to the file. The file is overwritten with the new list, where each score is written
 *     using the format defined in {@link ScoreEntry#toSaveString()}.
 *   </li>
 *   <li>
 *     <strong>Add and Save Score:</strong> The {@link #addAndSaveScore(int, String)} method loads the current high scores,
 *     adds a new score entry (constructed with the player's name and the new score), sorts the updated list in descending
 *     order (highest scores first), retains only the top {@value #MAX_SCORES_TO_KEEP} scores, and saves the final list back to the file.
 *   </li>
 * </ul>
 * </p>
 * <p>
 * All file operations use the UTF-8 character set to ensure proper encoding. Any IO exceptions encountered during reading
 * or writing are caught and logged to the standard error stream.
 * </p>
 * <p>
 * This class is declared final to prevent subclassing and to guarantee a consistent behavior for high score management.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class HighScoreManager
{

    private static final String SCORE_FILENAME = "RRHighScores.txt";
    private static final Path SCORE_FILE_PATH = Paths.get(SCORE_FILENAME);
    private static final int MAX_SCORES_TO_KEEP = 10;

    /*
     * Saves the provided list of high scores to the file.
     *
     * @param scores the list of ScoreEntry objects to save.
     */
    private static void saveHighScores(final List<ScoreEntry> scores)
    {
        try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(
                SCORE_FILE_PATH,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)))
        {
            for (final ScoreEntry entry : scores)
            {
                writer.println(entry.toSaveString());
            }
        } catch (final IOException e)
        {
            final String errorMessage;
            errorMessage = "Error writing high score file: " + e.getMessage();
            System.err.println(errorMessage);
        }
    }

    /**
     * Loads the high scores from the file.
     * If the file does not exist, it is created and an empty list is returned.
     *
     * @return a list of ScoreEntry objects representing the high scores.
     */
    public static List<ScoreEntry> loadHighScores()
    {
        if (!Files.exists(SCORE_FILE_PATH))
        {
            try {
                Files.createFile(SCORE_FILE_PATH);
                final String createdMessage;
                createdMessage = "High score file not found. Created new file at: " + SCORE_FILE_PATH;
                System.out.println(createdMessage);
            } catch (final IOException e)
            {
                final String errorMessage;
                errorMessage = "Error creating high score file: " + e.getMessage();
                System.err.println(errorMessage);
                return new ArrayList<>();
            }
        }

        try {
            final List<ScoreEntry> scores;
            scores = Files.lines(SCORE_FILE_PATH, StandardCharsets.UTF_8)
                    .map(ScoreEntry::fromSaveString)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());
            return scores;
        } catch (final IOException e)
        {
            final String errorMessage;
            errorMessage = "Error reading high score file: " + e.getMessage();
            System.err.println(errorMessage);
            return new ArrayList<>();
        }
    }

    /**
     * Adds a new score with the player's name and saves the updated high score list.
     *
     * @param newScore   the new score achieved.
     * @param playerName the name of the player.
     */
    public static void addAndSaveScore(final int newScore, final String playerName)
    {
        final ScoreEntry newEntry;
        newEntry = new ScoreEntry(newScore, playerName);

        final List<ScoreEntry> currentScores;
        currentScores = loadHighScores();
        currentScores.add(newEntry);
        currentScores.sort(null);

        final List<ScoreEntry> topScores;
        topScores = currentScores.stream()
                .limit(MAX_SCORES_TO_KEEP)
                .collect(Collectors.toList());
        saveHighScores(topScores);
    }
}
