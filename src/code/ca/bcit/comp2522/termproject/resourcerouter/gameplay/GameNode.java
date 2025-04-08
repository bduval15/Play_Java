package ca.bcit.comp2522.termproject.resourcerouter.gameplay;

import ca.bcit.comp2522.termproject.resourcerouter.managers.GameController;
import ca.bcit.comp2522.termproject.resourcerouter.util.Updatable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all game nodes in the Resource Router game.
 * <p>
 * A GameNode represents an interactive element on the game board,
 * such as a resource source, processor, or sink. Each node is uniquely identified by an ID
 * and has a fixed position (x, y) that serves as its center. It maintains references to its
 * connected pipes (both outgoing and incoming) to facilitate resource flow between nodes.
 * </p>
 * <p>
 * Subclasses must implement the abstract methods to define their specific visual representations and behaviors:
 * <ul>
 *   <li>{@link #createNodeBodyVisual()} - Constructs the main visual component for the node.</li>
 *   <li>{@link #createOutputConnectorVisual()} - Creates the visual representation for
 *   the output connector (if applicable).</li>
 *   <li>{@link #createInputConnectorVisual()} - Creates the visual representation for the
 *   input connector (if applicable).</li>
 *   <li>{@link #createInfoLabelVisual()} - Generates an optional label that displays additional node information.</li>
 *   <li>{@link #getInputConnectorOffset()} - Returns the offset to position the input
 *   connector relative to the node's origin.</li>
 *   <li>{@link #getOutputConnectorOffset()} - Returns the offset to position the output
 *   connector relative to the node's origin.</li>
 *   <li>{@link #update(double, GameController)} - Updates the node's state and appearance
 *   over time as part of the game simulation.</li>
 *   <li>{@link #resetState()} - Resets the node to its initial state, typically invoked
 *   at the start of a new level or simulation cycle.</li>
 * </ul>
 * </p>
 * <p>
 * The class also provides methods to manage connected pipes (adding, removing, and clearing), as well as
 * functionality to integrate the node's visuals into a JavaFX Pane
 * (e.g., via {@link #addToPane(Pane)} and {@link #removeFromPane(Pane)}).
 * </p>
 * <p>
 * By implementing the {@link Updatable} interface,
 * each GameNode ensures that it can be updated on every simulation tick.
 * Subclasses should call {@link #initializeVisuals()} before being added to the scene to
 * ensure proper visual setup.
 * </p>
 * <p>
 * Equality and hash code for GameNode are defined solely in terms of the unique node ID.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 *
 */

public abstract class GameNode
                implements Updatable
{
    private static final int    DEFAULT_VALUE                = 0;

    private static final String NODE_ID_PREFIX_BODY          = "node-body-";
    private static final String NODE_ID_PREFIX_CONNECTOR_IN  = "connector-in-";
    private static final String NODE_ID_PREFIX_CONNECTOR_OUT = "connector-out-";
    private static final String NODE_ID_PREFIX_LABEL         = "label-";
    private static final String CONNECTOR_STYLE_CLASS        = "connector";
    private static final String NODE_BODY_STYLE_CLASS        = "node";

    private final List<Pipe> outgoingPipes;
    private final List<Pipe> incomingPipes;

    private final double xCoordinate;
    private final double yCoordinate;
    private final String nodeId;

    private Node  outputConnectorVisual;
    private Node  inputConnectorVisual;
    private Label infoLabelVisual;

    Node nodeBodyVisual;

    {
        outgoingPipes = new ArrayList<>();
        incomingPipes = new ArrayList<>();
    }

    /**
     * Constructs a new GameNode with the specified unique identifier and center coordinates.
     * <p>
     * This constructor performs the following steps:
     * <ul>
     *   <li>Validates that the {@code id} is non-null and non-empty.</li>
     *   <li>Validates the coordinates to ensure they are not NaN and are non-negative.</li>
     *   <li>Assigns the id and coordinates to the corresponding instance variables.</li>
     * </ul>
     * </p>
     *
     * @param nodeId    the unique identifier for the node; must not be null or empty.
     * @param xCoordinate the x-coordinate of the node's center; must be non-negative and not NaN.
     * @param yCoordinate the y-coordinate of the node's center; must be non-negative and not NaN.
     *
     * @throws NullPointerException     if {@code id} is null.
     * @throws IllegalArgumentException if {@code id} is empty or if either coordinate is negative or NaN.
     */
    GameNode(final String nodeId,
             final double xCoordinate,
             final double yCoordinate)
    {
        validateId(nodeId);
        validateCoordinates(xCoordinate, yCoordinate);

        this.nodeId      = nodeId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    /*
     * Validates that the provided node nodeId is non-null and non-empty.
     *
     * @param nodeId the node nodeId to validate.
     *
     * @throws IllegalArgumentException if nodeId is null or an empty string.
     *
     */
    private static void validateId(final String nodeId)
    {
        if(nodeId == null || nodeId.trim().isEmpty())
        {
            throw new IllegalArgumentException("Node ID empty.");
        }
    }

    /*
     * Validates that the provided coordinates are not NaN and are non-negative.
     *
     * @param xCoordinate the xCoordinate-coordinate.
     * @param yCoordinate the yCoordinate-coordinate.
     *
     * @throws IllegalArgumentException if either xCoordinate or yCoordinate is NaN or if xCoordinate or yCoordinate is negative.
     */
    private static void validateCoordinates(final double xCoordinate,
                                            final double yCoordinate)
    {
        if (Double.isNaN(xCoordinate))
        {
            throw new IllegalArgumentException("xCoordinate cannot be NaN");
        }

        if (Double.isNaN(yCoordinate))
        {
            throw new IllegalArgumentException("yCoordinate cannot be NaN");
        }

        if (xCoordinate < DEFAULT_VALUE)
        {
            throw new IllegalArgumentException("xCoordinate cannot be negative: " + xCoordinate);
        }

        if (yCoordinate < DEFAULT_VALUE)
        {
            throw new IllegalArgumentException("yCoordinate cannot be negative: " + yCoordinate);
        }
    }

    /**
     * Constructs and returns the visual representation of the node's body.
     *
     * <p>
     * Implementations should create and return a JavaFX {@code Node}
     * that represents the main component of the game node.
     * This visual element is used to render the node on the game board.
     * </p>
     *
     * @return a JavaFX Node that visually represents the node's body.
     *
     */
    abstract Node createNodeBodyVisual();

    /**
     * Constructs and returns the visual representation of the node's output connector.
     *
     * <p>
     * Implementations should create and return a JavaFX {@code Shape} that represents the output connector,
     * which facilitates connecting this node to downstream nodes.
     * </p>
     *
     * @return a Shape representing the output connector, or null if not applicable.
     *
     */
    abstract Shape createOutputConnectorVisual();

    /**
     * Constructs and returns the visual representation of the node's input connector.
     *
     * <p>
     * Implementations should create and return a JavaFX {@code Shape} that represents the input connector,
     * which allows other nodes to connect to this node.
     * </p>
     *
     * @return a Shape representing the input connector, or null if not applicable.
     *
     */
    abstract Shape createInputConnectorVisual();

    /**
     * Constructs and returns the optional info label visual.
     *
     * <p>
     * Implementations may create a JavaFX {@code Label} containing additional information about the node.
     * This label is optional and may be null if not used.
     * </p>
     *
     * @return a Label with extra node information, or null if no label is needed.
     *
     */
    abstract Label createInfoLabelVisual();

    /**
     * Returns the offset used for positioning the node’s input connector.
     *
     * <p>
     * The offset is defined as a relative {@link Point2D}
     * value indicating how far the input connector should be positioned from the node’s center (xCord, yCord).
     * </p>
     *
     * @return a Point2D representing the offset for the input connector, or null if no offset is used.
     *
     */
    abstract Point2D getInputConnectorOffset();

    /**
     * Returns the offset used for positioning the node’s output connector.
     *
     * <p>
     * The offset is defined as a relative {@link Point2D} value indicating how far
     * the output connector should be positioned from the node’s center (xCord, yCord).
     * </p>
     *
     * @return a Point2D representing the offset for the output connector, or null if no offset is used.
     *
     */
    abstract Point2D getOutputConnectorOffset();

    /**
     * Resets the node to its initial state.
     *
     * <p>
     * This method should clear any dynamic state or animations so that
     * the node is ready for a new level or simulation cycle.
     * </p>
     *
     */
    public abstract void resetState();

    /**
     * Updates the node's state based on elapsed time.
     *
     * <p>
     * This method is called on every simulation tick.
     * Implementations should update the node’s appearance and internal state using the provided delta time
     * (in seconds) and may use the {@link GameController} for handling interactions.
     * </p>
     *
     * @param timeSeconds   the elapsed time (in seconds) since the last update.
     * @param controller    the GameController managing the game state and interactions.
     *
     */
    public abstract void update(final double timeSeconds,
                                final GameController controller);

    /**
     * Returns the list of outgoing pipes connected to this node.
     *
     * <p>
     * The outgoing pipes represent the connections that originate from this node and lead to other nodes.
     * </p>
     *
     * @return a List of Pipe objects representing outgoing connections.
     *
     */
    public List<Pipe> getOutgoingPipes()
    {
        return outgoingPipes;
    }

    /**
     * Returns the center point of the output connector.
     *
     * <p>
     * This method computes the output connector’s center as follows:
     * <ol>
     *   <li>Calls {@link #getOutputConnectorOffset()} to retrieve the relative offset.</li>
     *   <li>If the offset is null or its x-value is NaN, it returns the node's center (xCord, yCord).</li>
     *   <li>Otherwise, the center is calculated by adding the offset values to the node’s center coordinates.</li>
     * </ol>
     * </p>
     *
     * @return a Point2D representing the center of the output connector.
     *
     */
    public Point2D getOutputConnectorCenter()
    {
        final Point2D offset;
        final Point2D center;

        offset = getOutputConnectorOffset();

        if (offset == null || Double.isNaN(offset.getX()))
        {
            center = new Point2D(xCoordinate, yCoordinate);
        }
        else
        {
            final double centerXCoordinate;
            final double centerYCoordinate;

            centerXCoordinate = xCoordinate + offset.getX();
            centerYCoordinate = yCoordinate + offset.getY();
            center            = new Point2D(centerXCoordinate,
                                            centerYCoordinate);
        }

        return center;
    }

    /**
     * Returns the center point of the input connector.
     *
     * <p>
     * This method computes the input connector’s center as follows:
     * <ol>
     *   <li>Retrieves the offset from {@link #getInputConnectorOffset()}.</li>
     *   <li>If the offset is null or invalid (i.e. NaN), it returns the node’s center.</li>
     *   <li>Otherwise, it returns a new Point2D calculated by adding the offset to the node’s center.</li>
     * </ol>
     * </p>
     *
     * @return a Point2D representing the center of the input connector.
     *
     */
    public Point2D getInputConnectorCenter()
    {
        final Point2D offset;
        final Point2D center;

        offset = getInputConnectorOffset();

        if (offset == null || Double.isNaN(offset.getX()))
        {
            center = new Point2D(xCoordinate, yCoordinate);
        }
        else
        {
            final double centerXCoordinate;
            final double centerYCoordinate;

            centerXCoordinate = xCoordinate + offset.getX();
            centerYCoordinate = yCoordinate + offset.getY();
            center            = new Point2D(centerXCoordinate,
                                            centerYCoordinate);
        }

        return center;
    }

    /**
     * Adds an outgoing pipe to this node.
     * <pipe>
     * Before adding, this method checks that:
     * <ul>
     *   <li>The pipe is non-null.</li>
     *   <li>The pipe's start node is this node (to ensure proper connection).</li>
     *   <li>The pipe is not already in the outgoing pipes list.</li>
     * </ul>
     * If all conditions are met, the pipe is added to the list of outgoing pipes.
     * </pipe>
     *
     * @param pipe the Pipe to add; if null or invalid, nothing is added.
     *
     */
    public void addOutgoingPipe(final Pipe pipe)
    {
        if (pipe != null &&
            pipe.getStartNode() == this &&
            !outgoingPipes.contains(pipe))
        {
            outgoingPipes.add(pipe);
        }
    }

    /**
     * Adds an incoming pipe to this node.
     * <pipe>
     * This method checks that:
     * <ul>
     *   <li>The provided pipe is non-null.</li>
     *   <li>The pipe’s end node is this node.</li>
     *   <li>The pipe is not already in the incoming pipes list.</li>
     * </ul>
     * If all checks pass, the pipe is added to the incoming pipes.
     * </pipe>
     *
     * @param pipe the Pipe to add.
     *
     */
    public void addIncomingPipe(final Pipe pipe)
    {
        if (pipe != null &&
            pipe.getEndNode() == this &&
            !incomingPipes.contains(pipe))
        {
            incomingPipes.add(pipe);
        }
    }

    /**
     * Removes the specified outgoing pipe from this node.
     *
     * <p>
     * If the pipe is not null and exists in the outgoing pipes list, it is removed.
     * </p>
     *
     * @param pipe the Pipe to remove.
     *
     */
    public void removeOutgoingPipe(final Pipe pipe)
    {
        if (pipe != null)
        {
            outgoingPipes.remove(pipe);
        }
    }

    /**
     * Removes the specified incoming pipe from this node.
     *
     * <p>
     * If the pipe is not null and is present in the incoming pipes list, it is removed.
     * </p>
     *
     * @param pipe the Pipe to remove.
     *
     */
    public void removeIncomingPipe(final Pipe pipe)
    {
        if (pipe != null)
        {
            incomingPipes.remove(pipe);
        }
    }

    /**
     * Clears all pipes connected to this node.
     *
     * <p>
     * The method works by:
     * <ol>
     *   <li>Creating a new ArrayList from the outgoing pipes and iterating over each pipe.</li>
     *   <li>For each outgoing pipe, if its end node is non-null,
     *       removing this pipe from that end node’s incoming pipe list.</li>
     *   <li>Repeating the process for incoming pipes by removing each from its start node’s outgoing list.</li>
     *   <li>Finally, clearing both the outgoing and incoming pipe lists.</li>
     * </ol>
     * This ensures all pipe connections are completely removed from both ends.
     * </p>
     *
     */
    public void clearPipes()
    {
        for (final Pipe pipe : new ArrayList<>(outgoingPipes))
        {
            if (pipe.getEndNode() != null)
            {
                pipe.getEndNode().removeIncomingPipe(pipe);
            }
        }
        for (final Pipe pipe : new ArrayList<>(incomingPipes))
        {
            if (pipe.getStartNode() != null)
            {
                pipe.getStartNode().removeOutgoingPipe(pipe);
            }
        }
        outgoingPipes.clear();
        incomingPipes.clear();
    }

    /**
     * Initializes the visual representations of this node.
     *
     * <p>
     * This method performs the following for each visual element:
     * <ul>
     *   <li>If the node body visual is null, it calls
     *       {@link #createNodeBodyVisual()}, assigns an id by concatenating
     *       {@code NODE_ID_PREFIX_BODY} with the node id, and applies the style class
     *       {@code NODE_BODY_STYLE_CLASS}.</li>
     *   <li>For the input connector visual, it retrieves an offset via
     *       {@link #getInputConnectorOffset()}.
     *       If a valid offset is provided, it creates the input connector visual via
     *       {@link #createInputConnectorVisual()},
     *       assigns an id using {@code NODE_ID_PREFIX_CONNECTOR_IN} and applies the
     *       {@code CONNECTOR_STYLE_CLASS}.</li>
     *   <li>Similarly, if the output connector visual is null and a valid offset is available from
     *       {@link #getOutputConnectorOffset()},
     *       it creates the output connector visual, assigns an id using {@code NODE_ID_PREFIX_CONNECTOR_OUT},
     *       and applies the {@code CONNECTOR_STYLE_CLASS}.</li>
     *   <li>If the info label visual is null, it calls {@link #createInfoLabelVisual()}
     *       and, if non-null, sets its id using {@code NODE_ID_PREFIX_LABEL} followed by the node id.</li>
     * </ul>
     * </p>
     *
     */
    public void initializeVisuals()
    {
        if (nodeBodyVisual == null)
        {
            nodeBodyVisual = createNodeBodyVisual();
            nodeBodyVisual.setId(NODE_ID_PREFIX_BODY + nodeId);
            nodeBodyVisual.getStyleClass().add(NODE_BODY_STYLE_CLASS);
        }
        if (inputConnectorVisual == null)
        {
            final Point2D iOff;
            iOff = getInputConnectorOffset();

            if (iOff != null && !Double.isNaN(iOff.getX()))
            {
                inputConnectorVisual = createInputConnectorVisual();

                if (inputConnectorVisual != null)
                {
                    inputConnectorVisual.setId(NODE_ID_PREFIX_CONNECTOR_IN + nodeId);
                    inputConnectorVisual.getStyleClass().add(CONNECTOR_STYLE_CLASS);
                }
            }
        }
        if (outputConnectorVisual == null)
        {
            final Point2D oOff;
            oOff = getOutputConnectorOffset();

            if (oOff != null && !Double.isNaN(oOff.getX()))
            {
                outputConnectorVisual = createOutputConnectorVisual();

                if (outputConnectorVisual != null)
                {
                    outputConnectorVisual.setId(NODE_ID_PREFIX_CONNECTOR_OUT + nodeId);
                    outputConnectorVisual.getStyleClass().add(CONNECTOR_STYLE_CLASS);
                }
            }
        }
        if (infoLabelVisual == null)
        {
            infoLabelVisual = createInfoLabelVisual();

            if (infoLabelVisual != null)
            {
                infoLabelVisual.setId(NODE_ID_PREFIX_LABEL + nodeId);
            }
        }
    }

    /**
     * Adds this node's visual elements to a provided JavaFX Pane.
     *
     * <p>
     * This method first calls {@link #initializeVisuals()} to ensure that all visuals are properly set up.
     * It then adds each non-null visual element (input connector, output connector, node body, and info label)
     * to the Pane's children so that they appear on the game scene.
     * </p>
     *
     * @param gamePane the Pane to which the node's visuals will be added.
     *
     */
    public void addToPane(final Pane gamePane)
    {
        initializeVisuals();

        if (inputConnectorVisual != null)
        {
            gamePane.getChildren().add(inputConnectorVisual);
        }

        if (outputConnectorVisual != null)
        {
            gamePane.getChildren().add(outputConnectorVisual);
        }

        if (nodeBodyVisual != null)
        {
            gamePane.getChildren().add(nodeBodyVisual);
        }

        if (infoLabelVisual != null)
        {
            gamePane.getChildren().add(infoLabelVisual);
        }
    }

    /**
     * Removes this node's visual elements from the specified JavaFX Pane.
     *
     * <p>
     * The method removes each visual element (info label, node body, output connector, then input connector)
     * from the Pane's children if they are present.
     * </p>
     *
     * @param gamePane the Pane from which the node's visuals will be removed.
     *
     */
    public void removeFromPane(final Pane gamePane)
    {
        if (infoLabelVisual != null)
        {
            gamePane.getChildren().remove(infoLabelVisual);
        }

        if (nodeBodyVisual != null)
        {
            gamePane.getChildren().remove(nodeBodyVisual);
        }

        if (outputConnectorVisual != null)
        {
            gamePane.getChildren().remove(outputConnectorVisual);
        }

        if (inputConnectorVisual != null)
        {
            gamePane.getChildren().remove(inputConnectorVisual);
        }
    }

    /**
     * Returns a string representation of this node.
     *
     * <p>
     * The format is composed of the node's concrete class name followed by the node's id in square brackets.
     * For example, "GameNode[node1]".
     * </p>
     *
     * @return a String that succinctly describes the node.
     *
     */
    @Override
    public String toString()
    {
        final String format;
        format = String.format("%s[%s]", getClass().getSimpleName(), nodeId);

        return format;
    }

    /**
     * Determines if this node is equal to another object.
     *
     * <p>
     * Two GameNodes are considered equal if and only if they are of the same class and their unique ids are equal.
     * </p>
     *
     * @param object the object to compare with.
     *
     * @return true if the other object is a GameNode with the same id; false otherwise.
     *
     */
    @Override
    public boolean equals(final Object object)
    {
        if (this == object)
        {
            return true;
        }
        if (object == null || getClass() != object.getClass())
        {
            return false;
        }

        final GameNode gameNode;
        gameNode = (GameNode) object;

        final boolean isEqual;
        isEqual = nodeId.equals(gameNode.nodeId);

        return isEqual;
    }

    /**
     * Returns the hash code for this node.
     *
     * <p>
     * The hash code is computed solely from the unique id of the node.
     * </p>
     *
     * @return the hash code as an integer.
     *
     */
    @Override
    public int hashCode()
    {
        final int hash;
        hash = Objects.hash(nodeId);

        return hash;
    }

    /**
     * Returns the prefix used for labeling input connector visuals.
     *
     * <p>
     * This public static getter allows other packages (such as managers) to retrieve the constant value,
     * while keeping the underlying constant private.
     * </p>
     *
     * @return the input connector prefix string.
     *
     */
    public static String getNodeIdPrefixConnectorIn()
    {
        return NODE_ID_PREFIX_CONNECTOR_IN;
    }

    /**
     * Returns the prefix used for labeling output connector visuals.
     *
     * <p>
     * This method provides controlled access to the constant value used for output connectors.
     * </p>
     *
     * @return the output connector prefix string.
     *
     */
    public static String getNodeIdPrefixConnectorOut()
    {
        return NODE_ID_PREFIX_CONNECTOR_OUT;
    }

    /**
     * Returns the node's unique identifier.
     *
     * <p>
     * This method provides public read access to the node's id.
     * </p>
     *
     * @return the unique node id.
     *
     */
    public String getNodeId()
    {
        return nodeId;
    }

    /**
     * Returns the list of incoming pipes connected to this node.
     *
     * <p>
     * The incoming pipes are the connections leading into this node.
     * </p>
     *
     * @return a List of Pipe objects representing incoming connections.
     *
     */
    public List<Pipe> getIncomingPipes()
    {
        return incomingPipes;
    }

    /**
     * Returns the x-coordinate of the node's center.
     *
     * @return the x-coordinate as a double.
     *
     */
    public double getXCoordinate()
    {
        return xCoordinate;
    }

    /**
     * Returns the y-coordinate of the node's center.
     *
     * @return the y-coordinate as a double.
     */
    public double getYCoordinate()
    {
        return yCoordinate;
    }
}
