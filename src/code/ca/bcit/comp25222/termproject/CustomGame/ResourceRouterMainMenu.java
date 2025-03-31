package ca.bcit.comp25222.termproject.CustomGame;

import ca.bcit.comp25222.termproject.CustomGame.gameplay.GameNode;
import ca.bcit.comp25222.termproject.CustomGame.gameplay.Pipe;
import ca.bcit.comp25222.termproject.CustomGame.managers.GameController;
import ca.bcit.comp25222.termproject.CustomGame.managers.HighScoreManager;
import ca.bcit.comp25222.termproject.CustomGame.managers.ScoreEntry;
import ca.bcit.comp25222.termproject.CustomGame.util.GameState;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Main Application class for Resource Router. Sets up the JavaFX stage,
 * manages UI layout (including overlays), handles user input (clicks, keys),
 * and interacts with the GameController for game logic.
 */
public class ResourceRouterMainMenu extends Application
{
    private static final double INITIAL_WINDOW_WIDTH = 900;
    private static final double INITIAL_WINDOW_HEIGHT = 750;
    private static final double GAME_PANE_MIN_HEIGHT = 600;
    private static final double OVERLAY_PADDING_VAL = 30;
    private static final double OVERLAY_SPACING_VAL = 15;
    private static final double MENU_BUTTON_WIDTH_VAL = 180;
    private static final double BOTTOM_BUTTON_AREA_PADDING_VAL = 15.0;
    private static final double GAME_BUTTON_SPACING_VAL = 20.0;
    private static final double TOP_BAR_SPACING = 15.0;
    private static final double TOP_BAR_PADDING = 10.0;
    private static final double STATUS_LABEL_WIDTH = 350;
    private static final double TITLE_FONT_SIZE = 36;
    private static final double SCORE_FONT_SIZE = 16;
    private static final double SCORE_LIST_PREF_HEIGHT = 350;
    private static final double SCORE_LIST_PREF_WIDTH = 350;
    private static final double TIMER_LOW_THRESHOLD = 10.1;
    private static final int MAX_SOURCE_OUTPUTS = 2;

    private static final String STYLESHEET_PATH = "/nodeStyles.css";
    private static final String CONNECTOR_SELECTED_STYLE_CLASS = "connector-selected";
    private static final String TIMER_LOW_CLASS = "timer-low";
    private static final String TIMER_CRITICAL_CLASS = "timer-critical";
    private static final String TITLE_FONT_FAMILY = null;


// Seconds remaining

    private BorderPane gameViewPane;
    private Pane gamePane;
    private Label levelLabel, scoreLabel, timerLabel, statusLabel;
    Button gameStartStopButton;
    Button gameResetButton;
    private Button gameQuitButton;
    Button nextLevelButton;
    private VBox menuOverlay, tutorialOverlay, gameOverOverlay, highScoresOverlay;
    private Label promptLabel;
    private GameController gameController;
    private GameState currentState = GameState.MENU;
    private GameNode selectedSourceNode = null;
    private StackPane rootStackPane;


    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Resource Router");
        // --- UI Components ---
        rootStackPane = new StackPane();

        setupPromptLabel();
        setupGameView();
        setupMenuOverlay();
        setupTutorialOverlay();
        setupGameOverOverlay();
        setupHighScoresOverlay();

        rootStackPane.getChildren().addAll(gameViewPane, highScoresOverlay, gameOverOverlay, tutorialOverlay, menuOverlay);
        setupGridBackground(rootStackPane, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, 50);

        gameController = new GameController(this, gamePane);

        Scene scene = new Scene(rootStackPane, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, Color.BLACK);
        setupClickConnectAndSceneHandlers(scene);
        applyStyles(scene);

        primaryStage.setScene(scene);
        primaryStage.show();

