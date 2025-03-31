package ca.bcit.comp25222.termproject.CustomGame.gameplay;

import ca.bcit.comp25222.termproject.CustomGame.managers.GameController;
import ca.bcit.comp25222.termproject.CustomGame.util.ResourceType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import java.util.Map;
import java.util.Objects;

/**
 * ProcessorNode represents a processing unit in the Resource Router game that consumes multiple input resources,
 * processes them over a specified delay, and produces a single output resource.
 * <p>
 * Visually, the ProcessorNode is divided into two halves:
 * <ul>
 *   <li>The left half displays the required input resources. This area is dynamically populated with colored
 *       stripes corresponding to each input resource specified in the node's recipe. The colors are derived
 *       from the display colors of the respective resources.</li>
 *   <li>The right half shows the output resource. Once processing is complete, an indicator (a small circle)
 *       appears in this area, displaying the output resource using its associated color.</li>
 * </ul>
 * A narrow white divider separates these two halves.
 * </p>
 * <p>
 * The node is configured with a recipe—a mapping of input resource types to the required quantities—and a processing
 * delay measured in ticks. The ProcessorNode maintains an internal input buffer that accumulates incoming resources
 * from connected pipes. When the buffer satisfies the recipe, processing starts: the required resources are deducted
 * from the buffer and a countdown begins. Upon completion of the delay, the node produces its output resource,
 * attempting to deliver it via its outgoing pipe.
 * </p>
 * <p>
 * If the node receives an unexpected resource or if the input buffer exceeds the quantities specified in the recipe,
 * it enters an error state. In this state, the node's main visual body is filled with an error color (typically red),
 * processing halts, and the simulation is stopped until the error is resolved via a state reset.
 * </p>
 * <p>
 * The class provides methods to:
 * <ul>
 *   <li>Consume and validate incoming resources from pipes, updating the input buffer appropriately.</li>
 *   <li>Check if the current buffer meets the recipe requirements to start processing.</li>
 *   <li>Deduct the required resources from the buffer and initiate processing with a configurable delay.</li>
 *   <li>Advance the processing timer and, upon completion, attempt to output the processed resource.</li>
 *   <li>Update the node's visual state based on whether it is processing, waiting for output, or in an error state.</li>
 *   <li>Reset the node's state, clearing the input buffer and error flag for a fresh start.</li>
 * </ul>
 * </p>
 * <p>
 * The ProcessorNode's recipe and output resource type are immutable once set at construction time,
 * ensuring consistency throughout its lifecycle. Its dynamic behavior—processing, error management, and visual
 * updates—is handled during simulation ticks via the {@link #update(double, GameController)} method.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */
public final class ProcessorNode extends GameNode
{

    public static final String STYLE_CLASS = "processor-node";

    private static final double BODY_SIZE       = 50.0;
    private static final double HALF_BODY_SIZE  = BODY_SIZE / 2.0;
    private static final double CONNECTOR_SIZE  = 10.0;

    private static final double INPUT_CONNECTOR_OFFSET_X    = -HALF_BODY_SIZE;
    private static final double INPUT_CONNECTOR_OFFSET_Y    = 0.0;
    private static final double OUTPUT_CONNECTOR_OFFSET_X   = HALF_BODY_SIZE;
    private static final double OUTPUT_CONNECTOR_OFFSET_Y   = 0.0;

    public static final int MIN_DELAY_TICKS = 1;

    private static final String NODE_PROCESSING_STYLE_CLASS     = "node-active";
    private static final String NODE_WAITING_OUTPUT_STYLE_CLASS = "node-waiting";
    private static final String NODE_ERROR_STYLE_CLASS          = "node-error";

    private static final double INTERNAL_PADDING    = 4.0;
    private static final double DIVIDER_WIDTH       = 2.0;
    private static final Color ERROR_FILL_COLOR     = Color.RED;

    private final Map<ResourceType, Integer> inputRecipe;
    private final Map<ResourceType, Integer> inputBuffer;
    private final ResourceType outputResourceType;
    private final int processingDelayTicks;

