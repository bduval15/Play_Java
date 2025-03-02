package ca.bcit.comp25222.termproject;
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
class WordGame
{
    private static final int NUM_QUESTIONS_PER_GAME = 10;
    private static int totalGamesPlayed = 0;
    private static int totalCorrectFirstAttempt = 0;
    private static int totalCorrectSecondAttempt = 0;
    private static int totalIncorrect = 0;

    /*
     * Prompts the user to play again and validates their response.
     *
     * @param scan The scanner object to read user input.
     * @return {true} if the user wants to play again, {false} otherwise.
     */
    private static boolean playAgain(final Scanner scan)
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
     * The main method that starts the game.
     * It initializes the game, loads country data, handles game rounds,
     * tracks user scores, and checks for high scores.
     *
     * @param args Command-line arguments (not used).
     */
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

        final Random rand;
        rand = new Random();

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

                if (questionType == 0)
                {
                    System.out.printf("Question %d: What country has the capital city %s?%n",
                            i, country.getCapitalCityName());

                    userAnswer = scan.nextLine();

                    if (userAnswer.equalsIgnoreCase(country.getCountryName()))
                    {
                        System.out.println("Correct!");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                }
                else if (questionType == 1)
                {
                    System.out.printf("Question %d: What is the capital city of %s?%n",
                            i, country.getCountryName());

                    userAnswer = scan.nextLine();

                    if (userAnswer.equalsIgnoreCase(country.getCapitalCityName()))
                    {
                        System.out.println("Correct!");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                } else {

                    final String[] facts;
                    facts = country.getFacts();

                    if (facts.length == 0)
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
                        System.out.println("Correct!");
                        correctFirstAttempt++;
                        answerCorrect = true;
                    }
                }

                if (!answerCorrect)
                {
                    System.out.println("Incorrect! Please try again:");
                    userAnswer = scan.nextLine();

                    if ((questionType == 0 || questionType == 2) &&
                            userAnswer.equalsIgnoreCase(country.getCountryName()))
                    {
                        System.out.println("Correct on second attempt");
                        correctSecondAttempt++;

                    }
                    else if
                    (questionType == 1 && userAnswer.equalsIgnoreCase(country.getCapitalCityName()))
                    {
                        System.out.println("Correct on second attempt");
                        correctSecondAttempt++;

                    }
                    else
                    {
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

        final Score currentScore;
        currentScore = new Score(
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
}