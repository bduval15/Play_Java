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
 * SourceNode represents a source unit in the Resource Router game that produces a specified resource
 * type at regular time intervals.
 * <p>
 * A SourceNode is responsible for generating a particular resource and delivering it through its outgoing pipes.
 * Its functionality is governed by a production interval that determines how often it produces the resource.
 * When the accumulated elapsed time exceeds the production interval, the node attempts to deliver its resource
 * to each outgoing pipe that is available. If production is successful (i.e. at least one pipe accepts the resource),
 * the production timer is reset.
 * </p>
 * <p>
 * Visually, a SourceNode is represented by a circular shape whose fill color reflects the resource type it produces.
 * The node does not have an input connector because it does not accept input resources. Its output connector is
 * positioned relative to its center using predefined offset values.
 * </p>
 * <p>
 * The class also manages an internal production timer and a flag (canProduce) which indicates whether it is ready
 * to generate and deliver a resource. The update cycle (invoked via the update method) uses the elapsed time to determine
 * if the production interval has been reached and then attempts resource output accordingly.
 * </p>
 * <p>
 * Key behaviors include:
 * <ul>
 *   <li>Maintaining and resetting the production timer.</li>
 *   <li>Attempting to deliver the produced resource to every available outgoing pipe.</li>
 *   <li>Updating its visual style to indicate active production when the resource is being dispatched.</li>
 *   <li>Resetting its state to allow a new production cycle when requested.</li>
 * </ul>
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class SourceNode
             extends GameNode
{

    private static final double BODY_RADIUS                         = 28.0;
    private static final double NODE_DIAMETER_MULTIPLIER            = 2.0;
    private static final double CONNECTOR_SIZE                      = 10.0;
    private static final double CONNECTOR_OFFSET_Y                  = 0.0;
    private static final double TIME_RESET                          = 0.0;
    private static final double MIN_PRODUCTION_INTERVAL             = 0.1;
    private static final double DEFAULT_PRODUCTION_INTERVAL_SECONDS = 1.0;

    private static final int    DEFAULT_VALUE                       = 0;

    private static final String NODE_BODY_STYLE_CLASS               = "node";
    private static final String STYLE_CLASS                         = "source-node";
    private static final String NODE_ACTIVE_STYLE_CLASS             = "node-active";

    private static final double HALF_CONNECTOR_SIZE;
    private static final double CONNECTOR_OFFSET_X;
    private static final double NO_CONNECTOR_COORDINATE;

    private final ResourceType  resourceType;
    private final double        productionInterval;

    private double  productionTimer;
    private boolean productionAvailable;

    static
    {
        HALF_CONNECTOR_SIZE     = CONNECTOR_SIZE / 2.0;
        CONNECTOR_OFFSET_X      = BODY_RADIUS;
        NO_CONNECTOR_COORDINATE = Double.NaN;
    }

    {
        productionAvailable = true;
    }

    /**
     * Constructs a new SourceNode with the default production interval.
     * <p>
     * This constructor invokes the full-parameter constructor using
     * DEFAULT_PRODUCTION_INTERVAL_SECONDS as the production interval.
     * </p>
     *
     * @param nodeId         the unique identifier for this node
     * @param xCoordinate    the xCoordinate-coordinate of the node's center
     * @param yCoordinate    the yCoordinate-coordinate of the node's center
     * @param resourceType   the ResourceType produced by this node
     *
     */
    public SourceNode(final String nodeId,
                      final double xCoordinate,
                      final double yCoordinate,
                      final ResourceType resourceType)
    {
        this(nodeId,
             xCoordinate,
             yCoordinate,
             resourceType,
             DEFAULT_PRODUCTION_INTERVAL_SECONDS);
    }

    /**
     * Constructs a new SourceNode with the specified production interval.
     * <p>
     * The constructor performs the following actions:
     * <ol>
     *   <li>Calls the superclass (GameNode) constructor with the node nodeId and coordinates.</li>
     *   <li>Validates that the provided ResourceType is non-null; if it is null, a NullPointerException is thrown.</li>
     *   <li>Validates that the production interval is positive (greater than zero);
     *       if not, an IllegalArgumentException is thrown.</li>
     *   <li>Sets the resource resourceType to be produced and determines the production interval,
     *       ensuring that it is at least MIN_PRODUCTION_INTERVAL.</li>
     *   <li>Resets the production timer and state by invoking resetState().</li>
     * </ol>
     * </p>
     *
     * @param nodeId                the unique identifier for this node
     * @param xCoordinate           the xCoordinate-coordinate of the node's center
     * @param yCoordinate           the yCoordinate-coordinate of the node's center
     * @param resourceType          the ResourceType produced by this node; must not be null
     * @param interval              the production interval in seconds; must be positive
     *
     * @throws NullPointerException     if the resource resourceType is null
     * @throws IllegalArgumentException if the production interval is not positive
     */
    public SourceNode(final String nodeId,
                      final double xCoordinate,
                      final double yCoordinate,
                      final ResourceType resourceType,
                      final double interval)
    {
        super(nodeId,
              xCoordinate,
              yCoordinate);

        validateResourceType(resourceType, nodeId);
        validateInterval(interval, nodeId);

        this.resourceType           = resourceType;
        this.productionInterval     = Math.max(MIN_PRODUCTION_INTERVAL,
                                               interval);

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
     * If active is true, it adds a designated active style class.
     * Otherwise, it removes the active style class from the node visual.
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
     * Updates the SourceNode on each simulation tick.
     * <p>
     * This method increments an internal timer (timeSinceLastProduction) by the elapsed time. When the accumulated
     * time exceeds or equals the production interval, the node attempts to deliver its
     * produced resource to all outgoing pipes:
     * <ol>
     *   <li>Iterates over each outgoing pipe obtained from getOutgoingPipes().</li>
     *   <li>For each pipe that is empty and considered available (determined via isBusyThisTick()),
     *       the node calls trySetResource to attempt to set its produced resource on the pipe.</li>
     *   <li>If at least one pipe accepts the resource, the production timer is reset to TIME_RESET,
     *       and the node’s visual is updated to reflect an active production state.</li>
     *   <li>If no pipe accepts the resource, the node remains inactive and the active style is removed.</li>
     * </ol>
     * If the simulation is not running or if the node is not ready to produce (canProduce is false), this method
     * simply ensures that no active style is applied.
     * </p>
     *
     * @param timeSeconds the elapsed time in seconds since the last update tick
     * @param controller  the GameController responsible for updating pipe visuals and simulation state
     */
    @Override
    public void update(final double timeSeconds,
                       final GameController controller)
    {
        if (!controller.isSimulationActive() ||
            !productionAvailable)
        {
            setVisualActive(false);
            return;
        }
        productionTimer += timeSeconds;

        if (productionTimer >= productionInterval)
        {
            boolean produced = false;

            for (final Pipe outputPipe : getOutgoingPipes())
            {
                if (outputPipe.isEmpty() && outputPipe.isBusyThisTick())
                {
                    if (outputPipe.trySetResource(resourceType))
                    {
                        controller.updatePipeVisual(outputPipe);
                        produced = true;
                    }
                }
            }
            if (produced)
            {
                productionTimer = TIME_RESET;
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
     * Resets the state of the SourceNode so that it is ready to begin a new production cycle.
     * <p>
     * This method sets the internal production timer (timeSinceLastProduction) to the production interval,
     * enables production by setting canProduce to true, and clears any active visual indication.
     * </p>
     */
    @Override
    public void resetState()
    {
        productionTimer = productionInterval;
        productionAvailable = true;
        setVisualActive(false);
    }

    /**
     * Creates and returns the visual representation of the SourceNode.
     * <p>
     * The visual is composed of a circular shape representing the node's body:
     * <ol>
     *   <li>The method creates a StackPane whose layout positions are set so that the circle is centered at
     *       (xCord, yCord) by subtracting the BODY_RADIUS from both coordinates.</li>
     *   <li>A Circle is constructed with a radius equal to BODY_RADIUS, and its fill color is set to the display color
     *       associated with the produced resource.</li>
     *   <li>CSS style classes (NODE_BODY_STYLE_CLASS and STYLE_CLASS) are applied to the circle
     *       to define its appearance.</li>
     *   <li>The circle is added to the StackPane, which is then stored as the node's body visual.</li>
     * </ol>
     * This assembled Node is returned to serve as the visual representation of the SourceNode.
     * </p>
     *
     * @return a Node representing the SourceNode's body.
     */
    @Override
    Node createNodeBodyVisual()
    {
        final StackPane visualGroup;
        final Circle bodyCircle;

        visualGroup = new StackPane();
        visualGroup.setLayoutX(getXCoordinate() - BODY_RADIUS);
        visualGroup.setLayoutY(getYCoordinate() - BODY_RADIUS);
        visualGroup.setPrefSize(BODY_RADIUS * NODE_DIAMETER_MULTIPLIER,
                                BODY_RADIUS * NODE_DIAMETER_MULTIPLIER);

        bodyCircle = new Circle(BODY_RADIUS);
        bodyCircle.setFill(resourceType.getDisplayColor());
        bodyCircle.getStyleClass().addAll(NODE_BODY_STYLE_CLASS, STYLE_CLASS);

        visualGroup.getChildren().add(bodyCircle);
        this.nodeBodyVisual = visualGroup;

        final Node result;
        result = visualGroup;

        return result;
    }

    /**
     * Creates and returns the visual representation of the output connector.
     * <p>
     * The output connector is depicted as a small Rectangle. Its position is calculated by:
     * <ol>
     *   <li>Computing the X coordinate as the sum of the node's x-coordinate and CONNECTOR_OFFSET_X,
     *       minus half of CONNECTOR_SIZE.</li>
     *   <li>Computing the Y coordinate similarly with CONNECTOR_OFFSET_Y and half of CONNECTOR_SIZE.</li>
     * </ol>
     * The constructed Rectangle (with dimensions CONNECTOR_SIZE x CONNECTOR_SIZE) is returned.
     * </p>
     *
     * @return a Shape (Rectangle) representing the output connector.
     */
    @Override
    Shape createOutputConnectorVisual()
    {
        final double connectorXCoordinate;
        final double connectorYCoordinate;

        connectorXCoordinate = getXCoordinate() + CONNECTOR_OFFSET_X - HALF_CONNECTOR_SIZE;
        connectorYCoordinate = getYCoordinate() + CONNECTOR_OFFSET_Y - HALF_CONNECTOR_SIZE;

        final Rectangle outputConnector;
        final Shape     result;

        outputConnector = new Rectangle(connectorXCoordinate,
                                        connectorYCoordinate,
                                        CONNECTOR_SIZE,
                                        CONNECTOR_SIZE);
        result = outputConnector;

        return result;
    }

    /**
     * Source nodes do not have an input connector; therefore, this method returns null.
     *
     * @return null.
     */
    @Override
    Shape createInputConnectorVisual()
    {
        return null;
    }

    /**
     * Returns the offset for the input connector.
     * <p>
     * Since SourceNode does not have an input connector, this method returns a Point2D with NaN values.
     * </p>
     *
     * @return a Point2D with NaN values.
     */
    @Override
    public Point2D getInputConnectorOffset()
    {
        final Point2D inputOffset;
        inputOffset = new Point2D(NO_CONNECTOR_COORDINATE,
                                  NO_CONNECTOR_COORDINATE);

        return inputOffset;
    }

    /**
     * Returns the offset for the output connector.
     * <p>
     * The output connector's offset is determined by the constants CONNECTOR_OFFSET_X and CONNECTOR_OFFSET_Y.
     * </p>
     *
     * @return a Point2D representing the output connector's offset.
     */
    @Override
    public Point2D getOutputConnectorOffset()
    {
        final Point2D outputOffset;
        outputOffset= new Point2D(CONNECTOR_OFFSET_X,
                                  CONNECTOR_OFFSET_Y);

        return outputOffset;
    }

    /**
     * Source nodes do not use an info label; thus, this method returns null.
     *
     * @return null.
     */
    @Override
    protected Label createInfoLabelVisual()
    {
        return null;
    }
}
