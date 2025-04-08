package ca.bcit.comp2522.termproject.resourcerouter.gameplay;

import ca.bcit.comp2522.termproject.resourcerouter.managers.GameController;
import ca.bcit.comp2522.termproject.resourcerouter.util.ResourceType;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ProcessorNode represents a processing unit in the Resource Router game that consumes multiple input resources,
 * processes them over a specified delay, and produces a single output resource.
 * <p>
 * Although the node is configured with an input recipe (a mapping from ResourceType to required quantity),
 * its visual representation does not display the input resource colors. Instead, the ProcessorNode shows only the
 * output resource's display color throughout its lifecycle.
 * </p>
 * <p>
 * Upon receiving resources via incoming pipes, the node accumulates them in an internal input buffer.
 * When the buffer satisfies the recipe requirements, the node starts processing: it deducts the required
 * amounts from the buffer and begins a countdown (in ticks) for processing.
 * When the countdown completes, the node produces its output resource and attempts to dispatch it via its
 * outgoing pipe.
 * </p>
 * <p>
 * In the event that an unexpected resource is encountered or the node receives inputs in excess of its recipe,
 * it enters an error state. In this state, the ProcessorNode halts processing, its main visual body is filled with
 * an error color (typically red), and the overall simulation is halted until the error state is cleared.
 * </p>
 * <p>
 * Visually, the ProcessorNode is represented by a composed scene graph built as follows:
 * <ul>
 *   <li>
 *     A rectangular body serves as the primary visual component and is filled with the output resource's display color.
 *     This emphasizes the product of processing rather than the inputs.
 *   </li>
 *   <li>
 *     A horizontally arranged layout (HBox) divides the node's display into two halves:
 *     although a left pane is created (which could conceptually show input data), only the output is displayed.
 *     The right half contains a small circular output indicator also filled with the output resource's color.
 *     A narrow white divider separates these halves.
 *   </li>
 *   <li>
 *     Standard animations are applied to the node body:
 *     a continuous rotation (via RotateTransition) and a pulsing effect (via ScaleTransition) indicate that
 *     processing is active.
 *   </li>
 * </ul>
 * </p>
 * <p>
 * The ProcessorNode’s configuration is immutable in terms of its recipe and output resource type once set at
 * construction time, ensuring predictable behavior throughout its use. Its dynamic behavior is governed by the update
 * cycle (provided by the update(double, GameController) method), which:
 * <ol>
 *   <li>
 *     Consumes incoming resources—iterating over connected pipes, validating that each resource matches an entry
 *     in the recipe, and aggregating counts into the input buffer.
 *     If an unexpected resource is encountered, an error is signaled.
 *   </li>
 *   <li>
 *     Checks if the input buffer meets the recipe requirements.
 *     If so, it deducts the required amounts and begins a processing cycle
 *     by setting a countdown timer.
 *   </li>
 *   <li>
 *     Advances the processing timer on each simulation tick.
 *     When the timer reaches zero, processing ends and the output resource
 *     is marked as ready for dispatch.
 *   </li>
 *   <li>
 *     Updates the visual state of the node to reflect its current condition (processing, waiting for output, or error).
 *   </li>
 * </ol>
 * </p>
 * <p>
 * In summary, ProcessorNode encapsulates the logic for resource input validation,
 * timed processing, error detection, and visual feedback.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class ProcessorNode
             extends GameNode
{

    private static final double BODY_SIZE                   = 50.0;
    private static final double CONNECTOR_SIZE              = 10.0;
    private static final double INPUT_CONNECTOR_OFFSET_Y    = 0.0;
    private static final double OUTPUT_CONNECTOR_OFFSET_Y   = 0.0;
    private static final double INTERNAL_PADDING            = 4.0;
    private static final double DIVIDER_WIDTH               = 2.0;

    private static final double HALF_BODY_SIZE;
    private static final double INPUT_CONNECTOR_OFFSET_X;
    private static final double OUTPUT_CONNECTOR_OFFSET_X;

    private static final int    CIRCLE_RADIUS               = 8;
    private static final int    DEFAULT_VALUE               = 0;
    private static final int    MIN_DELAY_TICKS_SEC         = 1;

    private static final String NODE_PROCESSING_STYLE_CLASS     = "node-active";
    private static final String NODE_WAITING_OUTPUT_STYLE_CLASS = "node-waiting";
    private static final String NODE_ERROR_STYLE_CLASS          = "node-error";
    private static final String STYLE_CLASS                     = "processor-node";
    private static final String NODE_BODY_STYLE_CLASS           = "node";
    private static final Color  ERROR_FILL_COLOR;


    private final Map<ResourceType, Integer>    inputRecipe;
    private final Map<ResourceType, Integer>    inputBuffer;
    private final ResourceType                  outputResourceType;
    private final int                           processingDelayTicks;

    private int             ticksRemaining;
    private boolean         processing;
    private boolean         errorState;
    private ResourceType    processedResourceWaiting;
    private Rectangle       mainBodyRect;
    private StackPane       rightOutputPane;

    static
    {
        ERROR_FILL_COLOR            = Color.RED;
        HALF_BODY_SIZE              = BODY_SIZE / 2.0;
        OUTPUT_CONNECTOR_OFFSET_X   = HALF_BODY_SIZE;
        INPUT_CONNECTOR_OFFSET_X    = -HALF_BODY_SIZE;
    }

    /**
     * Constructs a new ProcessorNode with the specified identifier, position, processing delay,
     * input recipe, and output resource.
     * <p>
     * The constructor performs the following steps:
     * <ol>
     *   <li>Calls the super constructor with nodeId, xCoordinate, and yCoordinate values.</li>
     *   <li>Validates that the processing delay is non-negative; if not, throws an exception.</li>
     *   <li>Validates that the recipe map is not null, not empty, and that all entries are valid,
     *       with non-null ResourceTypes and positive quantities.</li>
     *   <li>Validates that the output resource type is non-null.</li>
     *   <li>Stores a copy of the recipe map (immutable) and the output resource, and initializes
     *       an empty input buffer.</li>
     *   <li>Calls resetState() to establish the initial processing state.</li>
     * </ol>
     * </p>
     *
     * @param nodeId     the unique identifier for this node
     * @param xCoordinate      the xCoordinate-coordinate of the node's center
     * @param yCoordinate      the yCoordinate-coordinate of the node's center
     * @param delay  the processing delay in ticks (must be non-negative; enforced to be at least 1)
     * @param recipe a map specifying the required input resources and their counts
     * @param output the ResourceType produced by this processor (must not be null)
     *
     * @throws IllegalArgumentException if delay is negative, or if the recipe is invalid or empty
     * @throws NullPointerException if recipe or output is null
     *
     */
    ProcessorNode(final String nodeId,
                  final double xCoordinate,
                  final double yCoordinate,
                  final int delay,
                  final Map<ResourceType, Integer> recipe,
                  final ResourceType output)
    {

        super(nodeId,
              xCoordinate,
              yCoordinate);

        validateDelay(delay, nodeId);
        validateRecipe(recipe, nodeId);
        validateOutput(output, nodeId);

        this.processingDelayTicks   = Math.max(MIN_DELAY_TICKS_SEC, delay);
        this.inputRecipe            = Map.copyOf(recipe);
        this.outputResourceType     = output;
        this.inputBuffer            = new HashMap<>();
        resetState();
    }

    /*
     * Validates the processing delay value.
     *
     * @param delay the processing delay (in ticks)
     * @param nodeId the identifier of the node (used for error messages)
     *
     * @throws IllegalArgumentException if delay is negative
     *
     */
    private static void validateDelay(final int delay,
                                      final String nodeId)
    {
        if (delay < DEFAULT_VALUE)
        {
            throw new IllegalArgumentException(
                    "Processing delay must be non-negative for node " + nodeId
            );
        }
    }

    /*
     * Validates the recipe map for required input resources.
     * Ensures that the recipe map is not null.
     * Checks that the recipe is not empty.
     *
     * @param recipe the map of resources required by this processor
     * @param nodeId the identifier of the node (used for error messages)
     *
     * @throws NullPointerException     if recipe is null
     * @throws IllegalArgumentException if the recipe is empty or has invalid entries
     *
     */
    private static void validateRecipe(final Map<ResourceType, Integer> recipe,
                                       final String nodeId)
    {
        Objects.requireNonNull(recipe,
                               "Recipe cannot be null for node " +
                               nodeId);

        if (recipe.isEmpty())
        {
            throw new IllegalArgumentException("Recipe is empty for node " +
                                               nodeId);
        }

        for (final Map.Entry<ResourceType, Integer> entry : recipe.entrySet())
        {
            final ResourceType resourceType;
            final Integer      quantity;

            resourceType    = entry.getKey();
            quantity        = entry.getValue();

            if (resourceType == null)
            {
                throw new IllegalArgumentException(
                        "Recipe contains a null ResourceType for node " + nodeId
                );
            }
            if (quantity == null || quantity <= DEFAULT_VALUE)
            {
                throw new IllegalArgumentException(
                        "Invalid quantity (" + quantity + ") for ResourceType " +
                         resourceType + " in node " + nodeId
                );
            }
        }
    }

    /*
     * Validates the output resource type.
     * Checks that the output resource type is not null.
     *
     * @param output the ResourceType produced by this processor
     * @param nodeId the identifier of the node (used for error messages)
     *
     * @throws NullPointerException if output is null
     */
    private static void validateOutput(final ResourceType output,
                                       final String nodeId)
    {
        Objects.requireNonNull(output,
                               "Output resource cannot be null for node " +
                               nodeId);
    }

    /*
     * Consumes resources from all incoming pipes and updates the processor's input buffer.
     *
     * <p>
     * This method iterates over a copy of the list of incoming
     * pipes and retrieves the current resource from each pipe.
     * For each resource:
     * </p>
     * <ul>
     *   <li>
     *     If the resource is not part of the processor's expected input recipe, the method sets an error state,
     *     updates the visual representation, and stops the simulation using the provided GameController.
     *   </li>
     *   <li>
     *     If the resource is expected, the method checks the current count
     *     in the input buffer against the required amount.
     *     If the buffer count is below the required amount, the resource count is increased.
     *   </li>
     * </ul>
     * <p>
     * Regardless of the outcome, the resource is cleared from the pipe and the pipe's visual state is updated.
     * </p>
     *
     * @param gameController the GameController managing the simulation,
     *           which is used to stop the simulation if an unexpected resource is encountered.
     *
     * @return true if at least one resource consumed without error;
     *         false if an error occurred during consumption.
     */
    private boolean consumeInputs(final GameController gameController)
    {
        boolean consumed = false;

        for (final Pipe pipe : new ArrayList<>(getIncomingPipes()))
        {
            final ResourceType resourceType;
            resourceType = pipe.getCurrentResource();

            if (resourceType != null)
            {
                if (!inputRecipe.containsKey(resourceType))
                {
                    errorState = true;

                    updateVisualState();

                    gameController.stopSimulation("Processor " + getNodeId() +
                                                  " error: unexpected input " +
                                                  resourceType);

                    return false;
                }

                final int current;
                final int required;

                current     = inputBuffer.getOrDefault(resourceType,
                                                       DEFAULT_VALUE);
                required    = inputRecipe.get(resourceType);

                if (current < required)
                {
                    inputBuffer.merge(resourceType,
                                      MIN_DELAY_TICKS_SEC,
                                      Integer::sum);
                    consumed = true;
                }

                pipe.clearResource();
                gameController.updatePipeVisual(pipe);
            }
        }
        return consumed;
    }

    /*
     * Checks whether for every required resource in inputRecipe,
     *   the count in inputBuffer is at least the required amount.
     *
     * Uses stream() and allMatch() to verify this condition.
     *
     * Returns true if the condition holds; otherwise, false.
     *
     * @return true if the collected inputs meet the recipe; false otherwise.
     *
     */
    private boolean canStartProcessing()
    {
        final boolean result;
        result = inputRecipe.entrySet()
                .stream()
                .allMatch(e ->
                 inputBuffer.getOrDefault(e.getKey(), DEFAULT_VALUE) >= e.getValue());

        return result;
    }

    /*
     * First verifies that canStartProcessing() returns true.
     *
     * For each entry in inputRecipe, it deducts the required amount from inputBuffer.
     *
     * Removes entries from inputBuffer if the count falls to zero or below.
     *
     * Sets processing to true and initializes ticksRemaining to processingDelayTicks.
     *
     */
    private void startProcessing()
    {
        if (!canStartProcessing())
        {
            return;
        }

        inputRecipe.forEach((type, needed) ->
        inputBuffer.computeIfPresent(type, (k, c) -> c - needed));

        inputBuffer.entrySet().removeIf(e -> e.getValue() <= DEFAULT_VALUE);
        processing      = true;
        ticksRemaining  = processingDelayTicks;
    }

    /*
     * If processing is not active, does nothing.
     *
     * Otherwise, decrements ticksRemaining by one.
     *
     * If ticksRemaining reaches zero or below, stops processing by setting processing to false
     * and resets ticksRemaining to zero.
     *
     */
    private void advanceProcessing()
    {
        if (!processing)
        {
            return;
        }

        ticksRemaining--;

        if (ticksRemaining <= DEFAULT_VALUE)
        {
            processing = false;
            ticksRemaining = DEFAULT_VALUE;
        }
    }

    /*
     * Updates the visual appearance of mainBodyRect and rightOutputPane based on the node state.
     *
     * If errorState is true, fills mainBodyRect with the ERROR_FILL_COLOR.
     *
     * Otherwise, uses the display color of the outputResourceType.
     *
     * Clears existing style classes for processing, waiting output, and error; then reapplies
     * the appropriate class (error if in error state, processing if currently processing,
     * or waiting output if a processed resource is ready).
     *
     * Also, if rightOutputPane contains an indicator (first child is a Circle), its visibility is set
     * based on whether processedResourceWaiting is non-null.
     *
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
        }
        else
        {
            mainBodyRect.setFill(outputResourceType.getDisplayColor());
        }

        mainBodyRect.getStyleClass().removeAll(
                NODE_PROCESSING_STYLE_CLASS,
                NODE_WAITING_OUTPUT_STYLE_CLASS,
                NODE_ERROR_STYLE_CLASS);

        if (errorState)
        {
            mainBodyRect.getStyleClass().add(NODE_ERROR_STYLE_CLASS);
        }
        else if (processing)
        {
            mainBodyRect.getStyleClass().add(NODE_PROCESSING_STYLE_CLASS);
        }
        else if (processedResourceWaiting != null)
        {
            mainBodyRect.getStyleClass().add(NODE_WAITING_OUTPUT_STYLE_CLASS);
        }

        if (rightOutputPane != null && !rightOutputPane.getChildren().isEmpty())
        {
            final Node indicator;
            indicator = rightOutputPane.getChildren().getFirst();

            if (indicator instanceof Circle)
            {
                final boolean visible;
                visible = processedResourceWaiting != null;
                indicator.setVisible(visible);
            }
        }
    }

    /**
     * Resets the ProcessorNode to its initial state.
     *
     * <p>
     * This method clears any processing activity by:
     * <ol>
     *   <li>Setting processing to false and ticksRemaining to 0.</li>
     *   <li>Clearing any stored output resource (processedResourceWaiting) and emptying the input buffer.</li>
     *   <li>Clearing the error state flag.</li>
     *   <li>Calling updateVisualState() to refresh visuals accordingly.</li>
     * </ol>
     * </p>
     *
     */
    @Override
    public void resetState()
    {
        processing                  = false;
        ticksRemaining              = DEFAULT_VALUE;
        processedResourceWaiting    = null;
        inputBuffer.clear();
        errorState                  = false;
        updateVisualState();
    }

    /**
     * Updates the processor node each simulation tick.
     *
     * <p>
     * This method manages the complete update cycle for the processor node:
     * <ul>
     *   <li>If the simulation is not running, it simply updates the visual state and returns.</li>
     *   <li>If a processed resource is waiting (i.e. processing is complete), it attempts to output
     *       that resource to every available outgoing pipe. For each outgoing pipe that is empty and not busy,
     *       the resource is set and the pipe visual is updated. After output is attempted on all pipes,
     *       the processed resource is cleared.</li>
     *   <li>If the node is still processing, it advances the processing timer. When processing completes,
     *       the output resource is stored for distribution.</li>
     *   <li>If the node is not processing and no resource is waiting, it first consumes incoming resources,
     *       and then, if the necessary inputs have been gathered, starts a new processing cycle.</li>
     * </ul>
     * </p>
     * <p>
     * Throughout the update cycle, if any changes occur, the node's visual state is refreshed.
     * In the event of an error state, the update will halt further processing.
     * </p>
     *
     * @param timeSeconds the elapsed time in seconds since the last simulation tick.
     * @param gameController the {@link GameController}
     *                       used for updating pipe visuals and managing simulation state.
     *
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

        boolean changed = false;

        if (!errorState && processedResourceWaiting != null)
        {

            for (final Pipe outPipe : getOutgoingPipes())
            {
                if (outPipe.isEmpty() && outPipe.isBusyThisTick())
                {
                    if (outPipe.trySetResource(processedResourceWaiting))
                    {
                        gameController.updatePipeVisual(outPipe);
                        changed = true;
                    }
                }
            }

            processedResourceWaiting = null;
        }

        if (!errorState && processing)
        {
            advanceProcessing();

            if (!processing)
            {
                processedResourceWaiting = outputResourceType;
            }
            changed = true;
        }

        if (!errorState && !processing)
        {
            if (consumeInputs(gameController))
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
    }

    /**
     * Creates and returns the visual representation of the processor node.
     *
     * <p>
     * The visual structure is built as follows:
     * <ol>
     *   <li>Create a root StackPane positioned such that its center is at the node's (x, y)
     *       coordinates by offsetting by HALF_BODY_SIZE.</li>
     *   <li>Create a Rectangle sized BODY_SIZE x BODY_SIZE to represent the main body, setting its fill to
     *       the output resource's display color and applying style classes NODE_BODY_STYLE_CLASS and STYLE_CLASS.</li>
     *   <li>Create an HBox with internal padding (INTERNAL_PADDING) to hold two panes separated by a divider.
     *       The left Pane (for inputs) has a pref size adjusted for internal padding, and the right StackPane
     *       (for output) similarly adjusts its size, with a white Rectangle (the divider) of width DIVIDER_WIDTH
     *       placed between them.</li>
     *   <li>Add a small Circle (output indicator) to the right pane, initially invisible.</li>
     *   <li>Compose all elements into the StackPane, then store the references to mainBodyRect, rightOutputPane,
     *       and nodeBodyVisual, and finally call updateVisualState()
     *       to ensure the visuals reflect the current state.</li>
     * </ol>
     * </p>
     *
     * @return the assembled Node representing the processor's complete body visual.
     *
     */
    @Override
    Node createNodeBodyVisual()
    {
        final StackPane rootStack;
        rootStack = new StackPane();
        rootStack.setLayoutX(getXCoordinate() - HALF_BODY_SIZE);
        rootStack.setLayoutY(getYCoordinate() - HALF_BODY_SIZE);
        rootStack.setPrefSize(BODY_SIZE, BODY_SIZE);

        final Rectangle rect;
        rect = new Rectangle(BODY_SIZE, BODY_SIZE);
        rect.setFill(outputResourceType.getDisplayColor());
        rect.getStyleClass().addAll(NODE_BODY_STYLE_CLASS,
                                    STYLE_CLASS);

        final HBox hBox;
        hBox = new HBox(DEFAULT_VALUE);
        hBox.setPadding(new Insets(INTERNAL_PADDING));
        hBox.setFillHeight(true);

        final Pane leftPane;
        leftPane = new Pane();
        leftPane.setPrefSize(HALF_BODY_SIZE - INTERNAL_PADDING,
                             BODY_SIZE - (INTERNAL_PADDING * DIVIDER_WIDTH));

        final Rectangle divider;
        divider = new Rectangle(DIVIDER_WIDTH,
                                BODY_SIZE - (INTERNAL_PADDING * DIVIDER_WIDTH),
                                Color.WHITE);

        final StackPane rightPane;
        rightPane = new StackPane();
        rightPane.setPrefSize(HALF_BODY_SIZE - INTERNAL_PADDING - DIVIDER_WIDTH,
                              BODY_SIZE - (INTERNAL_PADDING * DIVIDER_WIDTH));

        final Circle outputIndicator;
        outputIndicator = new Circle(CIRCLE_RADIUS,
                                     outputResourceType.getDisplayColor());
        outputIndicator.setVisible(false);
        rightPane.getChildren().add(outputIndicator);

        hBox.getChildren().addAll(leftPane,
                                  divider,
                                  rightPane);
        rootStack.getChildren().addAll(rect,
                                       hBox);

        mainBodyRect    = rect;
        rightOutputPane = rightPane;
        nodeBodyVisual  = rootStack;

        updateVisualState();

        return rootStack;
    }

    /**
     * Creates and returns the visual representation of the input connector.
     *
     * <p>
     * The input connector is depicted as a Rectangle positioned relative to the node's center.
     * The X coordinate is computed by adding INPUT_CONNECTOR_OFFSET_X (adjusted by a fraction
     * of CONNECTOR_SIZE) to the node's x-coordinate, and similarly for the Y coordinate.
     * </p>
     *
     * @return a Shape (Rectangle) representing the input connector.
     *
     */
    @Override
    Shape createInputConnectorVisual()
    {
        final double connectorXCoordinate;
        final double connectorYCoordinate;
        final Rectangle rectangle;

        connectorXCoordinate      = getXCoordinate() + INPUT_CONNECTOR_OFFSET_X - (CONNECTOR_SIZE / DIVIDER_WIDTH);
        connectorYCoordinate      = getYCoordinate() + INPUT_CONNECTOR_OFFSET_Y - (CONNECTOR_SIZE / DIVIDER_WIDTH);
        rectangle                 = new Rectangle(connectorXCoordinate,
                                                  connectorYCoordinate,
                                                  CONNECTOR_SIZE,
                                                  CONNECTOR_SIZE);

        return rectangle;
    }

    /**
     * Creates and returns the visual representation of the output connector.
     *
     * <p>
     * The output connector is created similarly to the input connector but uses the output offset values.
     * Its X coordinate is the sum of the node's x-coordinate and OUTPUT_CONNECTOR_OFFSET_X
     * (adjusted by CONNECTOR_SIZE fraction),
     * and likewise for the Y coordinate.
     * </p>
     *
     * @return a Shape (Rectangle) representing the output connector.
     *
     */
    @Override
    Shape createOutputConnectorVisual()
    {
        final double connectorXCoordinate;
        final double connectorYCoordinate;
        final Rectangle rectangle;

        connectorXCoordinate  = getXCoordinate() + OUTPUT_CONNECTOR_OFFSET_X - (CONNECTOR_SIZE / DIVIDER_WIDTH);
        connectorYCoordinate  = getYCoordinate() + OUTPUT_CONNECTOR_OFFSET_Y - (CONNECTOR_SIZE / DIVIDER_WIDTH);
        rectangle             = new Rectangle(connectorXCoordinate,
                                              connectorYCoordinate,
                                              CONNECTOR_SIZE,
                                              CONNECTOR_SIZE);

        return rectangle;
    }

    /**
     * Returns the offset for the input connector.
     *
     * <p>
     * This method simply returns a new Point2D constructed from the constant offsets
     * INPUT_CONNECTOR_OFFSET_X and INPUT_CONNECTOR_OFFSET_Y.
     * </p>
     *
     * @return a Point2D representing the input connector's relative offset.
     *
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
     *
     * <p>
     * This method returns a new Point2D based on the constant offsets for output connectors.
     * </p>
     *
     * @return a Point2D representing the output connector's relative offset.
     *
     */
    @Override
    public Point2D getOutputConnectorOffset()
    {
        final Point2D offset;
        offset = new Point2D(OUTPUT_CONNECTOR_OFFSET_X,
                             OUTPUT_CONNECTOR_OFFSET_Y);

        return offset;
    }

    /**
     * Creates and returns the info label visual for this node.
     *
     * <p>
     * Since ProcessorNode does not utilize an info label, this method returns null.
     * </p>
     *
     * @return null
     *
     */
    @Override
    protected Label createInfoLabelVisual()
    {
        return null;
    }
}