    private int ticksRemaining;
    private boolean isProcessing;
    private ResourceType processedResourceWaiting;
    private boolean errorState;

    private Rectangle mainBodyRect;
    private Pane leftColorPane;
    private StackPane rightOutputPane;

    /**
     * Constructs a new ProcessorNode.
     *
     * @param id     the unique identifier for this node
     * @param x      the x-coordinate of the node's center
     * @param y      the y-coordinate of the node's center
     * @param delay  the processing delay (in ticks)
     * @param recipe a map containing the required input resources and their counts
     * @param output the ResourceType produced by this processor
     */
    public ProcessorNode(final String id,
            final double x,
            final double y,
            final int delay,
            final Map<ResourceType, Integer> recipe,
            final ResourceType output)
    {
        super(id, x, y);
        Objects.requireNonNull(recipe, "Recipe cannot be null");
        Objects.requireNonNull(output, "Output resource cannot be null");
        if (recipe.isEmpty())
        {
            throw new IllegalArgumentException("Recipe is empty for node " + id);
        }

        this.processingDelayTicks = Math.max(MIN_DELAY_TICKS, delay);
        this.inputRecipe = Map.copyOf(recipe);
        this.outputResourceType = output;
        this.inputBuffer = new java.util.HashMap<>();
        resetState();
    }

    /*
     * Consumes incoming resources from pipes.
     * If an unexpected resource is received or if an extra resource would exceed the recipe,
     * sets the error state, updates visuals, and stops the simulation.
     *
     * @param gc the GameController.
     * @return true if consumption occurred without error; false otherwise.
     */
    private boolean consumeInputs(final GameController gc)
    {
        boolean consumed = false;
        for (final Pipe p : new java.util.ArrayList<>(incomingPipes))
        {
            final ResourceType r;
            r = p.getCurrentResource();
            if (r != null)
            {
                if (!inputRecipe.containsKey(r))
                {
                    errorState = true;
                    updateVisualState();
                    gc.stopSimulation("Processor " + getId() + " error: unexpected input");
                    return false;
                }

                final int current;
                final int required;
                current = inputBuffer.getOrDefault(r, 0);
                required = inputRecipe.get(r);

                if (current + 1 > required)
                {
                    errorState = true;
                    updateVisualState();
                    gc.stopSimulation("Processor " + getId() + " error: extra input " + r);
                    return false;
                }
                inputBuffer.merge(r, 1, Integer::sum);
                p.clearResource();
                gc.updatePipeVisual(p);
                consumed = true;
            }
        }
        return consumed;
    }

    /*
     * Checks if sufficient inputs have been collected.
     *
     * @return true if the collected inputs meet the recipe; false otherwise.
     */
    private boolean canStartProcessing()
    {
        final boolean result;
        result = inputRecipe.entrySet()
                .stream()
                .allMatch(e ->
                 inputBuffer.getOrDefault(e.getKey(), 0) >= e.getValue());
        return result;
    }

    /*
     * Starts processing by deducting the required inputs from the buffer.
     */
    private void startProcessing()
    {
        if (!canStartProcessing())
        {
            return;
        }
        inputRecipe.forEach((type, needed) -> {
            inputBuffer.computeIfPresent(type, (k, c) -> c - needed);
        });

        inputBuffer.entrySet().removeIf(e -> e.getValue() <= 0);
        isProcessing = true;
        ticksRemaining = processingDelayTicks;
    }

    /*
     * Advances the processing timer by one tick.
     */
    private void advanceProcessing()
    {
        if (!isProcessing)
        {
            return;
        }
        ticksRemaining--;
        if (ticksRemaining <= 0)
        {
            isProcessing = false;
            ticksRemaining = 0;
        }
    }

    /*
     * Attempts to output the processed resource to the first outgoing pipe.
     *
     * @param gc the GameController.
     * @return true if the output succeeded; false otherwise.
     */
    private boolean tryOutputResource(final GameController gc)
    {
        if (processedResourceWaiting == null)
        {
            return false;
        }
        if (outgoingPipes.isEmpty())
        {
            processedResourceWaiting = null;
            return true;
        }
        final Pipe p;
        final boolean ok;

        p = outgoingPipes.get(0);
        ok = p.trySetResource(processedResourceWaiting);

        if (ok)
        {
            gc.updatePipeVisual(p);
        }
        return ok;
    }

