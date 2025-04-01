package ca.bcit.comp25222.termproject.resourcerouter.managers;

import ca.bcit.comp25222.termproject.resourcerouter.ResourceRouterMainMenu;
import ca.bcit.comp25222.termproject.resourcerouter.gameplay.GameNode;
import ca.bcit.comp25222.termproject.resourcerouter.gameplay.NodeFactory;
import ca.bcit.comp25222.termproject.resourcerouter.gameplay.Pipe;
import ca.bcit.comp25222.termproject.resourcerouter.gameplay.ProcessorNode;
import ca.bcit.comp25222.termproject.resourcerouter.gameplay.SinkNode;
import ca.bcit.comp25222.termproject.resourcerouter.gameplay.SourceNode;
import ca.bcit.comp25222.termproject.resourcerouter.util.GameState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The GameController class orchestrates the core gameplay simulation for the Resource Router game.
 * <p>
 * This class is responsible for managing the overall game state, including level loading, simulation timing,
 * node and pipe updates, and session scoring. It acts as the central coordinator that ties together the game's
 * various components such as game nodes, pipes, and UI elements.
 * </p>
 * <p>
 * <strong>Key Responsibilities:</strong>
 * <ul>
 *   <li><strong>Level Management:</strong> Loads levels using a {@link LevelLoader} and instantiates game nodes
 *       via the {@link NodeFactory}. It maintains the sequence of levels for a session, handles level transitions,
 *       and resets the level state as necessary.</li>
 *   <li><strong>Simulation Timing:</strong> Drives the game loop using a JavaFX {@link Timeline} that ticks at a fixed
 *       interval (defined by {@value #SIMULATION_TICK_DURATION_MS}). Each tick updates the level timer, processes game nodes,
 *       and checks for win or error conditions.</li>
 *   <li><strong>Node and Pipe Management:</strong> Maintains collections of game nodes, a mapping of node IDs to nodes,
 *       and active pipes. Provides functionality to connect nodes (via pipes), update their visual states, and reset
 *       pipe states when needed.</li>
 *   <li><strong>Scoring:</strong> Calculates level scores based on simulation time and pipe usage (applying a penalty factor),
 *       updates the session score, and prompts the player to enter their name for high score submission when the session ends.</li>
 *   <li><strong>UI Interaction:</strong> Interfaces with a {@link ResourceRouterMainMenu} instance to update visual elements such
 *       as timers, scores, level numbers, and status messages, as well as to prompt for high score entries.</li>
 *   <li><strong>State Transitions:</strong> Manages transitions between game states (e.g., MENU, PLAYING, LEVEL_COMPLETE,
 *       LEVEL_TRANSITION, GAME_OVER) using the {@link GameState} enum, ensuring that the appropriate UI updates occur at
 *       each transition.</li>
 *   <li><strong>Unique ID Management:</strong> Provides methods for setting a unique controller ID, ensuring that nodes
 *       of the same type receive distinct identifiers when necessary.</li>
 * </ul>
 * </p>
 * <p>
 * During each simulation tick, the GameController:
 * <ul>
 *   <li>Decrements the level timer and updates the timer display.</li>
 *   <li>Iterates over all game nodes to invoke their update methods, which allow nodes to process resources (e.g.,
 *       a SinkNode consuming inputs, a ProcessorNode processing recipes, or a SourceNode producing output).</li>
 *   <li>Resets the busy state of pipes and checks for error conditions (such as sink errors) that might require the simulation to stop.</li>
 *   <li>Evaluates win conditions by verifying that all SinkNodes are satisfied, then records the level score and transitions
 *       the game state appropriately.</li>
 * </ul>
 * </p>
 * <p>
 * In summary, the GameController serves as the backbone of the game simulation, ensuring that level progression, node interactions,
 * pipe connections, and user interface updates are synchronized throughout the gameplay session.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class GameController
{

    private static final double SIMULATION_TICK_DURATION_MS = 50.0;
    private static final double CONTROL_OFFSET_MAX          = 20.0;
    private static final double CONTROL_OFFSET_DIVISOR      = 3.0;
    private static final double MIN_TIME_DIVISOR            = 1.0;
    private static final double ZERO_DOUBLE                 = 0.0;
    private static final double PIPE_PENALTY_FACTOR         = 30.0;
    private static final double MIN_SCORE_PER_LEVEL         = 100.0;
    private static final double DELTA_TIME                  = 0.05;

    private static final long BASE_SCORE_PER_LEVEL          = 10000;

    private static final int INITIAL_LEVEL_INDEX            = -1;
    private static final int INITIAL_SUFFIX                 = 2;
    private static final int SECOND_INDEX                   = 1;
    private static final int MAX_OUTGOING_PIPES             = 2;
    private static final int DEFAULT_VALUE                  = 0;

    private static final String DEFAULT_DIALOG_PLAYER       = "Player";
    private static final String SCORE_ZERO_STRING           = " Score=0";

    public static final int TOTAL_LEVEL_FILES_AVAILABLE     = 5;
    public static final int LEVELS_PER_SESSION              = 5;

    private final ResourceRouterMainMenu    resourceRouterMainMenu;
    private final Pane                      gamePane;
    private final List<GameNode>            gameNodes;
    private final Map<String, GameNode>     nodeMap;
    private final List<Pipe>                pipes;
    private final List<Integer>             availableLevelNumbers;

    private GameState       currentGameState = GameState.MENU;
    private Timeline        gameLoop;
    private LevelManager    currentLevelManager;
    private String          id = "";

    private boolean     simulationRunning       = false;
    private double      timeRemaining           = ZERO_DOUBLE;
    private double      simulationTimeElapsed   = ZERO_DOUBLE;
    private int         ticksSincePipeChange    = DEFAULT_VALUE;
    private int         currentSessionScore;

    public List<Integer>    levelSequenceForSession;
    public int              currentLevelIndex;

    public GameController(final ResourceRouterMainMenu resourceRouterMainMenu,
                          final Pane gamePane)
    {
        this.resourceRouterMainMenu = Objects.requireNonNull(resourceRouterMainMenu);
        this.gamePane               = Objects.requireNonNull(gamePane);
        this.gameNodes              = new ArrayList<>();
        this.nodeMap                = new HashMap<>();
        this.pipes                  = new ArrayList<>();
        this.availableLevelNumbers  = identifyAvailableLevels();

        initializeGameLoop();
        this.currentLevelIndex      = INITIAL_LEVEL_INDEX;
    }

    /*
     * Identifies available levels.
     *
     * @return an unmodifiable list of available level numbers.
     */
    private List<Integer> identifyAvailableLevels()
    {
        final List<Integer> levels;
        levels = new ArrayList<>();

        for (int i = 1; i <= TOTAL_LEVEL_FILES_AVAILABLE; i++)
        {
            levels.add(i);
        }

        final List<Integer> result;
        result = Collections.unmodifiableList(levels);

        return result;
    }

    /*
     * Handles game over: stops simulation, prompts for high score entry, resets level state, and changes game state.
     */
    private void handleGameOver()
    {
        stopSimulation("Session complete");

        if (currentSessionScore > DEFAULT_VALUE)
        {
            final TextInputDialog dialog = new TextInputDialog(DEFAULT_DIALOG_PLAYER);
            dialog.setTitle("High Score Entry");
            dialog.setHeaderText("Congratulations! You achieved a high score.");
            dialog.setContentText("Please enter your name:");
            dialog.showAndWait().ifPresent(name -> {
                HighScoreManager.addAndSaveScore(currentSessionScore, name);
            });
        }
        levelSequenceForSession = null;
        currentLevelIndex = INITIAL_LEVEL_INDEX;
        clearLevelState();
        changeGameState(GameState.GAME_OVER);
    }

    /*
     * Changes the current game state.
     *
     * @param newState the new game state.
     */
    private void changeGameState(final GameState newState)
    {
        if (!this.currentGameState.equals(newState))
        {
            this.currentGameState = newState;
            resourceRouterMainMenu.changeGameState(newState);
        }
    }

    /*
     * Initializes the game loop timeline.
     */
    private void initializeGameLoop()
    {
        gameLoop = new Timeline();
        gameLoop.setCycleCount(Animation.INDEFINITE);

        final KeyFrame kf;
        kf = new KeyFrame(Duration.millis(SIMULATION_TICK_DURATION_MS),
        e -> updateSimulationTick());

        gameLoop.getKeyFrames().add(kf);
    }

    /*
     * Updates the simulation on each tick.
     *
     * @param deltaTime the elapsed time in seconds since the last tick.
     */
    private void updateSimulationTick()
    {
        if (!(currentGameState.equals(GameState.PLAYING) ||
              currentGameState.equals(GameState.LEVEL_COMPLETE) ||
              currentGameState.equals(GameState.LEVEL_TRANSITION)))
        {
            if (gameLoop.getStatus() == Animation.Status.RUNNING)
            {
                gameLoop.stop();
            }
            return;
        }
        if (currentLevelManager == null)
        {
            if (gameLoop.getStatus() == Animation.Status.RUNNING)
            {
                gameLoop.stop();
            }
            return;
        }
        if (currentGameState.equals(GameState.PLAYING))
        {
            timeRemaining -= DELTA_TIME;
            resourceRouterMainMenu.updateTimer(timeRemaining);
            if (timeRemaining <= ZERO_DOUBLE)
            {
                final boolean notAllSatisfied;
                notAllSatisfied = gameNodes.stream()
                                           .filter(n -> n instanceof SinkNode)
                                           .map(n -> (SinkNode) n)
                                           .anyMatch(s -> !s.isSatisfied());
                if (notAllSatisfied)
                {
                    final String msg;
                    msg = "Level " + (currentLevelIndex + SECOND_INDEX) +
                          " Failed! (Time Out)";

                    stopSimulation(msg + SCORE_ZERO_STRING);
                    changeGameState(GameState.LEVEL_COMPLETE);
                    return;
                }
            }
        }
        if (simulationRunning || currentGameState.equals(GameState.LEVEL_COMPLETE))
        {
            simulationTimeElapsed += DELTA_TIME;
            pipes.forEach(Pipe::resetTickStatus);

            try
            {
                for (final GameNode node : gameNodes)
                {
                    node.update(DELTA_TIME, this);
                    if (currentGameState.equals(GameState.PLAYING) &&
                       (node instanceof SinkNode sn && sn.isInErrorState()))
                    {
                        stopSimulation("Sink Error (" + node.getId() + ")! Reset pipes (R).");
                        break;
                    }
                }
            } catch (final Exception e)
            {
                handleGameOver();
                return;
            }
            if (currentGameState.equals(GameState.PLAYING))
            {
                checkWinCondition();
            }
        }
    }

    /*
     * Checks if all sinks are satisfied.
     *
     * @return true if all sinks are satisfied; false otherwise.
     */
    private void checkWinCondition()
    {
        if (currentLevelManager == null || !simulationRunning)
        {
            return;
        }

        final boolean allSinksSatisfied;
        allSinksSatisfied = gameNodes.stream()
                                     .filter(n -> n instanceof SinkNode)
                                     .map(n -> (SinkNode) n)
                                     .allMatch(SinkNode::isSatisfied);
        if (allSinksSatisfied)
        {
            recordLevelScore();
            changeGameState(GameState.LEVEL_COMPLETE);
            resourceRouterMainMenu.updateButtonStates(false, true);
        }
    }

    /*
     * Records the level score based on simulation time and pipe penalty.
     */
    private void recordLevelScore()
    {
        if (currentLevelManager == null || simulationTimeElapsed <= ZERO_DOUBLE)
        {
            return;
        }

        final int pipeCount;
        final double t;
        final double penaltyFactor;
        final double rawScore;
        final int finalScore;

        pipeCount       = pipes.size();
        t               = simulationTimeElapsed;
        penaltyFactor   = MIN_TIME_DIVISOR + (pipeCount * PIPE_PENALTY_FACTOR / MIN_SCORE_PER_LEVEL);
        rawScore        = BASE_SCORE_PER_LEVEL / (Math.max(MIN_TIME_DIVISOR, t) * penaltyFactor);
        finalScore      = (int) Math.max(MIN_SCORE_PER_LEVEL, rawScore);

        currentSessionScore += finalScore;

        resourceRouterMainMenu.updateScoreDisplay(currentSessionScore);
    }

    /*
     * Loads a level internally based on the provided level number.
     *
     * @param levelNumber the level number to load.
     * @return true if the level is loaded successfully; false otherwise.
     */
    private boolean loadLevelInternal(final int levelNumber)
    {
        changeGameState(GameState.LOADING);
        clearLevelGraphics();
        clearLevelState();

        final Optional<LevelManager> levelOpt;
        levelOpt = LevelLoader.loadLevel(levelNumber);

        if (levelOpt.isPresent())
        {
            currentLevelManager = levelOpt.get();
            resourceRouterMainMenu.updateLevelDisplay(currentLevelIndex + SECOND_INDEX);
            resourceRouterMainMenu.updatePrompt(currentLevelManager.getPrompt());
            try
            {
                instantiateLevelNodes();
                timeRemaining = currentLevelManager.getTimeLimitSeconds();
                simulationTimeElapsed = ZERO_DOUBLE;
                simulationRunning = false;
                ticksSincePipeChange = DEFAULT_VALUE;
                resourceRouterMainMenu.updateTimer(timeRemaining);
                resourceRouterMainMenu.updateStatus("Level " +
                                                   (currentLevelIndex + SECOND_INDEX) +
                                                    " ready. Connect & start!");
                changeGameState(GameState.PLAYING);
                resourceRouterMainMenu.updateButtonStates(false, false);

                if (gameLoop.getStatus() != Animation.Status.RUNNING)
                {
                    gameLoop.play();
                }
                return true;
            }
            catch (final Exception e)
            {
                System.err.println("Error instantiating level: " + e);
                changeGameState(GameState.MENU);
                return false;
            }
        }
        else
        {
            System.err.println("Failed to load level " + levelNumber);
            changeGameState(GameState.MENU);
            return false;
        }
    }

    /*
     * Instantiates nodes for the current level.
     */
    private void instantiateLevelNodes()
    {
        if (currentLevelManager == null)
        {
            return;
        }
        gameNodes.clear();
        nodeMap.clear();
        pipes.clear();

        final Set<String> usedIds;
        usedIds = new HashSet<>();

        for (final LevelManager.NodeDefinition def : currentLevelManager.getNodeDefinitions())
        {
            final String originalId;
            originalId = def.id();

            String uniqueId;
            uniqueId = originalId;

            int suffix = INITIAL_SUFFIX;
            while (usedIds.contains(uniqueId))
            {
                uniqueId = originalId + "_" + suffix;
                suffix++;
            }
            usedIds.add(uniqueId);

            final LevelManager.NodeDefinition uniqueDef;
            final GameNode gn;

            uniqueDef = new LevelManager.NodeDefinition(
                    def.type(), uniqueId, def.x(), def.y(), def.config());

            gn = NodeFactory.createNode(uniqueDef);

            gameNodes.add(gn);
            nodeMap.put(gn.getId(), gn);

            gn.initializeVisuals();
            gn.addToPane(gamePane);
        }
    }

    /*
     * Clears pipe graphics: removes particle animations and visual elements.
     */
    private void clearPipeGraphics()
    {
        for (final Pipe pipe : pipes)
        {
            pipe.removeAllParticles();
        }
        final List<Node> pipeVisuals;
        pipeVisuals = pipes.stream()
                           .map(Pipe::getCurveVisual)
                           .filter(Objects::nonNull)
                           .collect(Collectors.toList());

        gamePane.getChildren().removeAll(pipeVisuals);
        pipes.forEach(pipe -> pipe.setCurveVisual(null));
    }

    /*
     * Clears the pipe state.
     */
    private void clearPipeState()
    {
        gameNodes.forEach(GameNode::clearPipes);
        pipes.clear();
    }

    /**
     * Clears level state: pipes, nodes, and the current level manager.
     */
    public void clearLevelState()
    {
        clearPipeState();
        gameNodes.clear();
        nodeMap.clear();
        currentLevelManager = null;
    }

    /**
     * Finds the game node associated with a connector visual.
     *
     * @param cv the connector visual.
     * @return the corresponding GameNode, or null if not found.
     */
    private GameNode findNodeByConnectorVisual(final Node cv)
    {
        final String nodeId;
        nodeId = getNodeIdFromConnectorVisual(cv);

        final GameNode result;

        if (nodeId != null)
        {
            result = nodeMap.get(nodeId);
        }
        else
        {
            result = null;
        }
        return result;
    }

    /**
     * Starts a new game session.
     */
    public void startGameSession()
    {
        currentSessionScore = DEFAULT_VALUE;
        resourceRouterMainMenu.updateScoreDisplay(DEFAULT_VALUE);

        if (availableLevelNumbers.isEmpty())
        {
            changeGameState(GameState.MENU);
            return;
        }

        final List<Integer> shuffled;
        shuffled = new ArrayList<>(availableLevelNumbers);
        Collections.shuffle(shuffled);

        final int numLevels;
        numLevels = Math.min(LEVELS_PER_SESSION, shuffled.size());

        if (numLevels == DEFAULT_VALUE)
        {
            changeGameState(GameState.MENU);
            return;
        }
        this.levelSequenceForSession = shuffled.subList(DEFAULT_VALUE, numLevels);
        this.currentLevelIndex = INITIAL_LEVEL_INDEX;
        loadNextLevel();
    }

    /**
     * Loads the next level in the session.
     */
    public void loadNextLevel()
    {
        stopSimulation("Loading next level");
        currentLevelIndex++;

        if (levelSequenceForSession != null && currentLevelIndex < levelSequenceForSession.size())
        {
            final int levelNum;
            final boolean success;

            levelNum = levelSequenceForSession.get(currentLevelIndex);
            success = loadLevelInternal(levelNum);

            if (!success)
            {
                handleGameOver();
            }
        }
        else
        {
            handleGameOver();
        }
    }

    /**
     * Starts the simulation (resource flow) if conditions are met.
     */
    public void startSimulation()
    {
        if (!currentGameState.equals(GameState.PLAYING) ||
                simulationRunning ||
                currentLevelManager == null ||
                timeRemaining <= ZERO_DOUBLE)
        {
            return;
        }

        resourceRouterMainMenu.updateStatus("Simulation Running...");
        simulationRunning = true;
        simulationTimeElapsed = ZERO_DOUBLE;
        ticksSincePipeChange = DEFAULT_VALUE;

        if (gameLoop.getStatus() != Animation.Status.RUNNING)
        {
            gameLoop.play();
        }
        resourceRouterMainMenu.updateButtonStates(true, false);
    }

    /**
     * Stops the simulation.
     *
     * @param reason the reason to display.
     */
    public void stopSimulation(final String reason)
    {
        final boolean wasRunning;
        wasRunning = simulationRunning;
        simulationRunning = false;

        if (wasRunning)
        {
            resourceRouterMainMenu.updateStatus(reason);
        }
        resourceRouterMainMenu.updateButtonStates(false,
                                                  currentGameState.equals(GameState.LEVEL_COMPLETE));

        if (gameLoop.getStatus() == Animation.Status.RUNNING)
        {
            gameLoop.stop();
        }
    }

    /**
     * Resets pipes after a failed attempt.
     */
    public void resetFailedAttemptPipes()
    {
        if (!currentGameState.equals(GameState.PLAYING) ||
                                     simulationRunning)
        {
            if (simulationRunning)
            {
                resourceRouterMainMenu.updateStatus("Cannot reset while simulation is running.");
            }
            return;
        }

        clearPipeGraphics();
        clearPipeState();

        gameNodes.stream()
                .filter(n -> n instanceof ProcessorNode)
                .forEach(GameNode::resetState);

        gameNodes.stream()
                .filter(n -> n instanceof SinkNode)
                .forEach(n -> ((SinkNode) n).clearErrorStateOnly());

        gameNodes.stream()
                .filter(n -> n instanceof SourceNode)
                .forEach(GameNode::resetState);

        ticksSincePipeChange = DEFAULT_VALUE;
        resourceRouterMainMenu.updateStatus("Pipes cleared. Rebuild & SIM!");
        resourceRouterMainMenu.updateButtonStates(false, false);

        if (gameLoop.getStatus() != Animation.Status.RUNNING)
        {
            gameLoop.play();
        }
    }

    /**
     * Clears level graphics.
     */
    public void clearLevelGraphics()
    {
        if (!currentGameState.equals(GameState.LEVEL_COMPLETE))
        {
            clearPipeGraphics();
        }
        gameNodes.forEach(gn -> gn.removeFromPane(gamePane));
    }

    /**
     * Attempts to connect two nodes via their connector visuals.
     *
     * @param startConnectorVisual the output connector visual.
     * @param endConnectorVisual   the input connector visual.
     */
    public void attemptPipeConnection(final Node startConnectorVisual,
                                      final Node endConnectorVisual)
    {
        if (startConnectorVisual    == null ||
            endConnectorVisual      == null ||
            startConnectorVisual    == endConnectorVisual)
        {
            return;
        }

        final GameNode startNode;
        final GameNode endNode;

        startNode   = findNodeByConnectorVisual(startConnectorVisual);
        endNode     = findNodeByConnectorVisual(endConnectorVisual);

        if (startNode == null || endNode == null)
        {
            resourceRouterMainMenu.updateStatus("Connection Error: Node(s) not found");
            return;
        }

        if (startNode == endNode)
        {
            resourceRouterMainMenu.updateStatus("Cannot connect node to itself");
            return;
        }

        if (!(isOutputConnector(startConnectorVisual) && isInputConnector(endConnectorVisual)))
        {
            resourceRouterMainMenu.updateStatus("Invalid direction (Use OUT->IN)");
            return;
        }

        if (endNode instanceof SinkNode)
        {
            if (endNode.getIncomingPipes().size() >= MAX_OUTGOING_PIPES)
            {
                resourceRouterMainMenu.updateStatus("Sink node '" +
                                                            endNode.getId() +
                                                            "' already has two inputs.");
                return;
            }
        }

        final boolean exists;
        exists = pipes.stream().anyMatch(p ->
                     p.getStartNode().getId().equals(startNode.getId()) &&
                     p.getEndNode().getId().equals(endNode.getId()));

        if (exists)
        {
            resourceRouterMainMenu.updateStatus("Pipe already exists.");
            return;
        }
        try
        {
            final Point2D sp;
            final Point2D ep;
            final CubicCurve curve;
            final double distance;
            final double controlOffset;
            final Pipe newPipe;

            sp = startNode.getOutputConnectorCenter();
            ep = endNode.getInputConnectorCenter();

            curve = new CubicCurve();
            curve.setStartX(sp.getX());
            curve.setStartY(sp.getY());
            curve.setEndX(ep.getX());
            curve.setEndY(ep.getY());

            distance = sp.distance(ep);

            controlOffset = Math.min(CONTROL_OFFSET_MAX, distance / CONTROL_OFFSET_DIVISOR);

            curve.setControlX1(sp.getX());
            curve.setControlY1(sp.getY() - controlOffset);
            curve.setControlX2(ep.getX());
            curve.setControlY2(ep.getY() - controlOffset);
            curve.setId(Pipe.PIPE_ID_PREFIX + startNode.getId() + "_" + endNode.getId());
            curve.getStyleClass().add(Pipe.BASE_STYLE_CLASS);
            curve.setMouseTransparent(false);


            newPipe = new Pipe(startNode, endNode);
            newPipe.setCurveVisual(curve);
            pipes.add(newPipe);

            startNode.addOutgoingPipe(newPipe);
            endNode.addIncomingPipe(newPipe);
            gamePane.getChildren().add(curve);
            curve.toBack();
            resourceRouterMainMenu.updateStatus("Pipe created.");

        }
        catch (final Exception ex)
        {
            resourceRouterMainMenu.updateStatus("Pipe Creation Error! " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Removes a pipe based on its line visual.
     *
     * @param lineVis the line visual to remove.
     */
    public void removePipeByVisual(final Line lineVis)
    {
        if (simulationRunning || lineVis == null)
        {
            return;
        }

        final Optional<Pipe> opt;
        opt = pipes.stream().filter(p -> p.getLineVisual() == lineVis).findFirst();

        if (opt.isPresent())
        {
            final Pipe pipe;
            final GameNode sN;
            final GameNode eN;

            pipe = opt.get();
            sN = pipe.getStartNode();
            eN = pipe.getEndNode();

            if (sN != null)
            {
                sN.removeOutgoingPipe(pipe);
            }
            if (eN != null)
            {
                eN.removeIncomingPipe(pipe);
            }

            pipes.remove(pipe);
            gamePane.getChildren().remove(lineVis);
            resourceRouterMainMenu.updateStatus("Pipe removed.");
        }
        else
        {
            resourceRouterMainMenu.updateStatus("Pipe removal error.");
        }
    }

    /**
     * Updates the visual state of the given pipe.
     *
     * @param p the pipe to update.
     */
    public void updatePipeVisual(final Pipe p)
    {
        if (p != null)
        {
            p.updateVisualState();
        }
    }

    /**
     * Returns the game node by its logical ID.
     *
     * @param id the node ID.
     * @return the corresponding GameNode.
     */
    public GameNode getLogicalNodeById(final String id)
    {
        final GameNode result;
        result = nodeMap.get(id);
        return result;
    }

    /**
     * Extracts the node ID from a connector visual.
     *
     * @param cv the connector visual.
     * @return the node ID, or null if not available.
     */
    public String getNodeIdFromConnectorVisual(final Node cv)
    {
        if (cv == null || cv.getId() == null)
        {
            return null;
        }
        final String vid;
        final int idx;

        vid = cv.getId();
        idx = vid.lastIndexOf('-');

        if (idx > 0 && idx < vid.length() - SECOND_INDEX)
        {
            final String result;
            result = vid.substring(idx + SECOND_INDEX);
            return result;
        }
        return null;
    }

    /**
     * Checks if a visual node is a connector.
     *
     * @param fx the visual node.
     * @return true if it is a connector visual; false otherwise.
     */
    public boolean isConnectorVisual(final Node fx)
    {
        if (fx == null || fx.getId() == null)
        {
            return false;
        }

        final boolean result;
        result = fx.getId().startsWith(GameNode.NODE_ID_PREFIX_CONNECTOR_IN) ||
                 fx.getId().startsWith(GameNode.NODE_ID_PREFIX_CONNECTOR_OUT);

        return result;
    }

    /**
     * Checks if the specified game node is output capable.
     *
     * @param n the game node.
     * @return true if the node is a SourceNode or ProcessorNode; false otherwise.
     */
    public boolean isOutputCapableNode(final GameNode n)
    {
        final boolean result;
        result = (n instanceof SourceNode || n instanceof ProcessorNode);

        return result;
    }

    /**
     * Checks if the specified game node is input capable.
     *
     * @param n the game node.
     * @return true if the node is a ProcessorNode or SinkNode; false otherwise.
     */
    public boolean isInputCapableNode(final GameNode n)
    {
        final boolean result;
        result = (n instanceof ProcessorNode || n instanceof SinkNode);

        return result;
    }

    /**
     * Checks if the given node is an output connector.
     *
     * @param fx the node to check.
     * @return true if it is an output connector; false otherwise.
     */
    public boolean isOutputConnector(final Node fx)
    {
        final boolean result;
        result = fx != null && fx.getId() != null &&
                 fx.getId().startsWith(GameNode.NODE_ID_PREFIX_CONNECTOR_OUT);

        return result;
    }

    /**
     * Checks if the given node is an input connector.
     *
     * @param fx the node to check.
     * @return true if it is an input connector; false otherwise.
     */
    public boolean isInputConnector(final Node fx)
    {
        final boolean result;
        result = fx != null && fx.getId() != null &&
                 fx.getId().startsWith(GameNode.NODE_ID_PREFIX_CONNECTOR_IN);

        return result;
    }

    /**
     * Returns whether the simulation is running.
     *
     * @return true if simulation is running; false otherwise.
     */
    public boolean isSimulationRunning()
    {
        return simulationRunning;
    }

    /**
     * Returns the current session score.
     *
     * @return the current session score.
     */
    public int getCurrentSessionScore()
    {
        final int result;
        result = currentSessionScore;

        return result;
    }

    /**
     * Resets the game state by stopping simulation, clearing level graphics and state, and clearing the game pane.
     */
    public void resetGameState()
    {
        stopSimulation("Resetting Game");
        clearLevelGraphics();
        clearLevelState();
        gamePane.getChildren().clear();
    }

    /**
     * Sets a unique ID for this controller.
     * If the provided ID is already in use among the current nodes, a numeric suffix is appended until it is unique.
     *
     * @param newId the candidate ID.
     * @throws IllegalArgumentException if newId is null or empty.
     */
    public void setId(final String newId)
    {
        if (newId == null || newId.trim().isEmpty())
        {
            throw new IllegalArgumentException("New ID cannot be null or empty.");
        }

        final String trimmed;
        String uniqueId;

        trimmed = newId.trim();
        uniqueId = trimmed;

        int suffix = SECOND_INDEX;

        while (nodeMap.containsKey(uniqueId))
        {
            uniqueId = trimmed + "_" + suffix;
            suffix++;
        }
        this.id = uniqueId;
    }

    /**
     * Returns the unique ID for this controller.
     *
     * @return the controller's ID.
     */
    public String getId()
    {
        return id;
    }
}
