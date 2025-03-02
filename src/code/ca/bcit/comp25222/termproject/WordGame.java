package ca.bcit.comp25222.termproject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WordGame {
    private static final int NUM_QUESTIONS_PER_GAME = 10;
    private static int totalGamesPlayed = 0;
    private static int totalCorrectFirstAttempt = 0;
    private static int totalCorrectSecondAttempt = 0;
    private static int totalIncorrect = 0;

    private static boolean playAgain(final Scanner scan) {
        while (true) {
            System.out.println("Would you like to play again? (Yes/No)");

            final String input;
            input = scan.nextLine().trim();

            if (input.equalsIgnoreCase("Yes")) {
                return true;
            } else if (input.equalsIgnoreCase("No")) {
                return false;
            } else {
                System.out.println("Invalid input. Please type 'Yes' or 'No'");
            }
        }
    }

    public static void main(final String[] args)
    {
        final Scanner scan;
        scan = new Scanner(System.in);

        final Map<String, Country> countryMap;

        try
        {
            countryMap = World.buildCountries();
        } catch (FileNotFoundException e)
        {
            System.out.println("File not found");
            return;
        }

        final List<String> countryKeys;
        countryKeys = new ArrayList<>(countryMap.keySet());

        Random rand;
        rand = new Random();

        boolean playAgain = true;

        while (playAgain)
        {
            totalGamesPlayed++;

            int correctFirstAttempt = 0;
            int correctSecondAttempt = 0;
            int incorrect = 0;
            System.out.println("Starting a new game!\n");

            for (int i = 1; i <= NUM_QUESTIONS_PER_GAME; i++) {
                final int questionType;
                questionType = rand.nextInt(3);

                final String randomCountry;
                randomCountry = countryKeys.get(rand.nextInt(countryKeys.size()));

                final Country country;
                country = countryMap.get(randomCountry);

                boolean answerCorrect = false;
                String userAnswer = "";

                if (questionType == 0) {
                    System.out.printf("Question %d: What country has the capital city %s?%n",
                            i, country.getCapitalCityName());

                    userAnswer = scan.nextLine();

                    if (userAnswer.equalsIgnoreCase(country.getCountryName())) {
                        System.out.println("Correct!");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                } else if (questionType == 1) {
                    System.out.printf("Question %d: What is the capital city of %s?%n",
                            i, country.getCountryName());

                    userAnswer = scan.nextLine();

                    if (userAnswer.equalsIgnoreCase(country.getCapitalCityName())) {
                        System.out.println("Correct!");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                } else {
                    String[] facts;
                    facts = country.getFacts();
                    if (facts.length == 0) {
                        System.out.printf("Question %d: What country has the capital city %s?%n",
                                i, country.getCapitalCityName());

                    } else {
                        String fact;
                        fact = facts[rand.nextInt(facts.length)];
                        System.out.printf("Question %d: Which country best describes the following fact:%n\"%s\"%n",
                                i, fact);

                    }

                    userAnswer = scan.nextLine();
                    if (userAnswer.equalsIgnoreCase(country.getCountryName())) {
                        System.out.println("Correct!");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                }

                if (!answerCorrect) {
                    System.out.println("Incorrect! Please try again:");
                    userAnswer = scan.nextLine();

                    if ((questionType == 0 || questionType == 2) &&
                            userAnswer.equalsIgnoreCase(country.getCountryName())) {
                        System.out.println("Correct on second attempt");
                        correctSecondAttempt++;

                    } else if (questionType == 1 &&
                            userAnswer.equalsIgnoreCase(country.getCapitalCityName())) {
                        System.out.println("Correct on second attempt");
                        correctSecondAttempt++;

                    } else {
                        System.out.printf("Incorrect again. The correct answer was '%s'.%n",
                                (questionType == 1 ? country.getCapitalCityName() : country.getCountryName()));
                        incorrect++;
                    }
                }
            }

            totalCorrectFirstAttempt += correctFirstAttempt;
            totalCorrectSecondAttempt += correctSecondAttempt;
            totalIncorrect += incorrect;

            System.out.println("\nRound results: ");
            System.out.printf("- %d correct answers on the first attempt%n", correctFirstAttempt);
            System.out.printf("- %d correct answers on the second attempt%n", correctSecondAttempt);
            System.out.printf("- %d incorrect answers on two attempts each%n", incorrect);

            playAgain = playAgain(scan);
        }

        Score currentScore = new Score(
                totalGamesPlayed,
                totalCorrectFirstAttempt,
                totalCorrectSecondAttempt,
                totalIncorrect);

        try
        {
            Score.appendScoreToFile(currentScore, "score.txt");
        } catch (IOException e)
        {
            System.out.println("Error saving score: " + e.getMessage());
        }
        Score.checkHighScore(currentScore);

        scan.close();
    }

    public static int getTotalIncorrect()
    {
        return totalIncorrect;
    }

    public static int getTotalCorrectSecondAttempt()
    {
        return totalCorrectSecondAttempt;
    }

    public static int getTotalCorrectFirstAttempt()
    {
        return totalCorrectFirstAttempt;
    }

    public static int getTotalGamesPlayed()
    {
        return totalGamesPlayed;
    }
}