    /*
     * Updates the left-side visual stripes that indicate required inputs.
     */
    private void updateNeededVisuals()
    {
        if (leftColorPane == null)
        {
            return;
        }
        leftColorPane.getChildren().clear();

        if (!isProcessing && processedResourceWaiting != null)
        {
            return;
        }
        if (inputRecipe.isEmpty())
        {
            return;
        }

        final double totalHeight;
        final double width;
        final int numResources;
        final double stripeHeight;

        totalHeight = leftColorPane.getPrefHeight();
        width = leftColorPane.getPrefWidth();
        numResources = inputRecipe.size();
        stripeHeight = totalHeight / numResources;

        int idx = 0;
        for (final ResourceType resourceType : inputRecipe.keySet())
        {
            final double yStart;
            yStart = idx * stripeHeight;
            final Rectangle stripe;
            stripe = new Rectangle(0, yStart, width, stripeHeight);
            stripe.setFill(resourceType.getDisplayColor());
            stripe.setStroke(Color.BLACK);
            stripe.setStrokeWidth(0.5);
            leftColorPane.getChildren().add(stripe);
            idx++;
        }
    }

    /*
     * Updates the visual state of the processor.
     * If in error state, the main body is filled with the error color.
     */
    private void updateVisualState()
    {
        if (mainBodyRect == null)
        {
            return;
        }
        if (errorState)
        {
            mainBodyRect.setFill(ERROR_FILL_COLOR);
        } else {
            mainBodyRect.setFill(outputResourceType.getDisplayColor());
        }

        mainBodyRect.getStyleClass().removeAll(
                NODE_PROCESSING_STYLE_CLASS,
                NODE_WAITING_OUTPUT_STYLE_CLASS,
                NODE_ERROR_STYLE_CLASS);

        if (errorState)
        {
            mainBodyRect.getStyleClass().add(NODE_ERROR_STYLE_CLASS);
        } else if (isProcessing) {
            mainBodyRect.getStyleClass().add(NODE_PROCESSING_STYLE_CLASS);
        } else if (processedResourceWaiting != null) {
            mainBodyRect.getStyleClass().add(NODE_WAITING_OUTPUT_STYLE_CLASS);
        }

        if (rightOutputPane != null && !rightOutputPane.getChildren().isEmpty())
        {
            final Node indicator;
            indicator = rightOutputPane.getChildren().get(0);

            if (indicator instanceof Circle)
            {
                final boolean visible;
                visible = processedResourceWaiting != null;
                indicator.setVisible(visible);
            }
        }
    }

    /**
     * Resets the processor's state and clears the error flag.
     */
    @Override
    public void resetState()
    {
        isProcessing = false;
        ticksRemaining = 0;
        processedResourceWaiting = null;
        inputBuffer.clear();
        errorState = false;
        updateVisualState();
    }

