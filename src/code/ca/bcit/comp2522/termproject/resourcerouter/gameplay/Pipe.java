package ca.bcit.comp2522.termproject.resourcerouter.gameplay;

import ca.bcit.comp2522.termproject.resourcerouter.util.ResourceType;
import javafx.animation.PathTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a pipe connecting two game nodes and animates the flow of resources between them.
 * <p>
 * A Pipe instance links a source or processor node to a processor or sink node, thereby facilitating
 * resource transfer within the game. The visual representation of the pipe is managed via a
 * {@link javafx.scene.shape.CubicCurve}, along which resource particles
 * (represented by {@link javafx.scene.shape.Circle}) are animated using a
 * {@link javafx.animation.PathTransition} to simulate the movement of resources.
 * </p>
 * <p>
 * The pipe maintains a reference to its start and end {@link GameNode} objects, as well as the current
 * resource being transported (if any). It also tracks whether the pipe is busy during a simulation tick
 * and updates its visual state (by applying or removing style classes such as the base style, a glow effect,
 * and resource-specific styles) based on whether it is empty or carrying a resource.
 * </p>
 * <p>
 * The class provides methods to set, clear, and query the current resource, to reset the pipe’s state,
 * and to animate resource flow. Active particle animations are tracked and removed once their animation completes.
 * The equality and hash code of a Pipe are defined solely by the IDs of its start and end nodes.
 * </p>
 * <p>
 * In summary, the Pipe class encapsulates the functionality needed to visually and functionally
 * represent a connection between nodes in the Resource Router game, handling both the underlying resource
 * management and the associated visual animations.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class Pipe
{

    private static final String PIPE_GLOW_CSS       = "pipe-glow";
    private static final String PIPE_RESOURCE_CSS   = "pipe-resource";

    private static final String BASE_STYLE_CLASS     = "pipe";
    private static final String PIPE_ID_PREFIX       = "pipe-";
    private static final double PARTICLE_RADIUS      = 6.0;
    private static final double PARTICLE_DURATION    = 1.0;
    private static final int    ONE_CYCLE            = 1;

    private final GameNode      startNode;
    private final GameNode      endNode;
    private final List<Circle>  activeParticles;

    private ResourceType    currentResource;
    private Line            lineVisual;
    private boolean         tick;
    private boolean         state;

    {
        activeParticles = new ArrayList<>();
    }

    /**
     * Constructs a new {@code Pipe} connecting the specified start and end {@link GameNode}s.
     * <p>
     * This constructor ensures that neither {@code startNode} nor {@code endNode} is {@code null},
     * and delegates further validation to {@link #validateConnection(GameNode, GameNode)}
     * to confirm that the link is logically valid (e.g., output-to-input or any other criteria).
     * If the connection is considered invalid, an {@link IllegalArgumentException} is thrown.
     * </p>
     *
     * @param startNode the {@link GameNode} from which the pipe originates
     * @param endNode   the {@link GameNode} where the pipe terminates
     *
     * @throws NullPointerException     if either {@code startNode} or {@code endNode} is {@code null}
     * @throws IllegalArgumentException if the connection between {@code startNode} and {@code endNode} is invalid
     *
     */
    public Pipe(final GameNode startNode,
                final GameNode endNode)
    {
        validateNodesNotNull    (startNode, endNode);
        validateConnection      (startNode, endNode);

        this.startNode = startNode;
        this.endNode   = endNode;

        resetState();
    }

    /*
     * Validates that neither the start node nor the end node is null.
     *
     * @param startNode the node from which the pipe originates
     * @param endNode   the node where the pipe terminates
     *
     * @throws NullPointerException if startNode or endNode is null
     *
     */
    private static void validateNodesNotNull(final GameNode startNode,
                                             final GameNode endNode)
    {
        Objects.requireNonNull(startNode, "Start node cannot be null");
        Objects.requireNonNull(endNode,   "End node cannot be null");
    }

    /*
     * Validates that the given start node and end node form a logically correct connection.
     *
     * For example, you may require that the start node is a source or processor (out-capable),
     * and the end node is a processor or sink (in-capable). If the nodes do not meet these criteria,
     * this method throws an IllegalArgumentException.
     *
     *
     * @param startNode the node from which the pipe originates
     * @param endNode   the node where the pipe terminates
     *
     * @throws IllegalArgumentException if the connection is invalid according to the game logic
     *
     */
    private static void validateConnection(final GameNode startNode,
                                           final GameNode endNode)
    {
        if (!isValidConnection(startNode, endNode))
        {
            throw new IllegalArgumentException("Invalid pipe connection: " +
                                               startNode.getClass().getSimpleName() +
                                               " -> " +
                                               endNode.getClass().getSimpleName());
        }
    }

    /*
     * Checks whether the given start and end nodes form a valid connection.
     *
     * @param start the starting GameNode
     * @param end   the ending GameNode
     *
     * @return true if the connection is valid; false otherwise
     *
     */
    private static boolean isValidConnection(final GameNode start,
                                             final GameNode end)
    {
        final boolean validStart;
        final boolean validEnd;
        final boolean validConnection;

        validStart       = (start instanceof SourceNode     || start instanceof ProcessorNode);
        validEnd         = (end   instanceof ProcessorNode  || end   instanceof SinkNode);
        validConnection  = validStart && validEnd;

        return validConnection;
    }

    /*
     * If lineVisual or currentResource is null, the method returns immediately.
     *
     * Otherwise, it creates a new Circle (particle), sets its radius (PARTICLE_RADIUS)
     * and fill color (from currentResource), and adds it to activeParticles.
     *
     * It then retrieves a PathTransition (configured by getPathTransition) for that particle.
     *
     * If the parent of lineVisual is a Pane, the particle is added to the Pane.
     *
     * Finally, the animation is started.
     *
     */
    private void animateResourceFlow()
    {
        if (lineVisual == null || currentResource == null)
        {
            return;
        }

        final Circle particle;
        particle = new Circle();

        particle.setRadius(PARTICLE_RADIUS);
        particle.setFill(currentResource.getDisplayColor());
        activeParticles.add(particle);

        final PathTransition pathTransition;
        pathTransition = getPathTransition(particle);

        final Node parent;
        parent = lineVisual.getParent();

        if (parent instanceof Pane)
        {
            final Pane parentPane;
            parentPane = (Pane) parent;
            parentPane.getChildren().add(particle);
        }
        pathTransition.play();
    }

    /*
     * Creates a new PathTransition.
     *
     * Sets its duration to PARTICLE_DURATION seconds.
     *
     * Uses lineVisual as the path and the given particle as the node to animate.
     *
     * Sets the cycle count to ONE_CYCLE.
     *
     * Determines the parent of lineVisual (if available), and registers an onFinished handler
     * to remove the particle from its parent Pane and from activeParticles.
     *
     * Returns the configured PathTransition.
     *
     */
    private PathTransition getPathTransition(final Circle particle)
    {

        final PathTransition path;
        path = new PathTransition();

        path.setDuration(Duration.seconds(PARTICLE_DURATION));
        path.setPath(lineVisual);
        path.setNode(particle);
        path.setCycleCount(ONE_CYCLE);

        final Node parent;
        if (lineVisual != null)
        {
            parent = lineVisual.getParent();
        }
        else
        {
            parent = null;
        }

        path.setOnFinished(event -> {
            if (parent instanceof Pane)
            {
                final Pane parentPane;
                parentPane = (Pane) parent;
                parentPane.getChildren().remove(particle);
            }
            activeParticles.remove(particle);
        });
        return path;
    }

    /**
     * Updates the visual state of the pipe by adjusting its style classes.
     *
     * <p>
     * This method ensures that the base style class is applied to the line visual.
     * It removes any existing glow or resource-specific classes and, if the pipe is not empty,
     * adds a glow class. Finally, it always adds the resource-specific class.
     * </p>
     *
     */
    public void updateVisualState()
    {
        if (lineVisual != null)
        {
            if (!lineVisual.getStyleClass().contains(BASE_STYLE_CLASS))
            {
                lineVisual.getStyleClass().add(BASE_STYLE_CLASS);
            }

            lineVisual.getStyleClass().remove(PIPE_GLOW_CSS);
            lineVisual.getStyleClass().removeAll(PIPE_RESOURCE_CSS);

            if (!isEmpty())
            {
                lineVisual.getStyleClass().add(PIPE_GLOW_CSS);
            }
            lineVisual.getStyleClass().add(PIPE_RESOURCE_CSS);
        }
    }

    /**
     * Resets the pipe's state.
     *
     * <p>
     * This method clears the current resource and resets the busy flags (tick and state)
     * so that the pipe is ready for a new simulation tick or restart.
     * </p>
     *
     */
    public void resetState()
    {
        currentResource         = null;
        tick                    = false;
        state = false;
    }

    /**
     * Attempts to assign the specified resource to this pipe.
     *
     * <p>
     * The method verifies that the pipe is not already busy or carrying a resource.
     * If it is available, it sets currentResource to the given resource, marks the pipe as busy,
     * triggers the resource flow animation, and returns true. Otherwise, it returns false.
     * </p>
     *
     * @param type the ResourceType to set on this pipe
     *
     * @return true if the resource was successfully assigned; false otherwise
     *
     */
    public boolean trySetResource(final ResourceType type)
    {
        if (tick || currentResource != null)
        {
            return false;
        }
        currentResource = type;
        tick = true;
        state = true;
        animateResourceFlow();

        return true;
    }

    /**
     * Clears the current resource from the pipe.
     *
     * <p>
     * Sets the internal currentResource field to null, effectively emptying the pipe.
     * </p>
     */
    public void clearResource()
    {
        currentResource = null;
    }

    /**
     * Checks if the pipe is empty.
     *
     * <p>
     * Returns true if no resource is currently assigned.
     * </p>
     *
     * @return true if currentResource is null, false otherwise
     *
     */
    public boolean isEmpty()
    {
        final boolean empty;
        empty = (currentResource == null);
        return empty;
    }

    /**
     * Indicates whether the pipe is busy for the current simulation tick.
     *
     * <p>
     * The busy state is determined by the tick flag.
     * </p>
     *
     * @return true if the pipe is busy (not available), false otherwise
     *
     */
    public boolean isBusyThisTick()
    {
        return !tick;
    }

    /**
     * Sets the line visual for the pipe and updates its visual state.
     *
     * <p>
     * The provided Line is stored as the pipe's visual element, and updateVisualState()
     * is invoked to adjust style classes accordingly.
     * </p>
     *
     * @param line the Line object representing the pipe's visual
     *
     */
    public void setLineVisual(final Line line)
    {
        this.lineVisual = line;
        updateVisualState();
    }

    /**
     * Returns the GameNode from which the pipe originates.
     *
     * <p>
     * This node provides the starting point for resource flow along the pipe.
     * </p>
     *
     * @return the start GameNode
     *
     */
    public GameNode getStartNode()
    {
        return startNode;
    }

    /**
     * Returns the GameNode at which the pipe terminates.
     *
     * <p>
     * This node is the destination for resources transported along the pipe.
     * </p>
     *
     * @return the end GameNode
     *
     */
    public GameNode getEndNode()
    {
        return endNode;
    }

    /**
     * Resets the busy flag for the current simulation tick.
     *
     * <p>
     * Sets the internal tick flag to false, allowing the pipe to accept a new resource next tick.
     * </p>
     *
     */
    public void resetTickStatus()
    {
        tick = false;
    }

    /**
     * Returns the current line visual representing the pipe.
     *
     * <p>
     * Typically, this is the Line object that is part of the JavaFX scene graph.
     * </p>
     *
     * @return the line visual, or null if not set
     *
     */
    public Object getLineVisual()
    {
        return lineVisual;
    }

    /**
     * Returns the current resource assigned to this pipe.
     *
     * <p>
     * If no resource is set, returns null.
     * </p>
     *
     * @return the current ResourceType, or null if the pipe is empty
     *
     */
    public ResourceType getCurrentResource()
    {
        return currentResource;
    }

    /**
     * Removes all active particle animations from the pipe.
     *
     * <p>
     * The method creates a copy of the active particle list, iterates over each Circle,
     * and if its parent is a Pane, removes the Circle from the Pane.
     * Finally, it clears the list of active particles.
     * </p>
     *
     */
    public void removeAllParticles()
    {
        final List<Circle> particlesCopy;
        particlesCopy = new ArrayList<>(activeParticles);

        for (final Circle particle : particlesCopy)
        {
            final Node parent;
            parent = particle.getParent();

            if (parent instanceof Pane)
            {
                final Pane parentPane;
                parentPane = (Pane) parent;
                parentPane.getChildren().remove(particle);
            }
        }
        activeParticles.clear();
    }

    /**
     * Returns a string representation of the pipe.
     *
     * <p>
     * The format is "Pipe[startNodeId->endNodeId, R:resourceName]". If no resource is set,
     * an underscore is used in place of the resource name.
     * </p>
     *
     * @return a formatted String representing this pipe.
     *
     */
    @Override
    public String toString()
    {
        final String resourceStr;

        if (currentResource == null)
        {
            resourceStr = "_";
        }
        else
        {
            resourceStr = currentResource.name();
        }

        final String result;
        result = String.format("Pipe[%s->%s, R:%s]",
                               startNode.getNodeId(),
                               endNode.getNodeId(),
                               resourceStr);
        return result;
    }

    /**
     * Determines whether this pipe is equal to another object.
     *
     * <p>
     * Two Pipe instances are considered equal if they have the same start and end node IDs.
     * </p>
     *
     * @param object the object to compare with
     *
     * @return true if both pipes have identical start and end node IDs; false otherwise
     *
     */
    @Override
    public boolean equals(final Object object)
    {
        if (this == object)
        {
            return true;
        }

        if (!(object instanceof Pipe))
        {
            return false;
        }

        final Pipe other;
        other = (Pipe) object;

        final boolean equalsStart;
        equalsStart = startNode.getNodeId().equals(other.startNode.getNodeId());

        final boolean equalsEnd;
        equalsEnd = endNode.getNodeId().equals(other.endNode.getNodeId());

        final boolean result;
        result = equalsStart && equalsEnd;

        return result;
    }

    /**
     * Returns a hash code value for the pipe.
     *
     * <p>
     * The hash code is calculated solely based on the IDs of the start and end nodes.
     * </p>
     *
     * @return an integer hash code for this pipe.
     *
     */
    @Override
    public int hashCode()
    {
        final int result;
        result = Objects.hash(startNode.getNodeId(), endNode.getNodeId());

        return result;
    }

    /**
     * Returns the base style class used for all pipes.
     *
     * <p>
     * This public static method provides controlled access to the internal constant.
     * </p>
     *
     * @return the base style class string.
     *
     */
    public static String getPipeStyle()
    {
        return BASE_STYLE_CLASS;
    }

    /**
     * Returns the pipe ID prefix.
     *
     * <p>
     * This static method exposes the prefix used to generate unique identifiers for pipe visuals.
     * </p>
     *
     * @return the pipe ID prefix string.
     *
     */
    public static String getPipeIdPrefix()
    {
        return PIPE_ID_PREFIX;
    }
}
