package ca.bcit.comp25222.termproject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Score
{
    private final LocalDateTime dateTimePlayed;
    private final int numGamesPlayed;
    private final int numCorrectFirstAttempt;
    private final int numCorrectSecondAttempt;
    private final int numIncorrectTwoAttempts;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Score(
            LocalDateTime dateTimePlayed,
            int numGamesPlayed,
            int numCorrectFirstAttempt,
            int numCorrectSecondAttempt,
            int numIncorrectTwoAttempts)
    {
        this.dateTimePlayed = dateTimePlayed;
        this.numGamesPlayed = numGamesPlayed;
        this.numCorrectFirstAttempt = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;
    }

    public Score(int numGamesPlayed,
            int numCorrectFirstAttempt,
            int numCorrectSecondAttempt,
            int numIncorrectTwoAttempts)
    {
        this.dateTimePlayed = LocalDateTime.now();
        this.numGamesPlayed = numGamesPlayed;
        this.numCorrectFirstAttempt = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;

    }

    public int getScore()
    {
        return (numCorrectFirstAttempt * 2) + numCorrectSecondAttempt;
    }


    public double getAverageScorePerGame()
    {
        return (double) getScore() / numGamesPlayed;
    }

    public LocalDateTime getDateTimePlayed()
    {
        return dateTimePlayed;
    }

    public int getNumGamesPlayed()
    {
        return numGamesPlayed;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Date and Time: ").append(dateTimePlayed.format(formatter)).append("\n");
        sb.append("Games Played: ").append(numGamesPlayed).append("\n");
        sb.append("Correct First Attempts: ").append(numCorrectFirstAttempt).append("\n");
        sb.append("Correct Second Attempts: ").append(numCorrectSecondAttempt).append("\n");
        sb.append("Incorrect Attempts: ").append(numIncorrectTwoAttempts).append("\n");

        if (numGamesPlayed == 1)
        {
            sb.append("Score: ").append(getScore()).append(" points\n");
        } else {
            sb.append("Total Score: ").append(getScore()).append("\n");
            sb.append("Average Score Per Game: ").append(String.format("%.2f", getAverageScorePerGame())).append("\n");
        }
        return sb.toString();
    }

    public static void appendScoreToFile(Score score, String fileName) throws IOException
    {
        try (FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            pw.println(score.toString());
            pw.println();
        }
    }

    public static List<Score> readScoresFromFile(String fileName) throws IOException
    {
        List<Score> scores = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(fileName));

        List<String> record = new ArrayList<>();
        for (String line : lines)
        {
            if (line.trim().isEmpty())
            {
                if (!record.isEmpty())
                {
                    Score s = parseScore(record);
                    if (s != null)
                    {
                        scores.add(s);
                    }
                    record.clear();
                }
            } else
            {
                record.add(line);
            }
        }
        if (!record.isEmpty())
        {
            Score s = parseScore(record);
            if (s != null)
            {
                scores.add(s);
            }
        }

        return scores;
    }

    private static Score parseScore(List<String> record)
    {
        try
        {
            String dateLine = record.get(0);
            String gamesLine = record.get(1);
            String firstLine = record.get(2);
            String secondLine = record.get(3);
            String incorrectLine = record.get(4);

            String dateString = dateLine.substring("Date and Time:".length()).trim();
            LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
            int games = Integer.parseInt(gamesLine.substring("Games Played:".length()).trim());
            int first = Integer.parseInt(firstLine.substring("Correct First Attempts:".length()).trim());
            int second = Integer.parseInt(secondLine.substring("Correct Second Attempts:".length()).trim());
            int incorrect = Integer.parseInt(incorrectLine.substring("Incorrect Attempts:".length()).trim());

            return new Score(dateTime, games, first, second, incorrect);
        } catch (Exception e)
        {
            return null;
        }
    }

    public static void checkHighScore(Score currentScore) {
        try {
            List<Score> scores = readScoresFromFile("score.txt");
            Score highestScore = scores.stream()
                    .max(Comparator.comparingDouble(Score::getAverageScorePerGame))
                    .orElse(null);

            double currentAvg = currentScore.getAverageScorePerGame();
            if (highestScore == null) {
                System.out.printf("CONGRATULATIONS! You are the new high score with an average of %.2f points per game; no previous record exists.%n", currentAvg);
            } else {
                double highestAvg = highestScore.getAverageScorePerGame();
                String highScoreDateTime = highestScore.dateTimePlayed.toString();
                if (currentAvg > highestAvg) {
                    System.out.printf("CONGRATULATIONS! You are the new high score with an average of %.2f points per game; the previous record was %.2f points per game on %s.%n",
                            currentAvg, highestAvg, highScoreDateTime);
                } else {
                    System.out.printf("You did not beat the high score of %.2f points per game from %s.%n",
                            highestAvg, highScoreDateTime);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading score file: " + e.getMessage());
        }
    }
}
