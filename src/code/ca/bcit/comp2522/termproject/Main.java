package ca.bcit.comp2522.termproject;

import ca.bcit.comp2522.termproject.resourcerouter.ResourceRouterMainMenu;
import ca.bcit.comp2522.termproject.numbergame.NumberGameMainMenu;
import ca.bcit.comp2522.termproject.wordgame.WordGame;
import javafx.application.Platform;
import java.util.Scanner;

/**
 * The Main class serves as the entry point for the Play_Java application.
 * It provides a simple console-based interface that allows the user to select and launch one of several games.
 *
 * <p>
 * Available games include:
 * <ul>
 *   <li>The Word Game, which is handled by {@link WordGame}</li>
 *   <li>The Number Game, which is launched by {@link NumberGameMainMenu}</li>
 *   <li>The Resource Router Game, which is launched by {@link ResourceRouterMainMenu}</li>
 * </ul>
 * </p>
 *
 * <p>
 * The application utilizes the JavaFX platform.
 * The toolkit is initialized in headless mode to support potential GUI elements,
 * even though the main interface is text-based.
 * </p>
 *
 * <p>
 * To exit the application, the user should input "Q".
 * </p>
 */
public class Main
{
    /**
     * The main method initializes the JavaFX platform, sets up the console-based game selection menu,
     * and processes user input to launch the corresponding game.
     * <p>
     * The program continuously prompts the user to select a game until "Q" is entered to quit.
     * If an invalid option is provided, the user is notified and prompted again.
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(final String[] args)
    {
        Platform.startup(() -> {});
        Platform.setImplicitExit(false);

        final Scanner scanner;
        scanner = new Scanner(System.in);

        System.out.println("Welcome to Play_Java!");

        while (true)
        {
            System.out.println("Select a game:");
            System.out.println("Press W to play the Word game.");
            System.out.println("Press N to play the Number game.");
            System.out.println("Press M to play the Resource Router game.");
            System.out.println("Press Q to quit.");

            final String input;
            input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Q"))
            {
                System.out.println("Goodbye!");
                break;
            }
            else if (input.equalsIgnoreCase("W"))
            {
                WordGame wordGame = new WordGame();
                wordGame.playWordGame();
            }
            else if (input.equalsIgnoreCase("N"))
            {
                NumberGameMainMenu.launchGame();
            }
            else if (input.equalsIgnoreCase("M"))
            {
                ResourceRouterMainMenu.launchGame();
            }
            else
            {
                System.out.println("Invalid input. Please enter W, N, M, or Q.");
            }
        }

        scanner.close();
        Platform.exit();
    }
}

