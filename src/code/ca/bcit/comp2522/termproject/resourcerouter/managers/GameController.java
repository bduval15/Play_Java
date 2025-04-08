package ca.bcit.comp2522.termproject.resourcerouter.managers;


import ca.bcit.comp2522.termproject.resourcerouter.ResourceRouter;
import ca.bcit.comp2522.termproject.resourcerouter.gameplay.GameNode;
import ca.bcit.comp2522.termproject.resourcerouter.gameplay.NodeFactory;
import ca.bcit.comp2522.termproject.resourcerouter.gameplay.Pipe;
import ca.bcit.comp2522.termproject.resourcerouter.gameplay.ProcessorNode;
import ca.bcit.comp2522.termproject.resourcerouter.gameplay.SinkNode;
import ca.bcit.comp2522.termproject.resourcerouter.gameplay.SourceNode;
import ca.bcit.comp2522.termproject.resourcerouter.util.GameState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
 *       updates the session score.
 *   <li><strong>UI Interaction:</strong> Interfaces with a {@link ResourceRouter} instance to update visual elements such
 *       as timers, scores, level numbers, and status messages.</li>
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
    private static final int TOTAL_LEVEL_FILES_AVAILABLE    = 10;
    private static final int LEVELS_PER_SESSION             = 3;

    private static final String SCORE_ZERO_STRING           = " Score=0";

    private final ResourceRouter            resourceRouter;
    private final Pane                      gamePane;
    private final List<GameNode>            gameNodes;
    private final Map<String, GameNode>     nodeMap;
    private final List<Pipe>                pipes;
    private final List<Integer>             availableLevelNumbers;

    private GameState       currentGameState;
    private Timeline        gameLoop;
    private LevelManager    currentLevelManager;
    private String          nodeId;
    private boolean         simulationActive;
    private double          timeRemaining;
    private double          simulationTimeElapsed;
    private int             ticksSincePipeChange;
    private int             currentSessionScore;
    private int             currentLevelIndex;
    private List<Integer>   levelSequence;

    {
        currentGameState        = GameState.MENU;
        nodeId = "";
        simulationActive        = false;
        timeRemaining           = ZERO_DOUBLE;
        simulationTimeElapsed   = ZERO_DOUBLE;
        ticksSincePipeChange    = DEFAULT_VALUE;
    }

    /**
     * Constructs a new GameController instance, initializing the core simulation structures
     * and linking to the specified UI and rendering pane.
     * <p>
     * This controller will manage all game-related logic, including level transitions, simulation
     * updates, and score tracking, using the provided {@code resourceRouterMainMenu} for UI
     * feedback and the specified {@code gamePane} for visual node placement.
     * </p>
     *
     * @param resourceRouter the main UI manager responsible for state changes and user interaction
     * @param gamePane               the JavaFX pane in which game visuals (nodes, pipes) are rendered
     *
     * @throws NullPointerException if {@code resourceRouterMainMenu} or {@code gamePane} is null
     */
    public GameController(final ResourceRouter resourceRouter,
                          final Pane gamePane)
    {
        validateResourceRouterMainMenu  (resourceRouter);
        validateGamePane                (gamePane);

        this.resourceRouter         = resourceRouter;
        this.gamePane               = gamePane;
        this.gameNodes              = new ArrayList<>();
        this.nodeMap                = new HashMap<>();
        this.pipes                  = new ArrayList<>();
        this.availableLevelNumbers  = identifyAvailableLevels();

        initializeGameLoop();

        this.currentLevelIndex      = INITIAL_LEVEL_INDEX;
    }

    /*
     * Validates that the given ResourceRouter instance (the main menu) is not null.
     *
     * This method uses Objects.requireNonNull to ensure that the ResourceRouter provided
     * is non-null. If it is null, a NullPointerException is thrown with a descriptive message.
     * This check is critical because the ResourceRouter is used later to update UI elements
     * and manage game state transitions.
     *
     * @param menu the ResourceRouter instance to validate
     * @throws NullPointerException if menu is null
     */
    private static void validateResourceRouterMainMenu(final ResourceRouter menu)
    {
        Objects.requireNonNull(menu, "ResourceRouterMainMenu cannot be null");
    }

    /*
     * Validates that the provided game pane is not null.
     *
     * Uses Objects.requireNonNull to ensure the Pane used for rendering game visuals
     * is non-null. If the pane is null, a NullPointerException is thrown with a message.
     * This validation ensures that all graphical elements have a valid parent for display.
     *
     * @param pane the Pane to validate
     * @throws NullPointerException if pane is null
     */
    private static void validateGamePane(final Pane pane)
    {
        Objects.requireNonNull(pane, "GamePane cannot be null");
    }

    /*
     * Identifies and returns an unmodifiable list of available level numbers.
     *
     * Iterates from 1 to the constant TOTAL_LEVEL_FILES_AVAILABLE, adding each integer value
     * to an ArrayList. After population, the list is wrapped using Collections.unmodifiableList
     * to prevent further modifications and then returned.
     *
     * @return an unmodifiable List of level numbers (Integer)
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
     * Handles the game-over scenario.
     *
     * This method stops the simulation by calling stopSimulation with a "Session complete" message.
     * It then resets the level-related state by setting the level sequence to null and reinitializing
     * the current level index. Finally, it clears any level-specific state and transitions the game state
     * to GAME_OVER by calling changeGameState.
     *
     */
    private void handleGameOver()
    {
        stopSimulation("Session complete");

        levelSequence       = null;
        currentLevelIndex   = INITIAL_LEVEL_INDEX;
        clearLevelState();
        changeGameState(GameState.GAME_OVER);
    }

    /*
     * Changes the current game state to the specified new state.
     *
     * Compares the new game state with the current one. If they differ, the method updates the internal
     * game state variable and then calls resourceRouter.changeGameState(newState) to ensure that the UI
     * properly reflects the transition. This method centralizes state changes so that all necessary UI updates occur.
     *
     *
     * @param newState the new GameState to set
     */
    private void changeGameState(final GameState newState)
    {
        if (!this.currentGameState.equals(newState))
        {
            this.currentGameState = newState;
            resourceRouter.changeGameState(newState);
        }
    }

    /*
     * Initializes the simulation game loop.
     *
     * Creates a Timeline that runs indefinitely. Constructs a KeyFrame with a duration specified by
     * SIMULATION_TICK_DURATION_MS. The event handler for the KeyFrame calls updateSimulationTick(), which
     * advances the simulation. This KeyFrame is then added to the Timeline.
     *
     */
    private void initializeGameLoop()
    {
        gameLoop = new Timeline();
        gameLoop.setCycleCount(Animation.INDEFINITE);

        final KeyFrame keyFrame;
        keyFrame = new KeyFrame(Duration.millis(SIMULATION_TICK_DURATION_MS),
                                _ -> updateSimulationTick());

        gameLoop.getKeyFrames().add(keyFrame);
    }

    /*
     * Updates the simulation on each tick.
     *
     * This method is called at each KeyFrame of the Timeline. It performs the following steps:
     *   Checks if the current game state is one that should be updated (i.e. PLAYING, LEVEL_COMPLETE,
     *   or LEVEL_TRANSITION). If not, and if the Timeline is running, it stops the Timeline and returns.
     *   If no current level manager is available, stops the Timeline and returns.
     *
     *   When in PLAYING state, decrements the level timer (timeRemaining) by DELTA_TIME,
     *   and updates the timer display via resourceRouter.updateTimer(timeRemaining).
     *
     *   If the time remaining drops to zero or below, checks whether any SinkNode remains unsatisfied.
     *   If so, stops the simulation with an appropriate failure message and sets the game state to LEVEL_COMPLETE.
     *
     *   If the simulation is running, increments the simulationTimeElapsed by DELTA_TIME and resets the busy flags
     *   on all pipes.
     *
     *   Iterates over all game nodes, calling their update(DELTA_TIME, this) method.
     *
     *   If a SinkNode encounters an error (isErrorState returns true), stops the simulation and breaks out.
     *
     *   If the game state remains PLAYING, calls checkWinCondition() to see if win conditions are met.
     *
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
            resourceRouter.updateTimer(timeRemaining);

            if (timeRemaining <= ZERO_DOUBLE)
            {
                final boolean notAllSatisfied;
                notAllSatisfied = gameNodes.stream()
                                           .filter(n -> n instanceof SinkNode)
                                           .map(n -> (SinkNode) n)
                                           .anyMatch(s -> !s.isSatisfied());
                if (notAllSatisfied)
                {
                    final String message;
                    message = "Level " + (currentLevelIndex + SECOND_INDEX) +
                                          " Failed! (Time Out)";

                    stopSimulation(message + SCORE_ZERO_STRING);
                    changeGameState(GameState.LEVEL_COMPLETE);
                    return;
                }
            }
        }
        if (simulationActive ||
            currentGameState.equals(GameState.LEVEL_COMPLETE))
        {
            simulationTimeElapsed += DELTA_TIME;
            pipes.forEach(Pipe::resetTickStatus);

            try
            {
                for (final GameNode node : gameNodes)
                {
                    node.update(DELTA_TIME, this);

                    if (currentGameState.equals(GameState.PLAYING) &&
                       (node instanceof SinkNode sn && sn.inErrorState()))
                    {
                        stopSimulation("Sink Error (" + node.getNodeId() + ")! Reset pipes (R).");
                        break;
                    }
                }
            }
            catch (final Exception e)
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
     * Checks if all SinkNodes are satisfied.
     *
     * Filters the gameNodes collection for SinkNode instances and verifies that each one meets its demand
     * by invoking its isSatisfied() method. If every SinkNode is satisfied, calls recordLevelScore()
     * to add the level score to the session, transitions the game state to LEVEL_COMPLETE,
     * and updates UI button states.
     *
     */
    private void checkWinCondition()
    {
        if (currentLevelManager == null ||
           !simulationActive)
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
            resourceRouter.updateButtonStates(false, true);
        }
    }

    /*
     * Calculates and records the score for the current level.
     *
     * Computes the level score based on a formula that divides BASE_SCORE_PER_LEVEL by the product of the elapsed
     * simulation time (ensuring a minimum value) and a penalty factor determined by the number of pipes.
     * The final score is guaranteed to be no less than MIN_SCORE_PER_LEVEL.
     * This score is added to currentSessionScore, and the UI is updated with both the new score
     * display and a prompt message.
     *
     */
    private void recordLevelScore()
    {
        if (currentLevelManager == null ||
            simulationTimeElapsed <= ZERO_DOUBLE)
        {
            return;
        }

        final int       pipeCount;
        final double    timeElapsed;
        final double    penaltyFactor;
        final double    rawScore;
        final int       finalScore;

        pipeCount       = pipes.size();
        timeElapsed     = simulationTimeElapsed;
        penaltyFactor   = MIN_TIME_DIVISOR + (pipeCount * PIPE_PENALTY_FACTOR / MIN_SCORE_PER_LEVEL);
        rawScore        = BASE_SCORE_PER_LEVEL / (Math.max(MIN_TIME_DIVISOR, timeElapsed) * penaltyFactor);
        finalScore      = (int) Math.max(MIN_SCORE_PER_LEVEL, rawScore);

        currentSessionScore += finalScore;

        resourceRouter.updateScoreDisplay(currentSessionScore);

        resourceRouter.updatePrompt(
                "You beat the level and gained " +
                finalScore +
                " points! Proceed to the next level!"
        );
    }

    /*
     * Loads a level internally based on the provided level number.
     *
     * The method performs the following steps:
     *
     * Changes the game state to LOADING and clears any existing level graphics and state.
     * Attempts to load the level by calling LevelLoader.loadLevel(levelNumber). If successful, sets the
     * currentLevelManager, updates the level display and prompt via resourceRouter, and instantiates level nodes.
     * Initializes level-specific timers (timeRemaining, simulationTimeElapsed) and simulation flags.
     * Updates UI elements accordingly and starts the game loop if not already running.
     * If the level fails to load, resets the game state to MENU and returns false.
     *
     * @param levelNumber the level to load
     * @return true if the level loads successfully; false otherwise
     *
     */
    private boolean loadLevelInternal(final int levelNumber)
    {
        changeGameState(GameState.LOADING);
        clearLevelGraphics();
        clearLevelState();

        final LevelManager levelManager;
        levelManager = LevelLoader.loadLevel(levelNumber);

        if (levelManager != null)
        {
            currentLevelManager = levelManager;
            resourceRouter.updateLevelDisplay(currentLevelIndex + SECOND_INDEX);
            resourceRouter.updatePrompt(currentLevelManager.getPrompt());

            try
            {
                instantiateLevelNodes();
                timeRemaining           = currentLevelManager.getTimeLimitSeconds();
                simulationTimeElapsed   = ZERO_DOUBLE;
                simulationActive        = false;
                ticksSincePipeChange    = DEFAULT_VALUE;

                resourceRouter.updateTimer(timeRemaining);
                resourceRouter.updateStatus("Level " +
                                                   (currentLevelIndex + SECOND_INDEX) +
                                                    " ready. Connect & start!");
                changeGameState(GameState.PLAYING);
                resourceRouter.updateButtonStates(false,
                                                  false);

                if (gameLoop.getStatus() != Animation.Status.RUNNING)
                {
                    gameLoop.play();
                }
                return true;
            }
            catch (final Exception e)
            {
                changeGameState(GameState.MENU);
                return false;
            }
        }
        else
        {
            changeGameState(GameState.MENU);
            return false;
        }
    }

    /*
     * Instantiates game nodes for the current level.
     *
     * Clears any existing game nodes, the node mapping, and pipe list. Then iterates through each NodeDefinition
     * provided by the currentLevelManager.
     *
     * For each node definition:
     * Ensures the node ID is unique by appending a numeric suffix if necessary.
     * Creates a new NodeDefinition with the unique ID.
     * Constructs a GameNode via NodeFactory.createNode(uniqueDefinition).
     * Adds the created node to the gameNodes list and maps it in nodeMap.
     * Initializes the node’s visuals by calling initializeVisuals() and adds them to the gamePane.
     *
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
            originalId = def.getNodeId();

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
                    def.getNodeType(),
                    uniqueId,
                    def.getXCoordinate(),
                    def.getYCoordinate(),
                    def.getConfiguration());

            gn = NodeFactory.createNode(uniqueDef);

            gameNodes.add(gn);
            nodeMap.put(gn.getNodeId(), gn);

            gn.initializeVisuals();
            gn.addToPane(gamePane);
        }
    }

    /*
     * Clears all pipe graphics from the game.
     *
     * Iterates over all pipes, and for each pipe:
     * Calls removeAllParticles() to remove active resource particle animations.
     * Collects each pipe's line visual (if non-null and a Node) and removes them from the gamePane.
     * Sets the line visual reference for each pipe to null.
     *
     */
    private void clearPipeGraphics()
    {
        for (final Pipe pipe : pipes)
        {
            pipe.removeAllParticles();
        }

        final List<Node> pipeVisuals;
        pipeVisuals = pipes.stream()
                            .map(Pipe::getLineVisual)
                            .filter(Objects::nonNull)
                            .filter(Node.class::isInstance)
                            .map(Node.class::cast)
                            .toList();

        gamePane.getChildren().removeAll(pipeVisuals);
        pipes.forEach(pipe -> pipe.setLineVisual(null));
    }

    /*
     * Clears the state of all pipes.
     *
     * Invokes clearPipes() on every GameNode to disconnect any associated pipes, then clears the internal list
     * of pipes maintained by the GameController.
     *
     */
    private void clearPipeState()
    {
        gameNodes.forEach(GameNode::clearPipes);
        pipes.clear();
    }

    /*
     * Finds a GameNode corresponding to a given connector visual.
     *
     * Extracts the node ID from the provided connector visual (using the visual’s ID attribute)
     * via getNodeIdFromConnectorVisual().
     * Then, uses the extracted ID to look up and return the corresponding GameNode from the internal nodeMap.
     * If the connector visual or the node ID is null, or if no matching node is found, returns null.
     *
     *
     * @param connectorVisual the connector visual from which to extract the node ID
     * @return the associated GameNode, or null if not found
     */
    private GameNode findNodeByConnectorVisual(final Node connectorVisual)
    {
        final String nodeId;
        nodeId = getNodeIdFromConnectorVisual(connectorVisual);

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
     * Clears the current level state.
     * <p>
     * Removes all pipes, clears the list of game nodes and node mapping, and sets the current level manager to null.
     * This method resets level-specific state, so the controller is ready to load a new level.
     * </p>
     */
    public void clearLevelState()
    {
        clearPipeState();
        gameNodes.clear();
        nodeMap.clear();
        currentLevelManager = null;
    }

    /**
     * Starts a new game session.
     * <p>
     * Resets the session score to zero and updates the score display. If available level numbers exist, shuffles them,
     * selects up to the defined number of levels per session, sets these as the level sequence,
     * resets the current level index, and then loads the first level.
     * If no levels are available, sets the game state to MENU.
     * </p>
     */
    public void startGameSession()
    {
        currentSessionScore = DEFAULT_VALUE;
        resourceRouter.updateScoreDisplay(DEFAULT_VALUE);

        if (availableLevelNumbers.isEmpty())
        {
            changeGameState(GameState.MENU);
            return;
        }

        final List<Integer> shuffled;
        final int           numLevels;

        shuffled = new ArrayList<>(availableLevelNumbers);
        Collections.shuffle(shuffled);

        numLevels = Math.min(LEVELS_PER_SESSION, shuffled.size());

        if (numLevels == DEFAULT_VALUE)
        {
            changeGameState(GameState.MENU);
            return;
        }
        this.levelSequence      = shuffled.subList(DEFAULT_VALUE,
                                                   numLevels);
        this.currentLevelIndex  = INITIAL_LEVEL_INDEX;

        loadNextLevel();
    }

    /**
     * Loads the next level in the session.
     * <p>
     * Increments the current level index and, if the level sequence contains a level corresponding to the new index,
     * stops the current simulation and attempts to load the level using an internal level-loading method.
     * If the level loads successfully, the game continues; otherwise, it handles the game over sequence.
     * </p>
     */
    public void loadNextLevel()
    {
        stopSimulation("Loading next level");
        currentLevelIndex++;

        if (levelSequence != null &&
            currentLevelIndex < levelSequence.size())
        {
            final int       levelNum;
            final boolean   success;

            levelNum    = levelSequence.get(currentLevelIndex);
            success     = loadLevelInternal(levelNum);

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
     * Starts the simulation if conditions are met.
     *
     * <p>
     * Conditions:
     * <ul>
     *   <li>Game state is {@code GameState.PLAYING}</li>
     *   <li>Simulation is not already active</li>
     *   <li>A valid level is loaded and time remaining is positive</li>
     * </ul>
     * If all conditions pass, the method updates the status, resets timers, starts the game loop if necessary,
     * and updates the UI button states.
     * </p>
     */
    public void startSimulation()
    {
        if (!currentGameState.equals(GameState.PLAYING) ||
            simulationActive ||
            currentLevelManager == null ||
            timeRemaining <= ZERO_DOUBLE)
        {
            return;
        }

        resourceRouter.updateStatus("Simulation Running...");

        simulationActive        = true;
        simulationTimeElapsed   = ZERO_DOUBLE;
        ticksSincePipeChange    = DEFAULT_VALUE;

        if (gameLoop.getStatus() != Animation.Status.RUNNING)
        {
            gameLoop.play();
        }
        resourceRouter.updateButtonStates(true,
                                          false);
    }

    /**
     * Stops the simulation.
     * <p>
     * Sets the simulationRunning flag to false. If the simulation was active, updates the UI with the provided reason.
     * Updates button states based on whether the level is complete, and stops the game loop if it is currently running.
     * </p>
     *
     * @param reason a message explaining why the simulation is being stopped
     */
    public void stopSimulation(final String reason)
    {
        final boolean wasRunning;
        wasRunning = simulationActive;
        simulationActive = false;

        if (wasRunning)
        {
            resourceRouter.updateStatus(reason);
        }
        resourceRouter.updateButtonStates(false,
                                          currentGameState.equals(GameState.LEVEL_COMPLETE));

        if (gameLoop.getStatus() == Animation.Status.RUNNING)
        {
            gameLoop.stop();
        }
    }

    /**
     * Resets pipes after a failed attempt.
     * <p>
     * If the simulation is not running, clears all pipe graphics and state by calling
     * methods to remove particles and disconnect pipes, resets each GameNode’s state
     * (for processor, sink, and source nodes),
     * resets the tick counter, updates the status prompt and button states, and ensures the game loop is playing.
     * </p>
     */
    public void resetFailedAttemptPipes()
    {
        if (!currentGameState.equals(GameState.PLAYING) ||
                simulationActive)
        {
            if (simulationActive)
            {
                resourceRouter.updateStatus("Cannot reset while simulation is running.");
            }
            return;
        }

        clearPipeGraphics();
        clearPipeState();

        gameNodes.stream()
                .filter(node -> node instanceof ProcessorNode)
                .forEach(GameNode::resetState);

        gameNodes.stream()
                .filter(node -> node instanceof SinkNode)
                .forEach(node -> ((SinkNode) node).clearErrorStateOnly());

        gameNodes.stream()
                .filter(node -> node instanceof SourceNode)
                .forEach(GameNode::resetState);

        ticksSincePipeChange = DEFAULT_VALUE;
        resourceRouter.updateStatus("Pipes cleared. Rebuild & SIM!");
        resourceRouter.updateButtonStates(false, false);

        if (gameLoop.getStatus() != Animation.Status.RUNNING)
        {
            gameLoop.play();
        }
    }

    /**
     * Clears all level graphics.
     * <p>
     * If the current game state is not LEVEL_COMPLETE,
     * clears pipe graphics by removing all pipe-related visual elements;
     * then, for every game node, removes its visual components from the game pane.
     * </p>
     */
    public void clearLevelGraphics()
    {
        if (!currentGameState.equals(GameState.LEVEL_COMPLETE))
        {
            clearPipeGraphics();
        }
        gameNodes.forEach(gameNode -> gameNode.removeFromPane(gamePane));
    }

    /**
     * Attempts to establish a pipe connection between two nodes based on their visual connector representations.
     *
     * <p>
     * The method first validates the following conditions:
     * <ul>
     *   <li>Neither {@code startConnectorVisual} nor {@code endConnectorVisual} is {@code null}.</li>
     *   <li>The two connector visuals are distinct.</li>
     *   <li>Corresponding game nodes for both visuals can be found via {@code findNodeByConnectorVisual()}.
     *       If either node is not found, the connection is aborted and a status message is displayed.
     *   </li>
     *   <li>The game nodes must be different; connecting a node to itself is not allowed.</li>
     *   <li>
     *       The connectors must have the correct direction:
     *       {@code startConnectorVisual} must be an output connector (verified by {@code isOutputConnector()}) and
     *       {@code endConnectorVisual} must be an input connector (verified by {@code isInputConnector()}).
     *   </li>
     *   <li>
     *       If the {@code endNode} is a sink node (an instance of {@code SinkNode}),
     *       it should not exceed its limit of incoming pipes
     *       (less than {@code MAX_OUTGOING_PIPES} allowed).
     *   </li>
     *   <li>
     *       A check is performed to ensure that a pipe connection between these specific nodes does not already exist.
     *   </li>
     * </ul>
     * </p>
     *
     * <p>
     * If any of the above validations fail, the method updates the status via {@code resourceRouter.updateStatus()}
     * with an appropriate error message and terminates the connection attempt.
     * </p>
     *
     * <p>
     * Upon successful validation, the following steps are executed:
     * <ol>
     *   <li>Calculate the center positions of the output connector from {@code startNode}
     *   and the input connector from {@code endNode}.</li>
     *   <li>Create a visual {@code Line} representing the pipe, setting its start and end
     *   coordinates to these calculated positions.</li>
     *   <li>Assign the line a unique identifier and apply the appropriate style class.</li>
     *   <li>Instantiate a new {@link Pipe} object that encapsulates the connection between
     *   {@code startNode} and {@code endNode}.</li>
     *   <li>Add the new pipe to the internal collection of pipes and associate it with the start
     *   and end nodes using their respective methods.</li>
     *   <li>Add the line to the game pane for rendering, ensuring it is positioned behind other UI elements.</li>
     *   <li>Update the status to indicate successful pipe creation.</li>
     * </ol>
     * </p>
     *
     * <p>
     * In case an exception occurs during the pipe creation process,
     * the method catches the exception, updates the status
     * with an error message including the exception details, and prints the stack trace for debugging purposes.
     * </p>
     *
     * @param startConnectorVisual the visual representation of the starting node's connector
     *                             (expected to be an output connector)
     * @param endConnectorVisual   the visual representation of the ending node's connector
     *                             (expected to be an input connector)
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
            resourceRouter.updateStatus("Connection Error: Node(s) not found");
            return;
        }

        if (startNode == endNode)
        {
            resourceRouter.updateStatus("Cannot connect node to itself");
            return;
        }

        if (!(isOutputConnector(startConnectorVisual) &&
              isInputConnector(endConnectorVisual)))
        {
            resourceRouter.updateStatus("Invalid direction (Use OUT->IN)");
            return;
        }

        if (endNode instanceof SinkNode)
        {
            if (endNode.getIncomingPipes().size() >= MAX_OUTGOING_PIPES)
            {
                resourceRouter.updateStatus("Sink node '" +
                                            endNode.getNodeId() +
                                            "' already has two inputs.");
                return;
            }
        }

        final boolean exists;
        exists = pipes.stream().anyMatch(p ->
                     p.getStartNode().getNodeId().equals(startNode.getNodeId()) &&
                     p.getEndNode().getNodeId().equals(endNode.getNodeId()));

        if (exists)
        {
            resourceRouter.updateStatus("Pipe already exists.");
            return;
        }
        try
        {
            final Point2D   startPoint;
            final Point2D   endPoint;
            final Line      line;
            final Pipe      newPipe;

            startPoint  = startNode.getOutputConnectorCenter();
            endPoint    = endNode.getInputConnectorCenter();

            line = new Line();
            line.setStartX(startPoint.getX());
            line.setStartY(startPoint.getY());
            line.setEndX(endPoint.getX());
            line.setEndY(endPoint.getY());

            startPoint.distance(endPoint);

            line.setId(Pipe.getPipeIdPrefix() +
                               startNode.getNodeId() +
                               "_" +
                               endNode.getNodeId());
            line.getStyleClass().add(Pipe.getPipeStyle());
            line.setMouseTransparent(false);

            newPipe = new Pipe(startNode,
                               endNode);
            newPipe.setLineVisual(line);
            pipes.add(newPipe);

            startNode.addOutgoingPipe(newPipe);
            endNode.addIncomingPipe(newPipe);
            gamePane.getChildren().add(line);
            line.toBack();
            resourceRouter.updateStatus("Pipe created.");

        }
        catch (final Exception ex)
        {
            resourceRouter.updateStatus("Pipe Creation Error! " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Removes a pipe using its visual representation.
     * <p>
     * If the simulation is not active and the provided line visual is non-null,
     * searches the internal collection for a Pipe whose visual matches the provided one.
     * If found, removes the pipe from the associated start and end nodes, from the internal
     * list, and from the game pane. Updates the UI status with a success or error message.
     * </p>
     *
     * @param lineVis the Line visual representing the pipe to remove
     */
    public void removePipeByVisual(final Line lineVis)
    {
        if (simulationActive || lineVis == null)
        {
            return;
        }

        final Pipe pipe;
        pipe = pipes.stream()
                    .filter(pipe1 -> pipe1.getLineVisual() == lineVis)
                    .findFirst()
                    .orElse(null);

        if (pipe != null)
        {
            final GameNode startNode;
            final GameNode endNode;

            startNode   = pipe.getStartNode();
            endNode     = pipe.getEndNode();

            if (startNode != null)
            {
                startNode.removeOutgoingPipe(pipe);
            }
            if (endNode != null)
            {
                endNode.removeIncomingPipe(pipe);
            }

            pipes.remove(pipe);
            gamePane.getChildren().remove(lineVis);
            resourceRouter.updateStatus("Pipe removed.");
        }
        else
        {
            resourceRouter.updateStatus("Pipe removal error.");
        }
    }

    /**
     * Updates the visual state of the specified pipe.
     * <pipe>
     * If the given Pipe is not null, calls its updateVisualState() method to
     * refresh its appearance based on its current state.
     * </pipe>
     *
     * @param pipe the Pipe whose visual state is to be updated
     */
    public void updatePipeVisual(final Pipe pipe)
    {
        if (pipe != null)
        {
            pipe.updateVisualState();
        }
    }

    /**
     * Retrieves a GameNode by its logical identifier.
     * <p>
     * Looks up the internal node mapping (nodeMap) using the provided nodeId and returns the corresponding GameNode.
     * </p>
     *
     * @param nodeId the unique identifier of the node
     * @return the GameNode associated with the given nodeId, or null if not found
     */
    public GameNode getLogicalNodeById(final String nodeId)
    {
        final GameNode result;
        result = nodeMap.get(nodeId);

        return result;
    }

    /**
     * Extracts the node ID from a connector visual.
     * <p>
     * If the given connector visual and its ID attribute are non-null, this method locates the
     * last occurrence of '-' in the ID and returns the substring that follows.
     * If the extraction fails or the visual is null, returns null.
     * </p>
     *
     * @param connectorVisual the connector visual node
     * @return the extracted node ID, or null if extraction is unsuccessful
     */
    public String getNodeIdFromConnectorVisual(final Node connectorVisual)
    {
        if (connectorVisual == null || connectorVisual.getId() == null)
        {
            return null;
        }

        final String nodeIdVisual;
        final int nodeIndex;

        nodeIdVisual    = connectorVisual.getId();
        nodeIndex       = nodeIdVisual.lastIndexOf('-');

        if (nodeIndex > DEFAULT_VALUE &&
            nodeIndex < nodeIdVisual.length() - SECOND_INDEX)
        {
            final String result;
            result = nodeIdVisual.substring(nodeIndex +
                                            SECOND_INDEX);
            return result;
        }
        return null;
    }

    /**
     * Determines whether the specified visual node is a connector.
     * <p>
     * Checks that the node and its ID are non-null, and returns true if the
     * ID starts with either the designated input or output connector prefixes.
     * </p>
     *
     * @param checkNode the visual node to check
     * @return true if the node is a connector visual; false otherwise
     */
    public boolean isConnectorVisual(final Node checkNode)
    {
        if (checkNode == null ||
            checkNode.getId() == null)
        {
            return false;
        }

        final boolean result;

        result = checkNode.getId().startsWith(GameNode.getNodeIdPrefixConnectorIn()) ||
                 checkNode.getId().startsWith(GameNode.getNodeIdPrefixConnectorOut());

        return result;
    }

    /**
     * Checks if the provided GameNode is output capable.
     * <p>
     * Returns true if the node is an instance of either SourceNode or ProcessorNode,
     * indicating that it can produce resources.
     * </p>
     *
     * @param gameNode the GameNode to evaluate
     * @return true if the node can output resources; false otherwise
     */
    public boolean isOutputCapableNode(final GameNode gameNode)
    {
        final boolean result;

        result = (gameNode instanceof SourceNode ||
                  gameNode instanceof ProcessorNode);

        return result;
    }


    /**
     * Checks if the provided GameNode is input capable.
     * <p>
     * Returns true if the node is an instance of either ProcessorNode or SinkNode,
     * indicating that it can receive resources.
     * </p>
     *
     * @param gameNode the GameNode to evaluate
     * @return true if the node can accept resources; false otherwise
     */
    public boolean isInputCapableNode(final GameNode gameNode)
    {
        final boolean result;

        result = (gameNode instanceof ProcessorNode ||
                  gameNode instanceof SinkNode);

        return result;
    }

    /**
     * Determines whether the given visual node is an output connector.
     * <p>
     * Validates that the node and its ID are non-null, then checks if the ID starts with the output connector prefix
     * (obtained via GameNode.getNodeIdPrefixConnectorOut()).
     * Returns true if these conditions are met.
     * </p>
     *
     * @param nodeCheck the connector visual to check
     * @return true if it is an output connector; false otherwise
     */
    public boolean isOutputConnector(final Node nodeCheck)
    {
        final boolean result;
        result = nodeCheck != null &&
                 nodeCheck.getId() != null &&
                 nodeCheck.getId().startsWith(GameNode.getNodeIdPrefixConnectorOut());

        return result;
    }

    /**
     * Determines whether the given visual node is an input connector.
     * <p>
     * Validates that the node and its ID are non-null, then checks if the ID starts with the input connector prefix
     * (obtained via GameNode.getNodeIdPrefixConnectorIn()). Returns true if these conditions are met.
     * </p>
     *
     * @param nodeCheck the connector visual to check
     * @return true if it is an input connector; false otherwise
     */
    public boolean isInputConnector(final Node nodeCheck)
    {
        final boolean result;
        result = nodeCheck != null &&
                 nodeCheck.getId() != null &&
                 nodeCheck.getId().startsWith(GameNode.getNodeIdPrefixConnectorIn());

        return result;
    }


    /**
     * Returns whether the simulation is currently running.
     * <p>
     * Simply returns the state of the internal simulationRunning flag.
     * </p>
     *
     * @return true if the simulation is active; false otherwise
     */
    public boolean isSimulationActive()
    {
        return simulationActive;
    }

    /**
     * Resets the overall game state.
     * <p>
     * Stops the simulation, clears level graphics and state, and clears all game node visuals from the game pane,
     * effectively resetting the controller to a fresh state.
     * </p>
     */
    public void resetGameState()
    {
        stopSimulation("Resetting Game");
        clearLevelGraphics();
        clearLevelState();
        gamePane.getChildren().clear();
    }

    /**
     * Returns the current session score.
     * <p>
     * Provides the cumulative score for the current game session, as maintained by the controller.
     * </p>
     *
     * @return an integer representing the current session score
     */
    public int getCurrentSessionScore()
    {
        final int result;
        result = currentSessionScore;

        return result;
    }

    /**
     * Returns the level sequence for the current session.
     * <p>
     * Provides an unmodifiable list of level numbers that indicates the order in
     * which levels will be played during the session.
     * </p>
     *
     * @return a List of Integer representing the level sequence
     */
    public List<Integer> getLevelSequence()
    {
        return levelSequence;
    }
}
