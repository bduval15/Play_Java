package ca.bcit.comp25222.termproject.CustomGame.gameplay;

import ca.bcit.comp25222.termproject.CustomGame.managers.LevelManager;
import ca.bcit.comp25222.termproject.CustomGame.util.ResourceType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Factory class for creating {@link GameNode} objects from level definitions.
 * <p>
 * The {@code NodeFactory} is responsible for instantiating concrete subclasses of
 * {@link GameNode} (namely, {@link SourceNode}, {@link ProcessorNode}, and {@link SinkNode})
 * based on the configuration details provided by a {@link LevelManager.NodeDefinition}.
 * This class encapsulates the parsing logic required to extract configuration parameters
 * from the node definition string and then uses these parameters to construct the appropriate node.
 * </p>
 * <p>
 * For a <strong>SourceNode</strong>, the factory expects the configuration string to contain
 * a token with the key {@value #CONFIG_KEY_PRODUCES} in the format:
 * <blockquote>
 *     PRODUCES=&lt;resource&gt;
 * </blockquote>
 * The resource value is used to determine the type of resource the node will produce,
 * and the node is created with a default production interval defined by
 * {@link SourceNode#DEFAULT_PRODUCTION_INTERVAL_SECONDS}.
 * </p>
 * <p>
 * For a <strong>ProcessorNode</strong>, the configuration string must include a token with the key
 * {@value #CONFIG_KEY_RECIPE} in the format:
 * <blockquote>
 *     RECIPE=&lt;inputList&gt;->&lt;output&gt;
 * </blockquote>
 * The input list consists of one or more comma-separated tokens in the format
 * {@code &lt;resource&gt;:&lt;quantity&gt;}, which are parsed into a recipe map.
 * Optionally, the configuration may contain a token with the key {@value #CONFIG_KEY_DELAY}
 * to specify the processing delay, with a minimum value enforced by {@link ProcessorNode#MIN_DELAY_TICKS}.
 * </p>
 * <p>
 * For a <strong>SinkNode</strong>, the configuration string must begin with the token
 * {@value #CONFIG_KEY_DEMAND} in the format:
 * <blockquote>
 *     DEMAND=&lt;resource&gt;:&lt;quantity&gt;,[&lt;resource&gt;:&lt;quantity&gt;,...]
 * </blockquote>
 * This token indicates the required resource demands that must be satisfied for the sink node.
 * The factory parses this token to construct a demand map, which is then used to instantiate a {@link SinkNode}.
 * </p>
 * <p>
 * All parsing of configuration strings is performed in a case-insensitive manner.
 * If any required configuration is missing or if the format does not conform to the expected pattern,
 * the factory will throw an {@code IllegalArgumentException} with a descriptive error message
 * indicating the nature of the configuration error for the respective node.
 * </p>
 * <p>
 * This factory abstracts the node creation process so that other parts of the system can
 * simply supply a level definition and obtain a fully configured {@code GameNode} instance,
 * ensuring consistency and reducing duplication of parsing logic throughout the codebase.
 * </p>
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
    private static final int DEFAULT_PROCESSOR_DELAY    = 1;

    /**
     * Creates a new GameNode instance from the provided node definition.
     *
     * @param definition the node definition from the level file
     * @return a new GameNode instance (SourceNode, ProcessorNode, or SinkNode)
     * @throws RuntimeException if node creation fails
     */
    public static GameNode createNode(final LevelManager.NodeDefinition definition)
    {
        Objects.requireNonNull(definition);
        final String typeUpper;
        typeUpper = definition.type().toUpperCase();

        final GameNode node;
        try {
            node = switch (typeUpper)
            {
                case NODE_TYPE_SOURCE -> parseSourceConfig(definition);
                case NODE_TYPE_PROCESSOR -> parseProcessorConfig(definition);
                case NODE_TYPE_SINK -> parseSinkConfig(definition);
                default -> throw new IllegalArgumentException("Unknown type: " + definition.type());
            };
        } catch (final Exception e) {
            throw new RuntimeException(
                    "Factory Error node " + definition.id() + ": " + e.getMessage(), e
            );
        }
        return node;
    }

    /*
     * Parses the configuration for a source node.
     *
     * @param def the node definition
     * @return a new SourceNode instance
     * @throws IllegalArgumentException if the configuration is invalid
     */
    private static SourceNode parseSourceConfig(final LevelManager.NodeDefinition def)
    {
        final String config;
        config = def.config();

        if (config == null
                || config.isBlank()
                || !config.toUpperCase().contains(CONFIG_KEY_PRODUCES + "="))
        {
            throw new IllegalArgumentException("Missing PRODUCES= for Source: " + def.id());
        }

        final double interval;
        interval = SourceNode.DEFAULT_PRODUCTION_INTERVAL_SECONDS;

        ResourceType type = null;

        for (final String p : config.trim().split("\\s+"))
        {
            final String pu;
            pu = p.toUpperCase();

            if (pu.startsWith(CONFIG_KEY_PRODUCES + "="))
            {
                final String s;
                s = p.substring(CONFIG_KEY_PRODUCES.length() + 1);
                type = ResourceType.fromString(s);
                if (type == null)
                {
                    throw new IllegalArgumentException(
                            "Bad type '" + s + "' Source: " + def.id()
                    );
                }
            }
        }
        if (type == null)
        {
            throw new IllegalArgumentException("Missing PRODUCES key for Source: " + def.id());
        }

        final SourceNode sourceNode;
        sourceNode = new SourceNode(def.id(), def.x(), def.y(), type, interval);
        return sourceNode;
    }

    /*
     * Parses the configuration for a processor node.
     *
     * @param def the node definition
     * @return a new ProcessorNode instance
     * @throws IllegalArgumentException if the configuration is invalid
     */
    private static ProcessorNode parseProcessorConfig(final LevelManager.NodeDefinition def)
    {
        final String config;
        config = def.config();

        if (config == null
                || config.isBlank()
                || !config.toUpperCase().contains(CONFIG_KEY_RECIPE + "="))
        {
            throw new IllegalArgumentException("Missing RECIPE= for Processor: " + def.id());
        }

        final Map<ResourceType, Integer> recipeIn;
        recipeIn = new HashMap<>();

        ResourceType typeOut = null;

        int delay;
        delay = DEFAULT_PROCESSOR_DELAY;

        for (final String p : config.trim().split("\\s+"))
        {
            final String pu;
            pu = p.toUpperCase();

            if (pu.startsWith(CONFIG_KEY_RECIPE + "="))
            {
                final String rStr;
                rStr = p.substring(CONFIG_KEY_RECIPE.length() + 1);

                final String[] io;
                io = rStr.split("->");

                if (io.length != 2 || io[0].isBlank() || io[1].isBlank())
                {
                    throw new IllegalArgumentException("Bad RECIPE '->':" + rStr);
                }

                final String[] ins;
                ins = io[0].trim().split(",");

                if (ins.length == 0 || ins[0].isBlank())
                {
                    throw new IllegalArgumentException("Need inputs:" + def.id());
                }

                for (final String i : ins)
                {
                    final String[] tc;
                    tc = i.trim().split(":");
                    if (tc.length != 2 || tc[0].isBlank() || tc[1].isBlank())
                    {
                        throw new IllegalArgumentException("Bad input 'T:C':" + i);
                    }

                    final ResourceType rt;
                    rt = ResourceType.fromString(tc[0]);

                    try {
                        final int c;
                        c = Integer.parseInt(tc[1]);
                        if (rt == null || c <= 0)
                        {
                            throw new IllegalArgumentException("Bad T/C:" + i);
                        }
                        recipeIn.put(rt, c);
                    } catch (final NumberFormatException e)
                    {
                        throw new IllegalArgumentException("Bad count:" + i);
                    }
                }

                final String[] outs;
                outs = io[1].trim().split(":");
                if (outs[0].isBlank())
                {
                    throw new IllegalArgumentException("Bad output type:" + io[1]);
                }
                typeOut = ResourceType.fromString(outs[0]);
                if (typeOut == null)
                {
                    throw new IllegalArgumentException("Bad output type:" + outs[0]);
                }
            } else if (pu.startsWith(CONFIG_KEY_DELAY + "="))
            {
                try {
                    final int d;
                    d = Integer.parseInt(p.substring(CONFIG_KEY_DELAY.length() + 1));
                    delay = Math.max(ProcessorNode.MIN_DELAY_TICKS, d);
                } catch (final NumberFormatException e)
                {
                    System.err.println("Warn: Bad Proc DELAY '" + p + "' id=" + def.id());
                }
            }
        }
        if (recipeIn.isEmpty())
        {
            throw new IllegalArgumentException("Bad RECIPE parse:" + def.id());
        }

        final ProcessorNode processorNode;
        processorNode = new ProcessorNode(def.id(), def.x(), def.y(), delay, recipeIn, typeOut);
        return processorNode;
    }

    /*
     * Parses the configuration for a sink node.
     *
     * @param def the node definition
     * @return a new SinkNode instance
     * @throws IllegalArgumentException if the configuration is invalid
     */
    private static SinkNode parseSinkConfig(final LevelManager.NodeDefinition def)
    {
        final String config;
        config = def.config();

        if (config == null
                || config.isBlank()
                || !config.toUpperCase().startsWith(CONFIG_KEY_DEMAND + "="))
        {
            throw new IllegalArgumentException("Missing DEMAND= for Sink: " + def.id());
        }

        final Map<ResourceType, Integer> demandMap;
        demandMap = new HashMap<>();

        final String dStr;
        dStr = def.config().substring(CONFIG_KEY_DEMAND.length() + 1).trim();

        if (dStr.isEmpty())
        {
            throw new IllegalArgumentException("Empty DEMAND:" + def.id());
        }

        final String[] demands;
        demands = dStr.split(",");

        for (final String d : demands)
        {
            final String[] tc;
            tc = d.trim().split(":");

            if (tc.length != 2 || tc[0].isBlank() || tc[1].isBlank())
            {
                throw new IllegalArgumentException("Bad demand 'T:C':" + d);
            }

            final ResourceType rt;
            rt = ResourceType.fromString(tc[0]);

            try {
                final int c;
                c = Integer.parseInt(tc[1]);
                final int MIN;
                MIN = 1;
                if (rt == null || c < MIN)
                {
                    throw new IllegalArgumentException("Bad T/C demand:" + d);
                }
                demandMap.put(rt, c);
            } catch (final NumberFormatException e)
            {
                throw new IllegalArgumentException("Bad count demand:" + d);
            }
        }

        if (demandMap.isEmpty())
        {
            throw new IllegalArgumentException("No valid demands parsed:" + def.id());
        }

        final SinkNode sinkNode;
        sinkNode = new SinkNode(def.id(), def.x(), def.y(), demandMap);
        return sinkNode;
    }
}
