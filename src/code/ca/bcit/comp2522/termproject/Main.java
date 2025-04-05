package ca.bcit.comp2522.termproject;

import ca.bcit.comp2522.termproject.resourcerouter.ResourceRouterMainMenu;
import ca.bcit.comp2522.termproject.numbergame.NumberGameMainMenu;
import ca.bcit.comp2522.termproject.wordgame.WordGame;
import javafx.application.Platform;
import java.util.Scanner;

public class Main
{
    public static void main(final String[] args)
    {
        Platform.startup(() -> {});
        Platform.setImplicitExit(false);

        final Scanner scanner;
        scanner = new Scanner(System.in);

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

