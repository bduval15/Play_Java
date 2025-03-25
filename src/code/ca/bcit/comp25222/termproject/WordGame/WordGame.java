package ca.bcit.comp25222.termproject.WordGame;

import java.io.*;
import java.util.*;

/**
 * <p>A quiz-based word game that tests users on country names, capital cities,
 * and country-related facts. The game tracks scores based on correct answers
 * given on the first and second attempts and records statistics in a file.
 * </p>
 * Users can choose to play multiple rounds, and the game checks for high scores
 * across sessions.
 *
 * @author Braeden Duval
 */
public class WordGame
{
    private static final int NUM_QUESTIONS_PER_GAME = 10;
    private static final int FIRST_QUESTION_TYPE = 0;
    private static final int SECOND_QUESTION_TYPE = 1;
    private static final int THIRD_QUESTION_TYPE = 2;

    private final Scanner scan;
    private final Random rand;
    private Map<String, Country> countryMap;
    private List<String> countryKeys;

    private int totalGamesPlayed;
    private int totalCorrectFirstAttempt;
    private int totalCorrectSecondAttempt;
    private int totalIncorrect;

    /**
     * Constructor to initialize the game state and resources
     */
    public WordGame()
    {
        totalGamesPlayed = 0;
        totalCorrectFirstAttempt = 0;
        totalCorrectSecondAttempt = 0;
        totalIncorrect = 0;
        this.scan = new Scanner(System.in);
        this.rand = new Random();

        try
        {
            this.countryMap = World.buildCountries();
            this.countryKeys = new ArrayList<>(countryMap.keySet());
        } catch (FileNotFoundException e)
        {
            System.out.println("File not found");
        }
    }

    /*
     * Prompts the user to play again and validates their response.
     *
     * @param scan The scanner object to read user input.
     * @return {true} if the user wants to play again, {false} otherwise.
     */
    private boolean askPlayAgain()
    {
        while (true)
        {
            System.out.println("Would you like to play again? (Yes/No)");

            final String input;
            input = scan.nextLine().trim();

            if (input.equalsIgnoreCase("Yes")) {
                return true;
            }
            else if (input.equalsIgnoreCase("No"))
            {
                return false;
            }
            else
            {
                System.out.println("Invalid input. Please type 'Yes' or 'No'");
            }
        }
    }

    /**
     * The method that starts the game.
     * It initializes the game, loads country data, handles game rounds,
     * tracks user scores, and checks for high scores.
     *
     */
    public void playWordGame()
    {

        final String scoreFile;
        scoreFile = System.getProperty("score.file", "score.txt");

        boolean playAgain = true;

        while (playAgain)
        {
            totalGamesPlayed++;

            int correctFirstAttempt = 0;
            int correctSecondAttempt = 0;
            int incorrect = 0;
            System.out.println("Starting a new game!\n");

            for (int i = 1; i <= NUM_QUESTIONS_PER_GAME; i++)
            {
                final int questionType;
                questionType = rand.nextInt(3);

                final String randomCountry;
                randomCountry = countryKeys.get(rand.nextInt(countryKeys.size()));

                final Country country;
                country = countryMap.get(randomCountry);

                boolean answerCorrect = false;
                String userAnswer = "";

                if (questionType == FIRST_QUESTION_TYPE)
                {
                    System.out.printf("Question %d: What country has the capital city %s?%n",
                            i, country.getCapitalCityName());

                    userAnswer = scan.nextLine();

                    if (userAnswer.equalsIgnoreCase(country.getCountryName()))
                    {
                        System.out.println("Correct!\n");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                }
                else if (questionType == SECOND_QUESTION_TYPE)
                {
                    System.out.printf("Question %d: What is the capital city of %s?%n",
                            i, country.getCountryName());

                    userAnswer = scan.nextLine();

                    if (userAnswer.equalsIgnoreCase(country.getCapitalCityName()))
                    {
                        System.out.println("Correct!\n");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                } else {

                    final String[] facts;
                    facts = country.getFacts();

                    if (facts.length == FIRST_QUESTION_TYPE)
                    {
                        System.out.printf("Question %d: What country has the capital city %s?%n",
                                i, country.getCapitalCityName());

                    }
                    else
                    {
                        final String fact;
                        fact = facts[rand.nextInt(facts.length)];
                        System.out.printf("Question %d: Which country best describes the following fact:%n\"%s\"%n",
                                i, fact);

                    }

                    userAnswer = scan.nextLine();

                    if (userAnswer.equalsIgnoreCase(country.getCountryName()))
                    {
                        System.out.println("Correct!\n");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                }

                if (!answerCorrect)
                {
                    System.out.println("Incorrect! Please try again:");
                    userAnswer = scan.nextLine();

                    if ((questionType == FIRST_QUESTION_TYPE || questionType == THIRD_QUESTION_TYPE) &&
                            userAnswer.equalsIgnoreCase(country.getCountryName()))
                    {
                        System.out.println("Correct on second attempt\n");
                        correctSecondAttempt++;

                    }
                    else if
                    (questionType == FIRST_QUESTION_TYPE &&
                                    userAnswer.equalsIgnoreCase(country.getCapitalCityName()))
                    {
                        System.out.println("Correct on second attempt\n");
                        correctSecondAttempt++;

                    }
                    else
                    {
                        System.out.printf("Incorrect again. The correct answer was '%s'.%n\n",
                                (questionType == SECOND_QUESTION_TYPE
                                        ? country.getCapitalCityName() : country.getCountryName()));
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

            playAgain = askPlayAgain();
        }

        final Score currentScore;
        currentScore = new Score(
                totalGamesPlayed,
                totalCorrectFirstAttempt,
                totalCorrectSecondAttempt,
                totalIncorrect);

        try
        {
            Score.appendScoreToFile(currentScore, scoreFile);
        } catch (IOException e)
        {
            System.out.println("Error saving score: " + e.getMessage());
        }
        Score.checkHighScore(currentScore, scoreFile);
    }
}