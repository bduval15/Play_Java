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
 *   <li><code>inputList</code> is a comma-separated list of resource:quantity pairs (for example, <code>wood:3,stone:2</code>).</li>
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
 * @author Braeden
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

    /**
     * Creates a new {@link GameNode} instance from the provided {@link LevelManager.NodeDefinition}.
     * <p>
     * This method examines the {@code type} field of the given definition and, based on its value
     * ({@code SOURCE}, {@code PROCESSOR}, or {@code SINK}), delegates to one of the parsing methods:
     * <ul>
     *   <li>{@link #parseSourceConfig(LevelManager.NodeDefinition)} for {@code SOURCE}</li>
     *   <li>{@link #parseProcessorConfig(LevelManager.NodeDefinition)} for {@code PROCESSOR}</li>
     *   <li>{@link #parseSinkConfig(LevelManager.NodeDefinition)} for {@code SINK}</li>
     * </ul>
     * If the node type is unrecognized or if any exception arises during instantiation
     * (e.g., invalid configuration data), this method wraps the error in a {@code RuntimeException}
     * and includes the node's ID in the exception message for easier debugging.
     * </p>
     *
     * @param definition the node definition containing type, id, coordinates, and optional config
     * @return a new {@link GameNode} (concretely either {@link SourceNode},
     * {@link ProcessorNode}, or {@link SinkNode})
     *
     * @throws NullPointerException if {@code definition} is {@code null}
     * @throws RuntimeException     if node creation fails or the node type is unrecognized
     */
    public static GameNode createNode(final LevelManager.NodeDefinition definition)
    {
        Objects.requireNonNull(definition);

        final String typeUpper;
        typeUpper = definition.getType().toUpperCase();

        final GameNode node;
        try
        {
            node = switch (typeUpper)
            {
                case NODE_TYPE_SOURCE       -> parseSourceConfig(definition);
                case NODE_TYPE_PROCESSOR    -> parseProcessorConfig(definition);
                case NODE_TYPE_SINK         -> parseSinkConfig(definition);
                default -> throw new IllegalArgumentException("Unknown type: " +
                                                              definition.getType());
            };
        }
        catch (final Exception e)
        {
            throw new RuntimeException(
                    "Factory Error node " + definition.getId() + ": " + e.getMessage(), e
            );
        }
        return node;
    }

    /*
     * Parses a LevelManager.NodeDefinition that represents a Source node, extracting
     * the resource type to be produced and applying a default production interval if none
     * is specified.
     *
     * The configuration for a Source node is expected to include a token in the format
     * PRODUCES=<resourceName>. This method looks for that token within
     * the node's configuration string and attempts to resolve the resource name
     * to a ResourceType. If it fails to find or parse a valid resource type,
     * an IllegalArgumentException is thrown.
     *
     *
     * @param def the node definition containing id, coordinates, and configuration string
     * @return a newly constructed SourceNode initialized with the identified resource type
     *
     * @throws IllegalArgumentException if the configuration is missing the PRODUCES= token,
     * or if the resource type is invalid
     */
    private static SourceNode parseSourceConfig(final LevelManager.NodeDefinition def)
    {
        final String config;
        config = def.getConfig();

        if (config == null
                || config.isBlank()
                || !config.toUpperCase().contains(CONFIG_KEY_PRODUCES + "="))
        {
            throw new IllegalArgumentException("Missing PRODUCES= for Source: " +
                                               def.getId());
        }

        final double interval;
        interval = SourceNode.DEFAULT_PRODUCTION_INTERVAL_SECONDS;

        ResourceType type = null;

        for (final String configToken : config.trim().split("\\s+"))
        {
            final String upperToken;
            upperToken = configToken.toUpperCase();

            if (upperToken.startsWith(CONFIG_KEY_PRODUCES + "="))
            {
                final String resourceString;
                resourceString = configToken.substring(CONFIG_KEY_PRODUCES.length() +
                                                       PARSE_INDEX_ONE);

                type = ResourceType.getResourceType(resourceString);

                if (type == null)
                {
                    throw new IllegalArgumentException(
                            "Bad type '" + resourceString + "' Source: " + def.getId()
                    );
                }
            }
        }
        if (type == null)
        {
            throw new IllegalArgumentException("Missing PRODUCES key for Source: " +
                                               def.getId());
        }

        final SourceNode sourceNode;
        sourceNode = new SourceNode(def.getId(), def.getX(), def.getY(), type, interval);

        return sourceNode;
    }

    /*
     * Parses a LevelManager.NodeDefinition that represents a Processor node, extracting
     * the input recipe map, output resource type, and optional processing delay.
     *
     * The configuration must include a token in the format RECIPE=<inputs>-><output>,
     * where <inputs> is a comma-separated list of resource:quantity, and
     * <output> is a single resource type optionally followed by a quantity. If a
     * DELAY=<value> token is found, it overrides the default processing delay.
     *
     *
     * This method populates the recipe map with all valid resource:quantity pairs,
     * then creates a ProcessorNode with the parsed data. If any part of the recipe
     * is missing or malformed (e.g., unknown resource, non-positive quantity), an
     * IllegalArgumentException is thrown.
     *
     *
     * @param def the node definition containing id, coordinates, and configuration string
     * @return a newly constructed ProcessorNode with the parsed recipe and output resource
     *
     * @throws IllegalArgumentException if the configuration does not contain a valid RECIPE=...
     * token or if numeric values within the recipe or delay are invalid
     */
    private static ProcessorNode parseProcessorConfig(final LevelManager.NodeDefinition def)
    {
        final String config;
        config = def.getConfig();

        if (config == null
                || config.isBlank()
                || !config.toUpperCase().contains(CONFIG_KEY_RECIPE + "="))
        {
            throw new IllegalArgumentException("Missing RECIPE= for Processor: " +
                                               def.getId());
        }

        final Map<ResourceType, Integer> recipeIn;
        recipeIn = new HashMap<>();

        ResourceType typeOut = null;

        int delay;
        delay = DEFAULT_PROCESSOR_DELAY;

        for (final String configToken : config.trim().split("\\s+"))
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
                    throw new IllegalArgumentException("Need inputs:" + def.getId());
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

                    delay   = Math.max(ProcessorNode.MIN_DELAY_TICKS, parseDelay);
                }
                catch (final NumberFormatException e)
                {
                    System.err.println("Warn: Bad Proc DELAY '" +
                                       configToken +
                                       "' id=" +
                                       def.getId());
                }
            }
        }
        if (recipeIn.isEmpty())
        {
            throw new IllegalArgumentException("Bad RECIPE parse:" + def.getId());
        }

        final ProcessorNode processorNode;
        processorNode = new ProcessorNode(def.getId(),
                                          def.getX(),
                                          def.getY(),
                                          delay,
                                          recipeIn,
                                          typeOut);

        return processorNode;
    }

    /*
     * Parses a LevelManager.NodeDefinition that represents a Sink node, extracting
     * the demand map for required resources.
     *
     * The configuration for a Sink node is expected to start with the token
     * DEMAND=<resource:quantity>,<resource:quantity>,.... Each pair is split
     * to determine the resource type and the required amount. If any resource type is
     * unknown or any quantity is non-positive, an IllegalArgumentException is thrown.
     *
     *
     * A valid configuration results in creating a SinkNode that tracks
     * how many of each demanded resource must be received before it is considered satisfied.
     *
     *
     * @param def the node definition containing id, coordinates, and configuration string
     * @return a newly constructed SinkNode populated with the parsed demands
     *
     * @throws IllegalArgumentException if the configuration is missing the DEMAND= token,
     * if it is empty, or if any resource demand is invalid
     */
    private static SinkNode parseSinkConfig(final LevelManager.NodeDefinition def)
    {
        final String config;
        config = def.getConfig();

        if (config == null
                || config.isBlank()
                || !config.toUpperCase().startsWith(CONFIG_KEY_DEMAND + "="))
        {
            throw new IllegalArgumentException("Missing DEMAND= for Sink: " +
                                               def.getId());
        }

        final Map<ResourceType, Integer> demandMap;
        demandMap = new HashMap<>();

        final String demandString;
        demandString = def.getConfig().substring(CONFIG_KEY_DEMAND.length() +
                                                 PARSE_INDEX_ONE ).trim();

        if (demandString.isEmpty())
        {
            throw new IllegalArgumentException("Empty DEMAND:" +
                                               def.getId());
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
                MIN_REQUIRED = 1;

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
                                               def.getId());
        }

        final SinkNode sinkNode;
        sinkNode = new SinkNode(def.getId(),
                                def.getX(),
                                def.getY(),
                                demandMap);

        return sinkNode;
    }
}