    /**
     * Updates the processor node each simulation tick.
     *
     * @param dt the elapsed time in seconds.
     * @param gc the GameController.
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

        boolean changed = false;

        if (!errorState && processedResourceWaiting != null)
        {
            if (tryOutputResource(gc))
            {
                processedResourceWaiting = null;
                changed = true;
            }
        }

        if (!errorState && isProcessing)
        {
            advanceProcessing();
            if (!isProcessing)
            {
                processedResourceWaiting = outputResourceType;
            }
            changed = true;
        }

        if (!errorState && !isProcessing)
        {
            if (consumeInputs(gc))
            {
                changed = true;
            }
            if (canStartProcessing() && processedResourceWaiting == null)
            {
                startProcessing();
                changed = true;
            }
        }

        if (changed)
        {
            updateVisualState();
        }
        updateNeededVisuals();
    }

    /**
     * Creates and returns the visual representation of the processor node.
     *
     * @return the Node representing the processor's body.
     */
    @Override
    protected Node createNodeBodyVisual()
    {
        final StackPane rootStack;
        rootStack = new StackPane();
        rootStack.setLayoutX(x - HALF_BODY_SIZE);
        rootStack.setLayoutY(y - HALF_BODY_SIZE);
        rootStack.setPrefSize(BODY_SIZE, BODY_SIZE);

        final Rectangle rect;
        rect = new Rectangle(BODY_SIZE, BODY_SIZE);
        rect.setFill(outputResourceType.getDisplayColor());
        rect.getStyleClass().addAll(NODE_BODY_STYLE_CLASS, STYLE_CLASS);

        final HBox hBox;
        hBox = new HBox(0);
        hBox.setPadding(new Insets(INTERNAL_PADDING));
        hBox.setFillHeight(true);

        final Pane leftPane;
        leftPane = new Pane();
        leftPane.setPrefSize(HALF_BODY_SIZE - INTERNAL_PADDING,
                             BODY_SIZE - (INTERNAL_PADDING * 2));

        final Rectangle divider;
        divider = new Rectangle(DIVIDER_WIDTH,
                                BODY_SIZE - (INTERNAL_PADDING * 2),
                                Color.WHITE);

        final StackPane rightPane;
        rightPane = new StackPane();
        rightPane.setPrefSize(HALF_BODY_SIZE - INTERNAL_PADDING - DIVIDER_WIDTH,
                              BODY_SIZE - (INTERNAL_PADDING * 2));

        final Circle outputIndicator;
        outputIndicator = new Circle(8, outputResourceType.getDisplayColor());
        outputIndicator.setVisible(false);
        rightPane.getChildren().add(outputIndicator);

        hBox.getChildren().addAll(leftPane, divider, rightPane);
        rootStack.getChildren().addAll(rect, hBox);

        mainBodyRect    = rect;
        leftColorPane   = leftPane;
        rightOutputPane = rightPane;
        nodeBodyVisual  = rootStack;

        updateNeededVisuals();
        updateVisualState();

        return rootStack;
    }

    /**
     * Creates and returns the input connector visual.
     *
     * @return the Shape representing the input connector.
     */
    @Override
    protected Shape createInputConnectorVisual()
    {
        final double cX;
        final double cY;
        final Rectangle rect;

        cX = x + INPUT_CONNECTOR_OFFSET_X - (CONNECTOR_SIZE / 2.0);
        cY = y + INPUT_CONNECTOR_OFFSET_Y - (CONNECTOR_SIZE / 2.0);
        rect = new Rectangle(cX, cY, CONNECTOR_SIZE, CONNECTOR_SIZE);

        return rect;
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
        cX = x + OUTPUT_CONNECTOR_OFFSET_X - (CONNECTOR_SIZE / 2.0);
        final double cY;
        cY = y + OUTPUT_CONNECTOR_OFFSET_Y - (CONNECTOR_SIZE / 2.0);
        final Rectangle rect;
        rect = new Rectangle(cX, cY, CONNECTOR_SIZE, CONNECTOR_SIZE);
        return rect;
    }

    /**
     * Returns the offset of the input connector.
     *
     * @return a Point2D representing the input connector's offset.
     */
    @Override
    public Point2D getInputConnectorOffset()
    {
        final Point2D offset;
        offset = new Point2D(INPUT_CONNECTOR_OFFSET_X, INPUT_CONNECTOR_OFFSET_Y);
        return offset;
    }

    /**
     * Returns the offset of the output connector.
     *
     * @return a Point2D representing the output connector's offset.
     */
    @Override
    public Point2D getOutputConnectorOffset()
    {
        final Point2D offset;
        offset = new Point2D(OUTPUT_CONNECTOR_OFFSET_X, OUTPUT_CONNECTOR_OFFSET_Y);
        return offset;
    }

    /**
     * Returns the info label visual.
     *
     * @return null since ProcessorNode does not use an info label.
     */
    @Override
    protected Label createInfoLabelVisual()
    {
        return null;
    }
}
