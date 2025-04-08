package ca.bcit.comp2522.termproject.resourcerouter.managers;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * LevelManager encapsulates the configuration and definitions for a single level in the Resource Router game.
 * <p>
 * Each LevelManager instance represents a level and contains:
 * <ul>
 *   <li>A positive integer level number that uniquely identifies the level.</li>
 *   <li>An unmodifiable list of node definitions that specify the types, positions, and additional configuration
 *       for each game node (e.g., sources, processors, sinks) used in the level.</li>
 *   <li>A time limit in seconds, which defines the maximum playable duration for the level. If not specified,
 *       a default time limit of {@value #DEFAULT_TIME_LIMIT_SECONDS} seconds is used.</li>
 *   <li>An optional prompt string that can provide instructions, narrative, or other level-specific information
 *       to the player.</li>
 * </ul>
 * </p>
 * <p>
 * The class provides two constructors:
 * <ul>
 *   <li>
 *     The primary constructor accepts a level number, a list of node definitions, a time limit, and a prompt.
 *     It validates that the level number and time limit are positive, throwing an {@code IllegalArgumentException}
 *     if these conditions are not met.
 *   </li>
 *   <li>
 *     The secondary constructor requires only the level number and node definitions, defaulting the time limit to
 *     {@value #DEFAULT_TIME_LIMIT_SECONDS} and the prompt to an empty string.
 *   </li>
 * </ul>
 * </p>
 * <p>
 * LevelManager also defines a nested immutable class, {@link NodeDefinition},
 * which represents the definition of an individual
 * game node within the level. Each NodeDefinition contains:
 * <ul>
 *   <li>The node type (for example, "SOURCE", "PROCESSOR", or "SINK").</li>
 *   <li>A unique identifier for the node.</li>
 *   <li>The x and y coordinates that specify the node's position on the game board.</li>
 *   <li>An optional configuration string that may include additional parameters or
 *   settings specific to the node type.</li>
 * </ul>
 * </p>
 * <p>
 * In summary, LevelManager serves as a fundamental component of the game’s level loading mechanism by converting
 * raw level data into structured configurations that can be used to instantiate and initialize game nodes.
 * This ensures that each level is defined consistently and can be easily managed throughout a game session.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class LevelManager
{

    private static final int    LOWER_BOUND_LIMIT            = 0;
    private static final int    MAX_X_VALUE                  = 900;
    private static final int    MAX_Y_VALUE                  = 600;
    public static final double  DEFAULT_TIME_LIMIT_SECONDS   = 60.0;

    private final List<NodeDefinition>  nodeDefinitions;
    private final double                timeLimitSeconds;
    private final String                prompt;


    /**
     * Constructs a LevelManager with the specified level number, node definitions,
     * time limit, and prompt.
     * <p>
     * The constructor validates that the level number is greater than zero and that the
     * time limit is positive; if not, an IllegalArgumentException is thrown. The list of
     * node definitions is copied into a new ArrayList and then wrapped in an unmodifiable
     * list to prevent external modification.
     * </p>
     *
     * @param levelNumber      the level number (must be positive)
     * @param nodeDefinitions  a list of node definitions that describe the game nodes
     * @param timeLimitSeconds the level’s time limit in seconds (must be positive)
     * @param prompt           a prompt string for the level (can be empty)
     *
     * @throws IllegalArgumentException if the level number or time limit is not positive
     */
    public LevelManager(final int levelNumber,
                        final List<NodeDefinition> nodeDefinitions,
                        final double timeLimitSeconds,
                        final String prompt)
    {

        validateLevelNumber(levelNumber);
        validateTimeLimitSeconds(timeLimitSeconds);

        final List<NodeDefinition> tempList;
        tempList = new ArrayList<>(nodeDefinitions);

        this.nodeDefinitions    = Collections.unmodifiableList(tempList);
        this.timeLimitSeconds   = timeLimitSeconds;
        this.prompt             = prompt;
    }

    /*
     * Validates that the provided level number is positive.
     * <p>
     * Throws an IllegalArgumentException if the level number is zero or negative.
     * </p>
     *
     * @param levelNumber the level number to validate
     * @throws IllegalArgumentException if levelNumber is not positive
     *
     */
    private void validateLevelNumber(final int levelNumber)
    {
        if (levelNumber <= LOWER_BOUND_LIMIT)
        {
            throw new IllegalArgumentException("Level number must be positive.");
        }
    }

    /*
     * Validates that the time limit is positive.
     * <p>
     * Throws an IllegalArgumentException if the provided time limit is zero or negative.
     * </p>
     *
     * @param timeLimitSeconds the time limit in seconds to validate
     * @throws IllegalArgumentException if timeLimitSeconds is not positive
     *
     */
    private void validateTimeLimitSeconds(final double timeLimitSeconds)
    {
        if (timeLimitSeconds <= LOWER_BOUND_LIMIT)
        {
            throw new IllegalArgumentException("Time limit must be positive.");
        }
    }

    /**
     * Returns an unmodifiable list of the node definitions for this level.
     * <p>
     * The returned list contains NodeDefinition objects that describe the nodes
     * (their type, ID, position, and configuration) used in the level.
     * </p>
     *
     * @return an unmodifiable List of NodeDefinition objects
     */
    public List<NodeDefinition> getNodeDefinitions()
    {
        final List<NodeDefinition> result;
        result = nodeDefinitions;

        return result;
    }

    /**
     * Returns the time limit for the level in seconds.
     * <p>
     * This time limit governs the maximum playable duration for the level.
     * </p>
     *
     * @return a double representing the level's time limit in seconds
     */
    public double getTimeLimitSeconds()
    {
        final double result;
        result = timeLimitSeconds;

        return result;
    }

    /**
     * Returns the prompt string for the level.
     * <p>
     * The prompt can provide instructions or narrative details to the player.
     * </p>
     *
     * @return a String containing the level prompt (or an empty string if not set)
     */
    public String getPrompt()
    {
        final String result;
        result = prompt;

        return result;
    }

    /**
     * Returns the default time limit value for levels.
     * <p>
     * This value is used as a fallback if a level does not specify a time limit.
     * </p>
     *
     * @return the default time limit in seconds
     */
    public static double getDefaultTime()
    {
        return DEFAULT_TIME_LIMIT_SECONDS;
    }

    /**
     * NodeDefinition is a static nested class that encapsulates the definition of a single game node.
     * <p>
     * A NodeDefinition includes:
     * <ul>
     *   <li>The node type (for example, "SOURCE", "PROCESSOR", or "SINK").</li>
     *   <li>A unique identifier for the node.</li>
     *   <li>The x and y coordinates specifying the node's position on the game board.</li>
     *   <li>An optional configuration string that may contain additional parameters or settings.</li>
     * </ul>
     * <p>
     * NodeDefinition objects are immutable; once created, their fields cannot be changed.
     * The constructor validates that the type and ID are non-null and not blank, and that the
     * coordinates are within acceptable limits. If validation fails, an IllegalArgumentException
     * is thrown.
     * </p>
     */
    public static final class NodeDefinition
    {
        private final String nodeType;
        private final String nodeId;
        private final double xCoordinate;
        private final double yCoordinate;
        private final String configuration;

        /**
         * Constructs a NodeDefinition with the specified node attributes.
         * <p>
         * The constructor validates that the node nodeType and ID are not null or blank, and that
         * the x and y coordinates are finite and within predefined bounds. If any validation fails,
         * an IllegalArgumentException is thrown.
         * </p>
         *
         * @param nodeType      the node nodeType (e.g., "SOURCE", "PROCESSOR", "SINK"); must not be null or blank
         * @param nodeId     the unique identifier for the node; must not be null or blank
         * @param xCoordinate      the x-coordinate of the node's center
         * @param yCoordinate      the y-coordinate of the node's center
         * @param configuration an optional configuration string; may be null or empty
         * @throws IllegalArgumentException if nodeType or id is null/blank, or if x or y is invalid
         *
         */
        public NodeDefinition(final String nodeType,
                              final String nodeId,
                              final double xCoordinate,
                              final double yCoordinate,
                              final String configuration)
        {
            validateType(nodeType);
            validateId(nodeId);
            validateCoordinates(xCoordinate, yCoordinate);

            this.nodeType       = nodeType;
            this.nodeId         = nodeId;
            this.xCoordinate    = xCoordinate;
            this.yCoordinate    = yCoordinate;
            this.configuration  = configuration;
        }

        /*
         * Validates that 'nodeType' is neither null nor empty.
         */
        private static void validateType(final String nodeType)
        {
            if (nodeType == null ||
                nodeType.isBlank())
            {
                throw new IllegalArgumentException(
                        "NodeDefinition 'nodeType' cannot be null/blank.");
            }
        }

        /*
         * Validates that 'nodeId' is neither null nor empty.
         */
        private static void validateId(final String nodeId)
        {
            if (nodeId == null ||
                nodeId.isBlank())
            {
                throw new IllegalArgumentException(
                        "NodeDefinition 'nodeId' cannot be null/blank.");
            }
        }

        /*
         * Validates that the coordinates are within acceptable bounds.
         * Checks that xCoordinate is finite and greater than the LOWER_BOUND, and does not exceed MAX_X_VALUE.
         * Similarly, for yCoordinate with MAX_Y_VALUE.
         */
        private static void validateCoordinates(final double xCoordinate,
                                                final double yCoordinate)
        {
            if (Double.isNaN(xCoordinate)        ||
                xCoordinate <= LOWER_BOUND_LIMIT ||
                xCoordinate > MAX_X_VALUE)
            {
                throw new IllegalArgumentException(
                        "NodeDefinition 'xCoordinate' is invalid.");
            }
            if (Double.isNaN(yCoordinate)        ||
                yCoordinate <= LOWER_BOUND_LIMIT ||
                yCoordinate > MAX_Y_VALUE)
            {
                throw new IllegalArgumentException(
                        "NodeDefinition 'yCoordinate' is invalid.");
            }
        }

        /**
         * Returns the node's type.
         *
         * @return the node type (e.g., "SOURCE", "PROCESSOR", "SINK")
         */
        public String getNodeType()
        {
            return nodeType;
        }

        /**
         * Returns the node's unique identifier.
         *
         * @return the node ID
         */
        public String getNodeId()
        {
            return nodeId;
        }

        /**
         * Returns the x-coordinate of the node's center.
         *
         * @return the x-coordinate
         */
        public double getXCoordinate()
        {
            return xCoordinate;
        }

        /**
         * Returns the y-coordinate of the node's center.
         *
         * @return the y-coordinate
         */
        public double getYCoordinate()
        {
            return yCoordinate;
        }

        /**
         * Returns the configuration string for the node, if any.
         *
         * @return the config string (can be null or empty)
         */
        public String getConfiguration()
        {
            return configuration;
        }
    }
}
