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
 * The SinkNode class represents a sink node in the Resource Router game,
 * responsible for consuming incoming resources based on a predefined demand map.
 * <p>
 * Each SinkNode is constructed with a map specifying the quantity of each {@link ResourceType}
 * required to satisfy it. As resources are delivered via incoming pipes, the node deducts
 * from its current demand. When all required resources have been received, the node is considered
 * "satisfied" and its visual appearance changes to indicate a win state.
 * </p>
 * <p>
 * The visual representation of a SinkNode is an octagon (created using a {@link Polygon}).
 * Its fill is updated dynamically:
 * <ul>
 *   <li>If multiple resource demands remain, a linear gradient (formed by the unsatisfied resource colors)
 *   is used.</li>
 *   <li>If only one resource type remains, a solid fill is applied.</li>
 *   <li>If the sink is fully satisfied, the node is filled with a win color
 *   (lime green) and plays a continuous rotation animation.</li>
 *   <li>If the sink receives an unexpected resource or extra input,
 *   it enters an error state: the fill turns red and the simulation halts.</li>
 * </ul>
 * </p>
 * <p>
 * Additionally, demand indicators are overlaid on the sink to provide visual feedback for unsatisfied demands.
 * </p>
 *
 * @author Braeden
 * @version 1.0
 */

public final class SinkNode extends GameNode
{

    private static final String NODE_WAITING_STYLE_CLASS   = "node-waiting";
    private static final String NODE_SATISFIED_STYLE_CLASS = "node-satisfied";
    private static final String SINK_ERROR_STATE_TEXT      = "sink-error-symbol";

    private static final double RADIUS                                  = 30.0;
    private static final double NODE_DIAMETER_MULTIPLIER                = 2.0;
    private static final double CONNECTOR_SIZE                          = 10.0;
    private static final double HALF_CONNECTOR_SIZE                     = CONNECTOR_SIZE / 2.0;
    private static final double INPUT_CONNECTOR_OFFSET_X                = -RADIUS;
    private static final double INPUT_CONNECTOR_OFFSET_Y                = 0.0;
    private static final double DEMAND_INDICATOR_PADDING                = 5.0;
    private static final double DEMAND_INDICATOR_PREF_WRAP_MULTIPLIER   = 1.8;
    private static final double ROTATE_TRANSITION_DURATION_SECONDS      = 2.0;
    private static final double FULL_ROTATION_ANGLE                     = 360.0;
    private static final double RESET_ROTATION_ANGLE                    = 0.0;
    private static final double OCTAGON_ANGLE_STEP_DEGREES              = 45.0;
    private static final double OCTAGON_INITIAL_ANGLE_OFFSET            = -22.5;
    private static final double OUTPUT_CONNECTOR_OFFSET_VALUE           = Double.NaN;
    private static final double GRADIENT_LEFT_HALF                      = 0.0;
    private static final double GRADIENT_MIDDLE                         = 0.5;
    private static final double GRADIENT_RIGHT_HALF                     = 1.0;
    private static final double SET_FROM_VALUE                          = 1.0;
    private static final double SET_TO_VALUE                            = 1.2;

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

    public static final String STYLE_CLASS = "sink-node";

    private final Map<ResourceType, Integer> initialDemand;
    private final Map<ResourceType, Integer> currentDemand;

    private int errorTickCounter = 0;
    private Polygon bodyShape;
    private RotateTransition rotateTransition;
    private ScaleTransition pulseTransition;
    private boolean isInErrorState = false;
    private Label infoLabelVisual;


    /**
     * Constructs a new SinkNode with the specified id, position, and demand map.
     *
     * @param id        the identifier for the node
     * @param x         the x-coordinate of the node
     * @param y         the y-coordinate of the node
     * @param demandMap a map representing the resource demand for the node
     * @throws NullPointerException     if the demandMap is null
     * @throws IllegalArgumentException if the demandMap is empty or contains non-positive demand values
     */
    public SinkNode(final String id,
            final double x,
            final double y,
            final Map<ResourceType, Integer> demandMap)
    {
        super(id, x, y);

        validateDemandMap(demandMap, id);

        this.initialDemand = new LinkedHashMap<>(demandMap);
        this.currentDemand = new HashMap<>();

        resetState();
    }

