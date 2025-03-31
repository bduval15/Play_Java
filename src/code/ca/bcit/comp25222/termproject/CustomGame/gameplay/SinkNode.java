package ca.bcit.comp25222.termproject.CustomGame.gameplay;

import ca.bcit.comp25222.termproject.CustomGame.managers.GameController;
import ca.bcit.comp25222.termproject.CustomGame.util.ResourceType;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The SinkNode class represents a sink node in the Resource Router game,
 * responsible for consuming incoming resources based on a predefined demand map.
 * <p>
 * Each SinkNode is constructed with a map that specifies the quantity of each {@link ResourceType}
 * required to satisfy it. As resources are delivered via incoming pipes, the node deducts
 * from its current demand. When all required resources have been received, the node is considered
 * "satisfied" and its visual appearance changes to indicate a win state.
 * </p>
 * <p>
 * The visual representation of a SinkNode is an octagon, created using a {@link javafx.scene.shape.Polygon}.
 * The octagon's fill is dynamically updated:
 * <ul>
 *   <li>If multiple resource demands remain, the node displays a linear gradient formed by
 *       the colors corresponding to the unsatisfied resources.</li>
 *   <li>If only one resource type remains, the node is filled with that resource’s color.</li>
 *   <li>If the sink is fully satisfied, it is filled with a win color (lime green), and a continuous
 *       rotation animation (implemented via a {@link javafx.animation.RotateTransition}) is played.</li>
 *   <li>If the sink receives an unexpected resource or more than required, it enters an error state,
 *       its fill turns red, and the simulation is halted.</li>
 * </ul>
 * </p>
 * <p>
 * Additionally, the SinkNode overlays a set of demand indicators on top of its main body.
 * These indicators, arranged in a {@link javafx.scene.layout.FlowPane}, display small circular symbols
 * for each unit of unsatisfied demand, up to a maximum limit. This visual cue helps the player
 * monitor which resources are still needed.
 * </p>
 * <p>
 * The SinkNode class provides methods to update its state on each simulation tick,
 * to query whether it is satisfied or in an error state, and to reset its state to the initial demand.
 * It also supports clearing only the error flag while preserving the current demand.
 * </p>
 * <p>
 * In summary, SinkNode encapsulates the logic for resource consumption and visual feedback within the game:
 * it manages incoming resources, updates its display based on demand satisfaction, and communicates error or win states
 * to the game controller.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class SinkNode extends GameNode
{

    private static final String NODE_WAITING_STYLE_CLASS   = "node-waiting";
    private static final String NODE_SATISFIED_STYLE_CLASS = "node-satisfied";

    private static final double RADIUS                         = 30.0;
    private static final double NODE_DIAMETER_MULTIPLIER       = 2.0;
    private static final double CONNECTOR_SIZE                 = 10.0;
    private static final double HALF_CONNECTOR_SIZE            = CONNECTOR_SIZE / 2.0;
    private static final double INPUT_CONNECTOR_OFFSET_X       = -RADIUS;
    private static final double INPUT_CONNECTOR_OFFSET_Y       = 0.0;

    private static final double DEMAND_SYMBOL_SIZE             = 9.0;
    private static final double DEMAND_SYMBOL_SCALE            = 1.5;
    private static final double DOT_STROKE_WIDTH               = 0.3;
    private static final int    MAX_DEMAND_INDICATORS          = 9;
    private static final String MORE_DEMAND_INDICATOR_SYMBOL   = "..";

    private static final int    FLOW_PANE_HGAP                          = 2;
    private static final int    FLOW_PANE_VGAP                          = 2;
    private static final double DEMAND_INDICATOR_PADDING                = 5.0;
    private static final double DEMAND_INDICATOR_PREF_WRAP_MULTIPLIER   = 1.8;

    private static final double ROTATE_TRANSITION_DURATION_SECONDS = 2.0;
    private static final double FULL_ROTATION_ANGLE                = 360.0;
    private static final double RESET_ROTATION_ANGLE               = 0.0;

    private static final int    OCTAGON_SIDES                  = 8;
    private static final double OCTAGON_ANGLE_STEP_DEGREES     = 45.0;
    private static final double OCTAGON_INITIAL_ANGLE_OFFSET   = -22.5;
    private static final int RESOURCE_DECREMENT_THRESHOLD      = 1;

    private static final double OUTPUT_CONNECTOR_OFFSET_VALUE  = Double.NaN;

    public static final String STYLE_CLASS                     = "sink-node";

    private final Map<ResourceType, Integer> initialDemand;
    private final Map<ResourceType, Integer> currentDemand;
    private FlowPane demandIndicatorPane;
    private boolean isInErrorState = false;
    private Polygon bodyShape;
    private RotateTransition rotateTransition;

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

        Objects.requireNonNull(demandMap, "Demand map cannot be null");

        if (demandMap.isEmpty() || demandMap.values().stream().anyMatch(c -> c <= 0))
        {
            throw new IllegalArgumentException("Invalid sink demand for " + id);
        }

        final Map<ResourceType, Integer> tempDemand;
        tempDemand = new LinkedHashMap<>(demandMap);

        this.initialDemand = tempDemand;
        this.currentDemand = new HashMap<>();

        resetState();
    }

    /*
     * Creates an octagon (Polygon) centered in a square of size 2*radius.
     *
     * @param radius the reference radius for the octagon
     * @return the Polygon representing the octagon
     */
    private Polygon createOctagon(final double radius)
    {
        final Polygon octagon;
        final double center;

        octagon = new Polygon();
        center = radius;

        for (int i = 0; i < OCTAGON_SIDES; i++)
        {
            final double angleDeg;
            final double angleRad;

            angleDeg = OCTAGON_ANGLE_STEP_DEGREES * i + OCTAGON_INITIAL_ANGLE_OFFSET;
            angleRad = Math.toRadians(angleDeg);

            final double vx;
            final double vy;
            vx = center + radius * Math.cos(angleRad);
            vy = center + radius * Math.sin(angleRad);

            octagon.getPoints().addAll(vx, vy);
        }
        return octagon;
    }

    /*
     * Updates the fill of the sink node based on the unsatisfied demand.
     * If multiple demand colors remain, a linear gradient is used.
     * If only one remains, a solid fill is applied.
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

        final java.util.List<Color> colors = currentDemand.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> e.getKey().getDisplayColor())
                .toList();

        if (colors.isEmpty())
        {
            bodyShape.setFill(Color.LIMEGREEN);
        } else if (colors.size() == 1)
        {
            bodyShape.setFill(colors.getFirst());
        } else
        {
            final int n;
            final Stop[] stops;

            n = colors.size();
            stops = new Stop[n];

            for (int i = 0; i < n; i++)
            {
                final double offset;
                offset = (double) i / (n - 1);

                stops[i] = new Stop(offset, colors.get(i));
            }
            final LinearGradient gradient;
            gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

            bodyShape.setFill(gradient);
        }
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
        } else if (isSatisfied()) {
            bodyShape.setFill(Color.LIMEGREEN);
            rotateTransition.play();
        } else {
            updateFill();
            rotateTransition.stop();
            bodyShape.setRotate(RESET_ROTATION_ANGLE);
        }
    }

    /**
     * Checks if the sink node has been fully satisfied.
     *
     * @return true if all resource demands are satisfied, false otherwise
     */
    public boolean isSatisfied()
    {
        final boolean isSat;
        isSat = currentDemand.values().stream().allMatch(c -> c <= 0);

        return isSat;
    }

    /**
     * Checks if the sink node is currently in an error state.
     *
     * @return true if in error state, false otherwise
     */
    public boolean isInErrorState()
    {
        return isInErrorState;
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
     * @return null as sinks have no output connector
     */
    @Override
    protected Shape createOutputConnectorVisual()
    {
        return null;
    }

    /**
     * Creates the visual representation for the input connector.
     *
     * @return the Shape representing the input connector
     */
    @Override
    protected Shape createInputConnectorVisual()
    {
        final double cX;
        final double cY;

        cX = x + INPUT_CONNECTOR_OFFSET_X - HALF_CONNECTOR_SIZE;
        cY = y + INPUT_CONNECTOR_OFFSET_Y - HALF_CONNECTOR_SIZE;

        final Rectangle rect;
        rect = new Rectangle(cX, cY, CONNECTOR_SIZE, CONNECTOR_SIZE);

        return rect;
    }

    /**
     * Creates the visual representation for the information label.
     *
     * @return null as sinks do not display an information label
     */
    @Override
    protected Label createInfoLabelVisual()
    {
        return null;
    }


    /**
     * Creates and returns the visual representation of the node body.
     *
     * @return the Node representing the sink body visual
     */
    @Override
    protected Node createNodeBodyVisual()
    {
        final StackPane vg;
        vg = new StackPane();
        vg.setLayoutX(x - RADIUS);
        vg.setLayoutY(y - RADIUS);
        vg.setPrefSize(RADIUS * NODE_DIAMETER_MULTIPLIER, RADIUS * NODE_DIAMETER_MULTIPLIER);

        bodyShape = createOctagon(RADIUS);
        bodyShape.getStyleClass().addAll(NODE_BODY_STYLE_CLASS, STYLE_CLASS);

        updateFill();

        demandIndicatorPane = new FlowPane(FLOW_PANE_HGAP, FLOW_PANE_VGAP);
        demandIndicatorPane.setAlignment(javafx.geometry.Pos.CENTER);
        demandIndicatorPane.setPadding(new Insets(DEMAND_INDICATOR_PADDING));
        demandIndicatorPane.setPrefWrapLength(RADIUS * DEMAND_INDICATOR_PREF_WRAP_MULTIPLIER);
        demandIndicatorPane.getStyleClass().add("demand-indicator-pane");
        demandIndicatorPane.setMouseTransparent(true);

        vg.getChildren().addAll(bodyShape, demandIndicatorPane);
        this.nodeBodyVisual = vg;

        rotateTransition = new RotateTransition(Duration.seconds(ROTATE_TRANSITION_DURATION_SECONDS), bodyShape);
        rotateTransition.setFromAngle(RESET_ROTATION_ANGLE);
        rotateTransition.setByAngle(FULL_ROTATION_ANGLE);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);

        updateVisualState();

        return vg;
    }

    /**
     * Returns the offset of the input connector.
     *
     * @return the Point2D representing the input connector offset
     */
    @Override
    public Point2D getInputConnectorOffset()
    {
        final Point2D iOffset;
        iOffset = new Point2D(INPUT_CONNECTOR_OFFSET_X, INPUT_CONNECTOR_OFFSET_Y);

        return iOffset;
    }

    /**
     * Returns the offset of the output connector.
     *
     * @return a Point2D with NaN values as sinks have no output connector
     */
    @Override
    public Point2D getOutputConnectorOffset()
    {
        final Point2D oOffset;
        oOffset = new Point2D(OUTPUT_CONNECTOR_OFFSET_VALUE, OUTPUT_CONNECTOR_OFFSET_VALUE);

        return oOffset;
    }

    /**
     * Updates the sink node state based on incoming resources and the game simulation state.
     *
     * @param dt the delta time since the last update
     * @param gc the GameController managing the game
     */
    @Override
    public void update(final double dt,
            final GameController gc)
    {
        if (!gc.isSimulationRunning())
        {
            updateVisualState();
            return;
        }

        if (isSatisfied())
        {
            for (final Pipe p : new ArrayList<>(incomingPipes))
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

        for (final Pipe p : new ArrayList<>(incomingPipes))
        {
            final ResourceType r;
            r = p.getCurrentResource();

            if (r != null) {
                if (currentDemand.containsKey(r) && currentDemand.get(r) > 0)
                {
                    p.clearResource();
                    gc.updatePipeVisual(p);
                    currentDemand.compute(r, (_, c) -> {
                        if (c == null || c <= RESOURCE_DECREMENT_THRESHOLD)
                        {
                            return 0;
                        } else {
                            return c - 1;
                        }
                    });
                } else {
                    isInErrorState = true;
                    gc.stopSimulation("Sink Error (" + id + ")! Reset pipes (R).");
                }
            }
        }
        updateVisualState();
    }
}
