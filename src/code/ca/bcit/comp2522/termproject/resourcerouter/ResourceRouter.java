package ca.bcit.comp2522.termproject.resourcerouter;

import ca.bcit.comp2522.termproject.resourcerouter.gameplay.GameNode;
import ca.bcit.comp2522.termproject.resourcerouter.gameplay.Pipe;
import ca.bcit.comp2522.termproject.resourcerouter.managers.GameController;
import ca.bcit.comp2522.termproject.resourcerouter.util.GameState;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

/**
 * The ResourceRouterMainMenu class is the primary entry point and user interface controller
 * for the Resource Router game application. As a JavaFX Application, it is responsible for
 * initializing the primary stage, constructing the overall UI layout, and managing the various
 * overlays and UI components that reflect the current game state.
 *
 * <p>This class provides the following key functionalities:</p>
 * <ul>
 *   <li>
 *     <strong>UI Layout Construction:</strong>
 *     <ul>
 *       <li>Initializes a layered UI using a {@code StackPane} which contains the game view,
 *           game over overlay, and main menu overlay.</li>
 *       <li>Creates a game view comprising a top status bar, a central game pane (wrapped in a scroll pane)
 *           with a grid background, and a bottom control area with game buttons and a prompt label.</li>
 *       <li>Applies CSS styling from an external stylesheet to ensure consistent look and feel.</li>
 *     </ul>
 *   </li>
 *   <li>
 *     <strong>Overlay Management:</strong>
 *     <ul>
 *       <li>Constructs and styles the main menu overlay for starting and quitting the game.</li>
 *       <li>Creates a game over overlay to display the final score and provide options to play again or
 *           return to the main menu.</li>
 *     </ul>
 *   </li>
 *   <li>
 *     <strong>User Interaction and Input Handling:</strong>
 *     <ul>
 *       <li>Handles mouse click events in the game pane for building and removing pipes, including identifying
 *           which visual node was clicked and mapping it to a logical game node.</li>
 *       <li>Implements keyboard shortcuts (e.g., start/stop simulation, reset pipes, escape to main menu) via key handlers.</li>
 *       <li>Uses JavaFX's {@code Platform.runLater} mechanism to update UI components (labels, overlays, button states)
 *           in a thread-safe manner.</li>
 *     </ul>
 *   </li>
 *   <li>
 *     <strong>Game State Management and Transitions:</strong>
 *     <ul>
 *       <li>Maintains the current game state (such as MENU, PLAYING, GAME_OVER, etc.) and updates the UI
 *           accordingly when state changes occur.</li>
 *       <li>Coordinates with the {@link GameController} to
 *           start new game sessions, transition between levels, and manage simulation control (start, stop, reset).</li>
 *       <li>Uses smooth fade transitions when changing between game states to enhance the user experience.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>
 * The class makes extensive use of private helper methods to modularize functionality, including:
 * </p>
 * <ul>
 *   <li>{@code setupPromptLabel()} – Initializes the prompt label used to display instructions or status messages.</li>
 *   <li>{@code setupGameView()} – Constructs the main game view layout with a grid background and control areas.</li>
 *   <li>{@code createGameStatusInfoBar()} and {@code createBottomArea()} – Build the top information bar and bottom button area,
 *       respectively.</li>
 *   <li>{@code setupMenuOverlay()} and {@code setupGameOverOverlay()} – Create and style the main menu and game over overlays.</li>
 *   <li>{@code setupClickConnectAndSceneHandlers(Scene)} and {@code setupSceneKeyHandler(Scene)} – Attach mouse and keyboard
 *       event handlers to enable interactive gameplay.</li>
 *   <li>Various update methods (e.g., {@code updatePrompt(String)}, {@code updateStatus(String)},
 *       {@code updateTimer(double)}, {@code updateLevelDisplay(int)}, and {@code updateScoreDisplay(int)}) that
 *       update UI elements in response to game events.</li>
 * </ul>
 *
 * <p>
 * The ResourceRouterMainMenu class serves as the bridge between the game logic (managed by the
 * {@link GameController}) and the visual presentation,
 * ensuring that the UI reflects the current game state at all times.
 * </p>
 *
 * @see GameController
 * @see GameNode
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class ResourceRouter
             extends Application
{

    private static final int    MAX_SOURCE_OUTPUTS              = 2;
    private static final int    NODE_START_INDEX                = 10;
    private static final int    CONNECTOR_IN_START_INDEX        = 13;
    private static final int    CONNECTOR_OUT_START_INDEX       = 14;
    private static final int    LABEL_START_INDEX               = 6;

    private static final double INITIAL_WINDOW_WIDTH            = 900.0;
    private static final double INITIAL_WINDOW_HEIGHT           = 750.0;
    private static final double GAME_PANE_MIN_HEIGHT            = 600.0;
    private static final double OVERLAY_PADDING_VAL             = 30.0;
    private static final double OVERLAY_SPACING_VAL             = 15.0;
    private static final double MENU_BUTTON_WIDTH_VAL           = 180.0;
    private static final double BOTTOM_BUTTON_AREA_PADDING_VAL  = 15.0;
    private static final double GAME_BUTTON_SPACING_VAL         = 20.0;
    private static final double TOP_BAR_SPACING                 = 15.0;
    private static final double TOP_BAR_PADDING                 = 10.0;
    private static final double STATUS_LABEL_WIDTH              = 500.0;
    private static final double TITLE_FONT_SIZE                 = 36.0;
    private static final double SCORE_FONT_SIZE                 = 16.0;
    private static final double TIMER_LOW_THRESHOLD             = 10.1;
    private static final double BOTTOM_AREA_SPACING             = 10.0;
    private static final double GRID_INCREMENT                  = 50.0;
    private static final double FADE_OUT_DURATION_MS            = 400.0;
    private static final double FADE_IN_DURATION_MS             = 500.0;
    private static final double FADE_START_VALUE                = 1.0;
    private static final double FADE_END_VALUE                  = 0.0;
    private static final double STARTING_CLAMP                  = 0.0;
    private static final double DEFAULT_VALUE                   = 0.0;

    private static final String STYLESHEET_PATH                 = "/nodeStyles.css";
    private static final String CONNECTOR_SELECTED_STYLE_CLASS  = "connector-selected";
    private static final String TIMER_LOW_CLASS                 = "timer-low";
    private static final String TIMER_CRITICAL_CLASS            = "timer-critical";
    private static final String DEFAULT_PROMPT_LABEL_TEXT       = "Default Prompt Label";
    private static final String CANCELLED_MESSAGE               = "Cancelled.";
    private static final String CONNECTOR_FIND_ERROR            = "Connector Find Error!";
    private static final String SIM_STOPPED_BUILD_PIPES         = "Simulation must be stopped to build pipes.";
    private static final String OUTPUT_SELECTED_MESSAGE         = "Output Selected. Click Target.";
    private static final String OUTPUT_BUSY_SUFFIX              = " output busy.";
    private static final String CANNOT_START_FROM_SINK          = "Cannot start pipe from Sink.";
    private static final String INVALID_TARGET_MESSAGE          = "Invalid Target.";
    private static final String STOP_SIM_TEXT                   = "Stop Sim (E)";
    private static final String START_SIM_TEXT                  = "Start Sim (E)";
    private static final String SCORE_LABEL_PREFIX              = "Score: ";
    private static final String LEVEL_LABEL_PREFIX              = "Level: ";
    private static final String STATUS_LABEL_PREFIX             = "Status: ";
    private static final String TIME_LABEL_FORMAT               = "Time: %.1f";
    private static final String FINAL_SCORE_PREFIX              = "Final Score: ";
    private static final String TIMER_LABEL_ID                  = "timer-label";
    private static final String FINAL_SCORE_LABEL_ID            = "final-score-label";
    private static final String DEFAULT_SCORE_TXT               = "0";
    private static final String MENU_OVERLAY_TITLE              = "Resource Router";
    private static final String MENU_START_GAME_TEXT            = "Start Game";
    private static final String MENU_QUIT_TEXT                  = "Quit";
    private static final String GAME_OVER_TITLE                 = "Game Over";
    private static final String GAME_OVER_PLAY_AGAIN            = "Play Again";
    private static final String GAME_OVER_MAIN_MENU             = "Main Menu";
    private static final String RESET_PIPES_BUTTON_TEXT         = "Reset Pipes (R)";
    private static final String QUIT_TO_MENU_TEXT               = "Quit to Menu";
    private static final String NEXT_LEVEL_TEXT                 = "Next Level ->";
    private static final String MANUAL_STOP_MESSAGE             = "Manual stop";
    private static final String RETURNED_TO_MENU_MESSAGE        = "Returned to menu";
    private static final String CONNECTOR_IN                    = "connector-in-";
    private static final String CONNECTOR_OUT                   = "connector-out-";
    private static final String TITLE_FONT_FAMILY;
    
    private Stage               primaryStage;
    private BorderPane          gameViewPane;
    private Pane                gamePane;
    private Label               levelLabel;
    private Label               scoreLabel;
    private Label               timerLabel;
    private Label               statusLabel;
    private VBox                menuOverlay;
    private VBox                gameOverOverlay;
    private Label               promptLabel;
    private GameController      gameController;
    private GameNode            selectedSourceNode;
    private StackPane           rootStackPane;
    private GameState           currentState;

    private Button gameStartStopButton;
    private Button gameResetButton;
    private Button gameQuitButton;
    private Button nextLevelButton;

    static
    {
        TITLE_FONT_FAMILY = null;
    }

    {
        currentState = GameState.MENU;
    }

    /*
     * Initializes the prompt label used for displaying status or instructional text.
     *
     * A Label is created, styled with the "prompt-label" CSS class, centered,
     * and given a fixed width equivalent to the initial window width.
     * This label's text is initialized with the default prompt text.
     *
     */
    private void setupPromptLabel()
    {
        final Label promptLabel;
        promptLabel = new Label(DEFAULT_PROMPT_LABEL_TEXT);
        promptLabel.getStyleClass().add("prompt-promptLabel");
        promptLabel.setAlignment(Pos.CENTER);
        promptLabel.setPrefWidth(INITIAL_WINDOW_WIDTH);

        this.promptLabel = promptLabel;
    }

    /*
     * Sets up the main game view by constructing the overall layout.
     *
     * This method builds a BorderPane consisting of:
     *
     * A top bar (returned by createGameStatusInfoBar()) displaying level, score, timer, and status.
     * A central scrollable pane that contains the main game pane (a Pane with minimum height)
     * A bottom area (returned by createBottomArea()) with the prompt label and control buttons.
     *
     * The game view is initially hidden and not managed (for layout purposes) until the game is ready.
     *
     */
    private void setupGameView()
    {
        final BorderPane gamePane;
        final HBox topBar;
        final Pane centerPane;
        final ScrollPane scrollPane;
        final VBox bottomArea;

        gamePane = new BorderPane();
        gamePane.setVisible(false);
        gamePane.setManaged(false);

        topBar = createGameStatusInfoBar();
        gamePane.setTop(topBar);

        centerPane = new Pane();
        centerPane.setMinHeight(GAME_PANE_MIN_HEIGHT);
        centerPane.getStyleClass().add("game-gamePane");
        this.gamePane = centerPane;

        scrollPane = new ScrollPane(centerPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("game-scroll-gamePane");
        gamePane.setCenter(scrollPane);

        bottomArea = createBottomArea();
        gamePane.setBottom(bottomArea);
        BorderPane.setAlignment(bottomArea, Pos.CENTER);

        gameViewPane = gamePane;
    }

    /*
     * Creates the status bar at the top of the game view.
     *
     * This method constructs an HBox containing labels for the current level, score,
     * timer, and overall status. The labels are styled via CSS classes and arranged
     * with spacing and padding.
     *
     *
     * @return an HBox containing the game status information.
     */
    private HBox createGameStatusInfoBar()
    {
        final HBox  statusBar;
        final Label levelLabel;
        final Label scoreLabel;
        final Label timeLabel;
        final Label statLabel;

        statusBar = new HBox(TOP_BAR_SPACING);
        statusBar.setPadding(new Insets(TOP_BAR_PADDING));
        statusBar.getStyleClass().add("control-statusBar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        levelLabel = createInfoLabel(LEVEL_LABEL_PREFIX + "-/-");
        this.levelLabel = levelLabel;

        scoreLabel = createInfoLabel(SCORE_LABEL_PREFIX +
                                     DEFAULT_SCORE_TXT);
        this.scoreLabel = scoreLabel;

        timeLabel = createInfoLabel(String.format(TIME_LABEL_FORMAT,
                                                  DEFAULT_VALUE));
        timeLabel.setId(TIMER_LABEL_ID);
        timerLabel = timeLabel;

        statLabel = createInfoLabel("Status:");
        statLabel.setPrefWidth(STATUS_LABEL_WIDTH);
        statLabel.setWrapText(true);
        statusLabel = statLabel;

        statusBar.getChildren().addAll(levelLabel,
                                       scoreLabel,
                                       timeLabel,
                                       statLabel);
        return statusBar;
    }

    /*
     * Builds the bottom area of the game view containing the prompt label and control buttons.
     *
     * This method creates a VBox that first contains a prompt label (for instructions or status)
     * and then a row of buttons for starting/stopping the simulation, resetting pipes, quitting, and
     * progressing to the next level.
     *
     *
     * @return a VBox representing the bottom control area.
     */
    private VBox createBottomArea()
    {
        final VBox      bottomArea;
        final Label     promptLabel;
        final HBox      buttonRow;
        final Button    startStopButton;
        final Button    resetButton;
        final Button    quitButton;
        final Button    nextLevelButton;

        bottomArea = new VBox(BOTTOM_AREA_SPACING);
        bottomArea.setAlignment(Pos.CENTER);
        bottomArea.setPadding(new Insets(BOTTOM_BUTTON_AREA_PADDING_VAL));
        bottomArea.getStyleClass().add("bottom-button-bar");

        promptLabel = new Label(DEFAULT_PROMPT_LABEL_TEXT);
        promptLabel.getStyleClass().add("prompt-label");
        promptLabel.setWrapText(true);
        this.promptLabel = promptLabel;

        buttonRow = new HBox(GAME_BUTTON_SPACING_VAL);
        buttonRow.setAlignment(Pos.CENTER);

        startStopButton = createMenuButton(START_SIM_TEXT);
        startStopButton.setId("game-start-stop-button");
        startStopButton.setOnAction(e -> handleStartStopClick());
        gameStartStopButton = startStopButton;


        resetButton = createMenuButton(RESET_PIPES_BUTTON_TEXT);
        resetButton.setId("game-reset-button");
        resetButton.setOnAction(e -> {
            if (!gameController.isSimulationActive())
            {
                gameController.resetFailedAttemptPipes();
            }
        });
        gameResetButton = resetButton;

        quitButton = createMenuButton(QUIT_TO_MENU_TEXT);
        quitButton.setId("game-quit-button");
        quitButton.setOnAction(event -> changeGameState(GameState.MENU));
        gameQuitButton = quitButton;

        nextLevelButton = createMenuButton(NEXT_LEVEL_TEXT);
        nextLevelButton.setId("next-level-button");
        nextLevelButton.setOnAction(e -> gameController.loadNextLevel());
        this.nextLevelButton = nextLevelButton;

        buttonRow.getChildren().addAll(startStopButton,
                                       resetButton,
                                       quitButton,
                                       nextLevelButton);

        bottomArea.getChildren().addAll(promptLabel,
                                        buttonRow);

        updateButtonStates(false, false);
        return bottomArea;
    }

    /*
     * Constructs and configures the main menu overlay.
     *
     * The overlay is a VBox that displays the game title and two buttons:
     * one to start the game and one to quit.
     *
     * The overlay is styled and initially hidden.
     *
     */
    private void setupMenuOverlay()
    {
        final VBox      overlay;
        final Label     gameTitle;
        final Button    startButton;
        final Button    quitButton;

        overlay = new VBox(OVERLAY_SPACING_VAL);
        styleOverlay(overlay);

        gameTitle = createTitleLabel(MENU_OVERLAY_TITLE);

        startButton = createMenuButton(MENU_START_GAME_TEXT);
        startButton.setOnAction(_ -> gameController.startGameSession());


        quitButton = createMenuButton(MENU_QUIT_TEXT);
        quitButton.setOnAction(_ -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
        });

        overlay.getChildren().addAll(gameTitle,
                                     startButton,
                                     quitButton);

        menuOverlay = overlay;
    }

    /*
     * Constructs and configures the game over overlay.
     *
     * This overlay displays the "Game Over" title, the final score, and provides buttons to play again
     * or return to the main menu.
     *
     */
    private void setupGameOverOverlay()
    {
        final VBox      overlay;
        final Label     gameOverTitle;
        final Label     scoreLabel;
        final Button    playAgainButton;
        final Button    mainMenuButton;

        overlay = new VBox(OVERLAY_SPACING_VAL);
        styleOverlay(overlay);

        gameOverTitle = createTitleLabel(GAME_OVER_TITLE);

        scoreLabel = createInfoLabel(FINAL_SCORE_PREFIX +
                                               DEFAULT_SCORE_TXT);
        scoreLabel.setId(FINAL_SCORE_LABEL_ID);
        scoreLabel.setFont(Font.font(TITLE_FONT_FAMILY,
                           FontWeight.BOLD,
                           SCORE_FONT_SIZE));

        playAgainButton = createMenuButton(GAME_OVER_PLAY_AGAIN);
        playAgainButton.setOnAction(e -> {
            gameController.resetGameState();
            gameController.startGameSession();
        });

        mainMenuButton = createMenuButton(GAME_OVER_MAIN_MENU);
        mainMenuButton.setOnAction(e -> changeGameState(GameState.MENU));

        overlay.getChildren().addAll(gameOverTitle,
                                     scoreLabel,
                                     playAgainButton,
                                     mainMenuButton);

        gameOverOverlay = overlay;
    }

    /*
     * Applies standardized styling to an overlay container.
     *
     * This helper method sets alignment, padding, and a default style class ("overlay") on the provided VBox.
     * It also sets the overlay to be initially invisible and unmanaged.
     *
     *
     * @param overlay the VBox to style.
     */
    private void styleOverlay(final VBox overlay)
    {
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(OVERLAY_PADDING_VAL));
        overlay.getStyleClass().add("overlay");
        overlay.setVisible(false);
        overlay.setManaged(false);
    }

    /*
     * Sets up the grid background on the root pane.
     *
     * This method creates a Pane with horizontal and vertical grid lines spaced by GRID_INCREMENT,
     * which is then added as the first child of the root pane. The grid is mouse-transparent to
     * not interfere with gameplay.
     *
     *
     * @param parent the parent pane on which to add the grid.
     */
    private void setupGridBackground(final Pane parent)
    {
        final Pane gridPane;
        gridPane = new Pane();
        gridPane.setPickOnBounds(false);
        gridPane.setPrefSize(INITIAL_WINDOW_WIDTH,
                             INITIAL_WINDOW_HEIGHT);
        gridPane.setMouseTransparent(true);

        for (double xAxis = 0.0; xAxis < INITIAL_WINDOW_WIDTH; xAxis += GRID_INCREMENT)
        {
            final Line line;
            line = new Line(xAxis,
                            DEFAULT_VALUE,
                            xAxis,
                            INITIAL_WINDOW_HEIGHT);

            line.getStyleClass().add("grid-line");
            gridPane.getChildren().add(line);
        }

        for (double yAxis = 0.0; yAxis < INITIAL_WINDOW_HEIGHT; yAxis += GRID_INCREMENT)
        {
            final Line line;
            line = new Line(DEFAULT_VALUE,
                            yAxis,
                            INITIAL_WINDOW_WIDTH,
                            yAxis);

            line.getStyleClass().add("grid-line");
            gridPane.getChildren().add(line);
        }

        parent.getChildren().addFirst(gridPane);
    }

    /*
     * Creates a title label used in overlays.
     *
     * The label is configured with the specified text, styled with bold text of size TITLE_FONT_SIZE,
     * and assigned the CSS class "overlay-title".
     *
     * @param titleLabelText the text to display in the title.
     * @return a configured Label for overlay titles.
     */
    private Label createTitleLabel(final String titleLabelText)
    {
        final Label overlayLabel;
        overlayLabel = new Label(titleLabelText);
        overlayLabel.setFont(Font.font(TITLE_FONT_FAMILY,
                                FontWeight.BOLD,
                                TITLE_FONT_SIZE));
        overlayLabel.getStyleClass().add("overlay-title");

        return overlayLabel;
    }

    /*
     * Creates a menu button with the provided text.
     *
     * The button is configured with a fixed width defined by MENU_BUTTON_WIDTH_VAL,
     * and assigned the "menu-button" CSS style class.
     *
     *
     * @param buttonText the text to appear on the button.
     * @return a new Button instance for menus.
     */
    private Button createMenuButton(final String buttonText)
    {
        final Button menuButton;
        menuButton = new Button(buttonText);
        menuButton.setPrefWidth(MENU_BUTTON_WIDTH_VAL);
        menuButton.getStyleClass().add("menu-Button");

        return menuButton;
    }

    /*
     * Creates an informational label with the specified initial text.
     *
     * The label is assigned the "info-label" CSS class.
     *
     *
     * @param initialText the text to initialize the label with.
     * @return a new Label configured for informational display.
     */
    private Label createInfoLabel(final String initialText)
    {
        final Label informationLabel;
        informationLabel = new Label(initialText);
        informationLabel.getStyleClass().add("info-label");

        return informationLabel;
    }

    /*
     * Sets up mouse click and keyboard handlers for the Scene.
     *
     * This method attaches event handlers to the Scene for handling:
     *
     *   Mouse clicks in the gamePane to build or remove pipes by
     *       mapping visual elements to logical game nodes.
     *   Keyboard shortcuts via setOnKeyPressed to control simulation actions,
     *       such as starting/stopping simulation, resetting pipes, or returning to the main menu.
     *
     * @param scene the Scene to attach handlers to.
     */
    private void setupClickConnectAndSceneHandlers(final Scene scene)
    {
        setupClickToConnectHandler();
        setupSceneKeyHandler(scene);
    }

    /*
     * Attaches a mouse click handler to the gamePane for building and removing pipes.
     *
     * This handler processes clicks as follows:
     *
     * If the simulation is not running and a pipe (Line) is clicked, removes that pipe.
     *
     * If the simulation is running, updates the status message to indicate that pipe building
     * cannot occur.
     *
     * If a game node is clicked that is not a connector, it marks the node as selected
     * (if it is output-capable) or clears the selection if it’s clicked again.
     *
     * If a valid target node (input-capable) is clicked while a source node is selected,
     * attempts to create a new pipe between them.
     *
     */
    private void setupClickToConnectHandler()
    {
        gamePane.setOnMouseClicked((final MouseEvent event) -> {
            if (currentState != GameState.PLAYING)
            {
                return;
            }

            final Node clickedVisual;
            final boolean simRunning;

            clickedVisual   = event.getPickResult().getIntersectedNode();
            simRunning      = gameController.isSimulationActive();

            if (!simRunning && clickedVisual instanceof Line lineClicked &&
                lineClicked.getId() != null &&
                lineClicked.getId().startsWith(Pipe.getPipeIdPrefix()))
            {
                gameController.removePipeByVisual(lineClicked);
                clearSelectionHighlight();
                selectedSourceNode = null;
                event.consume();
                return;
            }

            if (simRunning)
            {
                updateStatus(SIM_STOPPED_BUILD_PIPES);
                event.consume();
                return;
            }

            final GameNode clickedLogicalNode;
            clickedLogicalNode = findLogicalNodeViaVisual(clickedVisual);

            if (clickedLogicalNode == null ||
                gameController.isConnectorVisual(clickedVisual))
            {
                if (selectedSourceNode != null)
                {
                    updateStatus(CANCELLED_MESSAGE);
                    clearSelectionHighlight();
                    selectedSourceNode = null;
                }
                event.consume();
                return;
            }

            if (selectedSourceNode == null)
            {
                if (gameController.isOutputCapableNode(clickedLogicalNode))
                {
                    if (clickedLogicalNode.getOutgoingPipes().size() < MAX_SOURCE_OUTPUTS)
                    {
                        selectedSourceNode = clickedLogicalNode;
                        highlightConnectorForNode(selectedSourceNode);
                        updateStatus(OUTPUT_SELECTED_MESSAGE);
                    }
                    else
                    {
                        updateStatus(clickedLogicalNode.getNodeId() + OUTPUT_BUSY_SUFFIX);
                    }
                }
                else
                {
                    updateStatus(CANNOT_START_FROM_SINK);
                }
            }

            else
            {
                clearSelectionHighlight();

                if (clickedLogicalNode == selectedSourceNode)
                {
                    updateStatus(CANCELLED_MESSAGE);
                }
                else if (gameController.isInputCapableNode(clickedLogicalNode))
                {
                    final Node sVis;
                    final Node tVis;

                    sVis = findVisualConnectorForNode(selectedSourceNode, true);
                    tVis = findVisualConnectorForNode(clickedLogicalNode, false);

                    if (sVis != null && tVis != null)
                    {
                        gameController.attemptPipeConnection(sVis, tVis);
                    }
                    else
                    {
                        updateStatus(CONNECTOR_FIND_ERROR);
                    }
                }
                else
                {
                    updateStatus(INVALID_TARGET_MESSAGE);
                }
                selectedSourceNode = null;
            }
            event.consume();
        });
    }

    /*
     * Traverses up the visual hierarchy starting from the given Node to locate
     * the associated logical GameNode.
     *
     * The method inspects each parent Node’s ID and extracts a logical node identifier (lId)
     * using the known prefixes. It uses the GameController to map that lId to the logical GameNode.
     *
     *
     * @param visual the visual Node that was clicked.
     * @return the corresponding logical GameNode, or null if no match is found.
     */
    private GameNode findLogicalNodeViaVisual(final Node visual)
    {
        Node current;
        current = visual;

        while (current != null)
        {
            final String nodeId;
            nodeId = current.getId();

            if (nodeId != null)
            {
                final String logicalIdentifier;
                logicalIdentifier = getLogicalIdentifier(nodeId);

                if (logicalIdentifier != null && !logicalIdentifier.isEmpty())
                {
                    final GameNode result;
                    result = gameController.getLogicalNodeById(logicalIdentifier);

                    if (result != null)
                    {
                        return result;
                    }
                }
            }
            current = current.getParent();
        }
        return null;
    }

    /*
     * Extracts the logical identifier (lId) from the provided node ID string.
     *
     * This method checks if the given nodeId begins with any of the recognized prefixes:
     * "node-body-", "connector-in-", "connector-out-", or "label-". If a matching prefix is found,
     * it returns the substring of nodeId immediately following the prefix. If no recognized prefix is found,
     * or if the nodeId is null or empty, the method returns null.
     *
     *
     * @param nodeId the node ID from which to extract the logical identifier.
     * @return the extracted logical ID if a recognized prefix is present; otherwise, null.
     */
    private static String getLogicalIdentifier(final String nodeId)
    {
        String logicalIdentifier;
        logicalIdentifier = null;

        if (nodeId.startsWith("node-body-"))
        {
            logicalIdentifier = nodeId.substring(NODE_START_INDEX);
        }
        else if (nodeId.startsWith(CONNECTOR_IN))
        {
            logicalIdentifier = nodeId.substring(CONNECTOR_IN_START_INDEX);
        }
        else if (nodeId.startsWith(CONNECTOR_OUT))
        {
            logicalIdentifier = nodeId.substring(CONNECTOR_OUT_START_INDEX);
        }
        else if (nodeId.startsWith("label-"))
        {
            logicalIdentifier = nodeId.substring(LABEL_START_INDEX);
        }
        return logicalIdentifier;
    }

    /*
     * Locates and returns the visual connector Node for the given logical GameNode.
     *
     * If outputConnector is true, the method looks up the output connector; if false, it looks
     * for the input connector. The lookup is performed by constructing the expected connector ID
     * and searching the gamePane.
     *
     * @param logicalNode the logical GameNode.
     * @param outputConnector         true to look up the output connector; false for input connector.
     * @return the found connector Node, or null if not found.
     */
    private Node findVisualConnectorForNode(final GameNode logicalNode,
                                            final boolean outputConnector)
    {
        if (logicalNode == null)
        {
            return null;
        }

        final String prefix;
        if (outputConnector)
        {
            prefix = CONNECTOR_OUT;
        }
        else
        {
            prefix = CONNECTOR_IN;
        }

        final String connectorId;
        final Node connector;

        connectorId = prefix + logicalNode.getNodeId();
        connector   = gamePane.lookup("#" + connectorId);

        return connector;
    }


    /*
     * Highlights the output connector of the specified GameNode to indicate selection.
     *
     * First clears any existing selection highlight, then locates the output connector of the given node
     * and adds the "connector-selected" style class to it.
     *
     *
     * @param node the GameNode whose output connector should be highlighted.
     */
    private void highlightConnectorForNode(final GameNode node)
    {
        clearSelectionHighlight();

        final Node connector;
        connector = findVisualConnectorForNode(node, true);

        if (connector != null &&
           !connector.getStyleClass().contains(CONNECTOR_SELECTED_STYLE_CLASS))
        {
            connector.getStyleClass().add(CONNECTOR_SELECTED_STYLE_CLASS);
        }
    }

    /*
     * Clears any selection highlight from connector Nodes.
     *
     * Iterates over all children of gamePane, filtering for those with the "connector-selected"
     * style class, and removes that style class.
     *
     */
    private void clearSelectionHighlight()
    {
        gamePane.getChildren().stream()
                              .filter(node -> node != null &&
                              node.getStyleClass()
                              .contains(CONNECTOR_SELECTED_STYLE_CLASS))
                              .forEach(node -> node.getStyleClass()
                              .remove(CONNECTOR_SELECTED_STYLE_CLASS));
    }

    /*
     * Sets up a key handler on the given Scene to enable keyboard shortcuts.
     *
     * The handler listens for key presses and, depending on the key and current game state, triggers actions:
     *
     * Key 'E' toggles simulation start/stop if the game is in PLAYING state.
     * Key 'R' resets pipes if simulation is not active.
     * ESCAPE returns the game state to MENU if not already in MENU.
     *
     * @param scene the Scene on which to attach the key handler.
     */
    private void setupSceneKeyHandler(final Scene scene)
    {
        scene.setOnKeyPressed(e -> {
            final KeyCode code;
            code = e.getCode();

            if (currentState == GameState.PLAYING)
            {
                if (code == KeyCode.E)
                {
                    handleStartStopClick();
                }
                else if (code == KeyCode.R &&
                        !gameController.isSimulationActive())
                {
                    gameController.resetFailedAttemptPipes();
                }
            }
            if (code == KeyCode.ESCAPE &&
                currentState != GameState.MENU)
            {
                changeGameState(GameState.MENU);
            }
            e.consume();
        });
    }

    /*
     * Handles the logic to start or stop the simulation when the corresponding button is clicked.
     *
     * This method first verifies that the current game state is PLAYING. If not, it does nothing.
     * When in PLAYING state, it checks whether the simulation is active:
     *
     * If the simulation is not active, it invokes the GameController to start the simulation.</li>
     * If the simulation is already active, it instructs the GameController to stop the simulation,
     * passing a pre-defined manual stop message.
     *
     * This method is intended to be invoked by the action handler of the start/stop button.
     *
     */
    private void handleStartStopClick()
    {
        if (currentState != GameState.PLAYING)
        {
            return;
        }
        if (!gameController.isSimulationActive())
        {
            gameController.startSimulation();
        }
        else
        {
            gameController.stopSimulation(MANUAL_STOP_MESSAGE);
        }
    }

    /*
     * Applies the external CSS styles to the provided Scene.
     *
     * The method attempts to locate the stylesheet resource using the STYLESHEET_PATH constant.
     * If the stylesheet is found, its external form URL is added to the scene’s stylesheets.
     * If the stylesheet cannot be located (resulting in a NullPointerException), an error message
     * is printed to the error stream.
     *
     *
     * @param scene the JavaFX Scene to which the CSS styles should be applied.
     */
    private void applyStyles(final Scene scene)
    {
        try
        {
            final String cssPath;
            cssPath = Objects.requireNonNull(getClass()
                             .getResource(STYLESHEET_PATH))
                             .toExternalForm();

            scene.getStylesheets().add(cssPath);
        }
        catch (final NullPointerException e)
        {
            throw new IllegalArgumentException("Failed to load stylesheet: " + STYLESHEET_PATH, e);
        }
    }

    /*
     * Updates the UI components' visibility and layout based on the specified game state.
     *
     * This method adjusts the visibility and managed properties of the menu overlay, game over overlay,
     * and the main game view pane according to the new state. For non-visible states, it ensures that
     * the overlay corresponding to MENU or GAME_OVER is brought to the front.
     *
     *
     * @param state the new GameState to which the UI should adapt.
     */
    private void updateUIForState(final GameState state)
    {
        menuOverlay.setVisible(state == GameState.MENU);
        menuOverlay.setManaged(state == GameState.MENU);

        gameOverOverlay.setVisible(state == GameState.GAME_OVER);
        gameOverOverlay.setManaged(state == GameState.GAME_OVER);

        final boolean gameVisible;
        gameVisible = (state == GameState.PLAYING
                    || state == GameState.LOADING
                    || state == GameState.LEVEL_TRANSITION
                    || state == GameState.LEVEL_COMPLETE);

        gameViewPane.setVisible(gameVisible);
        gameViewPane.setManaged(gameVisible);

        if (!gameVisible)
        {
            switch (state)
            {
                case MENU -> menuOverlay.toFront();
                case GAME_OVER -> gameOverOverlay.toFront();
                default -> {
                }
            }
        }
    }

    /*
     * Updates the final score label displayed on the Game Over overlay.
     *
     * This method attempts to locate the Label node with the specific final score label ID
     * within the game over overlay and, if found, updates its text to reflect the provided final score.
     *
     *
     * @param score the final score to be displayed.
     */
    private void updateGameOverScore(final int score)
    {
        final Node node;
        node = gameOverOverlay.lookup("#" + FINAL_SCORE_LABEL_ID);

        if (node instanceof Label label)
        {
            label.setText(FINAL_SCORE_PREFIX + score);
        }
    }

    /**
     * Launches the Resource Router game application on the JavaFX Application Thread.
     * <p>
     * This static method uses Platform.runLater() to ensure that the UI initialization
     * and stage creation occur on the JavaFX thread. It creates a new Stage, instantiates a new
     * ResourceRouter application, calls its start() method, and brings the stage to the front.
     * If any exception occurs during the launch process, a message is printed to the console.
     * </p>
     */
    public static void launchGame()
    {
        Platform.runLater(() -> {
            try {
                final Stage       stage;
                final Application newGame;

                stage   = new Stage();
                newGame = new ResourceRouter();
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
     * Changes the current game state to the specified new state and updates the UI accordingly.
     * <p>
     * This method first checks if the new state is already active; if so, it does nothing.
     * For the LEVEL_COMPLETE state, it clears selection highlights, updates UI elements,
     * and disables/enables relevant buttons. For other states, it uses a fade-out transition on the root
     * pane; upon completion, it changes the state, updates the UI (including clearing selections and updating overlays),
     * and then applies a fade-in transition to smoothly reveal the new state.
     * </p>
     *
     * @param newState the new GameState to transition to.
     */
    public void changeGameState(final GameState newState)
    {
        if (this.currentState.equals(newState))
        {
            return;
        }
        if (newState.equals(GameState.LEVEL_COMPLETE))
        {
            this.currentState = newState;

            clearSelectionHighlight();
            selectedSourceNode = null;

            updateUIForState(newState);
            updateButtonStates(false,
                               true);
            return;
        }

        final FadeTransition fadeOut;
        fadeOut = new FadeTransition(Duration.millis(FADE_OUT_DURATION_MS),
                                     rootStackPane);

        fadeOut.setFromValue(FADE_START_VALUE );
        fadeOut.setToValue(FADE_END_VALUE);

        fadeOut.setOnFinished(e -> {
            if (newState.equals(GameState.MENU) && gameController != null)
            {
                gameController.stopSimulation(RETURNED_TO_MENU_MESSAGE);
                gameController.clearLevelGraphics();
                gameController.clearLevelState();
                gamePane.getChildren().clear();
            }

            this.currentState = newState;
            clearSelectionHighlight();

            selectedSourceNode = null;
            updateUIForState(newState);

            switch (newState)
            {
                case MENU -> {
                }
                case GAME_OVER -> updateGameOverScore(gameController.getCurrentSessionScore());
                default -> updateButtonStates(false, false);
            }

            final FadeTransition fadeIn;
            fadeIn = new FadeTransition(Duration.millis(FADE_IN_DURATION_MS), rootStackPane);
            fadeIn.setFromValue(FADE_END_VALUE);
            fadeIn.setToValue(FADE_START_VALUE );
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Updates the prompt label text in a thread-safe manner.
     * <p>
     * This method schedules a UI update on the JavaFX Application Thread via Platform.runLater().
     * If the prompt label exists, its text is updated to the specified message.
     * </p>
     *
     * @param message the new prompt message to display.
     */
    public void updatePrompt(final String message)
    {
        Platform.runLater(() -> {
            if (promptLabel != null)
            {
                promptLabel.setText(message);
            }
        });
    }

    /**
     * Updates the status label text in a thread-safe manner.
     * <p>
     * This method schedules a UI update on the JavaFX Application Thread. If the status label exists,
     * its text is updated to the specified message prefixed with a defined status label prefix.
     * </p>
     *
     * @param message the new status message to display.
     */
    public void updateStatus(final String message)
    {
        Platform.runLater(() -> {
            if (statusLabel != null)
            {
                statusLabel.setText(STATUS_LABEL_PREFIX + message);
            }
        });
    }

    /**
     * Updates the timer display label to show the new time value.
     * <p>
     * The method clamps the timer value to a minimum threshold, formats it using a predefined time format,
     * and then updates the timer label's text. It also adjusts the timer label's style classes to reflect
     * low or critical time conditions.
     * </p>
     *
     * @param timer the new timer value in seconds.
     */
    public void updateTimer(final double timer)
    {
        Platform.runLater(() -> {
            if (timerLabel != null)
            {
                final double clampedTime;
                final String timerText;

                clampedTime = Math.max(STARTING_CLAMP, timer);
                timerText   = String.format(TIME_LABEL_FORMAT, clampedTime);

                timerLabel.setText(timerText);

                timerLabel.getStyleClass().removeAll(TIMER_LOW_CLASS, TIMER_CRITICAL_CLASS);
                if (clampedTime <= STARTING_CLAMP)
                {
                    timerLabel.getStyleClass().add(TIMER_CRITICAL_CLASS);
                }
                else if (clampedTime < TIMER_LOW_THRESHOLD)
                {
                    timerLabel.getStyleClass().add(TIMER_LOW_CLASS);
                }
            }
        });
    }

    /**
     * Updates the level display label to reflect the current level number and total available levels.
     *
     * The label is updated to display a string formatted as "Level: current/total" where
     * "current" is the current level number and "total" is the size of the level sequence.
     * If the level information is unavailable, a placeholder is shown.
     *
     *
     * @param levelNumber the current level number.
     */
    public void updateLevelDisplay(final int levelNumber)
    {
        Platform.runLater(() -> {
            if (levelLabel     != null &&
                gameController != null &&
                gameController.getLevelSequence() != null)
            {
                final String displayText;
                displayText = LEVEL_LABEL_PREFIX + levelNumber + "/" +
                              gameController.getLevelSequence().size();

                levelLabel.setText(displayText);
            }
            else if (levelLabel != null)
            {
                levelLabel.setText("L:-/-");
            }
        });
    }

    /**
     * Updates the score display label with the provided score.
     * <p>
     * This method formats the score using a predefined score prefix and updates the corresponding label.
     * The update is done on the JavaFX Application Thread to ensure thread safety.
     * </p>
     *
     * @param score the current score to display.
     */
    public void updateScoreDisplay(final int score)
    {
        Platform.runLater(() -> {
            if (scoreLabel != null)
            {
                final String displayText;
                displayText = SCORE_LABEL_PREFIX + score;
                scoreLabel.setText(displayText);
            }
        });
    }

    /**
     * Updates the states of the game control buttons (Start/Stop, Reset, Quit, Next Level) based on the
     * current simulation activity and level completion status.
     * <p>
     * This method ensures that the buttons are shown or hidden (and enabled or disabled) appropriately:
     * <ul>
     *   <li>If the game is in PLAYING state and the level is not complete, it enables the start/stop and reset buttons,
     *       configuring the start/stop button text based on whether the simulation is active.</li>
     *   <li>If the level is complete, it shows the next level button and disables other controls as needed.</li>
     *   <li>Otherwise, it disables all control buttons.</li>
     * </ul>
     * The update is performed on the JavaFX Application Thread.
     * </p>
     *
     * @param isSimRunning  true if the simulation is currently active.
     * @param levelComplete true if the current level has been completed.
     */
    public void updateButtonStates(final boolean isSimRunning,
                                   final boolean levelComplete)
    {
        Platform.runLater(() -> {
            if (gameStartStopButton     == null
                    || gameResetButton  == null
                    || gameQuitButton   == null
                    || nextLevelButton  == null)
            {
                return;
            }

            final boolean showStandard;
            final boolean showNext;
            final boolean showQuit;

            showStandard = (currentState == GameState.PLAYING && !levelComplete);
            showNext     = (currentState == GameState.LEVEL_COMPLETE);
            showQuit     = (showStandard || showNext);

            gameStartStopButton.setVisible(showStandard);
            gameStartStopButton.setManaged(showStandard);

            gameResetButton.setVisible(showStandard);
            gameResetButton.setManaged(showStandard);

            nextLevelButton.setVisible(showNext);
            nextLevelButton.setManaged(showNext);

            gameQuitButton.setVisible(showQuit);
            gameQuitButton.setManaged(showQuit);

            if (showStandard)
            {
                if (isSimRunning)
                {
                    gameStartStopButton.setText(STOP_SIM_TEXT);
                    gameStartStopButton.setDisable(false);
                    gameResetButton.setDisable(true);
                }
                else
                {
                    gameStartStopButton.setText(START_SIM_TEXT);
                    gameStartStopButton.setDisable(false);
                    gameResetButton.setDisable(false);
                }
            }
            else
            {
                gameStartStopButton.setDisable(true);
                gameResetButton.setDisable(true);
            }

            nextLevelButton.setDisable(!showNext);
            gameQuitButton.setDisable(!showQuit);
        });
    }

    /**
     * Starts the application stage by initializing the UI, setting up event handlers,
     * and launching the game view.
     * <p>
     * This method performs the following tasks:
     * <ol>
     *   <li>Obtains a reference to the primary stage and sets its title.</li>
     *   <li>Creates a root StackPane and sets it as the container for the game view pane, overlays, and grid background.</li>
     *   <li>Invokes helper methods to initialize the prompt label, game view layout, main menu overlay, and game over overlay.</li>
     *   <li>Attaches the grid background to the root pane.</li>
     *   <li>Instantiates the GameController with references to the ResourceRouter and game pane.</li>
     *   <li>Creates a Scene with the root pane, applies CSS styles, and sets up mouse and keyboard event handlers.</li>
     *   <li>Finally, sets the scene on the primary stage, shows the stage, and updates the UI to the MENU state.</li>
     * </ol>
     * </p>
     *
     * @param primary the primary Stage provided by the JavaFX runtime.
     */
    @Override
    public void start(final Stage primary)
    {
        final Stage             stageRef;
        final StackPane         rootPane;
        final GameController    controller;
        final Scene             scene;

        stageRef        = primary;
        primaryStage    = stageRef;
        primaryStage.setTitle("Resource Router");

        rootPane        = new StackPane();
        rootStackPane   = rootPane;

        setupPromptLabel();
        setupGameView();
        setupMenuOverlay();
        setupGameOverOverlay();

        rootStackPane.getChildren().addAll(gameViewPane,
                                           gameOverOverlay,
                                           menuOverlay);
        setupGridBackground(rootStackPane);


        controller      = new GameController(this, gamePane);
        gameController  = controller;

        scene = new Scene(rootStackPane,
                          INITIAL_WINDOW_WIDTH,
                          INITIAL_WINDOW_HEIGHT,
                          Color.BLACK);

        setupClickConnectAndSceneHandlers(scene);
        applyStyles(scene);

        primaryStage.setScene(scene);
        primaryStage.show();

        updateUIForState(GameState.MENU);
    }
}
