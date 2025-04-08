package ca.bcit.comp2522.termproject.resourcerouter.gameplay;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ca.bcit.comp2522.termproject.resourcerouter.managers.GameController;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

// DummyGameNode is a minimal concrete subclass of GameNode for testing
final class DummyGameNode extends GameNode
{

    DummyGameNode(final String id,
                  final double x,
                  final double y)
    {
        super(id, x, y);
    }

    @Override
    Node createNodeBodyVisual()
    {
        Rectangle rect;
        rect = new Rectangle(50, 50);
        rect.setId("dummy-body-" + getNodeId());
        return rect;
    }

    @Override
    Shape createOutputConnectorVisual()
    {
        Rectangle rect;
        rect = new Rectangle(10, 10);
        return rect;
    }

    @Override
    Shape createInputConnectorVisual()
    {
        Rectangle rect;
        rect = new Rectangle(10, 10);
        return rect;
    }

    @Override
    Label createInfoLabelVisual()
    {
        final Label label;
        label = new Label("Info:" + getNodeId());
        return label;
    }

    @Override
    public Point2D getInputConnectorOffset()
    {
        final Point2D inputOffset;
        inputOffset = new Point2D(-5, 0);

        return inputOffset;
    }

    @Override
    public Point2D getOutputConnectorOffset()
    {
        final Point2D outputOffset;
        outputOffset = new Point2D(5, 0);

        return outputOffset;
    }

    @Override
    public void resetState()
    {
    }

    @Override
    public void update(final double timeSeconds,
                       final GameController controller)
    {
    }
}

final class GameNodeTest
{

    // ============================== Positive Tests ==============================

    @Test
    @DisplayName("Positive Test: Construct Valid DummyGameNodes")
    void testValidGameNodeCreation()
    {
        final DummyGameNode node1;
        final DummyGameNode node2;
        final DummyGameNode node3;

        node1 = new DummyGameNode("node1", 100, 100);
        node2 = new DummyGameNode("node2", 200, 200);
        node3 = new DummyGameNode("node3", 300, 300);

        assertNotNull(node1);
        assertEquals("node1", node1.getNodeId());
        assertEquals(100, node1.getXCoordinate());
        assertEquals(100, node1.getYCoordinate());

        assertNotNull(node2);
        assertEquals("node2", node2.getNodeId());
        assertEquals(200, node2.getXCoordinate());
        assertEquals(200, node2.getYCoordinate());

        assertNotNull(node3);
        assertEquals("node3", node3.getNodeId());
        assertEquals(300, node3.getXCoordinate());
        assertEquals(300, node3.getYCoordinate());
    }

    @Test
    @DisplayName("Positive Test: GameNode Equality and HashCode")
    void testGameNodeEqualityAndHashCode()
    {
        final DummyGameNode nodeA1;
        final DummyGameNode nodeA2;
        final DummyGameNode nodeB;

        nodeA1= new DummyGameNode("A", 50, 50);
        nodeA2= new DummyGameNode("A", 100, 100); // Same id as nodeA1
        nodeB = new DummyGameNode("B", 50, 50);

        assertEquals(nodeA1, nodeA2);
        assertEquals(nodeA1.hashCode(), nodeA2.hashCode());
        assertNotEquals(nodeA1, nodeB);

        Set<GameNode> nodeSet = new HashSet<>(Arrays.asList(nodeA1, nodeA2, nodeB));
        assertEquals(2, nodeSet.size(), "Set should contain two unique nodes based on id.");
    }

    // ============================== Negative Tests ==============================

    @Test
    @DisplayName("Negative Test: Null Node ID")
    void testNullNodeId()
    {
        final Exception exception;
        final String expectedMsg;

        exception = assertThrows(IllegalArgumentException.class,
                                           () -> new DummyGameNode(null, 100, 100));
        expectedMsg = "Node ID empty.";

        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    @DisplayName("Negative Test: Empty Node ID")
    void testEmptyNodeId()
    {
        final Exception exception;
        final String expectedMsg;

        exception = assertThrows(IllegalArgumentException.class,
                                           () -> new DummyGameNode("  ", 100, 100));
        expectedMsg = "Node ID empty.";

        assertTrue(exception.getMessage().contains(expectedMsg));
    }

    @Test
    @DisplayName("Negative Test: Negative Coordinates")
    void testNegativeCoordinates()
    {
        final Exception exceptionX;
        final Exception exceptionY;

        exceptionX = assertThrows(IllegalArgumentException.class,
                                           () -> new DummyGameNode("nodeNegX", -10, 100));

        exceptionY= assertThrows(IllegalArgumentException.class,
                                            () -> new DummyGameNode("nodeNegY", 100, -10));

        assertTrue(exceptionX.getMessage().contains("xCoordinate cannot be negative"),
                   "Error message should indicate negative xCoordinate.");

        assertTrue(exceptionY.getMessage().contains("yCoordinate cannot be negative"),
                   "Error message should indicate negative yCoordinate.");
    }
}
