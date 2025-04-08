package ca.bcit.comp2522.termproject.wordgame;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a score record for a game session, encapsulating key statistics such as the number of games played,
 * the counts of correct answers on first and second attempts, and the count of incorrect answers. It also captures
 * the date and time when the score was recorded.
 * <p>
 * This class provides methods to:
 * <ul>
 *   <li>Calculate the total score for the session (with first-attempt correct answers worth 2 points and
 *   second-attempt correct answers worth 1 point).</li>
 *   <li>Compute the average score per game.</li>
 *   <li>Parse a score record from a list of strings (typically read from a file)
 *   using the {@link #parseScore(List)} method.</li>
 *   <li>Convert a score record to a formatted string for saving via {@link #toString()}.</li>
 *   <li>Append a score record to a file using the static {@link #appendScoreToFile(Score, String)} method.</li>
 *   <li>Check if the current score beats previous high scores by reading from a file and comparing averages via the
 *       {@link #checkHighScore(Score, String)} method.</li>
 *   <li>Read multiple score records from a file into a list using the {@link #readScoresFromFile(String)} method.</li>
 *
 * @author Braeden Duval
 * @version 1.0
 */

final class Score
{
    private static final int DEFAULT_VALUE              = 0;
    private static final int GAMES_LINE_INDEX           = 1;
    private static final int FIRST_CORRECT_LINE_INDEX   = 2;
    private static final int SECOND_CORRECT_LINE_INDEX  = 3;
    private static final int INCORRECT_LINE_INDEX       = 4;

    private static final String dateTimeText                = "Date and Time: ";
    private static final String gamesPlayedText             = "Games Played: ";
    private static final String correctFirstAttemptText     = "Correct First Attempts: ";
    private static final String correctSecondAttemptText    = "Correct Second Attempts: ";
    private static final String incorrectAttemptsText       = "Incorrect Attempts: ";

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime dateTimePlayed;
    private final int           numGamesPlayed;
    private final int           numCorrectFirstAttempt;
    private final int           numCorrectSecondAttempt;
    private final int           numIncorrectTwoAttempts;

    /**
     * Constructs a {@code Score} object for the current session using the current date and time.
     * <p>
     * The provided game statistics are validated so that none is negative using the shared
     * {@link #validateStats(int, int, int, int)} method. If any parameter is less than {@code DEFAULT_VALUE},
     * an {@link IllegalArgumentException} is thrown.
     * </p>
     *
     * @param numGamesPlayed          the number of games played (must be non-negative).
     * @param numCorrectFirstAttempt  the number of correct answers on the first attempt
     *                                (non-negative; each worth 2 points).
     * @param numCorrectSecondAttempt the number of correct answers on the second attempt
     *                                (non-negative; each worth 1 point).
     * @param numIncorrectTwoAttempts the number of incorrect attempts (non-negative).
     * @throws IllegalArgumentException if any of the statistics is negative.
     */
    Score(
            final int numGamesPlayed,
            final int numCorrectFirstAttempt,
            final int numCorrectSecondAttempt,
            final int numIncorrectTwoAttempts)
    {
        validateStats(numGamesPlayed,
                      numCorrectFirstAttempt,
                      numCorrectSecondAttempt,
                      numIncorrectTwoAttempts);

        this.dateTimePlayed             = LocalDateTime.now();
        this.numGamesPlayed             = numGamesPlayed;
        this.numCorrectFirstAttempt     = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt    = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts    = numIncorrectTwoAttempts;

    }

    /**
     * Constructs a {@code Score} object using a specified timestamp along with game statistics.
     * <p>
     * The {@code dateTimePlayed} parameter must not be {@code null}. In addition, all the numeric statistics
     * are validated (using {@link #validateStats(int, int, int, int)}) to ensure they are not negative.
     * </p>
     *
     * @param dateTimePlayed          the date and time when the game was played (must not be {@code null}).
     * @param numGamesPlayed          the number of games played.
     * @param numCorrectFirstAttempt  the number of correct answers on the first attempt.
     * @param numCorrectSecondAttempt the number of correct answers on the second attempt.
     * @param numIncorrectTwoAttempts the number of incorrect answers after two attempts.
     * @throws IllegalArgumentException if {@code dateTimePlayed} is {@code null} or any statistic is negative.
     */
    Score(
            final LocalDateTime dateTimePlayed,
            final int numGamesPlayed,
            final int numCorrectFirstAttempt,
            final int numCorrectSecondAttempt,
            final int numIncorrectTwoAttempts)
    {
        if (dateTimePlayed == null)
        {
            throw new IllegalArgumentException("dateTimePlayed cannot be null.");
        }

        validateStats(numGamesPlayed,
                      numCorrectFirstAttempt,
                      numCorrectSecondAttempt,
                      numIncorrectTwoAttempts);

        this.dateTimePlayed             = dateTimePlayed;
        this.numGamesPlayed             = numGamesPlayed;
        this.numCorrectFirstAttempt     = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt    = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts    = numIncorrectTwoAttempts;
    }

    /*
     * Validates the numeric game statistics.
     *
     * Each parameter is checked to ensure it is not less than DEFAULT_VALUE.
     * If any parameter is negative, this method throws an IllegalArgumentException with a descriptive message.
     *
     *
     * @param numGamesPlayed          the number of games played.
     * @param numCorrectFirstAttempt  the number of first-attempt correct answers.
     * @param numCorrectSecondAttempt the number of second-attempt correct answers.
     * @param numIncorrectTwoAttempts the number of incorrect answers after two attempts.
     * @throws IllegalArgumentException if any of the parameters is less than DEFAULT_VALUE.
     */
    private static void validateStats(final int numGamesPlayed,
                                      final int numCorrectFirstAttempt,
                                      final int numCorrectSecondAttempt,
                                      final int numIncorrectTwoAttempts)
    {
        if (numGamesPlayed < DEFAULT_VALUE)
        {
            throw new IllegalArgumentException("GamesPlayed cannot be negative.");
        }
        if (numCorrectFirstAttempt < DEFAULT_VALUE)
        {
            throw new IllegalArgumentException("CorrectFirstAttempt cannot be negative.");
        }
        if (numCorrectSecondAttempt < DEFAULT_VALUE)
        {
            throw new IllegalArgumentException("CorrectSecondAttempt cannot be negative.");
        }
        if (numIncorrectTwoAttempts < DEFAULT_VALUE)
        {
            throw new IllegalArgumentException("IncorrectTwoAttempts cannot be negative.");
        }
    }

    /*
     * Parses a list of strings into a Score object.
     *
     * The provided record list is expected to contain a fixed number of lines, where:
     *
     *   The first line contains the date and time string prefixed by dateTimeText.
     *   The line at index GAMES_LINE_INDEX contains the number of games played,
     *   prefixed by gamesPlayedText.</li>
     *   The line at index FIRST_CORRECT_LINE_INDEX contains the count of correct first attempts,
     *   prefixed by correctFirstAttemptText.
     *   The line at index SECOND_CORRECT_LINE_INDEX contains the count of correct second attempts,
     *   prefixed by correctSecondAttemptText.
     *   The line at index INCORRECT_LINE_INDEX contains the count of incorrect attempts,
     *       prefixed by incorrectAttemptsText.
     *
     * The method extracts the numeric values from each line (by removing the respective prefix text),
     * converts them to integers, and parses the date and time string using the specified formatter.
     * If any conversion fails, an IllegalArgumentException is thrown.
     * </p>
     *
     * @param record a List<String> representing the lines of a score record.
     * @return a new Score object constructed from the parsed values.
     * @throws IllegalArgumentException if the record does not conform to the expected format.
     */
    private static Score parseScore(final List<String> record)
    {
        try
        {
            final String dateLine;
            final String gamesLine;
            final String firstLine;
            final String secondLine;
            final String incorrectLine;
            final String dateString;
            final LocalDateTime dateTime;

            dateLine        = record.getFirst();
            gamesLine       = record.get(GAMES_LINE_INDEX);
            firstLine       = record.get(FIRST_CORRECT_LINE_INDEX);
            secondLine      = record.get(SECOND_CORRECT_LINE_INDEX);
            incorrectLine   = record.get(INCORRECT_LINE_INDEX);
            dateString      = dateLine.substring(dateTimeText.length()).trim();
            dateTime        = LocalDateTime.parse(dateString, formatter);

            final int games;
            final int first;
            final int second;
            final int incorrect;
            final Score score;

            games       = Integer.parseInt(gamesLine.substring(gamesPlayedText.length()).trim());
            first       = Integer.parseInt(firstLine.substring(correctFirstAttemptText.length()).trim());
            second      = Integer.parseInt(secondLine.substring(correctSecondAttemptText.length()).trim());
            incorrect   = Integer.parseInt(incorrectLine.substring(incorrectAttemptsText.length()).trim());
            score       = new Score(dateTime, games, first, second, incorrect);

            return score;

        } catch (final NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid score format: ");
        }
    }

    /**
     * Appends a score record to a file.
     * <p>
     * This method opens the specified file in append mode and writes the string representation
     * of the provided {@code score} (obtained via {@link #toString()}) followed by a blank line for separation.
     * It uses a {@code FileWriter}, {@code BufferedWriter}, and {@code PrintWriter} to perform the write operation.
     * </p>
     *
     * @param score    the {@code Score} object to be written.
     * @param fileName the file path where the score record should be appended.
     * @throws IOException if an error occurs while writing to the file.
     */
    static void appendScoreToFile(final Score  score,
                                  final String fileName)
                                  throws IOException
    {
            final FileWriter fileWriter;
            final BufferedWriter bufferedWriter;
            final PrintWriter printWriter;

            fileWriter      = new FileWriter(fileName, true);
            bufferedWriter  = new BufferedWriter(fileWriter);
            printWriter     = new PrintWriter(bufferedWriter);

        try (fileWriter;
             bufferedWriter;
             printWriter)
        {
            printWriter.println(score.toString());
            printWriter.println();
        }
    }

    /**
     * Reads high score records from a file and compares them with the current score.
     * <p>
     * This method reads all score records from the specified file using {@link #readScoresFromFile(String)},
     * determines the highest average score per game from the list, and then compares it with the current score's
     * average (obtained via {@link #getAverageScorePerGame()}). Depending on the comparison:
     * <ul>
     *   <li>If no previous records exist, a congratulatory message is printed indicating that the current score is
     *       the new high score.</li>
     *   <li>If the current average is higher than the previous high,
     *       a message is printed indicating the new high score,
     *       along with the previous record's average and timestamp.</li>
     *   <li>If the current average does not exceed the previous high,
     *       a message is printed informing the user of the existing record.</li>
     * </ul>
     * </p>
     *
     * @param currentScore the current {@code Score} to be compared.
     * @param fileName     the file containing previous score records.
     */
    static void checkHighScore(final Score currentScore,
                               final String fileName)
    {
        try
        {
            final List<Score>   scores;
            final Score         highestScore;
            final double        currentAvg;

            scores = readScoresFromFile(fileName);

            highestScore = scores.stream()
                                 .max(Comparator.comparingDouble(Score::getAverageScorePerGame))
                                 .orElse(null);

            currentAvg = currentScore.getAverageScorePerGame();

            if (highestScore == null)
            {
                System.out.printf("CONGRATULATIONS! You are the new high score with an average of " +
                                  "%.2f points per game;\nNo previous record exists.%n", currentAvg);
            }
            else
            {
                final double highestAvg;
                final String highScoreDateTime;

                highestAvg          = highestScore.getAverageScorePerGame();
                highScoreDateTime   = highestScore.dateTimePlayed.toString();

                if (currentAvg > highestAvg)
                {
                    System.out.printf("CONGRATULATIONS! You are the new high score with an average of" +
                                      " %.2f points per game;\n " +
                                      "The previous record was %.2f points per game on %s.%n",
                                      currentAvg, highestAvg, highScoreDateTime);
                }
                else
                {
                    System.out.printf("You did not beat the high score of %.2f points per game from %s.%n",
                                      highestAvg, highScoreDateTime);
                }
            }
        } catch (final IOException e)
        {
            throw new IllegalArgumentException(
                    "Error reading score file: " + e.getMessage());
        }
    }

    /**
     * Reads score records from a file and parses them into a list of {@code Score} objects.
     * <p>
     * This method reads all lines of text from the specified file. It then iterates through the lines,
     * grouping non-empty lines into a record. When an empty line is encountered or the end of file is reached,
     * the grouped lines are passed to {@link #parseScore(List)} to construct a {@code Score} object which is then
     * added to a list. The list of {@code Score} objects is returned.
     * </p>
     *
     * @param fileName the file path from which to read score records.
     * @return a {@code List<Score>} containing all parsed score records.
     * @throws IOException if an error occurs while reading the file.
     */
    static List<Score> readScoresFromFile(
            final String fileName)
            throws IOException
    {

        final List<Score> scores;
        final List<String> lines;
        final List<String> record;

        scores  = new ArrayList<>();
        lines   = Files.readAllLines(Paths.get(fileName));
        record  = new ArrayList<>();

        for (final String line : lines)
        {
            if (line.trim().isEmpty())
            {
                if (!record.isEmpty())
                {
                    final Score s;
                    s = parseScore(record);

                    scores.add(s);

                    record.clear();
                }
            }
            else
            {
                record.add(line);
            }
        }
        if (!record.isEmpty())
        {
            final Score s;
            s = parseScore(record);

            scores.add(s);
        }
        return scores;
    }

    /**
     * Calculates the total score for the session.
     * <p>
     * The total score is computed by multiplying the number of first-attempt correct answers by the constant
     * {@code FIRST_CORRECT_LINE_INDEX} (representing two points per first-attempt) and then adding the number
     * of second-attempt correct answers (worth one point each).
     * </p>
     *
     * @return the total score as an integer.
     */
    int getScore()
    {
        final int totalScore;
        totalScore = (numCorrectFirstAttempt * FIRST_CORRECT_LINE_INDEX) +
                      numCorrectSecondAttempt;

        return totalScore;
    }

    /**
     * Computes the average score per game for the session.
     * <p>
     * The average score is calculated by dividing the total score (as computed by {@link #getScore()})
     * by the number of games played. The result is returned as a {@code double}.
     * </p>
     *
     * @return the average score per game.
     */
    double getAverageScorePerGame()
    {
        final double avgScore;
        avgScore = (double) getScore() / numGamesPlayed;

        return avgScore;
    }

    /**
     * Returns a formatted string representation of the score record.
     * <p>
     * The string representation includes:
     * <ul>
     *   <li>The date and time when the score was recorded, prefixed by {@code dateTimeText}.</li>
     *   <li>The number of games played, prefixed by {@code gamesPlayedText}.</li>
     *   <li>The count of correct first attempts, prefixed by {@code correctFirstAttemptText}.</li>
     *   <li>The count of correct second attempts, prefixed by {@code correctSecondAttemptText}.</li>
     *   <li>The count of incorrect attempts, prefixed by {@code incorrectAttemptsText}.</li>
     * </ul>
     * Additionally, if only one game was played (i.e. {@code numGamesPlayed} equals {@code GAMES_LINE_INDEX}),
     * a simple "Score: ..." message is appended. Otherwise, both the total score and
     * average score per game are included.
     * </p>
     *
     * @return a formatted {@code String} detailing all score statistics.
     */
    @Override
    public String toString()
    {
        final StringBuilder stringBuilder;
        stringBuilder = new StringBuilder();

        stringBuilder.append(dateTimeText).append(dateTimePlayed.format(formatter)).append("\n");
        stringBuilder.append(gamesPlayedText).append(numGamesPlayed).append("\n");
        stringBuilder.append(correctFirstAttemptText).append(numCorrectFirstAttempt).append("\n");
        stringBuilder.append(correctSecondAttemptText).append(numCorrectSecondAttempt).append("\n");
        stringBuilder.append(incorrectAttemptsText).append(numIncorrectTwoAttempts).append("\n");

        if (numGamesPlayed == GAMES_LINE_INDEX)
        {
            stringBuilder.append("Score: ").append(getScore()).append(" points\n");
        }
        else
        {
            stringBuilder.append("Total Score: ").append(getScore()).append("\n");
            stringBuilder.append("Average Score Per Game: ")
                         .append(String.format("%.2f", getAverageScorePerGame()));
        }

        final String gameInfo;
        gameInfo = stringBuilder.toString();

        return gameInfo;
    }
}
