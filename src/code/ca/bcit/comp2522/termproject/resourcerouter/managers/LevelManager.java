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

    private static final int    INITIAL_VALUE                = 0;
    public static final double  DEFAULT_TIME_LIMIT_SECONDS   = 60.0;

    private final List<NodeDefinition>  nodeDefinitions;
    private final double                timeLimitSeconds;
    private final String                prompt;

    /**
     * Constructs a LevelManager with the specified level number, node definitions,
     * time limit in seconds, and prompt.
     *
     * @param levelNumber      the level number; must be positive.
     * @param nodeDefinitions  the list of node definitions.
     * @param timeLimitSeconds the time limit in seconds; must be positive.
     * @param prompt           the prompt for the level.
     * @throws IllegalArgumentException if the level number or time limit is not positive.
     */
    public LevelManager(final int levelNumber,
                        final List<NodeDefinition> nodeDefinitions,
                        final double timeLimitSeconds,
                        final String prompt)
    {

        validateLevelNumber(levelNumber);
        validateTimeLimitSeconds(timeLimitSeconds);

        final ArrayList<NodeDefinition> tempList;
        tempList = new ArrayList<>(nodeDefinitions);

        this.nodeDefinitions    = Collections.unmodifiableList(tempList);
        this.timeLimitSeconds   = timeLimitSeconds;
        this.prompt             = prompt;
    }

    /**
     * Returns the unmodifiable list of node definitions.
     *
     * @return the list of NodeDefinition objects.
     */
    public List<NodeDefinition> getNodeDefinitions()
    {
        final List<NodeDefinition> result;
        result = nodeDefinitions;
        return result;
    }

    /**
     * Returns the time limit for the level in seconds.
     *
     * @return the time limit in seconds.
     */
    public double getTimeLimitSeconds()
    {
        final double result;
        result = timeLimitSeconds;
        return result;
    }

    /**
     * Returns the prompt for the level.
     *
     * @return the prompt as a String.
     */
    public String getPrompt()
    {
        final String result;
        result = prompt;
        return result;
    }

    /**
     * Validates the level numbers.
     *
     * @param levelNumber the level number the user is on
     */
    private void validateLevelNumber(final int levelNumber)
    {
        if (levelNumber <= INITIAL_VALUE)
        {
            throw new IllegalArgumentException("Level number must be positive.");
        }
    }

    /**
     * Validation method for Time Limit Seconds.
     *
     * @param timeLimitSeconds time limit per level
     */
    private void validateTimeLimitSeconds(final double timeLimitSeconds)
    {
        if (timeLimitSeconds <= INITIAL_VALUE)
        {
            throw new IllegalArgumentException("Time limit must be positive.");
        }
    }

    /**
     * A static nested final class that represents a definition of a node in the level,
     * including its type, unique ID,
     * (x, y) coordinates, and an optional configuration string.
     */
    public static final class NodeDefinition
    {
        private final String type;
        private final String id;
        private final double x;
        private final double y;
        private final String config;

        /**
         * Constructs a NodeDefinition with the specified attributes, validating
         * that the type and ID are not null or empty, and that x and y are finite.
         *
         * @param type   the type of the node (e.g., "SOURCE", "PROCESSOR", "SINK")
         * @param id     the unique identifier of the node
         * @param x      the x-coordinate of the node's center
         * @param y      the y-coordinate of the node's center
         * @param config an optional configuration string for the node (can be null or empty)
         *
         * @throws IllegalArgumentException if {@code type} or {@code id} is null/empty,
         *                                  or if x or y is not finite
         */
        public NodeDefinition(final String type,
                              final String id,
                              final double x,
                              final double y,
                              final String config)
        {
            validateType(type);
            validateId(id);
            validateCoordinates(x, y);

            this.type = type;
            this.id   = id;
            this.x    = x;
            this.y    = y;
            this.config = config;
        }

        /**
         * Returns the node's type.
         *
         * @return the node type (e.g., "SOURCE", "PROCESSOR", "SINK")
         */
        public String getType()
        {
            return type;
        }

        /**
         * Returns the node's unique identifier.
         *
         * @return the node ID
         */
        public String getId()
        {
            return id;
        }

        /**
         * Returns the x-coordinate of the node's center.
         *
         * @return the x-coordinate
         */
        public double getX()
        {
            return x;
        }

        /**
         * Returns the y-coordinate of the node's center.
         *
         * @return the y-coordinate
         */
        public double getY()
        {
            return y;
        }

        /**
         * Returns the configuration string for the node, if any.
         *
         * @return the config string (can be null or empty)
         */
        public String getConfig()
        {
            return config;
        }

        /*
         * Validates that 'type' is neither null nor empty.
         */
        private static void validateType(final String type)
        {
            if (type == null || type.isBlank())
            {
                throw new IllegalArgumentException("NodeDefinition 'type' cannot be null/blank.");
            }
        }

        /*
         * Validates that 'id' is neither null nor empty.
         */
        private static void validateId(final String id)
        {
            if (id == null || id.isBlank())
            {
                throw new IllegalArgumentException("NodeDefinition 'id' cannot be null/blank.");
            }
        }

        /*
         * Validates that x and y are numbers.
         */
        private static void validateCoordinates(final double x,
                                                final double y)
        {
            if (Double.isNaN(x) || x <= 0 || x >= 900)
            {
                throw new IllegalArgumentException("NodeDefinition 'x' is invalid.");
            }
            if (Double.isNaN(y) || y <= 0 || y >= 600)
            {
                throw new IllegalArgumentException("NodeDefinition 'y' is invalid.");
            }
        }
    }
}
