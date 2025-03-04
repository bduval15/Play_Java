package ca.bcit.comp25222.termproject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a score record for a game session, including statistics on correct
 * and incorrect attempts and the date and time of play.
 * This class provides functionality to calculate scores, store and retrieve
 * scores from a file, and check for high scores.
 *
 * @author Braeden Duval
 */
class Score
{
    private static final int GAMES_LINE_INDEX = 1;
    private static final int FIRST_CORRECT_LINE_INDEX = 2;
    private static final int SECOND_CORRECT_LINE_INDEX = 3;
    private static final int INCORRECT_LINE_INDEX = 4;

    private final LocalDateTime dateTimePlayed;
    private final int numGamesPlayed;
    private final int numCorrectFirstAttempt;
    private final int numCorrectSecondAttempt;
    private final int numIncorrectTwoAttempts;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a Score object with a specified date and game statistics.
     *
     * @param dateTimePlayed            The date and time when the game was played.
     * @param numGamesPlayed            The number of games played.
     * @param numCorrectFirstAttempt    The number of first-attempt correct answers.
     * @param numCorrectSecondAttempt   The number of second-attempt correct answers.
     * @param numIncorrectTwoAttempts   The number of incorrect answers after two attempts.
     */
    Score(
            final LocalDateTime dateTimePlayed,
            final int numGamesPlayed,
            final int numCorrectFirstAttempt,
            final int numCorrectSecondAttempt,
            final int numIncorrectTwoAttempts)
    {
        this.dateTimePlayed = dateTimePlayed;
        this.numGamesPlayed = numGamesPlayed;
        this.numCorrectFirstAttempt = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;
    }

    /**
     * Constructs a Score object using the current date and time.
     *
     * @param numGamesPlayed            The number of games played.
     * @param numCorrectFirstAttempt    The number of first-attempt correct answers.
     * @param numCorrectSecondAttempt   The number of second-attempt correct answers.
     * @param numIncorrectTwoAttempts   The number of incorrect answers after two attempts.
     */
    Score(
            final int numGamesPlayed,
            final int numCorrectFirstAttempt,
            final int numCorrectSecondAttempt,
            final int numIncorrectTwoAttempts)
    {
        this.dateTimePlayed = LocalDateTime.now();
        this.numGamesPlayed = numGamesPlayed;
        this.numCorrectFirstAttempt = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;

    }

    /**
     * Calculates the total score for the session.
     *
     * @return The total score, where first-attempt correct answers are worth
     *         2 points and second-attempt correct answers are worth 1 point.
     */
    int getScore()
    {
        final int totalScore;
        totalScore = (numCorrectFirstAttempt * FIRST_CORRECT_LINE_INDEX) + numCorrectSecondAttempt;

        return totalScore;
    }

    /**
     * Computes the average score per game in the session.
     *
     * @return The average score per game.
     */
    double getAverageScorePerGame()
    {
        final double avgScore;
        avgScore = (double) getScore() / numGamesPlayed;

        return avgScore;
    }

    /**
     * Returns a string representation of the score, including all relevant details.
     *
     * @return A formatted string containing the date, number of games,
     *         correct and incorrect answers, and calculated scores.
     */
    @Override
    public String toString()
    {
        final StringBuilder sb;
        sb = new StringBuilder();

        sb.append("Date and Time: ").append(dateTimePlayed.format(formatter)).append("\n");
        sb.append("Games Played: ").append(numGamesPlayed).append("\n");
        sb.append("Correct First Attempts: ").append(numCorrectFirstAttempt).append("\n");
        sb.append("Correct Second Attempts: ").append(numCorrectSecondAttempt).append("\n");
        sb.append("Incorrect Attempts: ").append(numIncorrectTwoAttempts).append("\n");

        if (numGamesPlayed == GAMES_LINE_INDEX)
        {
            sb.append("Score: ").append(getScore()).append(" points\n");
        } else {
            sb.append("Total Score: ").append(getScore()).append("\n");
            sb.append("Average Score Per Game: ").append(String.format("%.2f", getAverageScorePerGame())).append("\n");
        }

        final String gameInfo;
        gameInfo = sb.toString();

        return gameInfo;
    }

