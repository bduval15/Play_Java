package ca.bcit.comp25222.termproject;

import ca.bcit.comp25222.termproject.NumberGame.NumberGameMainMenu;
import ca.bcit.comp25222.termproject.WordGame.WordGame;
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
            System.out.println("3. Quit");

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
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                    break;
            }
        } while (choice != 3);

        scanner.close();
        Platform.exit();
    }
}
