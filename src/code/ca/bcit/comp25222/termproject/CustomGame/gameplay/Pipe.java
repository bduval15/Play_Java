package ca.bcit.comp25222.termproject.CustomGame.gameplay;

import ca.bcit.comp25222.termproject.CustomGame.util.ResourceType;
import javafx.animation.PathTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.CubicCurve;
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
 * {@link javafx.scene.shape.CubicCurve}, along which resource particles (represented by {@link javafx.scene.shape.Circle})
 * are animated using a {@link javafx.animation.PathTransition} to simulate the movement of resources.
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

    public static final String BASE_STYLE_CLASS     = "pipe";
    public static final String PIPE_ID_PREFIX       = "pipe-";
    public static final double PARTICLE_RADIUS      = 6.0;
    public static final double PARTICLE_DURATION    = 1.0;
    public static final int ONE_CYCLE               = 1;

    private final GameNode startNode;
    private final GameNode endNode;
    private ResourceType currentResource;
    private boolean busyThisTick;
    private boolean stateChangedThisTick;

    private CubicCurve curveVisual;
    private Line lineVisual;

    private final List<Circle> activeParticles = new ArrayList<>();

    /**
     * Constructs a new Pipe connecting the specified start and end GameNodes.
     *
     * @param startNode the starting GameNode
     * @param endNode the ending GameNode
     * @throws NullPointerException if either node is null
     * @throws IllegalArgumentException if the connection is invalid
     */
    public Pipe(final GameNode startNode,
                final GameNode endNode)
    {
        this.startNode = Objects.requireNonNull(startNode, "Start node cannot be null");
        this.endNode   = Objects.requireNonNull(endNode, "End node cannot be null");

        if (!isValidConnection(startNode, endNode))
        {
            throw new IllegalArgumentException("Invalid pipe connection: " +
                                               startNode.getClass().getSimpleName() + " -> " +
                                               endNode.getClass().getSimpleName());
        }
        resetState();
    }

    /*
     * Checks whether the given start and end nodes form a valid connection.
     *
     * @param start the starting GameNode
     * @param end   the ending GameNode
     * @return true if the connection is valid; false otherwise
     */
    private boolean isValidConnection(final GameNode start,
                                      final GameNode end)
    {
        final boolean validStart;
        final boolean validEnd;
        final boolean validConnection;

        validStart       = (start instanceof SourceNode || start instanceof ProcessorNode);
        validEnd         = (end   instanceof ProcessorNode || end   instanceof SinkNode);
        validConnection  = validStart && validEnd;

        return validConnection;
    }


    /*
     * Animates a particle along the curved pipe to represent resource flow.
     */
    private void animateResourceFlow()
    {
        if (curveVisual == null || currentResource == null)
        {
            return;
        }

        final Circle particle;
        particle = new Circle();

        particle.setRadius(PARTICLE_RADIUS);
        particle.setFill(currentResource.getDisplayColor());
        activeParticles.add(particle);

        final PathTransition pt;
        pt = getPathTransition(particle);

        Node parent;
        parent = curveVisual.getParent();

        if (parent instanceof Pane)
        {
            final Pane parentPane;
            parentPane = (Pane) parent;
            parentPane.getChildren().add(particle);
        }
        pt.play();
    }

    /*
     * Configures and returns a PathTransition for the given particle.
     *
     * @param particle the Circle to animate along the pipe
     * @return the configured PathTransition
     */
    private PathTransition getPathTransition(final Circle particle)
    {

        final PathTransition pt;
        pt = new PathTransition();
        pt.setDuration(Duration.seconds(PARTICLE_DURATION));
        pt.setPath(curveVisual);
        pt.setNode(particle);
        pt.setCycleCount(ONE_CYCLE);

        final Node parent;
        if (curveVisual != null)
        {
            parent = curveVisual.getParent();
        } else {
            parent = null;
        }

        pt.setOnFinished(event -> {
            if (parent instanceof Pane)
            {
                final Pane parentPane;
                parentPane = (Pane) parent;
                parentPane.getChildren().remove(particle);
            }
            activeParticles.remove(particle);
        });
        return pt;
    }

    /*
     * Updates the visual state of the pipe by setting its style classes.
     */
    public void updateVisualState()
    {
        if (curveVisual != null)
        {
            if (!curveVisual.getStyleClass().contains(BASE_STYLE_CLASS))
            {
                curveVisual.getStyleClass().add(BASE_STYLE_CLASS);
            }
            curveVisual.getStyleClass().remove("pipe-glow");
            curveVisual.getStyleClass().removeAll("pipe-resource");
            if (!isEmpty())
            {
                curveVisual.getStyleClass().add("pipe-glow");
            }
            curveVisual.getStyleClass().add("pipe-resource");
        }
    }

    /**
     * Resets the pipe's state.
     */
    public void resetState()
    {
        currentResource = null;
        busyThisTick = false;
        stateChangedThisTick = false;
    }

    /**
     * Attempts to set the given resource on this pipe.
     *
     * @param type the ResourceType to set
     * @return true if the resource was set successfully; false otherwise
     */
    public boolean trySetResource(final ResourceType type)
    {
        if (busyThisTick || currentResource != null)
        {
            return false;
        }
        currentResource = type;
        busyThisTick = true;
        stateChangedThisTick = true;
        animateResourceFlow();
        return true;
    }

    /**
     * Clears the current resource from this pipe.
     */
    public void clearResource()
    {
        currentResource = null;
    }

    /**
     * Checks whether this pipe is empty.
     *
     * @return true if there is no resource set; false otherwise
     */
    public boolean isEmpty()
    {
        return currentResource == null;
    }

    /**
     * Indicates whether this pipe is busy during the current tick.
     *
     * @return true if busy; false otherwise
     */
    public boolean isBusyThisTick()
    {
        return busyThisTick;
    }

    /**
     * Sets the curve visual for this pipe.
     *
     * @param curve the CubicCurve representing the pipe's visual
     */
    public void setCurveVisual(final CubicCurve curve)
    {
        this.curveVisual = curve;
        updateVisualState();
    }

    /**
     * Returns the current curve visual.
     *
     * @return the CubicCurve, or null if not set
     */
    public CubicCurve getCurveVisual()
    {
        return curveVisual;
    }

    /**
     * Returns the start node.
     *
     * @return the starting GameNode
     */
    public GameNode getStartNode()
    {
        return startNode;
    }

    /**
     * Returns the end node.
     *
     * @return the ending GameNode
     */
    public GameNode getEndNode()
    {
        return endNode;
    }

    /**
     * Resets the busy flag for the current simulation tick.
     */
    public void resetTickStatus()
    {
        busyThisTick = false;
    }

    /**
     * Returns the line visual.
     *
     * @return the line visual object
     */
    public Object getLineVisual()
    {
        return lineVisual;
    }

    /**
     * Returns the current resource on the pipe.
     *
     * @return the ResourceType, or null if none is set
     */
    public ResourceType getCurrentResource()
    {
        return currentResource;
    }

    /**
     * Removes all active particle animations from this pipe.
     */
    public void removeAllParticles()
    {
        List<Circle> particlesCopy;
        particlesCopy = new ArrayList<>(activeParticles);

        for (Circle particle : particlesCopy)
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
     * Returns a string representation of this Pipe.
     *
     * @return a string in the format "Pipe[startNodeId->endNodeId, R:resourceName]"
     */
    @Override
    public String toString()
    {
        final String resourceStr;

        if (currentResource == null)
        {
            resourceStr = "_";
        } else {
            resourceStr = currentResource.name();
        }
        final String result;
        result = String.format("Pipe[%s->%s, R:%s]",
                               startNode.getId(),
                               endNode.getId(),
                               resourceStr);
        return result;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two Pipe objects are considered equal if their start node IDs and end node IDs are equal.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Pipe))
        {
            return false;
        }

        final Pipe other;
        other = (Pipe) o;

        final boolean equalsStart;
        equalsStart = startNode.getId().equals(other.startNode.getId());

        final boolean equalsEnd;
        equalsEnd = endNode.getId().equals(other.endNode.getId());

        final boolean result;
        result = equalsStart && equalsEnd;

        return result;
    }

    /**
     * Returns a hash code value for the object.
     * The hash code is computed based on the IDs of the start and end nodes.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode()
    {
        final int result;
        result = Objects.hash(startNode.getId(), endNode.getId());

        return result;
    }
}
