package ca.bcit.comp25222.termproject.NumberGame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The {@code NumberGameMainMenu} class serves as the main JavaFX window for the
 * "Retro 20-Number Challenge" game. It extends the {@link javafx.application.Application}
 * class and provides the entry point for initializing and displaying the game's user interface.
 * <p>
 * This class creates a new {@link RetroNumberGame} instance which encapsulates the game logic,
 * and sets up the UI components including:
 * <ul>
 *   <li>A game pane that displays the game board</li>
 *   <li>An overlay pane containing the game title and control buttons ("Start Game" and "Exit")</li>
 * </ul>
 * The UI is organized in a {@link javafx.scene.Scene} with an external stylesheet applied if available.
 * </p>
 * <p>
 * The {@link #start(Stage)} method initializes the primary stage by creating and combining these
 * UI elements, and then displays the stage. The private {@link #buildOverlayPane(RetroNumberGame, Stage)}
 * method constructs the overlay pane and wires up the event handlers for starting a new game or closing
 * the current window.
 * </p>
 * <p>
 * The class is designed to be launched within a JavaFX runtime environment. It can be invoked via
 * {@link javafx.application.Application#launch(String...)} or by using helper methods (such as a static
 * {@code launchGame()} method) that schedule the creation of a new game window on the JavaFX Application Thread.
 * </p>
 *
 * @author Braeden Duval
 */
public class NumberGameMainMenu extends Application
{

    /**
     * Initializes and displays the primary JavaFX stage for the Retro 20-Number Challenge game.
     * <p>
     * This method creates a new {@link RetroNumberGame} instance and sets up its user interface by:
     * <ul>
     *   <li>Creating a game pane containing the game board.</li>
     *   <li>Building an overlay pane with the game title and control buttons.</li>
     *   <li>Combining these panes into a scene and applying the CSS stylesheet if available.</li>
     * </ul>
     * The primary stage's title is set and the stage is then displayed.
     * </p>
     *
     * @param primaryStage the primary stage on which the game UI will be displayed
     */
    @Override
    public void start(final Stage primaryStage)
    {
        final RetroNumberGame game = new RetroNumberGame();
        final StackPane gamePane = new StackPane(game.getRootPane());
        final VBox overlayPane = buildOverlayPane(game, primaryStage);
        final StackPane root = new StackPane();
        root.getChildren().addAll(gamePane, overlayPane);

        final Scene scene = new Scene(root, 700, 500);
        try {
            scene.getStylesheets().add(getClass().getResource("/NumberGame.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Error: NumberGame.css not found.");
        }

        primaryStage.setTitle("Retro 20-Number Challenge");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Builds and returns the overlay pane containing the game title and control buttons.
     * <p>
     * The overlay pane is composed of:
     * <ul>
     *   <li>A title label displaying "Retro 20-Number Challenge".</li>
     *   <li>A "Start Game" button that hides the overlay and initiates a new game by calling {@code game.startNewGame()}.</li>
     *   <li>An "Exit" button that closes the primary stage, effectively closing the current game window.</li>
     * </ul>
     * </p>
     *
     * @param game the {@link RetroNumberGame} instance controlling the game logic
     * @param primaryStage the stage to be closed when the exit button is pressed
     * @return a {@link VBox} containing the overlay UI elements
     */
    private VBox buildOverlayPane(final RetroNumberGame game, final Stage primaryStage)
    {
        final VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.getStyleClass().add("overlay");

        final Label titleLabel = new Label("Retro 20-Number Challenge");
        titleLabel.getStyleClass().add("overlay-title");

        final Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("overlay-button");
        startButton.setOnAction(e -> {
            overlay.setVisible(false);
            game.startNewGame();
        });

        final Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("overlay-button");
        exitButton.setOnAction(e -> primaryStage.close());

        overlay.getChildren().addAll(titleLabel, startButton, exitButton);
        return overlay;
    }

    /**
     * Schedules the creation and display of a new Number Game window on the JavaFX Application Thread.
     * <p>
     * This method uses {@code Platform.runLater} to ensure that the creation of a new {@link NumberGameMainMenu}
     * instance and the initialization of its UI occur on the JavaFX Application Thread. It creates a new {@link Stage}
     * and passes it to the {@link NumberGameMainMenu#start(Stage)} method to display the game window.
     * </p>
     * <p>
     * Any exceptions encountered during the initialization of the game window are caught and printed to the standard error stream.
     * </p>
     */
    public static void launchGame()
    {
        Platform.runLater(() -> {
            try {
                new NumberGameMainMenu().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