        updateUIForState(GameState.MENU);
    }

    // --- UI Setup ---
    private void setupGameView() {
        gameViewPane = new BorderPane();
        gameViewPane.setVisible(false);
        gameViewPane.setManaged(false);
        gameViewPane.setTop(createGameStatusInfoBar());
        gamePane = new Pane();
        gamePane.setMinHeight(GAME_PANE_MIN_HEIGHT);
        gamePane.getStyleClass().add("game-pane");
        ScrollPane sp = new ScrollPane(gamePane);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.getStyleClass().add("game-scroll-pane");
        gameViewPane.setCenter(sp);

        VBox bottomArea = createBottomArea();
        gameViewPane.setBottom(bottomArea);
        BorderPane.setAlignment(bottomArea, Pos.CENTER); // Set alignment on the bottomArea (not on bottomButtonBox)
    }


    private HBox createGameStatusInfoBar()
    {
        HBox bar = new HBox(TOP_BAR_SPACING);
        bar.setPadding(new Insets(TOP_BAR_PADDING));
        bar.getStyleClass().add("control-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        levelLabel = createInfoLabel("Level: -/-");
        scoreLabel = createInfoLabel("Score: 0");
        timerLabel = createInfoLabel("Time: 0.0");
        timerLabel.setId("timer-label");
        statusLabel = createInfoLabel("Status:");
        statusLabel.setPrefWidth(STATUS_LABEL_WIDTH);
        statusLabel.setWrapText(true);
        bar.getChildren().addAll(levelLabel, scoreLabel, timerLabel, statusLabel, new Spacer());
        return bar;
    }

    private VBox createBottomArea() {
        // Create a container that stacks the prompt label and the button row vertically
        VBox bottomArea = new VBox(10); // 10 is spacing between label and buttons
        bottomArea.setAlignment(Pos.CENTER);
        bottomArea.setPadding(new Insets(BOTTOM_BUTTON_AREA_PADDING_VAL));
        bottomArea.getStyleClass().add("bottom-button-bar");
        // Or use a separate style if you prefer

        // Create the prompt label
        promptLabel = new Label("Connect the Water and Energy sources to produce FUEL for Sink (K_FIN).");
        promptLabel.getStyleClass().add("prompt-label");
        promptLabel.setWrapText(true); // Allow multiline if text is long

        // Create the button row as an HBox
        HBox buttonRow = new HBox(GAME_BUTTON_SPACING_VAL);
        buttonRow.setAlignment(Pos.CENTER);

        gameStartStopButton = createMenuButton("Start Sim (SPACE)");
        gameStartStopButton.setId("game-start-stop-button");
        gameStartStopButton.setOnAction(e -> handleStartStopClick());

        gameResetButton = createMenuButton("Reset Pipes (R)");
        gameResetButton.setId("game-reset-button");
        gameResetButton.setOnAction(e -> {
            if (!gameController.isSimulationRunning())
                gameController.resetFailedAttemptPipes();
        });

        gameQuitButton = createMenuButton("Quit to Menu");
        gameQuitButton.setId("game-quit-button");
        gameQuitButton.setOnAction(event -> changeGameState(GameState.MENU));

        nextLevelButton = createMenuButton("Next Level ->");
        nextLevelButton.setId("next-level-button");
        nextLevelButton.setOnAction(e -> gameController.loadNextLevel());

        // Add the buttons to the button row
        buttonRow.getChildren().addAll(gameStartStopButton, gameResetButton, gameQuitButton, nextLevelButton);

        // Now add the prompt label and button row to the bottomArea
        bottomArea.getChildren().addAll(promptLabel, buttonRow);

        // Initialize button states
        updateButtonStates(false, false);

        return bottomArea;
    }


    public void updatePrompt(String message) {
        Platform.runLater(() -> {
            if (promptLabel != null)
                promptLabel.setText(message);
        });
    }

    private void setupMenuOverlay()
    {
        menuOverlay = new VBox(OVERLAY_SPACING_VAL);
        styleOverlay(menuOverlay);
        Label t = createTitleLabel("Resource Router");
        Button b1 = createMenuButton("Start Game"), b2 = createMenuButton("How to Play"), b3 = createMenuButton("High Scores"), b4 = createMenuButton("Quit");
        b1.setOnAction(e -> gameController.startGameSession());
        b2.setOnAction(e -> changeGameState(GameState.TUTORIAL));
        b3.setOnAction(e -> changeGameState(GameState.HIGH_SCORES));
        b4.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });
        menuOverlay.getChildren().addAll(t, b1, b2, b3, b4);
    }

    private void setupTutorialOverlay()
    {
        tutorialOverlay = new VBox(OVERLAY_SPACING_VAL);
        styleOverlay(tutorialOverlay);
        Label t = createTitleLabel("How to Play");
        Text i = new Text(loadTutorialText());
        i.getStyleClass().add("tutorial-text");
        i.setTextAlignment(TextAlignment.LEFT);
        i.setWrappingWidth(INITIAL_WINDOW_WIDTH * 0.8);
        Button c = createMenuButton("Close");
        c.setOnAction(e -> changeGameState(GameState.MENU));
        tutorialOverlay.getChildren().addAll(t, i, c);
    }

    private String loadTutorialText() {
        StringBuilder content = new StringBuilder();
        try (InputStream is = getClass().getResourceAsStream("/tutorial.txt"))
        {
            assert is != null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load tutorial text: " + e.getMessage());
            return "Tutorial information unavailable.";
        }
        return content.toString();
    }


    private void setupGameOverOverlay()
    {
        gameOverOverlay = new VBox(OVERLAY_SPACING_VAL);
        styleOverlay(gameOverOverlay);
        Label t = createTitleLabel("Game Over");
        Label sL = createInfoLabel("Final Score: 0");
        sL.setId("final-score-label");
        sL.setFont(Font.font(TITLE_FONT_FAMILY, FontWeight.BOLD, SCORE_FONT_SIZE * 1.5));
        Button b1 = createMenuButton("Play Again"), b2 = createMenuButton("High Scores"), b3 = createMenuButton("Main Menu");
        b1.setOnAction(e -> { gameController.resetGameState(); gameController.startGameSession(); });
        b2.setOnAction(e -> changeGameState(GameState.HIGH_SCORES));
        b3.setOnAction(e -> changeGameState(GameState.MENU));
        gameOverOverlay.getChildren().addAll(t, sL, b1, b2, b3);
    }


    private void setupHighScoresOverlay()
    {
        highScoresOverlay = new VBox(OVERLAY_SPACING_VAL);
        styleOverlay(highScoresOverlay);
        Label t = createTitleLabel("High Scores");
        ListView<String> sL = new ListView<>();
        sL.setId("score-list-view");
        sL.setPrefHeight(SCORE_LIST_PREF_HEIGHT);
        sL.setPrefWidth(SCORE_LIST_PREF_WIDTH);
        Button b = createMenuButton("Back to Menu");
        b.setOnAction(e -> changeGameState(GameState.MENU));
        highScoresOverlay.getChildren().addAll(t, sL, b);
    }

    /**
     * Applies common alignment, padding, style class, visibility to overlays
     */
    private void styleOverlay(VBox overlay)
    {
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(OVERLAY_PADDING_VAL));
        overlay.getStyleClass().add("overlay");
        overlay.setVisible(false);
        overlay.setManaged(false);
    }

    private void setupGridBackground(Pane parent, double width, double height, double gridSize) {
        Pane gridPane = new Pane();
        gridPane.setPickOnBounds(false);
        gridPane.setPrefSize(width, height);
        gridPane.setMouseTransparent(true);
        for (double x = 0; x < width; x += gridSize) {
            javafx.scene.shape.Line line = new javafx.scene.shape.Line(x, 0, x, height);
            line.getStyleClass().add("grid-line");
            gridPane.getChildren().add(line);
        }
        for (double y = 0; y < height; y += gridSize) {
            javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, y, width, y);
            line.getStyleClass().add("grid-line");
            gridPane.getChildren().add(line);
        }
        parent.getChildren().add(0, gridPane); // Add as the bottom layer
    }


    /**
     * Loads and displays high scores
     */
    @SuppressWarnings("unchecked")
    private void populateHighScores() {
        Node node = highScoresOverlay.lookup("#score-list-view");
        if (node instanceof ListView<?> listCasted) {
            ListView<String> scoreListView = (ListView<String>) listCasted;
            List<ScoreEntry> scores = HighScoreManager.loadHighScores();
            scoreListView.getItems().clear();
            if (scores.isEmpty()) {
                scoreListView.getItems().add("No scores recorded yet!");
            } else {
                scores.forEach(score -> scoreListView.getItems().add(score.toString()));
            }
        }
    }


    private Label createTitleLabel(String text)
    {
        Label l = new Label(text);
        l.setFont(Font.font(TITLE_FONT_FAMILY, FontWeight.BOLD, TITLE_FONT_SIZE));
        l.getStyleClass().add("overlay-title");
        return l;
    }

    private Button createMenuButton(String text)
    {
        Button b = new Button(text);
        b.setPrefWidth(MENU_BUTTON_WIDTH_VAL);
        b.getStyleClass().add("menu-button");
        return b;
    }

    private Label createInfoLabel(String initialText)
    {
        Label l = new Label(initialText);
        l.getStyleClass().add("info-label");
        return l;
    }

    public void changeGameState(GameState newState) {
        if (this.currentState.equals(newState))
            return;


        if (newState.equals(GameState.LEVEL_COMPLETE)) {
            this.currentState = newState;
            clearSelectionHighlight();
            selectedSourceNode = null;
            updateUIForState(newState);
            updateButtonStates(false, true);
            return;
        }

        // Fade out the entire root container
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), rootStackPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // If going back to MENU, clear game state
            if (newState.equals(GameState.MENU) && gameController != null) {
                gameController.stopSimulation("Returned to menu");
                gameController.clearLevelGraphics();
                gameController.clearLevelState();
                gamePane.getChildren().clear();
            }
            // Update state and UI
            this.currentState = newState;
            clearSelectionHighlight();
            selectedSourceNode = null;
            updateUIForState(newState);
            switch (newState) {
                case MENU -> {
                    // MENU-specific actions if needed
                }
                case HIGH_SCORES -> populateHighScores();
                case TUTORIAL -> {
                    // Tutorial-specific actions if needed
                }
                case GAME_OVER -> updateGameOverScore(gameController.getCurrentSessionScore());
                default -> updateButtonStates(false, false);
            }
            // Fade in the entire root container
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), rootStackPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }




    private void updateUIForState(GameState state)
    {
        menuOverlay.setVisible(state == GameState.MENU);
        menuOverlay.setManaged(state == GameState.MENU);
        tutorialOverlay.setVisible(state == GameState.TUTORIAL);
        tutorialOverlay.setManaged(state == GameState.TUTORIAL);
        gameOverOverlay.setVisible(state == GameState.GAME_OVER);
        gameOverOverlay.setManaged(state == GameState.GAME_OVER);
        highScoresOverlay.setVisible(state == GameState.HIGH_SCORES);
        highScoresOverlay.setManaged(state == GameState.HIGH_SCORES);
        boolean gameVisible = (state == GameState.PLAYING || state == GameState.LOADING || state == GameState.LEVEL_TRANSITION || state == GameState.LEVEL_COMPLETE);
        gameViewPane.setVisible(gameVisible);
        gameViewPane.setManaged(gameVisible);
        if (!gameVisible)
        {
            switch (state)
            {
                case MENU -> menuOverlay.toFront();
                case TUTORIAL -> tutorialOverlay.toFront();
                case GAME_OVER -> gameOverOverlay.toFront();
                case HIGH_SCORES -> highScoresOverlay.toFront();
                default ->
                {
                }
            }
        }
    }

    private void updateGameOverScore(int score)
    {
        Node n = gameOverOverlay.lookup("#final-score-label");
        if (n instanceof Label l)
            l.setText("Final Score: " + score);
    }

    public void updateButtonStates(boolean isSimRunning, boolean levelComplete) {
        Platform.runLater(() -> {
            if (gameStartStopButton == null || gameResetButton == null
                    || gameQuitButton == null || nextLevelButton == null)
                return;

            boolean showStandard = (currentState == GameState.PLAYING && !levelComplete);
            boolean showNext = (currentState == GameState.LEVEL_COMPLETE);
            boolean showQuit = (showStandard || showNext);

            gameStartStopButton.setVisible(showStandard);
            gameStartStopButton.setManaged(showStandard);
            gameResetButton.setVisible(showStandard);
            gameResetButton.setManaged(showStandard);
            nextLevelButton.setVisible(showNext);
            nextLevelButton.setManaged(showNext);
            gameQuitButton.setVisible(showQuit);
            gameQuitButton.setManaged(showQuit);

            if (showStandard) {
                if (isSimRunning) {
                    // Simulation is running -> Show "Stop Sim" button
                    gameStartStopButton.setText("Stop Sim");
                    gameStartStopButton.setDisable(false);
                    // Typically you disable reset while running
                    gameResetButton.setDisable(true);
                } else {
                    // Simulation is not running -> Show "Start Sim (SPACE)"
                    gameStartStopButton.setText("Start Sim");
                    gameStartStopButton.setDisable(false);
                    // Reset is available if sim is stopped
                    gameResetButton.setDisable(false);
                }
            } else {
                // If not in a standard playable state, disable them
                gameStartStopButton.setDisable(true);
                gameResetButton.setDisable(true);
            }

            nextLevelButton.setDisable(!showNext);
            gameQuitButton.setDisable(!showQuit);
        });
    }


    // --- Event Handlers ---
    private void setupClickConnectAndSceneHandlers(Scene scene)
    {
        setupClickToConnectHandler();
        setupSceneKeyHandler(scene);
    }

    private void setupClickToConnectHandler()
    { // Handles pipe build / remove clicks
        gamePane.setOnMouseClicked((MouseEvent event) -> {
            if (currentState != GameState.PLAYING)
                return; // Ignore clicks if not playing
            Node clickedVisual = event.getPickResult().getIntersectedNode();
            boolean simRunning = gameController.isSimulationRunning();

            // Try pipe removal only if sim STOPPED
            if (!simRunning && clickedVisual instanceof Line lineClicked && lineClicked.getId() != null && lineClicked.getId().startsWith(Pipe.PIPE_ID_PREFIX))
            {
                gameController.removePipeByVisual(lineClicked);
                clearSelectionHighlight();
                selectedSourceNode = null;
                event.consume();
                return;
            }
            // Try pipe building only if sim STOPPED
            if (simRunning)
            {
                updateStatus("Simulation must be stopped to build pipes.");
                event.consume();
                return;
            }

            // Find related logical node
            GameNode clickedLogicalNode = findLogicalNodeViaVisual(clickedVisual);
            if (clickedLogicalNode == null || (gameController.isConnectorVisual(clickedVisual) && clickedLogicalNode != null))
            { // Ignore background or direct connector clicks
                if (selectedSourceNode != null)
                {
                    updateStatus("Connection Cancelled.");
                    clearSelectionHighlight();
                    selectedSourceNode = null;
                }
                event.consume();
                return;
            }

            // Process node click for piping
            if (selectedSourceNode == null)
            {
                if (gameController.isOutputCapableNode(clickedLogicalNode))
                {
                    if (clickedLogicalNode.getOutgoingPipes().size() < MAX_SOURCE_OUTPUTS)
                    {
                        selectedSourceNode = clickedLogicalNode;
                        highlightConnectorForNode(selectedSourceNode, true);
                        updateStatus("Output Selected. Click Target.");
                    } else
                    {
                        updateStatus(clickedLogicalNode.getId() + " output busy.");
                    }
                } else
                {
                    updateStatus("Cannot start pipe from Sink.");
                }
            } else
            { // SECOND CLICK
                clearSelectionHighlight(); // Clear first highlight
                if (clickedLogicalNode == selectedSourceNode)
                {
                    updateStatus("Cancelled.");
                } else if (gameController.isInputCapableNode(clickedLogicalNode))
                {
                    Node sVis = findVisualConnectorForNode(selectedSourceNode, true);
                    Node tVis = findVisualConnectorForNode(clickedLogicalNode, false);
                    if (sVis != null && tVis != null)
                        gameController.attemptPipeConnection(sVis, tVis);
                    else
                        updateStatus("Connector Find Error!");
                } else
                {
                    updateStatus("Invalid Target.");
                }
                selectedSourceNode = null; // Always reset selection state
            }
            event.consume();
        });
    }

    /**
     * Finds Logical GameNode via Visual FX Node click
     */
    private GameNode findLogicalNodeViaVisual(Node visual)
    {
        Node c = visual;
        while (c != null)
        {
            if (c.getId() != null)
            {
                String id = c.getId();
                String lId = null;
                if (id.startsWith("node-body-"))
                    lId = id.substring(10);
                else if (id.startsWith("connector-in-"))
                    lId = id.substring(13);
                else if (id.startsWith("connector-out-"))
                    lId = id.substring(14);
                else if (id.startsWith("label-"))
                    lId = id.substring(6);
                if (lId != null && !lId.isEmpty())
                    return gameController.getLogicalNodeById(lId);
            }
            c = c.getParent();
        }
        return null;
    }

    /**
     * Finds Visual FX Connector for Logical GameNode
     */
    private Node findVisualConnectorForNode(GameNode logicalNode, boolean out)
    {
        if (logicalNode == null)
            return null;
        String tId = (out ? "connector-out-" : "connector-in-") + logicalNode.getId();
        Node v = gamePane.lookup("#" + tId);
        return v;
    }

    // Highlight/Clear Logic
    private void highlightConnectorForNode(GameNode n, boolean out)
    {
        clearSelectionHighlight();
        Node cv = findVisualConnectorForNode(n, out);
        if (cv != null && !cv.getStyleClass().contains(CONNECTOR_SELECTED_STYLE_CLASS))
            cv.getStyleClass().add(CONNECTOR_SELECTED_STYLE_CLASS);
    }

    private void clearSelectionHighlight()
    {
        gamePane.getChildren().stream().filter(n -> n != null &&
                n.getStyleClass().contains(CONNECTOR_SELECTED_STYLE_CLASS))
                .forEach(n -> n.getStyleClass().remove(CONNECTOR_SELECTED_STYLE_CLASS));
    }

    // Keyboard Logic
    private void setupSceneKeyHandler(Scene scene)
    {
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (currentState == GameState.PLAYING)
            {
                if (c == KeyCode.SPACE)
                {
                    handleStartStopClick();
                } else if (c == KeyCode.R && !gameController.isSimulationRunning())
                {
                    gameController.resetFailedAttemptPipes();
                }
            }
            if (c == KeyCode.ESCAPE & currentState != GameState.MENU)
                changeGameState(GameState.MENU);
            e.consume();
        });
    }

    private void handleStartStopClick() {
        if (currentState != GameState.PLAYING) {
            return;
        }
        if (!gameController.isSimulationRunning()) {
            gameController.startSimulation();
        } else {
            gameController.stopSimulation("Manual stop");
        }
    }

    // Apply CSS
    private void applyStyles(Scene scene)
    {
        try
        {
            String css = Objects.requireNonNull(getClass().getResource(STYLESHEET_PATH)).toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e)
        {
            System.err.println("CSS Load Fail: " + e);
        }
    }

    // --- UI Update Callbacks ---
    public void updateStatus(String m)
    {
        Platform.runLater(() -> {
            if (statusLabel != null)
                statusLabel.setText("Status: " + m);
        });
    }

    public void updateTimer(double t)
    {
        Platform.runLater(() -> {
            if (timerLabel != null)
            {
                timerLabel.setText(String.format("Time: %.1f", Math.max(0.0, t)));
                timerLabel.getStyleClass().removeAll(TIMER_LOW_CLASS, TIMER_CRITICAL_CLASS);
                if (t <= 0)
                    timerLabel.getStyleClass().add(TIMER_CRITICAL_CLASS);
                else if (t < TIMER_LOW_THRESHOLD)
                    timerLabel.getStyleClass().add(TIMER_LOW_CLASS);
            }
        });
    }

    public void updateLevelDisplay(int num)
    {
        Platform.runLater(() -> {
            if (levelLabel != null && gameController != null && gameController.levelSequenceForSession != null)
                levelLabel.setText("Level: " + num + "/" + gameController.levelSequenceForSession.size());
            else if (levelLabel != null)
                levelLabel.setText("L:-/-");
        });
    }

    public void updateScoreDisplay(int score)
    {
        Platform.runLater(() -> {
            if (scoreLabel != null)
                scoreLabel.setText("Score: " + score);
        });
    }

    // --- Static Nested Helper Class for Layout ---
    private static class Spacer extends Region
    {
        Spacer()
        {
            HBox.setHgrow(this, Priority.ALWAYS);
            VBox.setVgrow(this, Priority.ALWAYS);
        }
    }
    private void setupPromptLabel() {
        promptLabel = new Label("Welcome! Build your network to satisfy sink demands.");
        promptLabel.getStyleClass().add("prompt-label");
        promptLabel.setAlignment(Pos.CENTER);
        promptLabel.setPrefWidth(INITIAL_WINDOW_WIDTH);
    }

}