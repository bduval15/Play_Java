package ca.bcit.comp25222.termproject.wordgame;

import java.io.*;
import java.util.*;

/**
 * Represents a quiz-based word game that challenges users on their knowledge of countries, capital cities,
 * and country-related facts.
 * <p>
 * The game operates in rounds with a fixed number of questions per game. Each question can ask the user to
 * identify a country based on its capital city, to provide the capital city for a given country, or to associate
 * a fact with the correct country. User responses are evaluated for correctness on both the first and a potential
 * second attempt. The game tracks the number of games played, correct answers on first attempts, correct answers on
 * second attempts, and incorrect answers.
 * </p>
 * <p>
 * After a game round, the results are displayed and the user is prompted to decide whether to play another round.
 * At the end of the session, a final score is calculated from the accumulated statistics. This score is compared
 * against previous high scores stored in a file, and a message displays when a new high score is achieved.
 * Additionally, the session’s score record appends to the high score file for future reference.
 * </p>
 * <p>
 * The class makes use of file I/O operations to persist game scores and utilizes a {@link Scanner} to capture user input
 * from the console. It interacts with other components such as the {@link Country} class for country data and the {@link Score}
 * class for score calculation and management.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class WordGame
{
    private static final int NUM_QUESTIONS_PER_GAME     = 10;
    private static final int FIRST_QUESTION_TYPE        = 0;
    private static final int SECOND_QUESTION_TYPE       = 1;
    private static final int THIRD_QUESTION_TYPE        = 2;

    private static final String correctText             = "Correct!\n";
    private static final String attemptTwoCorrectText   = "Correct on second attempt\n";

    private final Scanner           scan;
    private final Random            rand;
    private Map<String, Country>    countryMap;
    private List<String>            countryKeys;

    private int totalGamesPlayed;
    private int totalCorrectFirstAttempt;
    private int totalCorrectSecondAttempt;
    private int totalIncorrect;

    /**
     * Constructor to initialize the game state and resources
     */
    public WordGame()
    {
        totalGamesPlayed            = 0;
        totalCorrectFirstAttempt    = 0;
        totalCorrectSecondAttempt   = 0;
        totalIncorrect              = 0;

        final Scanner scan;
        final Random random;

        scan    = new Scanner(System.in);
        random  = new Random();

        this.scan = scan;
        this.rand = random;

        try
        {
            this.countryMap     = World.buildCountries();
            this.countryKeys    = new ArrayList<>(countryMap.keySet());
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

        try {
            File file = new File(scoreFile);
            if (!file.exists()) {
                if (file.createNewFile())
                {
                    System.out.println("File '" + scoreFile + "' created successfully.");
                } else {
                    System.out.println("Failed to create file '" + scoreFile + "'.");
                }
            }
        } catch (final IOException e) {
            System.out.println("Error retrieving score file: " + e.getMessage());
            e.printStackTrace();
        }

        boolean playAgain = true;

        while (playAgain)
        {
            totalGamesPlayed++;

            int correctFirstAttempt     = 0;
            int correctSecondAttempt    = 0;
            int incorrect               = 0;
            System.out.println("Starting a new game!\n");

            for (int i = 1; i <= NUM_QUESTIONS_PER_GAME; i++)
            {
                final int       questionType;
                final String    randomCountry;
                final Country   country;

                questionType    = rand.nextInt(3);
                randomCountry   = countryKeys.get(rand.nextInt(countryKeys.size()));
                country         = countryMap.get(randomCountry);

                boolean answerCorrect = false;
                String userAnswer;

                if (questionType == FIRST_QUESTION_TYPE)
                {
                    System.out.printf("Question %d: What country has the capital city %s?%n",
                            i, country.getCapitalCityName());

                    userAnswer = scan.nextLine();

                    if (userAnswer.equalsIgnoreCase(country.getCountryName()))
                    {
                        System.out.println(correctText);
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
                        System.out.println(correctText);
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
                        System.out.println(correctText);
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
                        System.out.println(attemptTwoCorrectText);
                        correctSecondAttempt++;

                    }
                    else if
                    (questionType == FIRST_QUESTION_TYPE &&
                                    userAnswer.equalsIgnoreCase(country.getCapitalCityName()))
                    {
                        System.out.println(attemptTwoCorrectText);
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

            totalCorrectFirstAttempt    += correctFirstAttempt;
            totalCorrectSecondAttempt   += correctSecondAttempt;
            totalIncorrect              += incorrect;

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

        Score.checkHighScore(currentScore, scoreFile);

        try
        {
            Score.appendScoreToFile(currentScore, scoreFile);
        } catch (final IOException e)
        {
            System.out.println("Error saving score: " + e.getMessage());
        }
    }
}
