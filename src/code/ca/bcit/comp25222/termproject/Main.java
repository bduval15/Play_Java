package ca.bcit.comp25222.termproject;

import ca.bcit.comp25222.termproject.resourcerouter.ResourceRouterMainMenu;
import ca.bcit.comp25222.termproject.numbergame.NumberGameMainMenu;
import ca.bcit.comp25222.termproject.wordgame.WordGame;
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
        int choice;

        do {
            System.out.println("Select a game:");
            System.out.println("1. Word Game");
            System.out.println("2. Retro 20-Number Challenge");
            System.out.println("3. Resource Router");
            System.out.println("4. Quit");

            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    WordGame wordGame = new WordGame();
                    wordGame.playWordGame();
                    break;
                case 2:
                    NumberGameMainMenu.launchGame();
                    break;
                case 3:
                    ResourceRouterMainMenu.launchGame();
                    break;
                case 4:
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Try again.");
                    break;
            }
        } while (choice != 5);

        scanner.close();
        Platform.exit();
    }
}
