package ca.bcit.comp2522.termproject.resourcerouter.gameplay;

import ca.bcit.comp2522.termproject.resourcerouter.managers.GameController;
import ca.bcit.comp2522.termproject.resourcerouter.util.ResourceType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.Objects;

/**
 * Represents a source node that produces a specific resource type at a fixed interval.
 * <p>
 * A SourceNode is a type of game node responsible for generating a particular resource,
 * which it then outputs through its outgoing pipes to connected nodes in the Resource Router game.
 * The visual representation of the node is a circle whose fill color is determined by the resource type
 * it produces. This class manages the production timing by accumulating elapsed time until it reaches
 * a predetermined production interval, at which point it attempts to deliver the resource to each available
 * outgoing pipe.
 * </p>
 * <p>
 * The node is constructed with a specific resource type, and its production interval is either provided
 * explicitly or defaults to a constant value. The production cycle resets once the resource is successfully
 * delivered to at least one pipe, and the node's active visual style is applied when production occurs.
 * If no pipe accepts the resource, the node remains inactive.
 * </p>
 * <p>
 * As a SourceNode does not receive inputs, it does not have an input connector, and its input connector offset
 * returns a Point2D with NaN values. The output connector is positioned relative to the node's center using
 * predefined offset values.
 * </p>
 * <p>
 * Key responsibilities include:
 * <ul>
 *   <li>Maintaining the production timer and determining when a new resource should be produced.</li>
 *   <li>Attempting to deliver the produced resource to each outgoing pipe that is available and not busy.</li>
 *   <li>Updating the node's visual state to reflect active production (by applying a specific style class)
 *       and resetting the production timer once successful output occurs.</li>
 *   <li>Providing a visual representation (a circle) with dimensions and styles defined by constants,
 *       ensuring consistency and eliminating magic numbers.</li>
 *   <li>Resetting its state (production timer and output readiness) when requested, so that a new production
 *       cycle can begin at the start of a level or upon simulation reset.</li>
 * </ul>
 * </p>
 * <p>
 * By extending the abstract {@link GameNode} class, SourceNode implements all required abstract methods,
 * including those for creating visual elements and for handling updates during simulation ticks.
 * This ensures that the node integrates seamlessly with the overall game simulation and UI update mechanisms.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class SourceNode extends GameNode
{

    private static final double BODY_RADIUS                         = 28.0;
    private static final double NODE_DIAMETER_MULTIPLIER            = 2.0;
    private static final double CONNECTOR_SIZE                      = 10.0;
    private static final double HALF_CONNECTOR_SIZE                 = CONNECTOR_SIZE / 2.0;
    private static final double CONNECTOR_OFFSET_X                  = BODY_RADIUS;
    private static final double CONNECTOR_OFFSET_Y                  = 0.0;
    private static final double TIME_RESET                          = 0.0;
    private static final double NO_CONNECTOR_COORDINATE             = Double.NaN;
    private static final double MIN_PRODUCTION_INTERVAL             = 0.1;
    private static final int    DEFAULT_VALUE                       = 0;

    public static final String STYLE_CLASS                          = "source-node";
    public static final String NODE_ACTIVE_STYLE_CLASS              = "node-active";
    public static final double DEFAULT_PRODUCTION_INTERVAL_SECONDS  = 1.0;

    private final ResourceType  resourceTypeToProduce;
    private final double        productionInterval;

    private double  timeSinceLastProduction;
    private boolean canProduce = true;

    /**
     * Constructs a new SourceNode with the default production interval.
     *
     * @param id   the unique identifier for this node
     * @param x    the x-coordinate of the node's center
     * @param y    the y-coordinate of the node's center
     * @param type the ResourceType produced by this node
     */
    public SourceNode(final String id,
                      final double x,
                      final double y,
                      final ResourceType type)
    {
        this(id, x, y, type, DEFAULT_PRODUCTION_INTERVAL_SECONDS);
    }

    /**
     * Constructs a new SourceNode.
     *
     * @param id       the unique identifier for this node
     * @param x        the x-coordinate of the node's center
     * @param y        the y-coordinate of the node's center
     * @param type     the ResourceType produced by this node
     * @param interval the production interval (in seconds)
     */
    public SourceNode(final String id,
                      final double x,
                      final double y,
                      final ResourceType type,
                      final double interval)
    {
        super(id, x, y);

        validateResourceType(type, id);
        validateInterval(interval, id);

        this.resourceTypeToProduce  = type;
        this.productionInterval     = Math.max(MIN_PRODUCTION_INTERVAL, interval);

        resetState();
    }

    /*
     * Checks that the ResourceType is non-null.
     *
     * @param resourceType the resource type to validate
     * @param nodeId       the node identifier (for error messages)
     *
     * @throws NullPointerException if resourceType is null
     */
    private static void validateResourceType(final ResourceType resourceType,
                                             final String nodeId)
    {
        Objects.requireNonNull(resourceType,
                               "Source type missing for " +
                               nodeId);
    }

    /*
     * Validates the production interval.
     * Adjust or throw exceptions as fits your design (e.g. must be >= 0.1).
     *
     * @param interval the desired production interval in seconds
     * @param nodeId   the node identifier (for error messages)
     *
     * @throws IllegalArgumentException if interval is negative, or zero, etc.
     */
    private static void validateInterval(final double interval,
                                         final String nodeId)
    {
        if (interval <= DEFAULT_VALUE)
        {
            throw new IllegalArgumentException(
                    "Production interval must be positive for node " + nodeId
            );
        }
    }

    /*
     * Sets or removes the active visual style for the source node.
     *
     * @param active true to set the node as active; false otherwise.
     */
    private void setVisualActive(final boolean active)
    {
        if (nodeBodyVisual != null)
        {
            nodeBodyVisual.getStyleClass().remove(NODE_ACTIVE_STYLE_CLASS);
            if (active)
            {
                nodeBodyVisual.getStyleClass().add(NODE_ACTIVE_STYLE_CLASS);
            }
        }
    }

    /**
     * Updates the state of the source node. When the production interval is reached,
     * this node attempts to output its resource to each available outgoing pipe (up to two).
     *
     * @param deltaTime  time elapsed since the last update (in seconds)
     * @param controller the GameController for UI updates and interactions.
     */
    @Override
    public void update(final double deltaTime,
                       final GameController controller)
    {
        if (!controller.isSimulationRunning() ||
            !canProduce)
        {
            setVisualActive(false);
            return;
        }
        timeSinceLastProduction += deltaTime;

        if (timeSinceLastProduction >= productionInterval)
        {
            boolean produced = false;

            for (final Pipe outputPipe : getOutgoingPipes())
            {
                if (outputPipe.isEmpty() && !outputPipe.isBusyThisTick())
                {
                    if (outputPipe.trySetResource(resourceTypeToProduce))
                    {
                        controller.updatePipeVisual(outputPipe);
                        produced = true;
                    }
                }
            }
            if (produced)
            {
                timeSinceLastProduction = TIME_RESET;
                setVisualActive(true);
            }
            else
            {
                setVisualActive(false);
            }
        }
        else
        {
            setVisualActive(false);
        }
    }

    /**
     * Resets the state of the source node.
     */
    @Override
    public void resetState()
    {
        timeSinceLastProduction = productionInterval;
        canProduce = true;
        setVisualActive(false);
    }

    /**
     * Creates and returns the visual representation of the source node.
     *
     * @return the Node representing the body of this source node.
     */
    @Override
    protected Node createNodeBodyVisual()
    {
        final StackPane visualGroup;
        final Circle bodyCircle;

        visualGroup = new StackPane();
        visualGroup.setLayoutX(x - BODY_RADIUS);
        visualGroup.setLayoutY(y - BODY_RADIUS);
        visualGroup.setPrefSize(BODY_RADIUS * NODE_DIAMETER_MULTIPLIER,
                                BODY_RADIUS * NODE_DIAMETER_MULTIPLIER);

        bodyCircle = new Circle(BODY_RADIUS);
        bodyCircle.setFill(resourceTypeToProduce.getDisplayColor());
        bodyCircle.getStyleClass().addAll(NODE_BODY_STYLE_CLASS, STYLE_CLASS);

        visualGroup.getChildren().add(bodyCircle);
        this.nodeBodyVisual = visualGroup;

        final Node result;
        result = visualGroup;

        return result;
    }

    /**
     * Creates and returns the output connector visual.
     *
     * @return the Shape representing the output connector.
     */
    @Override
    protected Shape createOutputConnectorVisual()
    {
        final double cX;
        final double cY;

        cX = x + CONNECTOR_OFFSET_X - HALF_CONNECTOR_SIZE;
        cY = y + CONNECTOR_OFFSET_Y - HALF_CONNECTOR_SIZE;

        final Rectangle outputConnector;
        final Shape result;

        outputConnector = new Rectangle(cX,
                                        cY,
                                        CONNECTOR_SIZE,
                                        CONNECTOR_SIZE);
        result = outputConnector;

        return result;
    }

    /**
     * Source nodes do not have an input connector.
     *
     * @return null.
     */
    @Override
    protected Shape createInputConnectorVisual()
    {
        return null;
    }

    /**
     * Returns the center of the (non-existent) input connector.
     *
     * @return a Point2D with NaN values.
     */
    @Override
    public Point2D getInputConnectorOffset()
    {
        final Point2D inputOffset;
        inputOffset = new Point2D(NO_CONNECTOR_COORDINATE, NO_CONNECTOR_COORDINATE);

        return inputOffset;
    }

    /**
     * Returns the center of the output connector.
     *
     * @return a Point2D representing the output connector's offset.
     */
    @Override
    public Point2D getOutputConnectorOffset()
    {
        final Point2D outputOffset;
        outputOffset= new Point2D(CONNECTOR_OFFSET_X, CONNECTOR_OFFSET_Y);

        return outputOffset;
    }

    /**
     * Source nodes do not have an info label.
     *
     * @return null.
     */
    @Override
    protected Label createInfoLabelVisual()
    {
        return null;
    }
}
