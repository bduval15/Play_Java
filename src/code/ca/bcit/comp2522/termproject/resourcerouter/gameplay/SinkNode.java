package ca.bcit.comp2522.termproject.resourcerouter.gameplay;

import ca.bcit.comp2522.termproject.resourcerouter.managers.GameController;
import ca.bcit.comp2522.termproject.resourcerouter.util.ResourceType;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.*;

/**
 * SinkNode represents a sink node in the Resource Router game, responsible for consuming incoming resources
 * according to a predetermined resource demand map.
 * <p>
 * Each SinkNode is configured with an initial demand—a mapping of ResourceType to the number of units required—
 * which remains constant throughout the node’s lifetime. The node maintains a mutable current demand that is updated
 * as resources are received. When resources arrive via incoming pipes, SinkNode deducts the appropriate amounts
 * from its current demand. When all demanded resources have been received (i.e. every entry in the current demand
 * map has a value of zero or less), the sink is considered satisfied, and its visual appearance changes to indicate
 * a win state (for example, filling the node with a win color such as gold).
 * </p>
 * <p>
 * Visually, a SinkNode is represented by an octagon that is constructed using a Polygon, with a dynamic fill that
 * changes based on its state:
 * <ul>
 *   <li>If unsatisfied, the fill is determined by the remaining resources: a solid color is used when only one
 *       resource type is required, or a linear gradient is constructed if multiple resources remain.</li>
 *   <li>If fully satisfied, the fill changes to a win color (lime green) and a
 *       continuous rotation animation is triggered.</li>
 *   <li>If an error condition occurs—such as receiving a resource that is not demanded or exceeding the required
 *       quantity—the node enters an error state, its fill turns red, and an error indicator is displayed via an
 *       info label.</li>
 * </ul>
 * </p>
 * <p>
 * The SinkNode also manages demand indicators which provide visual feedback on unsatisfied resource requirements.
 * These indicators are arranged in a FlowPane overlay that is transparent to mouse events. An error message may also
 * be displayed through a Label when the node is in error state.
 * </p>
 * <p>
 * The node’s update cycle (invoked on each simulation tick via the update method) carries out the following steps:
 * <ol>
 *   <li>If the simulation is not running (as determined by a GameController),
 *       the node only updates its visual state.</li>
 *   <li>If the sink is satisfied (all demanded resources have been fulfilled), it clears any residual resources from
 *       its incoming pipes and updates the corresponding visuals.</li>
 *   <li>If the sink is not yet satisfied, it iterates over all incoming pipes:
 *       <ul>
 *         <li>For each pipe that has a resource:
 *             <ul>
 *               <li>If the resource matches an entry in the demand map and the current demand is greater than zero,
 *                   the node decrements the demanded count, resets the error tick counter, clears the resource from the
 *                   pipe, and updates the pipe’s visual.</li>
 *               <li>If the resource is not part of the demand or exceeds the required amount, the node increments an
 *                   error tick counter. Once the counter exceeds a predefined threshold, the node enters an error state
 *                   and signals the GameController to halt the simulation.</li>
 *             </ul>
 *         </li>
 *       </ul>
 *   </li>
 *   <li>After processing, the node refreshes its visual state to reflect the latest demand,
 *       satisfaction, or error status.</li>
 * </ol>
 * </p>
 * <p>
 * Additionally, SinkNode provides methods for resetting its state:
 * <ul>
 *   <li>{@code resetState()} clears the current demand (restoring it from the initial demand), resets error flags
 *       and counters, stops any ongoing animations, and updates the visual appearance accordingly.</li>
 *   <li>{@code clearErrorStateOnly()} selectively clears only the error state and resets animations without altering
 *       the current demand.</li>
 * </ul>
 * </p>
 * <p>
 * Visual elements specific to SinkNode include:
 * <ul>
 *   <li>An octagon body (created via the {@code createOctagon()} helper method) that serves as the primary visual
 *       component. Its fill is dynamically updated via {@code updateFill()} based on the node's current demand
 *       or error state.</li>
 *   <li>A FlowPane that displays demand indicators, arranged with specified gaps, padding, and wrap length.</li>
 *   <li>An optional info label, used primarily for error notification.</li>
 *   <li>Two types of animations—RotateTransition for continuous rotation when satisfied and ScaleTransition for a
 *       pulsing effect—that provide visual feedback of active processing.</li>
 * </ul>
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class SinkNode
             extends GameNode
{

    private static final String NODE_WAITING_STYLE_CLASS   = "node-waiting";
    private static final String NODE_SATISFIED_STYLE_CLASS = "node-satisfied";
    private static final String SINK_ERROR_STATE_TEXT      = "sink-error-symbol";
    private static final String EMPTY_STRING               = "";
    private static final String NODE_BODY_STYLE_CLASS      = "node";

    private static final double RADIUS                                  = 30.0;
    private static final double NODE_DIAMETER_MULTIPLIER                = 2.0;
    private static final double CONNECTOR_SIZE                          = 10.0;
    private static final double INPUT_CONNECTOR_OFFSET_Y                = 0.0;
    private static final double DEMAND_INDICATOR_PADDING                = 5.0;
    private static final double DEMAND_INDICATOR_PREF_WRAP_MULTIPLIER   = 1.8;
    private static final double ROTATE_TRANSITION_DURATION_SECONDS      = 2.0;
    private static final double FULL_ROTATION_ANGLE                     = 360.0;
    private static final double RESET_ROTATION_ANGLE                    = 0.0;
    private static final double OCTAGON_ANGLE_STEP_DEGREES              = 45.0;
    private static final double OCTAGON_INITIAL_ANGLE_OFFSET            = -22.5;
    private static final double GRADIENT_LEFT_HALF                      = 0.0;
    private static final double GRADIENT_MIDDLE                         = 0.5;
    private static final double GRADIENT_RIGHT_HALF                     = 1.0;
    private static final double SET_FROM_VALUE                          = 1.0;
    private static final double SET_TO_VALUE                            = 1.2;

    private static final double HALF_CONNECTOR_SIZE;
    private static final double INPUT_CONNECTOR_OFFSET_X;
    private static final double OUTPUT_CONNECTOR_OFFSET_VALUE;

    private static final int SCALE_DURATION                  = 1;
    private static final int FLOW_PANE_HGAP                  = 2;
    private static final int FLOW_PANE_VGAP                  = 2;
    private static final int OCTAGON_SIDES                   = 8;
    private static final int RESOURCE_DECREMENT_THRESHOLD    = 1;
    private static final int DEFAULT_VALUE                   = 0;
    private static final int COLOUR_SIZE                     = 1;
    private static final int OFFSET_CALCULATOR               = 1;
    private static final int FIRST_COLOUR                    = 0;
    private static final int SECOND_COLOUR                   = 1;
    private static final int ERROR_TICK_THRESHOLD            = 3;

    private static final String STYLE_CLASS = "sink-node";

    private final Map<ResourceType, Integer> initialDemand;
    private final Map<ResourceType, Integer> currentDemand;

    private int                 errorTickCounter;
    private boolean             errorState;
    private Polygon             bodyShape;
    private RotateTransition    rotateTransition;
    private ScaleTransition     pulseTransition;
    private Label               infoLabelVisual;

    static
    {
        HALF_CONNECTOR_SIZE = CONNECTOR_SIZE / 2.0;
        INPUT_CONNECTOR_OFFSET_X = -RADIUS;
        OUTPUT_CONNECTOR_OFFSET_VALUE = Double.NaN;
    }

    {
        errorState = false;
    }

    /**
     * Constructs a new SinkNode with the specified nodeId, coordinates, and resource demand.
     *
     * <p>
     * The constructor performs several validation steps:
     * <ol>
     *   <li>Validates that the demand map is non-null, non-empty, and that every resource key
     *       is non-null with a positive quantity. If validation fails, an exception is thrown.</li>
     *   <li>Saves an immutable copy of the initial demand and initializes a mutable currentDemand map.</li>
     *   <li>Calls resetState() to initialize the node’s runtime state
     *       (clearing any errors, resetting counters, etc.).</li>
     * </ol>
     * </p>
     *
     * @param nodeId        the unique identifier for the node
     * @param xCoordinate         the xCoordinate-coordinate of the node's center
     * @param yCoordinate         the yCoordinate-coordinate of the node's center
     * @param demandMap a map mapping ResourceType to the required quantity; must be non-null and non-empty
     *
     * @throws NullPointerException if the demand map is null
     * @throws IllegalArgumentException if the demand map is empty or contains non-positive values
     *
     */
    public SinkNode(final String nodeId,
                    final double xCoordinate,
                    final double yCoordinate,
                    final Map<ResourceType, Integer> demandMap)
    {
        super(nodeId,
              xCoordinate,
              yCoordinate);

        validateDemandMap(demandMap, nodeId);

        this.initialDemand = new LinkedHashMap<>(demandMap);
        this.currentDemand = new HashMap<>();

        resetState();
    }

    /*
     * Ensures the demand map is not null, not empty, and that all values are positive.
     *
     * Iterates through each entry verifying
     * that each ResourceType key is not null and that each quantity is positive.
     *
     * @param demandMap the map of resource demands
     * @param nodeId    the identifier of the node (for error messages)
     *
     * @throws NullPointerException     if demandMap is null
     * @throws IllegalArgumentException if demandMap is empty or contains invalid demand quantities
     *
     */
    private static void validateDemandMap(final Map<ResourceType, Integer> demandMap,
                                          final String nodeId)
    {
        Objects.requireNonNull(demandMap,
                               "Demand map cannot be null for node " +
                               nodeId);

        if (demandMap.isEmpty())
        {
            throw new IllegalArgumentException("Demand map is empty for node " + nodeId);
        }

        for (final Map.Entry<ResourceType, Integer> entry : demandMap.entrySet())
        {
            if (entry.getKey() == null)
            {
                throw new IllegalArgumentException(
                        "Demand map contains a null ResourceType for node " + nodeId
                );
            }

            final Integer demandValue;
            demandValue = entry.getValue();

            if (demandValue == null ||
                demandValue <= DEFAULT_VALUE)
            {
                throw new IllegalArgumentException(
                        "Invalid demand quantity (" + demandValue + ") for resource " +
                                entry.getKey() + " in node " + nodeId
                );
            }
        }
    }

    /*
     * Creates a new Polygon to represent an octagon.
     *
     * For each of the OCTAGON_SIDES, calculates the vertex position using:
     *   angle (in degrees) = (OCTAGON_ANGLE_STEP_DEGREES * i) + OCTAGON_INITIAL_ANGLE_OFFSET,
     *   then converts to radians and computes (x, y) as:
     *        vertexX = center + RADIUS * cos(angle)
     *        vertexY = center + RADIUS * sin(angle)
     *
     * Adds these vertices to the Polygon's point list and returns the Polygon.
     *
     */
    private Polygon createOctagon()
    {
        final Polygon   octagon;
        final double    center;

        octagon     = new Polygon();
        center      = RADIUS;

        for (int i = 0; i < OCTAGON_SIDES; i++)
        {
            final double angleDegree;
            final double angleRadius;
            final double vertexX;
            final double vertexY;

            angleDegree    = OCTAGON_ANGLE_STEP_DEGREES * i + OCTAGON_INITIAL_ANGLE_OFFSET;
            angleRadius    = Math.toRadians(angleDegree);
            vertexX        = center + RADIUS * Math.cos(angleRadius);
            vertexY        = center + RADIUS * Math.sin(angleRadius);
            octagon.getPoints().addAll(vertexX, vertexY);
        }
        return octagon;
    }

    /*
     * Updates the fill of the bodyShape based on the current state:
     *
     *  a. If errorState is true, sets the fill to red.
     *  b. Otherwise, checks the remaining demands:
     *     - If no resources are still required, fills with lime green.
     *     - If only one resource type remains unsatisfied, uses a solid fill of that resource's display color.
     *     - If multiple resource types remain, computes a linear gradient from the
     *          colors of the first two resources.
     *
     * Sets the fill on the bodyShape accordingly.
     *
     */
    private void updateFill()
    {
        if (bodyShape == null)
        {
            return;
        }

        if (errorState)
        {
            bodyShape.setFill(Color.RED);
            return;
        }

        final List<Color> colors;
        colors = currentDemand.entrySet().stream()
                .filter(e -> e.getValue() > DEFAULT_VALUE)
                .map(e -> e.getKey().getDisplayColor())
                .toList();

        if (colors.isEmpty())
        {
            bodyShape.setFill(Color.LIMEGREEN);
        }
        else if (colors.size() == COLOUR_SIZE)
        {
            final Color singleColor;
            singleColor = colors.getFirst();
            bodyShape.setFill(singleColor);
        }
        else
        {
            final LinearGradient gradient;
            gradient = getLinearGradient(colors);
            bodyShape.setFill(gradient);
        }
    }

    /*
     * Given a list of Colors (expected to have at least two colors), constructs a linear gradient:
     *  a. Retrieves the left and right colors from the list (first and second).
     *
     *  b. Creates an array of Stops with positions at GRADIENT_LEFT_HALF, GRADIENT_MIDDLE (twice),
     *     and GRADIENT_RIGHT_HALF corresponding to leftColor and rightColor.
     *
     *  c. Creates and returns a new LinearGradient configured with these stops,
     *     spanning from position (GRADIENT_LEFT_HALF, GRADIENT_MIDDLE) to
     *     (GRADIENT_MIDDLE, GRADIENT_RIGHT_HALF), with no cycle.
     *
     */
    private static LinearGradient getLinearGradient(final List<Color> colors)
    {
        final Color             leftColor;
        final Color             rightColor;
        final Stop[]            stops;
        final LinearGradient    gradient;

        leftColor   = colors.getFirst();
        rightColor  = colors.get(SECOND_COLOUR);

        stops = new Stop[]
                {
                new Stop(GRADIENT_LEFT_HALF, leftColor),
                new Stop(GRADIENT_MIDDLE, leftColor),
                new Stop(GRADIENT_MIDDLE, rightColor),
                new Stop(GRADIENT_RIGHT_HALF, rightColor)
        };

        gradient = new LinearGradient(
                DEFAULT_VALUE, DEFAULT_VALUE,
                SECOND_COLOUR, FIRST_COLOUR,
                true,
                CycleMethod.NO_CYCLE,
                stops);

        return gradient;
    }

    /*
     * Updates the visual appearance of the SinkNode based on its current state.
     *
     * If errorState is true, fills the bodyShape with red, stops any animations, and shows an error indicator.
     *
     * If the sink is satisfied, sets the fill to a win color (e.g., gold), and starts rotation and pulse animations.
     *
     * Otherwise, calls updateFill() to update the fill based on the current input demands and stops animations.
     *
     * Also, if an info label exists, it is updated accordingly (for example, displaying an error symbol).
     *
     */
    private void updateVisualState()
    {
        if (bodyShape == null)
        {
            return;
        }

        bodyShape.getStyleClass().removeAll(NODE_WAITING_STYLE_CLASS, NODE_SATISFIED_STYLE_CLASS);

        if (errorState)
        {
            bodyShape.setFill(Color.RED);
            rotateTransition.stop();
            pulseTransition.stop();
            infoLabelVisual.setText("!");
            infoLabelVisual.setVisible(true);
            infoLabelVisual.toFront();
        }
        else if (isSatisfied())
        {
            bodyShape.setFill(Color.GOLD);
            rotateTransition.play();
            pulseTransition.play();
            infoLabelVisual.setText(EMPTY_STRING );
            infoLabelVisual.setVisible(false);
        }
        else
        {
            updateFill();
            rotateTransition.stop();
            bodyShape.setRotate(RESET_ROTATION_ANGLE);
            infoLabelVisual.setText(EMPTY_STRING );
            infoLabelVisual.setVisible(false);
        }
    }

    /**
     * Checks if the sink node has been fully satisfied.
     * <p>
     * It returns true if every resource in the current demand has a required quantity of zero or less.
     * </p>
     *
     * @return true if all resource demands are met; false otherwise.
     *
     */
    public boolean isSatisfied()
    {
        final boolean satisfied;
        satisfied = currentDemand.values()
                                 .stream()
                                 .allMatch(c -> c <= DEFAULT_VALUE);

        return satisfied;
    }

    /**
     * Checks whether the sink node is in an error state.
     * <p>
     * Returns true if the node has received an unexpected resource or extra input beyond its demand.
     * </p>
     *
     * @return true if the sink is in an error state; false otherwise.
     *
     */
    public boolean inErrorState()
    {
        final boolean inError;
        inError= errorState;

        return inError;
    }

    /**
     * Resets the sink node to its initial state.
     *
     * <p>
     * This method restores the initial demands by copying the initialDemand map into the currentDemand map.
     * It clears any errors, resets error tick counters, stops animations (such as rotation),
     * resets the body rotation, updates the fill, and then calls updateVisualState to reflect the reset.
     * </p>
     *
     */
    @Override
    public void resetState()
    {
        if (currentDemand == null ||
            initialDemand == null)
        {
            return;
        }

        currentDemand.clear();
        currentDemand.putAll(initialDemand);
        errorState = false;
        errorTickCounter = DEFAULT_VALUE;

        if (bodyShape != null)
        {
            rotateTransition.stop();
            bodyShape.setRotate(RESET_ROTATION_ANGLE);
            updateFill();
        }

        updateVisualState();
    }

    /**
     * Clears only the error state of the sink node.
     *
     * <p>
     * If the node is currently in an error state, this method resets the error flag and error tick counter,
     * stops the rotation animation, resets the rotation of the body, updates the fill, and refreshes the visuals.
     * </p>
     *
     */
    public void clearErrorStateOnly()
    {
        if (errorState)
        {
            errorState = false;
            errorTickCounter    = DEFAULT_VALUE;

            if (bodyShape != null)
            {
                rotateTransition.stop();
                bodyShape.setRotate(RESET_ROTATION_ANGLE);
                updateFill();
            }
            updateVisualState();
        }
    }

    /**
     * Creates the visual representation for the output connector.
     *
     * <p>
     * Sinks do not have an output connector, so this method returns null.
     * </p>
     *
     * @return null.
     *
     */
    @Override
    Shape createOutputConnectorVisual()
    {
        return null;
    }

    /**
     * Creates and returns the visual representation for the input connector.
     *
     * <p>
     * The input connector is depicted as a Rectangle. Its X coordinate is computed by adding INPUT_CONNECTOR_OFFSET_X
     * (minus half of the connector size) to the node's x-coordinate, and its Y coordinate is computed similarly using
     * INPUT_CONNECTOR_OFFSET_Y.
     * </p>
     *
     * @return a Rectangle representing the input connector.
     *
     */
    @Override
    Shape createInputConnectorVisual()
    {
        final double connectorXCoordinate;
        final double connectorYCoordinate;
        final Rectangle rectangle;

        connectorXCoordinate      = getXCoordinate() + INPUT_CONNECTOR_OFFSET_X - HALF_CONNECTOR_SIZE;
        connectorYCoordinate      = getYCoordinate() + INPUT_CONNECTOR_OFFSET_Y - HALF_CONNECTOR_SIZE;
        rectangle                 = new Rectangle(connectorXCoordinate,
                                                  connectorYCoordinate,
                                                  CONNECTOR_SIZE,
                                                  CONNECTOR_SIZE);

        return rectangle;
    }

    /**
     * Creates and returns the visual representation for the information label.
     *
     * <p>
     * For SinkNode, the information label is used for error display. It is styled with a specific class
     * and initially hidden.
     * </p>
     *
     * @return a Label initialized with an empty string, styled for error indication, and initially invisible.
     *
     */
    @Override
    Label createInfoLabelVisual()
    {
        final Label infoLabel;

        infoLabel = new Label(EMPTY_STRING);
        infoLabel.getStyleClass().add(SINK_ERROR_STATE_TEXT);
        infoLabel.setVisible(false);

        return infoLabel;
    }

    /**
     * Creates and returns the visual representation of the sink node's body.
     *
     * <p>
     * The construction is as follows:
     * <ol>
     *   <li>Create a StackPane positioned such that its center aligns with the node's (x, y)
     *       coordinates using RADIUS.</li>
     *   <li>Create an octagon Polygon by calling createOctagon(); the octagon represents the sink's main body.
     *       Apply the NODE_BODY_STYLE_CLASS and STYLE_CLASS to this shape.</li>
     *   <li>Call updateFill() to set the initial fill color based on current demand and state.</li>
     *   <li>Create a FlowPane for demand indicators with specified horizontal and vertical gaps, padding,
     *       and a preferred wrap length based on RADIUS and a multiplier.</li>
     *   <li>Make the FlowPane mouse transparent.</li>
     *   <li>Create the info label visual if not already created.</li>
     *   <li>Add the octagon, demand indicator pane, and info label into the StackPane.</li>
     *   <li>Initialize animations: configure a RotateTransition (duration, angle, cycle count, interpolator)
     *       for continuous rotation, and a ScaleTransition (duration, scale factors, auto-reverse) for pulsing.</li>
     *   <li>Call updateVisualState() to ensure the node's visuals reflect its current state.</li>
     * </ol>
     * </p>
     *
     * @return the assembled Node representing the SinkNode's body.
     *
     */
    @Override
    Node createNodeBodyVisual()
    {
        final StackPane nodeContainer;
        nodeContainer = new StackPane();
        nodeContainer.setLayoutX(getXCoordinate() - RADIUS);
        nodeContainer.setLayoutY(getYCoordinate() - RADIUS);
        nodeContainer.setPrefSize(RADIUS * NODE_DIAMETER_MULTIPLIER,
                                  RADIUS * NODE_DIAMETER_MULTIPLIER);

        bodyShape = createOctagon();
        bodyShape.getStyleClass().addAll(NODE_BODY_STYLE_CLASS,
                                         STYLE_CLASS);

        updateFill();

        final FlowPane demandIndicatorPane;
        demandIndicatorPane = new FlowPane(FLOW_PANE_HGAP,
                                           FLOW_PANE_VGAP);
        demandIndicatorPane.setAlignment(javafx.geometry.Pos.CENTER);
        demandIndicatorPane.setPadding(new Insets(DEMAND_INDICATOR_PADDING));
        demandIndicatorPane.setPrefWrapLength(RADIUS * DEMAND_INDICATOR_PREF_WRAP_MULTIPLIER);
        demandIndicatorPane.getStyleClass().add("demand-indicator-pane");
        demandIndicatorPane.setMouseTransparent(true);

        if (infoLabelVisual == null)
        {
            infoLabelVisual = createInfoLabelVisual();
        }

        nodeContainer.getChildren().addAll(bodyShape,
                                demandIndicatorPane,
                                infoLabelVisual);

        this.nodeBodyVisual = nodeContainer;

        rotateTransition = new RotateTransition(Duration.seconds(ROTATE_TRANSITION_DURATION_SECONDS),
                                                bodyShape);
        rotateTransition.setFromAngle(RESET_ROTATION_ANGLE);
        rotateTransition.setByAngle(FULL_ROTATION_ANGLE);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);

        pulseTransition = new ScaleTransition(Duration.seconds(SCALE_DURATION),
                                              bodyShape);
        pulseTransition.setFromX(SET_FROM_VALUE);
        pulseTransition.setFromY(SET_FROM_VALUE);
        pulseTransition.setToX(SET_TO_VALUE);
        pulseTransition.setToY(SET_TO_VALUE);
        pulseTransition.setCycleCount(Animation.INDEFINITE);
        pulseTransition.setAutoReverse(true);

        updateVisualState();
        return nodeContainer;
    }

    /**
     * Returns the offset for the input connector.
     * <p>
     * Constructs a Point2D using the constants INPUT_CONNECTOR_OFFSET_X and INPUT_CONNECTOR_OFFSET_Y.
     * </p>
     *
     * @return a Point2D representing the input connector's offset.
     */
    @Override
    public Point2D getInputConnectorOffset()
    {
        final Point2D offset;
        offset = new Point2D(INPUT_CONNECTOR_OFFSET_X,
                             INPUT_CONNECTOR_OFFSET_Y);

        return offset;
    }

    /**
     * Returns the offset for the output connector.
     * <p>
     * Since SinkNode does not have an output connector, this method returns a Point2D with NaN values.
     * </p>
     *
     * @return a Point2D with NaN values.
     */
    @Override
    public Point2D getOutputConnectorOffset()
    {
        final Point2D offset;
        offset = new Point2D(OUTPUT_CONNECTOR_OFFSET_VALUE,
                             OUTPUT_CONNECTOR_OFFSET_VALUE);

        return offset;
    }

    /**
     * Updates the SinkNode based on the simulation tick.
     * <p>
     * The update cycle performs the following steps:
     * <ol>
     *   <li>If the simulation is not running (as determined by the GameController),
     *       simply update the visual state and exit.</li>
     *   <li>If the sink node is satisfied (i.e. all resource demands in currentDemand are zero or less),
     *       clear any resource from incoming pipes:
     *       <ul>
     *         <li>Create a copy of the incoming pipes list.</li>
     *         <li>For each pipe with a resource, clear its resource and
     *             update the pipe's visuals via GameController.</li>
     *         <li>Update the node's visual state and exit the update method.</li>
     *       </ul>
     *   </li>
     *   <li>If the sink is not satisfied, iterate over a copy of incoming pipes and process each:
     *       <ol type="a">
     *         <li>Retrieve the resource from the pipe.</li>
     *         <li>If the resource is part of the node's demand and the current demand is greater than zero:
     *             <ul>
     *               <li>Reset the error tick counter.</li>
     *               <li>Clear the resource from the pipe and update the pipe's visual.</li>
     *               <li>Decrement the demanded count in currentDemand for that resource.</li>
     *             </ul>
     *         </li>
     *         <li>If the resource is not demanded or is excess, increment the error tick counter,
     *             update the fill, and update the pipe visual.
     *             If the error tick counter exceeds the ERROR_TICK_THRESHOLD,
     *             set errorState to true and stop the simulation with an error message.</li>
     *       </ol>
     *   </li>
     *   <li>Finally, update the visual state of the sink node to reflect any changes.</li>
     * </ol>
     * </p>
     *
     * @param timeSeconds the elapsed time in seconds since the last tick (not used for logic in this method)
     * @param gameController the GameController instance used to check simulation status, update pipe visuals, and stop the simulation if an error occurs
     */
    @Override
    public void update(final double timeSeconds,
                       final GameController gameController)
    {
        if (!gameController.isSimulationActive())
        {
            updateVisualState();
            return;
        }

        if (isSatisfied())
        {
            final List<Pipe> pipesToClear;
            pipesToClear = new ArrayList<>(getIncomingPipes());

            for (final Pipe pipe : pipesToClear)
            {
                if (pipe.getCurrentResource() != null)
                {
                    pipe.clearResource();
                    gameController.updatePipeVisual(pipe);
                }
            }
            updateVisualState();
            return;
        }

        final List<Pipe> pipesToProcess;
        pipesToProcess = new ArrayList<>(getIncomingPipes());

        for (final Pipe pipe : pipesToProcess)
        {
            final ResourceType resource;
            resource = pipe.getCurrentResource();

            if (resource != null)
            {
                if (currentDemand.containsKey(resource) &&
                    currentDemand.get(resource) > DEFAULT_VALUE)
                {
                    errorTickCounter = DEFAULT_VALUE;
                    pipe.clearResource();
                    gameController.updatePipeVisual(pipe);

                    currentDemand.compute(resource, (key, c) -> {
                        if (c == null || c <= RESOURCE_DECREMENT_THRESHOLD)
                        {
                            return DEFAULT_VALUE;
                        }
                        else
                        {
                            return c - OFFSET_CALCULATOR;
                        }
                    });
                }
                else
                {
                    errorTickCounter++;
                    updateFill();
                    gameController.updatePipeVisual(pipe);

                    if (errorTickCounter >= ERROR_TICK_THRESHOLD)
                    {
                        errorState = true;
                        gameController.stopSimulation("Sink Error (" + getNodeId() + ")! Reset pipes (R).");
                    }
                }
            }
        }
        updateVisualState();
    }
}
