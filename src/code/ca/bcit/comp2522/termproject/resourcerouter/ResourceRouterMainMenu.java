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

public final class ResourceRouterMainMenu
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
    private static final String TITLE_FONT_FAMILY               = null;
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
    private static final String CSS_LOAD_FAIL_PREFIX            = "CSS Load Fail: ";
    private static final String CONNECTOR_IN                    = "connector-in-";
    private static final String CONNECTOR_OUT                   = "connector-out-";
    
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
    private GameState           currentState = GameState.MENU;

    private Button gameStartStopButton;
    private Button gameResetButton;
    private Button gameQuitButton;
    private Button nextLevelButton;

    /*
     * Sets up the prompt label at the top of the UI.
     */
    private void setupPromptLabel()
    {
        final Label label;
        label = new Label(DEFAULT_PROMPT_LABEL_TEXT);
        label.getStyleClass().add("prompt-label");
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(INITIAL_WINDOW_WIDTH);

        promptLabel = label;
    }

    /*
     * Sets up the main game view, including the top bar, center pane, and bottom area.
     */
    private void setupGameView()
    {
        final BorderPane pane;
        final HBox topBar;
        final Pane centerPane;
        final ScrollPane scrollPane;
        final VBox bottomArea;

        pane = new BorderPane();
        pane.setVisible(false);
        pane.setManaged(false);

        topBar = createGameStatusInfoBar();
        pane.setTop(topBar);

        centerPane = new Pane();
        centerPane.setMinHeight(GAME_PANE_MIN_HEIGHT);
        centerPane.getStyleClass().add("game-pane");
        gamePane = centerPane;

        scrollPane = new ScrollPane(centerPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("game-scroll-pane");
        pane.setCenter(scrollPane);

        bottomArea = createBottomArea();
        pane.setBottom(bottomArea);
        BorderPane.setAlignment(bottomArea, Pos.CENTER);

        gameViewPane = pane;
    }

    /*
     * Creates the HBox that displays level, score, timer, and status information.
     *
     * @return an HBox containing the game status info.
     */
    private HBox createGameStatusInfoBar()
    {
        final HBox bar;
        final Label lvlLabel;
        final Label scrLabel;
        final Label tLabel;
        final Label statLabel;

        bar = new HBox(TOP_BAR_SPACING);
        bar.setPadding(new Insets(TOP_BAR_PADDING));
        bar.getStyleClass().add("control-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        lvlLabel = createInfoLabel(LEVEL_LABEL_PREFIX + "-/-");
        levelLabel = lvlLabel;

        scrLabel = createInfoLabel(SCORE_LABEL_PREFIX +
                                             DEFAULT_SCORE_TXT);
        scoreLabel = scrLabel;

        tLabel = createInfoLabel(String.format(TIME_LABEL_FORMAT,
                                               DEFAULT_VALUE));
        tLabel.setId(TIMER_LABEL_ID);
        timerLabel = tLabel;

        statLabel = createInfoLabel("Status:");
        statLabel.setPrefWidth(STATUS_LABEL_WIDTH);
        statLabel.setWrapText(true);
        statusLabel = statLabel;

        bar.getChildren().addAll(lvlLabel,
                                 scrLabel,
                                 tLabel,
                                 statLabel);
        return bar;
    }

    /*
     * Creates the bottom area containing the prompt label and game control buttons.
     *
     * @return a VBox that holds the prompt label and the button row.
     */
    private VBox createBottomArea()
    {
        final VBox bottomArea;
        final Label pLabel;
        final HBox buttonRow;
        final Button startStopBtn;
        final Button resetBtn;
        final Button quitBtn;
        final Button nextBtn;

        bottomArea = new VBox(BOTTOM_AREA_SPACING);
        bottomArea.setAlignment(Pos.CENTER);
        bottomArea.setPadding(new Insets(BOTTOM_BUTTON_AREA_PADDING_VAL));
        bottomArea.getStyleClass().add("bottom-button-bar");

        pLabel = new Label(DEFAULT_PROMPT_LABEL_TEXT);
        pLabel.getStyleClass().add("prompt-label");
        pLabel.setWrapText(true);
        promptLabel = pLabel;

        buttonRow = new HBox(GAME_BUTTON_SPACING_VAL);
        buttonRow.setAlignment(Pos.CENTER);

        startStopBtn = createMenuButton(START_SIM_TEXT);
        startStopBtn.setId("game-start-stop-button");
        startStopBtn.setOnAction(e -> handleStartStopClick());
        gameStartStopButton = startStopBtn;


        resetBtn = createMenuButton(RESET_PIPES_BUTTON_TEXT);
        resetBtn.setId("game-reset-button");
        resetBtn.setOnAction(e -> {
            if (!gameController.isSimulationRunning())
            {
                gameController.resetFailedAttemptPipes();
            }
        });
        gameResetButton = resetBtn;

        quitBtn = createMenuButton(QUIT_TO_MENU_TEXT);
        quitBtn.setId("game-quit-button");
        quitBtn.setOnAction(event -> changeGameState(GameState.MENU));
        gameQuitButton = quitBtn;

        nextBtn = createMenuButton(NEXT_LEVEL_TEXT);
        nextBtn.setId("next-level-button");
        nextBtn.setOnAction(e -> gameController.loadNextLevel());
        nextLevelButton = nextBtn;

        buttonRow.getChildren().addAll(startStopBtn,
                                       resetBtn,
                                       quitBtn,
                                       nextBtn);

        bottomArea.getChildren().addAll(pLabel,
                                        buttonRow);

        updateButtonStates(false, false);
        return bottomArea;
    }

    /*
     * Sets up the main menu overlay (start, quit).
     */
    private void setupMenuOverlay()
    {
        final VBox overlay;
        final Label title;
        final Button startBtn;
        final Button quitBtn;

        overlay = new VBox(OVERLAY_SPACING_VAL);
        styleOverlay(overlay);

        title = createTitleLabel(MENU_OVERLAY_TITLE);

        startBtn = createMenuButton(MENU_START_GAME_TEXT);
        startBtn.setOnAction(_ -> gameController.startGameSession());


        quitBtn = createMenuButton(MENU_QUIT_TEXT);
        quitBtn.setOnAction(_ -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
        });

        overlay.getChildren().addAll(title,
                                     startBtn,
                                     quitBtn);

        menuOverlay = overlay;
    }

    /*
     * Sets up the game over overlay with final score and navigation buttons.
     */
    private void setupGameOverOverlay()
    {
        final VBox overlay;
        final Label title;
        final Label scoreLbl;
        final Button playAgainBtn;
        final Button mainMenuBtn;

        overlay = new VBox(OVERLAY_SPACING_VAL);
        styleOverlay(overlay);

        title = createTitleLabel(GAME_OVER_TITLE);

        scoreLbl = createInfoLabel(FINAL_SCORE_PREFIX +
                                             DEFAULT_SCORE_TXT);
        scoreLbl.setId(FINAL_SCORE_LABEL_ID);
        scoreLbl.setFont(Font.font(TITLE_FONT_FAMILY,
                                   FontWeight.BOLD,
                                   SCORE_FONT_SIZE));

        playAgainBtn = createMenuButton(GAME_OVER_PLAY_AGAIN);
        playAgainBtn.setOnAction(e -> {
            gameController.resetGameState();
            gameController.startGameSession();
        });

        mainMenuBtn = createMenuButton(GAME_OVER_MAIN_MENU);
        mainMenuBtn.setOnAction(e -> changeGameState(GameState.MENU));

        overlay.getChildren().addAll(title,
                                     scoreLbl,
                                     playAgainBtn,
                                     mainMenuBtn);

        gameOverOverlay = overlay;
    }

    /*
     * Applies styling to a VBox overlay, setting alignment, padding, and initial visibility.
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
     * Sets up the grid background on a parent pane, drawing horizontal and vertical lines.
     *
     * @param parent the pane on which the grid is drawn.
     */
    private void setupGridBackground(final Pane parent)
    {
        final Pane gridPane;
        gridPane = new Pane();
        gridPane.setPickOnBounds(false);
        gridPane.setPrefSize(INITIAL_WINDOW_WIDTH,
                             INITIAL_WINDOW_HEIGHT);
        gridPane.setMouseTransparent(true);

        for (double x = 0.0; x < INITIAL_WINDOW_WIDTH; x += GRID_INCREMENT)
        {
            final Line line;
            line = new Line(x,
                            DEFAULT_VALUE,
                            x,
                            INITIAL_WINDOW_HEIGHT);

            line.getStyleClass().add("grid-line");
            gridPane.getChildren().add(line);
        }

        for (double y = 0.0; y < INITIAL_WINDOW_HEIGHT; y += GRID_INCREMENT)
        {
            final Line line;
            line = new Line(DEFAULT_VALUE,
                            y,
                            INITIAL_WINDOW_WIDTH,
                            y);

            line.getStyleClass().add("grid-line");
            gridPane.getChildren().add(line);
        }

        parent.getChildren().addFirst(gridPane);
    }

    /*
     * Creates a title label for overlays.
     *
     * @param text the label text.
     * @return a configured Label for overlay titles.
     */
    private Label createTitleLabel(final String text)
    {
        final Label label;
        label = new Label(text);
        label.setFont(Font.font(TITLE_FONT_FAMILY,
                                FontWeight.BOLD,
                                TITLE_FONT_SIZE));
        label.getStyleClass().add("overlay-title");

        return label;
    }

    /*
     * Creates a generic menu button with the specified text.
     *
     * @param text the button text.
     * @return a configured Button for menus.
     */
    private Button createMenuButton(final String text)
    {
        final Button button;
        button = new Button(text);
        button.setPrefWidth(MENU_BUTTON_WIDTH_VAL);
        button.getStyleClass().add("menu-button");

        return button;
    }

    /*
     * Creates an informational label with a given initial text.
     *
     * @param initialText the initial text for the label.
     * @return a configured Label for info display.
     */
    private Label createInfoLabel(final String initialText)
    {
        final Label label;
        label = new Label(initialText);
        label.getStyleClass().add("info-label");

        return label;
    }

    /*
     * Sets up mouse-click handling for building/removing pipes and scene key handling.
     *
     * @param scene the Scene to set up handlers on.
     */
    private void setupClickConnectAndSceneHandlers(final Scene scene)
    {
        setupClickToConnectHandler();
        setupSceneKeyHandler(scene);
    }

    /*
     * Handles mouse clicks in the gamePane for pipe building and removal.
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
            simRunning      = gameController.isSimulationRunning();

            if (!simRunning && clickedVisual instanceof Line lineClicked &&
                    lineClicked.getId() != null &&
                    lineClicked.getId().startsWith(Pipe.PIPE_ID_PREFIX))
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
                        updateStatus(clickedLogicalNode.getId() + OUTPUT_BUSY_SUFFIX);
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
     * Locates the logical GameNode that corresponds to a given visual Node.
     *
     * @param visual the visual Node clicked.
     * @return the corresponding GameNode, or null if not found.
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
                final String lId;
                lId = getLId(nodeId);

                if (lId != null && !lId.isEmpty())
                {
                    final GameNode result;
                    result = gameController.getLogicalNodeById(lId);

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
    private static String getLId(final String nodeId)
    {
        String lId;
        lId = null;

        if (nodeId.startsWith("node-body-"))
        {
            lId = nodeId.substring(NODE_START_INDEX);
        }
        else if (nodeId.startsWith(CONNECTOR_IN))
        {
            lId = nodeId.substring(CONNECTOR_IN_START_INDEX);
        }
        else if (nodeId.startsWith(CONNECTOR_OUT))
        {
            lId = nodeId.substring(CONNECTOR_OUT_START_INDEX);
        }
        else if (nodeId.startsWith("label-"))
        {
            lId = nodeId.substring(LABEL_START_INDEX);
        }
        return lId;
    }

    /*
     * Finds the visual connector node for the specified logical GameNode.
     *
     * @param logicalNode the logical node.
     * @param out         true for output connector, false for input connector.
     * @return the visual Node of the connector, or null if not found.
     */
    private Node findVisualConnectorForNode(final GameNode logicalNode,
                                            final boolean out)
    {
        if (logicalNode == null)
        {
            return null;
        }

        final String prefix;
        if (out)
        {
            prefix = CONNECTOR_OUT;
        }
        else
        {
            prefix = CONNECTOR_IN;
        }

        final String connectorId;
        final Node connector;

        connectorId = prefix + logicalNode.getId();
        connector   = gamePane.lookup("#" + connectorId);

        return connector;
    }


    /*
     * Highlights the connector of a given GameNode, clearing existing highlights first.
     *
     * @param node the GameNode to highlight.
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

    /**
     * Clears the 'selected' style from any connector node.
     */
    private void clearSelectionHighlight()
    {
        gamePane.getChildren().stream()
                              .filter(n -> n != null &&
                              n.getStyleClass()
                              .contains(CONNECTOR_SELECTED_STYLE_CLASS))
                              .forEach(n -> n.getStyleClass()
                              .remove(CONNECTOR_SELECTED_STYLE_CLASS));
    }

    /*
     * Sets up key handling for the specified Scene (keyboard shortcuts).
     *
     * @param scene the Scene to attach key handlers to.
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
                        !gameController.isSimulationRunning())
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
     * Handles the start/stop simulation logic when the corresponding button is clicked.
     */
    private void handleStartStopClick()
    {
        if (currentState != GameState.PLAYING)
        {
            return;
        }
        if (!gameController.isSimulationRunning())
        {
            gameController.startSimulation();
        }
        else
        {
            gameController.stopSimulation(MANUAL_STOP_MESSAGE);
        }
    }

    /*
     * Applies CSS styles to the given scene.
     *
     * @param scene the Scene to style.
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
            System.err.println(CSS_LOAD_FAIL_PREFIX + e);
        }
    }

    /*
     * Updates the UI based on the given GameState.
     *
     * @param state the new GameState.
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
     * Updates the final score label in the Game Over overlay.
     *
     * @param score the final score to display.
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
     * Launches the game via JavaFX Platform.runLater mechanism.
     */
    public static void launchGame()
    {
        Platform.runLater(() -> {
            try {
                final Stage       stage;
                final Application newGame;

                stage   = new Stage();
                newGame = new ResourceRouterMainMenu();
                newGame.start(stage);
                stage.show();
                stage.toFront();
                stage.requestFocus();
            }
            catch (final Exception e)
            {
                System.out.println("Cannot launch game." + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Changes the game state and updates the UI accordingly.
     *
     * @param newState the new GameState.
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
     *
     * @param message the text to set.
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
     *
     * @param message the status message.
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
     * Updates the timer display label.
     *
     * @param timer the new time value.
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
     * Updates the level display label to show the current level index.
     *
     * @param num the current level number.
     */
    public void updateLevelDisplay(final int num)
    {
        Platform.runLater(() -> {
            if (levelLabel     != null &&
                gameController != null &&
                gameController.levelSequenceForSession != null)
            {
                final String displayText;
                displayText = LEVEL_LABEL_PREFIX + num + "/" +
                              gameController.levelSequenceForSession.size();

                levelLabel.setText(displayText);
            }
            else if (levelLabel != null)
            {
                levelLabel.setText("L:-/-");
            }
        });
    }

    /**
     * Updates the score display label to show the current score.
     *
     * @param score the current score.
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
     * Updates button states (Start/Stop, Reset, Quit, Next Level) based on simulation and level status.
     *
     * @param isSimRunning  true if the simulation is running.
     * @param levelComplete true if the current level is complete.
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
     * Called by JavaFX to start the application stage.
     *
     * @param primary the primary Stage provided by the JavaFX runtime.
     */
    @Override
    public void start(final Stage primary)
    {
        final Stage stageRef;
        final StackPane rootPane;
        final GameController controller;
        final Scene scene;

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
