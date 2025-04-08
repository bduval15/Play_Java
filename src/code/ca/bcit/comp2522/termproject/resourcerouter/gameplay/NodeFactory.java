package ca.bcit.comp2522.termproject.resourcerouter.gameplay;

import ca.bcit.comp2522.termproject.resourcerouter.managers.LevelManager;
import ca.bcit.comp2522.termproject.resourcerouter.util.ResourceType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A factory for creating GameNode instances from node configuration definitions.
 *
 * <p>
 * This class processes configuration strings from LevelManager.NodeDefinition objects
 * and creates the appropriate GameNode subclass (SourceNode, ProcessorNode, or SinkNode)
 * based on the specified type.
 * </p>
 *
 * <h2>Supported Configuration Formats</h2>
 *
 * <h3>SourceNode</h3>
 * <p>
 * The configuration for a SourceNode must include a token in the following format:
 * <br>
 * PRODUCES=resourceType
 * <br>
 * where <code>resourceType</code> is the name of the resource produced by the node.
 * </p>
 *
 * <h3>ProcessorNode</h3>
 * <p>
 * The configuration for a ProcessorNode must include a token formatted as:
 * <br>
 * RECIPE=inputList->output
 * <br>
 * Here:
 * <ul>
 *   <li><code>inputList</code> is a comma-separated list of resource:quantity pairs
 *   (for example, <code>red:3,green:2</code>).</li>
 *   <li><code>output</code> is the resource produced after processing.</li>
 * </ul>
 * Optionally, the configuration may include a token:
 * <br>
 * DELAY=value
 * <br>
 * which specifies the processing delay. A minimum delay is enforced.
 * </p>
 *
 * <h3>SinkNode</h3>
 * <p>
 * The configuration for a SinkNode must start with a token in the following format:
 * <br>
 * DEMAND=resourceType:quantity[,resourceType:quantity,...]
 * <br>
 * In this format:
 * <ul>
 *   <li><code>resourceType</code> is the name of the required resource.</li>
 *   <li><code>quantity</code> is a positive integer indicating how many units of the resource are needed.</li>
 *   <li>Multiple resource demands can be specified by separating them with commas.</li>
 * </ul>
 * For example:
 * <br>
 * DEMAND=wood:10,stone:5
 * </p>
 *
 * <h2>Error Handling</h2>
 * <p>
 * If the configuration string is missing required tokens or does not conform to the expected format,
 * the factory throws an IllegalArgumentException with a clear error message.
 * </p>
 *
 * <p>
 * By centralizing node creation and configuration parsing, NodeFactory ensures consistency across
 * the system and simplifies the instantiation of GameNode objects.
 * </p>
 *
 * @see LevelManager.NodeDefinition
 * @see SourceNode
 * @see ProcessorNode
 * @see SinkNode
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class NodeFactory
{

    private static final String NODE_TYPE_SOURCE        = "SOURCE";
    private static final String NODE_TYPE_PROCESSOR     = "PROCESSOR";
    private static final String NODE_TYPE_SINK          = "SINK";
    private static final String CONFIG_KEY_PRODUCES     = "PRODUCES";
    private static final String CONFIG_KEY_RECIPE       = "RECIPE";
    private static final String CONFIG_KEY_DEMAND       = "DEMAND";
    private static final String CONFIG_KEY_DELAY        = "DELAY";

    private static final int    DEFAULT_PROCESSOR_DELAY = 1;
    private static final int    PARSE_INDEX_ZERO        = 0;
    private static final int    PARSE_INDEX_ONE         = 1;
    private static final int    PARSE_INDEX_TWO         = 2;
    private static final int    MIN_DELAY_TICKS_SEC     = 1;
    private static final int    MIN_DEMANDS             = 1;

    private static final double DEFAULT_PRODUCTION_INTERVAL_SECONDS  = 1.0;

    /**
     * Creates a new {@link GameNode} from the specified nodeDefinition.
     * <p>
     * The {@link LevelManager.NodeDefinition} provides:
     * <ul>
     *   <li>A {@code nodeId}, unique across the level.</li>
     *   <li>Coordinates ({@code x}, {@code y}) for node placement.</li>
     *   <li>A {@code nodeType} indicating which node subclass to create:
     *       {@code SOURCE}, {@code PROCESSOR}, or {@code SINK}.</li>
     *   <li>An optional configuration string, containing tokens
     *       like {@code PRODUCES=...}, {@code RECIPE=...}, or
     *       {@code DEMAND=...} depending on the node type.</li>
     * </ul>
     * Based on the {@code type} value, this method:
     * <ol>
     *   <li>Converts the type to uppercase.</li>
     *   <li>Uses a {@code switch} to delegate to the matching parser:
     *     <ul>
     *       <li>{@link #parseSourceConfig(LevelManager.NodeDefinition)}
     *           if type is {@code SOURCE}</li>
     *       <li>{@link #parseProcessorConfig(LevelManager.NodeDefinition)}
     *           if type is {@code PROCESSOR}</li>
     *       <li>{@link #parseSinkConfig(LevelManager.NodeDefinition)}
     *           if type is {@code SINK}</li>
     *     </ul>
     *   </li>
     *   <li>If none of the recognized types match, throws an
     *       {@link IllegalArgumentException}.</li>
     *   <li>Catches any parsing errors, wraps them in a
     *       {@link RuntimeException}, and includes the node's ID
     *       for debugging.</li>
     * </ol>
     * </p>
     *
     * @param nodeDefinition the node nodeDefinition with type, id, coordinates,
     *                   and configuration tokens
     * @return a newly created {@link GameNode} subclass instance
     *
     * @throws NullPointerException if {@code nodeDefinition} is null
     * @throws RuntimeException if an error occurs while parsing or
     *                          if the node type is unrecognized
     *
     */
    public static GameNode createNode(final LevelManager.NodeDefinition nodeDefinition)
    {
        Objects.requireNonNull(nodeDefinition);

        final String typeUpper;
        typeUpper = nodeDefinition.getNodeType().toUpperCase();

        final GameNode node;
        try
        {
            node = switch (typeUpper)
            {
                case NODE_TYPE_SOURCE       -> parseSourceConfig(nodeDefinition);
                case NODE_TYPE_PROCESSOR    -> parseProcessorConfig(nodeDefinition);
                case NODE_TYPE_SINK         -> parseSinkConfig(nodeDefinition);
                default -> throw new IllegalArgumentException("Unknown type: " +
                                                              nodeDefinition.getNodeType());
            };
        }
        catch (final Exception e)
        {
            throw new RuntimeException(
                    "Factory Error node " + nodeDefinition.getNodeId() + ": " + e.getMessage(), e
            );
        }
        return node;
    }

    /*
     * Parses a LevelManager.NodeDefinition for a Source node.
     *
     * Steps:
     * 1. Retrieve the configuration string via nodeDefinition.getConfig().
     * 2. Check that the config is not null or blank and contains "PRODUCES=" (uppercase).
     * 3. Split the config string by whitespace and look for a token starting with "PRODUCES=".
     * 4. From the matching token, extract the resource type string (substring after "PRODUCES=").
     * 5. Convert the resource string to a ResourceType using ResourceType.getResourceType.
     * 6. If no valid resource type is found, throw an IllegalArgumentException.
     * 7. Use the found ResourceType and a default production interval (DEFAULT_PRODUCTION_INTERVAL_SECONDS)
     *    to create and return a new SourceNode with the node id and coordinates from nodeDefinition.
     *
     * @param nodeDefinition the node definition for the Source node
     *
     * @return a new SourceNode configured with the identified resource type
     *
     * @throws IllegalArgumentException if the configuration is missing PRODUCES= or if the resource type is invalid
     *
     */
    private static SourceNode parseSourceConfig(final LevelManager.NodeDefinition nodeDefinition)
    {
        final String config;
        config = nodeDefinition.getConfiguration();

        if (config == null
                || config.isBlank()
                || !config.toUpperCase().contains(CONFIG_KEY_PRODUCES + "="))
        {
            throw new IllegalArgumentException("Missing PRODUCES= for Source: " +
                                               nodeDefinition.getNodeId());
        }

        final double interval;
        interval = DEFAULT_PRODUCTION_INTERVAL_SECONDS;

        ResourceType resourceType = null;

        for (final String configToken : config.trim().split("\\s+"))
        {
            final String upperToken;
            upperToken = configToken.toUpperCase();

            if (upperToken.startsWith(CONFIG_KEY_PRODUCES + "="))
            {
                final String resourceString;
                resourceString = configToken.substring(CONFIG_KEY_PRODUCES.length() +
                                                       PARSE_INDEX_ONE);

                resourceType = ResourceType.getResourceType(resourceString);

                if (resourceType == null)
                {
                    throw new IllegalArgumentException(
                            "Bad resourceType '" + resourceString + "' Source: " + nodeDefinition.getNodeId()
                    );
                }
            }
        }
        if (resourceType == null)
        {
            throw new IllegalArgumentException("Missing PRODUCES key for Source: " +
                                               nodeDefinition.getNodeId());
        }

        final SourceNode sourceNode;
        sourceNode = new SourceNode(nodeDefinition.getNodeId(),
                                    nodeDefinition.getXCoordinate(),
                                    nodeDefinition.getYCoordinate(),
                                    resourceType,
                                    interval);

        return sourceNode;
    }

    /*
     * Parses a LevelManager.NodeDefinition for a Processor node.
     *
     * Steps:
     * 1. Retrieve the configuration string via nodeDefinition.getConfig() and verify that it contains "RECIPE=".
     * 2. Initialize an empty map for input resources (recipeIn), a variable for output ResourceType (typeOut),
     *    and set delay to DEFAULT_PROCESSOR_DELAY.
     * 3. Split the config by whitespace and process each token.
     * 4. For a token starting with "RECIPE=":
     *    a. Extract the recipe portion (substring after "RECIPE=").
     *    b. Split the recipe string on "->" to separate inputs from the output.
     *    c. Validate that both input and output parts exist and are non-blank.
     *    d. Split the input part by commas to obtain individual resource:quantity pairs.
     *    e. For each pair, split by ":" to separate resource type and quantity.
     *    f. Convert the resource name using ResourceType.getResourceType and parse the quantity as an integer.
     *    g. If any resource is invalid or quantity is non-positive, throw an IllegalArgumentException.
     * 5. For tokens starting with "DELAY=", extract and parse the delay value,
     *    ensuring it is at least MIN_DELAY_TICKS_SEC.
     * 6. After processing all tokens, if no valid recipe is found, throw an exception.
     * 7. Create and return a new ProcessorNode with the parsed id, coordinates, delay, input recipe, and output type.
     *
     * @param nodeDefinition the node definition for the Processor node
     *
     * @return a new ProcessorNode configured with the parsed recipe and output resource
     *
     * @throws IllegalArgumentException if RECIPE= is missing or if any value in the recipe or delay is invalid
     *
     */
    private static ProcessorNode parseProcessorConfig(final LevelManager.NodeDefinition nodeDefinition)
    {
        final String configuration;
        configuration = nodeDefinition.getConfiguration();

        if (configuration == null ||
            configuration.isBlank() ||
            !configuration.toUpperCase().contains(CONFIG_KEY_RECIPE + "="))
        {
            throw new IllegalArgumentException("Missing RECIPE= for Processor: " +
                                               nodeDefinition.getNodeId());
        }

        final Map<ResourceType, Integer> recipeIn;
        recipeIn = new HashMap<>();

        ResourceType typeOut = null;

        int processorDelay;
        processorDelay = DEFAULT_PROCESSOR_DELAY;

        for (final String configToken : configuration.trim().split("\\s+"))
        {
            final String upperToken;
            upperToken = configToken.toUpperCase();

            if (upperToken.startsWith(CONFIG_KEY_RECIPE + "="))
            {
                final String recipeString;
                recipeString = configToken.substring(CONFIG_KEY_RECIPE.length() +
                                                     PARSE_INDEX_ONE);

                final String[] recipeTokens;
                recipeTokens = recipeString.split("->");

                if (recipeTokens.length != PARSE_INDEX_TWO ||
                    recipeTokens[PARSE_INDEX_ZERO].isBlank() ||
                    recipeTokens[PARSE_INDEX_ONE].isBlank())
                {
                    throw new IllegalArgumentException("Bad RECIPE '->':" + recipeString);
                }

                final String[] inputTokens;
                inputTokens = recipeTokens[PARSE_INDEX_ZERO].trim().split(",");

                if (inputTokens.length == PARSE_INDEX_ZERO ||
                    inputTokens[PARSE_INDEX_ZERO].isBlank())
                {
                    throw new IllegalArgumentException("Need inputs:" + nodeDefinition.getNodeId());
                }

                for (final String inputDef : inputTokens)
                {
                    final String[] tc;
                    tc = inputDef.trim().split(":");

                    if (tc.length != PARSE_INDEX_TWO ||
                        tc[PARSE_INDEX_ZERO].isBlank() ||
                        tc[PARSE_INDEX_ONE].isBlank())
                    {
                        throw new IllegalArgumentException("Bad input 'T:C':" + inputDef);
                    }

                    final ResourceType resourceType;
                    resourceType = ResourceType.getResourceType(tc[PARSE_INDEX_ZERO]);

                    try
                    {
                        final int quantity;
                        quantity = Integer.parseInt(tc[PARSE_INDEX_ONE]);

                        if (resourceType == null || quantity <= PARSE_INDEX_ZERO)
                        {
                            throw new IllegalArgumentException("Bad T/C:" + inputDef);
                        }
                        recipeIn.put(resourceType, quantity);
                    }
                    catch (final NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Bad count:" + inputDef);
                    }
                }

                final String[] outputTokens;
                outputTokens = recipeTokens[PARSE_INDEX_ONE].trim().split(":");

                if (outputTokens[PARSE_INDEX_ZERO].isBlank())
                {
                    throw new IllegalArgumentException("Bad output type:" +
                                                       recipeTokens[PARSE_INDEX_ONE]);
                }

                typeOut = ResourceType.getResourceType(outputTokens[PARSE_INDEX_ZERO]);

                if (typeOut == null)
                {
                    throw new IllegalArgumentException("Bad output type:" + outputTokens[0]);
                }

            }
            else if (upperToken.startsWith(CONFIG_KEY_DELAY + "="))
            {
                try
                {
                    final int parseDelay;
                    parseDelay       = Integer.parseInt(configToken.substring(
                                                       CONFIG_KEY_DELAY.length() +
                                                        PARSE_INDEX_ONE));

                    processorDelay   = Math.max(MIN_DELAY_TICKS_SEC, parseDelay);
                }
                catch (final NumberFormatException e)
                {
                    throw new IllegalArgumentException("Warn: Bad Proc DELAY '" +
                                                        configToken +
                                                        "' id=" +
                                                        nodeDefinition.getNodeId());
                }
            }
        }
        if (recipeIn.isEmpty())
        {
            throw new IllegalArgumentException("Bad RECIPE parse:" + nodeDefinition.getNodeId());
        }

        final ProcessorNode processorNode;
        processorNode = new ProcessorNode(nodeDefinition.getNodeId(),
                                          nodeDefinition.getXCoordinate(),
                                          nodeDefinition.getYCoordinate(),
                                          processorDelay,
                                          recipeIn,
                                          typeOut);

        return processorNode;
    }

    /*
     * Parses a LevelManager.NodeDefinition for a Sink node.
     *
     * Steps:
     * 1. Retrieve the configuration string and ensure it is not null or blank and starts with "DEMAND=".
     * 2. Extract the substring following "DEMAND=" to obtain the demand specification.
     * 3. Split the extracted string by commas to separate multiple resource demands.
     * 4. For each demand string:
     *    a. Split by ":" to separate the resource name and required quantity.
     *    b. Convert the resource name to a ResourceType.
     *    c. Parse the quantity as an integer and ensure it is at least 1.
     *    d. Insert the resource and quantity into a demand map.
     * 5. If no valid demands are found, throw an IllegalArgumentException.
     * 6. Create and return a new SinkNode with the node id, coordinates, and the demand map.
     *
     * @param nodeDefinition the node definition for the Sink node
     *
     * @return a new SinkNode configured with the parsed resource demands
     *
     * @throws IllegalArgumentException if the configuration is missing DEMAND=,
     *                                  is empty, or contains invalid demand data
     *
     */
    private static SinkNode parseSinkConfig(final LevelManager.NodeDefinition nodeDefinition)
    {
        final String configuration;
        configuration = nodeDefinition.getConfiguration();

        if (configuration == null ||
            configuration.isBlank() ||
            !configuration.toUpperCase().startsWith(CONFIG_KEY_DEMAND + "="))
        {
            throw new IllegalArgumentException("Missing DEMAND= for Sink: " +
                                               nodeDefinition.getNodeId());
        }

        final Map<ResourceType, Integer> demandMap;
        demandMap = new HashMap<>();

        final String demandString;
        demandString = nodeDefinition.getConfiguration().substring(CONFIG_KEY_DEMAND.length() +
                                                 PARSE_INDEX_ONE ).trim();

        if (demandString.isEmpty())
        {
            throw new IllegalArgumentException("Empty DEMAND:" +
                                               nodeDefinition.getNodeId());
        }

        final String[] demands;
        demands = demandString.split(",");

        for (final String demandItem : demands)
        {
            final String[] typeCountPair;
            typeCountPair = demandItem.trim().split(":");

            if (typeCountPair.length != PARSE_INDEX_TWO ||
                typeCountPair[PARSE_INDEX_ZERO].isBlank() ||
                typeCountPair[PARSE_INDEX_ONE].isBlank())
            {
                throw new IllegalArgumentException("Bad demand 'T:C':" + demandItem);
            }

            final ResourceType resourceType;
            resourceType = ResourceType.getResourceType(typeCountPair[PARSE_INDEX_ZERO]);

            try
            {
                final int requiredCount;
                requiredCount = Integer.parseInt(typeCountPair[PARSE_INDEX_ONE]);

                final int MIN_REQUIRED;
                MIN_REQUIRED = MIN_DEMANDS;

                if (resourceType == null || requiredCount < MIN_REQUIRED)
                {
                    throw new IllegalArgumentException("Bad T/C demand:" + demandItem);
                }
                demandMap.put(resourceType, requiredCount);
            }
            catch (final NumberFormatException e)
            {
                throw new IllegalArgumentException("Bad count demand:" + demandItem);
            }
        }

        if (demandMap.isEmpty())
        {
            throw new IllegalArgumentException("No valid demands parsed:" +
                                               nodeDefinition.getNodeId());
        }

        final SinkNode sinkNode;
        sinkNode = new SinkNode(nodeDefinition.getNodeId(),
                                nodeDefinition.getXCoordinate(),
                                nodeDefinition.getYCoordinate(),
                                demandMap);

        return sinkNode;
    }
}
