package ca.bcit.comp2522.termproject.numbergame;

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
 * This class creates a new {@link NumberGame} instance which encapsulates the game logic,
 * and sets up the UI components including:
 * <ul>
 *   <li>A game pane that displays the game board</li>
 *   <li>An overlay pane containing the game title and control buttons ("Start Game" and "Exit")</li>
 * </ul>
 * The UI is organized in a {@link javafx.scene.Scene} with an external stylesheet applied if available.
 * </p>
 * <p>
 * The {@link #start(Stage)} method initializes the primary stage by creating and combining these
 * UI elements, and then displays the stage. The private {@link #buildOverlayPane(NumberGame, Stage)}
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
 * @version 1.0
 */

public final class NumberGameMainMenu
             extends Application
{
    private static final String cssFile             = "/NumberGame.css";
    private static final String buttonOverlayCSS    = "overlay-button";
    private static final String gameTitle           = "Retro 20-Number Challenge";
    private static final String exitButtonText      = "Exit";
    private static final String startButtonText     = "Start Game";

    private static final int    vboxAxis            = 20;
    private static final int    sceneHeight         = 500;
    private static final int    sceneWidth          = 700;

    /*
     * Constructs and returns an overlay pane that contains the game title and the control buttons.
     *
     * The overlay pane is intended to be displayed on top of the game pane and includes:
     *
     *   A title label displaying the name of the game (i.e. gameTitle).
     *   A "Start Game" button that, when pressed, hides the overlay and calls NumberGame#startNewGame()
     *   to initialize a new game session.
     *   An "Exit" button that closes the main application window via the provided primaryStage.
     *
     * The pane is organized using a vertical box layout VBox with spacing set to
     * the value vboxAxis and is centered.
     * </p>
     *
     * @param game         an instance of NumberGame that provides the game logic and UI components.
     * @param primaryStage the main application Stage that should be closed when the "Exit" button is pressed.
     *
     * @return a VBox containing the overlay components (title and control buttons).
     *
     */
    private VBox buildOverlayPane(final NumberGame game,
                                  final Stage primaryStage)
    {
        final VBox      overlay;
        final Label     titleLabel;
        final Button    startButton;
        final Button    exitButton;

        overlay = new VBox(vboxAxis);
        overlay.setAlignment(Pos.CENTER);
        overlay.getStyleClass().add("overlay");

        titleLabel = new Label(gameTitle);
        titleLabel.getStyleClass().add("overlay-title");

        startButton = new Button(startButtonText);
        startButton.getStyleClass().add(buttonOverlayCSS);
        startButton.setOnAction(e -> {
            overlay.setVisible(false);
            game.startNewGame();
        });

        exitButton = new Button(exitButtonText);
        exitButton.getStyleClass().add(buttonOverlayCSS);
        exitButton.setOnAction(e -> primaryStage.close());

        overlay.getChildren().addAll(titleLabel, startButton, exitButton);
        return overlay;
    }

    /**
     * Schedules the creation and display of a new game window on the JavaFX Application Thread.
     * <p>
     * This static method uses {@link Platform#runLater(Runnable)} to ensure that all UI-related operations occur
     * on the JavaFX Application Thread. Inside the runnable:
     * <ul>
     *   <li>A new {@link Stage} is created for the game window.</li>
     *   <li>An instance of {@code NumberGameMainMenu} is created and its {@link #start(Stage)} method is called with
     *       the new stage to initialize the game UI.</li>
     *   <li>The stage is made visible, brought to the front, and given focus.</li>
     * </ul>
     * Any exceptions thrown during this process are caught, logged, and printed to the standard error stream.
     * </p>
     */
    public static void launchGame()
    {
        Platform.runLater(() -> {
            try
            {
                final Stage stage;
                final Application newGame;

                stage   = new Stage();
                newGame = new NumberGameMainMenu();

                newGame.start(stage);
                stage.show();
                stage.toFront();
                stage.requestFocus();
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Initializes and displays the primary JavaFX stage for the Retro 20-Number Challenge game.
     * <p>
     * This method performs the following tasks:
     * <ul>
     *   <li>Creates an instance of {@link NumberGame} which encapsulates the game board and logic.</li>
     *   <li>Wraps the game UI in a {@link StackPane} to allow layering of UI components.</li>
     *   <li>Builds an overlay pane (using {@link #buildOverlayPane(NumberGame, Stage)}) that contains the game title
     *       and control buttons.</li>
     *   <li>Creates a root {@link StackPane} and adds both the game pane and the overlay pane.
     *   The overlay pane is placed on top so that it can intercept user input until a new game is started.</li>
     *   <li>Sets up a {@link Scene} with dimensions defined by the constants {@code sceneWidth}
     *       and {@code sceneHeight}, and attempts to load an external CSS file
     *       (referenced by {@code cssFile}) for styling.</li>
     *   <li>Sets the title of the primary stage to the game title and assigns the scene
     *       to the stage before showing it.</li>
     * </ul>
     * </p>
     *
     * @param primaryStage the primary {@link Stage} that serves as the main window for the application.
     */
    @Override
    public void start(final Stage primaryStage)
    {
        final NumberGame game;
        final StackPane         gamePane;
        final VBox              overlayPane;
        final StackPane         root;
        final Scene             scene;

        game        = new NumberGame();
        gamePane    = new StackPane(game.getRootPane());
        overlayPane = buildOverlayPane(game, primaryStage);
        root        = new StackPane();
        scene       = new Scene(root, sceneWidth, sceneHeight);

        root.getChildren().addAll(gamePane, overlayPane);

        try
        {
            scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        }
        catch (final NullPointerException e)
        {
            throw new IllegalArgumentException(
                    "Error: %s not found." + cssFile);
        }

        primaryStage.setTitle(gameTitle);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