    /*
     * Creates an octagon (Polygon) centered in a square of size 2 * radius.
     *
     * @return the Polygon representing the octagon.
     */
    private Polygon createOctagon()
    {
        final Polygon   octagon;
        final double    center;

        octagon = new Polygon();
        center = RADIUS;

        for (int i = 0; i < OCTAGON_SIDES; i++)
        {
            final double angleDeg;
            final double angleRad;
            final double vx;
            final double vy;

            angleDeg= OCTAGON_ANGLE_STEP_DEGREES * i + OCTAGON_INITIAL_ANGLE_OFFSET;
            angleRad= Math.toRadians(angleDeg);
            vx = center + RADIUS * Math.cos(angleRad);
            vy = center + RADIUS * Math.sin(angleRad);
            octagon.getPoints().addAll(vx, vy);
        }
        return octagon;
    }

    /*
     * Updates the fill of the sink node based on the unsatisfied demand.
     * Uses a solid color if only one resource type remains, a linear gradient if multiple remain,
     * or red if in error state.
     */
    private void updateFill()
    {
        if (bodyShape == null)
        {
            return;
        }

        if (isInErrorState)
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
     * Produces a linear gradient for sinks with two demands.
     *
     * @param colors each color in the gradient.
     * @return the LinearGradient for the sink node.
     */
    private static LinearGradient getLinearGradient(final List<Color> colors)
    {
        final Color leftColor;
        final Color rightColor;
        final Stop[] stops;
        final LinearGradient gradient;

        leftColor = colors.getFirst();
        rightColor = colors.get(SECOND_COLOUR);

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
     * Updates the visual state of the node based on its error and demand satisfaction status.
     */
    private void updateVisualState()
    {
        if (bodyShape == null)
        {
            return;
        }

        bodyShape.getStyleClass().removeAll(NODE_WAITING_STYLE_CLASS, NODE_SATISFIED_STYLE_CLASS);

        if (isInErrorState)
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
            infoLabelVisual.setText("");
            infoLabelVisual.setVisible(false);
        }
        else
        {
            updateFill();
            rotateTransition.stop();
            bodyShape.setRotate(RESET_ROTATION_ANGLE);
            infoLabelVisual.setText("");
            infoLabelVisual.setVisible(false);
        }
    }

    /**
     * Checks if the sink node has been fully satisfied.
     *
     * @return true if all resource demands are met; false otherwise.
     */
    public boolean isSatisfied()
    {
        final boolean satisfied;
        satisfied = currentDemand.values().stream().allMatch(c -> c <= DEFAULT_VALUE);

        return satisfied;
    }

    /**
     * Checks if the sink node is currently in an error state.
     *
     * @return true if in error state; false otherwise.
     */
    public boolean isInErrorState()
    {
        final boolean inError;
        inError= isInErrorState;

        return inError;
    }

    /**
     * Resets the sink node state to its initial demand and clears any error state.
     */
    @Override
    public void resetState()
    {
        if (currentDemand == null || initialDemand == null)
        {
            return;
        }

        currentDemand.clear();
        currentDemand.putAll(initialDemand);
        isInErrorState = false;
        errorTickCounter = 0;

        if (bodyShape != null)
        {
            rotateTransition.stop();
            bodyShape.setRotate(RESET_ROTATION_ANGLE);
            updateFill();
        }

        updateVisualState();
    }

    /**
     * Clears only the error flag and resets the fill color of the sink node.
     */
    public void clearErrorStateOnly()
    {
        if (isInErrorState)
        {
            isInErrorState = false;
            errorTickCounter = 0;

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
     * Sinks have no output connector, so this returns null.
     *
     * @return null.
     */
    @Override
    protected Shape createOutputConnectorVisual()
    {
        return null;
    }

    /**
     * Creates the visual representation for the input connector.
     *
     * @return a Rectangle representing the input connector.
     */
    @Override
    protected Shape createInputConnectorVisual()
    {
        final double cX;
        final double cY;
        final Rectangle rect;

        cX = x + INPUT_CONNECTOR_OFFSET_X - HALF_CONNECTOR_SIZE;
        cY = y + INPUT_CONNECTOR_OFFSET_Y - HALF_CONNECTOR_SIZE;
        rect = new Rectangle(cX, cY, CONNECTOR_SIZE, CONNECTOR_SIZE);

        return rect;
    }

    /**
     * Creates the visual representation for the information label.
     * For sinks, this is hidden unless an error occurs.
     *
     * @return the Label for error indication.
     */
    @Override
    protected Label createInfoLabelVisual()
    {
        final Label infoLabel;

        infoLabel = new Label("");
        infoLabel.getStyleClass().add(SINK_ERROR_STATE_TEXT);
        infoLabel.setVisible(false);

        return infoLabel;
    }

    /**
     * Creates and returns the visual representation of the node body.
     *
     * @return a Node representing the sink body.
     */
    @Override
    protected Node createNodeBodyVisual()
    {
        final StackPane vg;
        vg = new StackPane();
        vg.setLayoutX(x - RADIUS);
        vg.setLayoutY(y - RADIUS);
        vg.setPrefSize(RADIUS * NODE_DIAMETER_MULTIPLIER,
                       RADIUS * NODE_DIAMETER_MULTIPLIER);

        bodyShape = createOctagon();
        bodyShape.getStyleClass().addAll(NODE_BODY_STYLE_CLASS, STYLE_CLASS);

        updateFill();

        final FlowPane demandIndicatorPane;
        demandIndicatorPane = new FlowPane(FLOW_PANE_HGAP, FLOW_PANE_VGAP);
        demandIndicatorPane.setAlignment(javafx.geometry.Pos.CENTER);
        demandIndicatorPane.setPadding(new Insets(DEMAND_INDICATOR_PADDING));
        demandIndicatorPane.setPrefWrapLength(RADIUS * DEMAND_INDICATOR_PREF_WRAP_MULTIPLIER);
        demandIndicatorPane.getStyleClass().add("demand-indicator-pane");
        demandIndicatorPane.setMouseTransparent(true);

        if (infoLabelVisual == null)
        {
            infoLabelVisual = createInfoLabelVisual();
        }

        vg.getChildren().addAll(bodyShape, demandIndicatorPane, infoLabelVisual);
        this.nodeBodyVisual = vg;

        rotateTransition = new RotateTransition(Duration.seconds(ROTATE_TRANSITION_DURATION_SECONDS), bodyShape);
        rotateTransition.setFromAngle(RESET_ROTATION_ANGLE);
        rotateTransition.setByAngle(FULL_ROTATION_ANGLE);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);

        pulseTransition = new ScaleTransition(Duration.seconds(SCALE_DURATION), bodyShape);
        pulseTransition.setFromX(SET_FROM_VALUE);
        pulseTransition.setFromY(SET_FROM_VALUE);
        pulseTransition.setToX(SET_TO_VALUE);
        pulseTransition.setToY(SET_TO_VALUE);
        pulseTransition.setCycleCount(Animation.INDEFINITE);
        pulseTransition.setAutoReverse(true);

        updateVisualState();
        return vg;
    }

    /**
     * Returns the offset of the input connector.
     *
     * @return a Point2D representing the input connector offset.
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
     * Returns the offset of the output connector.
     * For sinks, this returns a Point2D with NaN values.
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
     * Updates the sink node on each simulation tick by processing incoming resources from its pipes.
     * <p>
     * The update cycle performs the following actions:
     * <ol>
     *   <li>If the simulation is not running, the sink node updates its visual state and returns immediately.</li>
     *   <li>If the sink is already satisfied (all required resource demands have been met), it clears any resources
     *       present in its incoming pipes and updates their visuals.</li>
     *   <li>If the sink is not satisfied, it iterates through each incoming pipe:
     *     <ol type="a">
     *       <li>If a pipe contains a resource that is part of the sink's demand and the current demand for that resource is greater than zero,
     *           the resource is consumed. This is done by decrementing the required count in the {@code currentDemand} map,
     *           clearing the resource from the pipe, and updating the pipe's visual. The error tick counter is also reset.</li>
     *       <li>If a pipe contains a resource that is either not part of the demand or is extra (i.e. the demand is already met),
     *           the error tick counter is incremented. If this counter reaches a defined threshold, the sink enters an error state,
     *           its visual state is updated, and the simulation is stopped.</li>
     *     </ol>
     *   </li>
     *   <li>After processing all pipes, the sink node updates its visual state to reflect the current status.</li>
     * </ol>
     * </p>
     *
     * @param dt the elapsed time since the last simulation tick, in seconds.
     * @param gc the {@link GameController} instance used for updating pipe visuals and managing the simulation.
     */
    @Override
    public void update(final double dt, final GameController gc)
    {
        if (!gc.isSimulationRunning())
        {
            updateVisualState();
            return;
        }

        if (isSatisfied())
        {
            final List<Pipe> pipesToClear;
            pipesToClear = new ArrayList<>(incomingPipes);

            for (Pipe p : pipesToClear)
            {
                if (p.getCurrentResource() != null)
                {
                    p.clearResource();
                    gc.updatePipeVisual(p);
                }
            }
            updateVisualState();
            return;
        }

        final List<Pipe> pipesToProcess;
        pipesToProcess = new ArrayList<>(incomingPipes);

        for (Pipe p : pipesToProcess)
        {
            final ResourceType resource;
            resource = p.getCurrentResource();

            if (resource != null)
            {
                if (currentDemand.containsKey(resource) &&
                    currentDemand.get(resource) > DEFAULT_VALUE)
                {
                    errorTickCounter = 0;
                    p.clearResource();
                    gc.updatePipeVisual(p);

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
                    gc.updatePipeVisual(p);

                    if (errorTickCounter >= ERROR_TICK_THRESHOLD)
                    {
                        isInErrorState = true;
                        gc.stopSimulation("Sink Error (" + id + ")! Reset pipes (R).");
                    }
                }
            }
        }
        updateVisualState();
    }

    /*
     * Ensures the demand map is not null, not empty, and that all values are positive.
     *
     * @param demandMap the map of resource demands
     * @param nodeId    the identifier of the node (for error messages)
     *
     * @throws NullPointerException     if demandMap is null
     * @throws IllegalArgumentException if demandMap is empty or contains invalid demand quantities
     */
    private static void validateDemandMap(final Map<ResourceType, Integer> demandMap,
                                          final String nodeId)
    {
        Objects.requireNonNull(demandMap, "Demand map cannot be null for node " + nodeId);

        if (demandMap.isEmpty())
        {
            throw new IllegalArgumentException("Demand map is empty for node " + nodeId);
        }

        for (Map.Entry<ResourceType, Integer> entry : demandMap.entrySet())
        {
            if (entry.getKey() == null)
            {
                throw new IllegalArgumentException(
                        "Demand map contains a null ResourceType for node " + nodeId
                );
            }

            final Integer demandValue;
            demandValue = entry.getValue();

            if (demandValue == null || demandValue <= DEFAULT_VALUE)
            {
                throw new IllegalArgumentException(
                        "Invalid demand quantity (" + demandValue + ") for resource " +
                         entry.getKey() + " in node " + nodeId
                );
            }
        }
    }
}
