package ca.bcit.comp2522.termproject.wordgame;

import java.io.*;
import java.util.*;

/**
 * Represents a quiz-based word game that challenges users on their knowledge of countries, capital cities,
 * and country-related facts.
 * <p>
 * The game operates by posing a fixed number of questions per round (defined by {@code NUM_QUESTIONS_PER_GAME}).
 * Each question is randomly determined to be one of several types:
 * <ul>
 *   <li>Identifying the country given its capital city.</li>
 *   <li>Identifying the capital city given its country.</li>
 *   <li>Associating a fact with its correct country.</li>
 * </ul>
 * For each question, the user is given up to two attempts. First-attempt correct answers
 * contribute more toward the score, while a correct answer on the second attempt still earns a lower credit.
 * If both attempts are incorrect, the correct answer is shown.
 * </p>
 * <p>
 * After each round, the round results are displayed (number of first-attempt correct answers,
 * second-attempt correct answers, and incorrect answers). The user is then prompted via the console
 * to choose whether to play again. At the end of the session, overall statistics are compiled into
 * a {@link Score} object. This final score is compared to previous high scores stored in a file
 * (using methods from the {@link Score} class), and then it is appended to the score file.
 * </p>
 * <p>
 * The class utilizes file I/O to persist game scores, uses a {@link Scanner} for console input, and interacts with
 * the {@link Country} class for country data and the {@link Score} class for score management.
 * </p>
 *
 * @author Braeden
 * @version 1.0
 */

public final class WordGame
{
    private static final int NUM_QUESTIONS_PER_GAME     = 10;
    private static final int FIRST_QUESTION_TYPE        = 0;
    private static final int SECOND_QUESTION_TYPE       = 1;
    private static final int THIRD_QUESTION_TYPE        = 2;

    private static final String correctText             = "Correct!\n";
    private static final String attemptTwoCorrectText   = "Correct on second attempt.\n";

    private final Scanner           scan;
    private final Random            rand;
    private Map<String, Country>    countryMap;
    private List<String>            countryKeys;

    private int totalGamesPlayed;
    private int totalCorrectFirstAttempt;
    private int totalCorrectSecondAttempt;
    private int totalIncorrect;

    /**
     * Constructs a new {@code WordGame} instance and initializes all game state and required resources.
     * <p>
     * This constructor performs the following:
     * <ul>
     *   <li>Initializes all score counters to zero.</li>
     *   <li>Creates a {@link Scanner} to capture console input and a {@link Random}
     *   instance for randomizing questions.</li>
     *   <li>Attempts to load country data by calling {@link World#buildCountries()}.
     *   If successful, the countries are stored in a map, and the country names (keys)
     *   are extracted into a list for random selection.</li>
     * </ul>
     * If the country data file is not found, a message ("File not found") is printed to the console.
     * </p>
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
        }
        catch (final FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Continuously prompts the user to decide whether to play another round until a valid response is entered.
     *
     * The method prints a prompt "Would you like to play again? (Yes/No)" and reads the user's input via the console.
     * If the input (ignoring case) is "Yes", the method returns true.
     * If the input is "No", it returns false. For any other input, an error message is
     * displayed and the prompt is repeated.
     *
     *
     * @return true if the user chooses to play again; false otherwise.
     */
    private boolean askPlayAgain()
    {
        while (true)
        {
            System.out.println("Would you like to play again? (Yes/No)");

            final String input;
            input = scan.nextLine().trim();

            if (input.equalsIgnoreCase("Yes"))
            {
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
     * Starts and manages the word game session.
     * <p>
     * This method carries out the following steps:
     * <ol>
     *   <li>Determines the high score file to use by retrieving the system property {@code score.file}
     *   (defaults to "score.txt").
     *       It then checks if the file exists; if not, it attempts to create it and reports the outcome.</li>
     *   <li>Enters a loop to conduct game rounds. In each round:
     *       <ul>
     *         <li>The total games played counter is incremented.</li>
     *         <li>A fixed number of questions (defined by {@code NUM_QUESTIONS_PER_GAME}) is asked.</li>
     *         <li>For each question, a random question type is determined:
     *             <ul>
     *               <li>If the question type is {@code FIRST_QUESTION_TYPE},
     *               the user is asked to identify the country given its capital.</li>
     *               <li>If the type is {@code SECOND_QUESTION_TYPE},
     *               the user is asked to provide the capital of a given country.</li>
     *               <li>If the type is {@code THIRD_QUESTION_TYPE},
     *               the user is asked to associate a fact (randomly selected from the country's facts)
     *               with the correct country. If no facts are available,
     *               it falls back to asking as in the first type.</li>
     *             </ul>
     *         </li>
     *         <li>The user's answer is evaluated. If correct on the first try, the corresponding counter is incremented.
     *             If incorrect, the user is given a second attempt.
     *             A correct answer on the second attempt increments a different counter;
     *             otherwise, the incorrect counter is incremented and the correct answer displayed.</li>
     *       </ul>
     *   </li>
     *   <li>After processing all questions in the round, the round’s results are printed to the console.</li>
     *   <li>The user is then asked (via {@link #askPlayAgain()}) if they wish to play another round.</li>
     *   <li>Once the user opts out, a {@link Score} object is created using the cumulative statistics,
     *       which is then compared against previous high scores by invoking
     *       {@link Score#checkHighScore(Score, String)}.</li>
     *   <li>Finally, the current score is appended to the score file by calling
     *       {@link Score#appendScoreToFile(Score, String)}.
     *       Any I/O errors encountered during this process are caught and reported.</li>
     * </ol>
     * </p>
     */
    public void playWordGame()
    {

        final String scoreFile;
        scoreFile = System.getProperty("score.file", "score.txt");

        try
        {
            final File file;
            file = new File(scoreFile);

            if (!file.exists() &&
                !file.createNewFile())
            {
                throw new IOException("Failed to create file: " + scoreFile);
            }
        }
        catch (final IOException e)
        {
            throw new IllegalArgumentException(
                    "Error retrieving score file: " + e.getMessage());
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
                }
                else
                {
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
                    (questionType == SECOND_QUESTION_TYPE &&
                                    userAnswer.equalsIgnoreCase(country.getCapitalCityName()))
                    {
                        System.out.println(attemptTwoCorrectText);
                        correctSecondAttempt++;

                    }
                    else
                    {
                        if (questionType == SECOND_QUESTION_TYPE)
                        {
                            System.out.printf("Incorrect again. The correct answer was '%s'.%n\n",
                                              country.getCapitalCityName());
                        }
                        else
                        {
                            System.out.printf("Incorrect again. The correct answer was '%s'.%n\n",
                                              country.getCountryName());
                        }
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
        }
        catch (final IOException e)
        {
            throw new IllegalArgumentException(
                    "Error saving score: " + e.getMessage());
        }
    }
}
