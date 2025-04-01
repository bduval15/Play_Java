package ca.bcit.comp25222.termproject.resourcerouter.gameplay;

import ca.bcit.comp25222.termproject.resourcerouter.managers.GameController;
import ca.bcit.comp25222.termproject.resourcerouter.util.Updatable;
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
 * A GameNode represents an interactive element on the game board, such as a resource source, processor, or sink.
 * Each node is uniquely identified by an ID and has a fixed position (x, y) that serves as its center.
 * It maintains references to its connected pipes (both outgoing and incoming) to facilitate resource flow between nodes.
 * </p>
 * <p>
 * Subclasses must implement the abstract methods to define their specific visual representations and behaviors:
 * <ul>
 *   <li>{@link #createNodeBodyVisual()} - Constructs the main visual component for the node.</li>
 *   <li>{@link #createOutputConnectorVisual()} - Creates the visual representation for the output connector (if applicable).</li>
 *   <li>{@link #createInputConnectorVisual()} - Creates the visual representation for the input connector (if applicable).</li>
 *   <li>{@link #createInfoLabelVisual()} - Generates an optional label that displays additional node information.</li>
 *   <li>{@link #getInputConnectorOffset()} - Returns the offset to position the input connector relative to the node's origin.</li>
 *   <li>{@link #getOutputConnectorOffset()} - Returns the offset to position the output connector relative to the node's origin.</li>
 *   <li>{@link #update(double, GameController)} - Updates the node's state and appearance over time as part of the game simulation.</li>
 *   <li>{@link #resetState()} - Resets the node to its initial state, typically invoked at the start of a new level or simulation cycle.</li>
 * </ul>
 * </p>
 * <p>
 * The class also provides methods to manage connected pipes (adding, removing, and clearing), as well as
 * functionality to integrate the node's visuals into a JavaFX Pane (e.g., via {@link #addToPane(Pane)} and {@link #removeFromPane(Pane)}).
 * </p>
 * <p>
 * By implementing the {@link ca.bcit.comp25222.termproject.resourcerouter.util.Updatable} interface, each GameNode
 * ensures that it can be updated on every simulation tick. Subclasses should call {@link #initializeVisuals()}
 * before being added to the scene to ensure proper visual setup.
 * </p>
 * <p>
 * Equality and hash code for GameNode are defined solely in terms of the unique node ID.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public abstract class GameNode
                implements Updatable
{

    public static final String NODE_ID_PREFIX_BODY          = "node-body-";
    public static final String NODE_ID_PREFIX_CONNECTOR_IN  = "connector-in-";
    public static final String NODE_ID_PREFIX_CONNECTOR_OUT = "connector-out-";
    public static final String NODE_ID_PREFIX_LABEL         = "label-";
    public static final String CONNECTOR_STYLE_CLASS        = "connector";
    public static final String NODE_BODY_STYLE_CLASS        = "node";

    protected final List<Pipe> outgoingPipes;
    protected final List<Pipe> incomingPipes;

    protected final double x;
    protected final double y;
    protected       String id;

    protected Node  nodeBodyVisual;
    protected Node  outputConnectorVisual;
    protected Node  inputConnectorVisual;
    protected Label infoLabelVisual;

    {
        outgoingPipes = new ArrayList<>();
        incomingPipes = new ArrayList<>();
    }

    /**
     * Constructs a new GameNode with the specified id and coordinates.
     *
     * @param id the unique identifier for the node
     * @param x  the x-coordinate of the node's center
     * @param y  the y-coordinate of the node's center
     * @throws NullPointerException if id is null
     * @throws IllegalArgumentException if id is empty
     */
    protected GameNode(final String id,
                       final double x,
                       final double y)
    {
        validateId(id);

        this.id = id;
        this.x = x;
        this.y = y;
    }

    /**
     * Creates the visual representation of the node body.
     *
     * @return a Node representing the node body.
     */
    protected abstract Node createNodeBodyVisual();

    /**
     * Creates the visual representation of the output connector.
     *
     * @return a Shape representing the output connector.
     */
    protected abstract Shape createOutputConnectorVisual();

    /**
     * Creates the visual representation of the input connector.
     *
     * @return a Shape representing the input connector.
     */
    protected abstract Shape createInputConnectorVisual();

    /**
     * Creates the visual representation of the info label.
     *
     * @return a Label for the node, or null if not used.
     */
    protected abstract Label createInfoLabelVisual();

    // --- Public Methods ---
    /**
     * Returns the offset for the input connector.
     *
     * @return a Point2D representing the input connector's offset.
     */
    public abstract Point2D getInputConnectorOffset();

    /**
     * Returns the offset for the output connector.
     *
     * @return a Point2D representing the output connector's offset.
     */
    public abstract Point2D getOutputConnectorOffset();

    /**
     * Resets the node's state.
     */
    public abstract void resetState();

    /**
     * Updates the node's state.
     *
     * @param deltaTime  the time elapsed since the last update in seconds
     * @param controller the GameController for interactions
     */
    public abstract void update(double deltaTime,
                                GameController controller);

    /**
     * Returns the node's unique identifier.
     *
     * @return the node ID.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets a new unique identifier for the node.
     *
     * @param newId the new identifier to set; must not be null or empty.
     * @throws IllegalArgumentException if newId is null or empty.
     */
    public void setId(final String newId)
    {
        if (newId == null || newId.trim().isEmpty())
        {
            throw new IllegalArgumentException("New ID cannot be null or empty.");
        }
        this.id = newId.trim();
    }

    /**
     * Returns the list of outgoing pipes.
     *
     * @return the list of outgoing pipes.
     */
    public List<Pipe> getOutgoingPipes()
    {
        return outgoingPipes;
    }

    /**
     * Returns the list of incoming pipes.
     *
     * @return the list of incoming pipes.
     */
    public List<Pipe> getIncomingPipes()
    {
        return incomingPipes;
    }

    /**
     * Returns the center of the output connector.
     *
     * @return a Point2D representing the center of the output connector.
     */
    public Point2D getOutputConnectorCenter()
    {
        final Point2D offset;
        final Point2D center;

        offset = getOutputConnectorOffset();

        if (offset == null || Double.isNaN(offset.getX()))
        {
            center = new Point2D(x, y);
        }
        else
        {
            final double centerX;
            final double centerY;

            centerX = x + offset.getX();
            centerY = y + offset.getY();
            center = new Point2D(centerX, centerY);
        }

        return center;
    }

    /**
     * Returns the center of the input connector.
     *
     * @return a Point2D representing the center of the input connector.
     */
    public Point2D getInputConnectorCenter()
    {
        final Point2D offset;
        final Point2D center;

        offset = getInputConnectorOffset();

        if (offset == null || Double.isNaN(offset.getX()))
        {
            center = new Point2D(x, y);
        }
        else
        {
            final double centerX;
            final double centerY;

            centerX = x + offset.getX();
            centerY = y + offset.getY();
            center = new Point2D(centerX, centerY);
        }

        return center;
    }

    /**
     * Adds an outgoing pipe to this node.
     *
     * @param p the Pipe to add.
     */
    public void addOutgoingPipe(final Pipe p)
    {
        if (p != null && p.getStartNode() == this && !outgoingPipes.contains(p))
        {
            outgoingPipes.add(p);
        }
    }

    /**
     * Adds an incoming pipe to this node.
     *
     * @param p the Pipe to add.
     */
    public void addIncomingPipe(final Pipe p)
    {
        if (p != null && p.getEndNode() == this && !incomingPipes.contains(p))
        {
            incomingPipes.add(p);
        }
    }

    /**
     * Removes the specified outgoing pipe from this node.
     *
     * @param pipe the Pipe to remove.
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
     * @param pipe the Pipe to remove.
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
     */
    public void clearPipes()
    {
        for (Pipe p : new ArrayList<>(outgoingPipes))
        {
            if (p.getEndNode() != null)
            {
                p.getEndNode().removeIncomingPipe(p);
            }
        }
        for (Pipe p : new ArrayList<>(incomingPipes))
        {
            if (p.getStartNode() != null)
            {
                p.getStartNode().removeOutgoingPipe(p);
            }
        }
        outgoingPipes.clear();
        incomingPipes.clear();
    }

    /**
     * Initializes the visual representations of this node.
     */
    public void initializeVisuals()
    {
        if (nodeBodyVisual == null)
        {
            nodeBodyVisual = createNodeBodyVisual();
            nodeBodyVisual.setId(NODE_ID_PREFIX_BODY + id);
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
                    inputConnectorVisual.setId(NODE_ID_PREFIX_CONNECTOR_IN + id);
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
                    outputConnectorVisual.setId(NODE_ID_PREFIX_CONNECTOR_OUT + id);
                    outputConnectorVisual.getStyleClass().add(CONNECTOR_STYLE_CLASS);
                }
            }
        }
        if (infoLabelVisual == null)
        {
            infoLabelVisual = createInfoLabelVisual();

            if (infoLabelVisual != null)
            {
                infoLabelVisual.setId(NODE_ID_PREFIX_LABEL + id);
            }
        }
    }

    /**
     * Adds this node's visuals to the specified Pane.
     *
     * @param gp the Pane to add the visuals to.
     */
    public void addToPane(final Pane gp)
    {
        initializeVisuals();

        if (inputConnectorVisual != null)
        {
            gp.getChildren().add(inputConnectorVisual);
        }

        if (outputConnectorVisual != null)
        {
            gp.getChildren().add(outputConnectorVisual);
        }

        if (nodeBodyVisual != null)
        {
            gp.getChildren().add(nodeBodyVisual);
        }

        if (infoLabelVisual != null)
        {
            gp.getChildren().add(infoLabelVisual);
        }
    }

    /**
     * Removes this node's visuals from the specified Pane.
     *
     * @param gp the Pane to remove the visuals from.
     */
    public void removeFromPane(final Pane gp)
    {
        if (infoLabelVisual != null)
        {
            gp.getChildren().remove(infoLabelVisual);
        }

        if (nodeBodyVisual != null)
        {
            gp.getChildren().remove(nodeBodyVisual);
        }

        if (outputConnectorVisual != null)
        {
            gp.getChildren().remove(outputConnectorVisual);
        }

        if (inputConnectorVisual != null)
        {
            gp.getChildren().remove(inputConnectorVisual);
        }
    }

    /**
     * Returns a string representation of this node.
     *
     * @return a string representing this node.
     */
    @Override
    public String toString()
    {
        return String.format("%s[%s]", getClass().getSimpleName(), id);
    }

    /**
     * Determines if this node is equal to another.
     *
     * @param o the other object
     * @return true if the nodes have the same ID; false otherwise.
     */
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final GameNode gameNode;
        gameNode = (GameNode) o;

        return id.equals(gameNode.id);
    }

    /**
     * Returns the hash code for this node.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    /**
     * Validation method for Node id
     *
     * @param id the unique identifier for each node
     */
    private static void validateId(final String id)
    {
        if(id == null || id.isEmpty())
        {
            throw new IllegalArgumentException("Node ID empty.");
        }
    }
}