    /**
     * Appends a score record to a file.
     *
     * @param score         The score object to be written to the file.
     * @param fileName      The name of the file where the score should be stored.
     * @throws IOException  If an error occurs while writing to the file.
     */
    static void appendScoreToFile(
            final Score score,
            final String fileName)
            throws IOException
    {
        try (FileWriter     fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter    pw = new PrintWriter(bw))
        {
            pw.println(score.toString());
            pw.println();
        }
    }

    /**
     * Reads and parses scores from a file into a list of Score objects.
     *
     * @param fileName The name of the file containing score records.
     * @return A list of Score objects parsed from the file.
     * @throws IOException If an error occurs while reading the file.
     */
    static List<Score> readScoresFromFile(
            final String fileName)
            throws IOException
    {

        final List<Score> scores;
        scores = new ArrayList<>();

        final List<String> lines;
        lines = Files.readAllLines(Paths.get(fileName));

        final List<String> record;
        record = new ArrayList<>();

        for (String line : lines)
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

    /*
     * Parses a list of strings representing a score record into a Score object.
     *
     * @param record A list of strings representing a single score entry.
     * @return A Score object parsed from the given record.
     * @throws IllegalArgumentException If the format of the score record is invalid.
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

            dateLine = record.getFirst();
            gamesLine = record.get(GAMES_LINE_INDEX);
            firstLine = record.get(FIRST_CORRECT_LINE_INDEX);
            secondLine = record.get(SECOND_CORRECT_LINE_INDEX);
            incorrectLine = record.get(INCORRECT_LINE_INDEX);
            dateString = dateLine.substring("Date and Time:".length()).trim();
            dateTime = LocalDateTime.parse(dateString, formatter);

            final int games;
            final int first;
            final int second;
            final int incorrect;

            games = Integer.parseInt(gamesLine.substring("Games Played:".length()).trim());
            first = Integer.parseInt(firstLine.substring("Correct First Attempts:".length()).trim());
            second = Integer.parseInt(secondLine.substring("Correct Second Attempts:".length()).trim());
            incorrect = Integer.parseInt(incorrectLine.substring("Incorrect Attempts:".length()).trim());

            final Score score;
            score = new Score(dateTime, games, first, second, incorrect);
            return score;

        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid score format: ");
        }
    }

    /**
     * Compares the current score against the highest recorded average score
     * from a file and displays a message if a new high score is achieved.
     *
     * @param currentScore The current score to compare against the high score.
     */
    static void checkHighScore(Score currentScore)
    {
        try
        {
            final List<Score> scores;
            final Score highestScore;
            final double currentAvg;

            scores = readScoresFromFile("score.txt");

            highestScore = scores.stream()
                    .max(Comparator.comparingDouble(Score::getAverageScorePerGame))
                    .orElse(null);

            currentAvg = currentScore.getAverageScorePerGame();

            if (highestScore == null)
            {
                System.out.printf("CONGRATULATIONS! You are the new high score with an average of " +
                        "%.2f points per game; no previous record exists.%n", currentAvg);
            }
            else
            {
                final double highestAvg;
                final String highScoreDateTime;

                highestAvg = highestScore.getAverageScorePerGame();
                highScoreDateTime = highestScore.dateTimePlayed.toString();

                if (currentAvg > highestAvg)
                {
                    System.out.printf("CONGRATULATIONS! You are the new high score with an average of" +
                                    " %.2f points per game; the previous record was %.2f points per game on %s.%n",
                            currentAvg, highestAvg, highScoreDateTime);
                }
                else
                {
                    System.out.printf("You did not beat the high score of %.2f points per game from %s.%n",
                            highestAvg, highScoreDateTime);
                }
            }
        } catch (IOException e)
        {
            System.out.println("Error reading score file: " + e.getMessage());
        }
    }
}
