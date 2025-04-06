package ca.bcit.comp2522.termproject.resourcerouter;

import static org.junit.jupiter.api.Assertions.*;

import ca.bcit.comp2522.termproject.resourcerouter.gameplay.GameNode;
import ca.bcit.comp2522.termproject.resourcerouter.managers.GameController;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link GameNode}.
 * <p>
 * Since GameNode is abstract, a dummy subclass (DummyGameNode) is defined for testing purposes.
 * This class tests positive scenarios such as valid construction, updating IDs, and equality behavior,
 * as well as negative scenarios that ensure exceptions are thrown for invalid inputs, including
 * negative x or y coordinate values.
 * </p>
 *
 * <p>
 * Positive Tests:
 * <ul>
 *   <li>Valid construction with proper ID and coordinates.</li>
 *   <li>Proper functioning of {@link GameNode#setId(String)}.</li>
 *   <li>Equality and hashCode based solely on node ID.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Negative Tests:
 * <ul>
 *   <li>Constructor with a null ID should throw an exception.</li>
 *   <li>Constructor with an empty ID should throw an exception.</li>
 *   <li>{@link GameNode#setId(String)} should throw an exception when given a null or empty value.</li>
 *   <li>Constructor with negative x coordinate should throw an exception.</li>
 *   <li>Constructor with negative y coordinate should throw an exception.</li>
 * </ul>
 * </p>
 *
 * @version 1.0
 */
public class GameNodeTest {

    /**
     * Dummy subclass of {@link GameNode} for testing.
     * <p>
     * Provides trivial implementations of abstract methods.
     * </p>
     */
    private static class DummyGameNode extends GameNode
    {

        /**
         * Constructs a new DummyGameNode.
         *
         * @param id the unique identifier for the node
         * @param x  the x-coordinate of the node's center
         * @param y  the y-coordinate of the node's center
         */
        public DummyGameNode(String id, double x, double y) {
            super(id, x, y);
        }

        @Override
        protected Node createNodeBodyVisual() {
            return new Label("Body");
        }

        @Override
        protected Shape createOutputConnectorVisual() {
            return new Circle(5);
        }

        @Override
        protected Shape createInputConnectorVisual() {
            return new Circle(5);
        }

        @Override
        protected Label createInfoLabelVisual() {
            return new Label("Info");
        }

        @Override
        public Point2D getInputConnectorOffset() {
            return new Point2D(0, 0);
        }

        @Override
        public Point2D getOutputConnectorOffset() {
            return new Point2D(0, 0);
        }

        @Override
        public void resetState() {
        }

        @Override
        public void update(double deltaTime, GameController controller) {
        }
    }

    // ----------------------- Positive Tests -----------------------

    /**
     * Positive Test: Verifies that a DummyGameNode is constructed correctly
     * and that its getters return the expected values.
     */
    @Test
    @DisplayName("Positive Test: Valid Construction and Getters")
    public void testValidConstruction() {
        DummyGameNode node = new DummyGameNode("node1", 100, 200);
        assertEquals("node1", node.getId(), "ID should match the provided value.");
    }

    /**
     * Positive Test: Verifies that calling {@link GameNode#setId(String)} updates the node's ID.
     */
    @Test
    @DisplayName("Positive Test: setId Updates Node ID")
    public void testSetId() {
        DummyGameNode node = new DummyGameNode("node1", 50, 50);
        node.setId("newNodeId");
        assertEquals("newNodeId", node.getId(), "ID should be updated to the new value.");
    }

    /**
     * Positive Test: Verifies that the equals and hashCode methods function as expected,
     * based solely on the node's unique identifier.
     */
    @Test
    @DisplayName("Positive Test: equals and hashCode")
    public void testEqualsAndHashCode() {
        DummyGameNode node1 = new DummyGameNode("node1", 10, 10);
        DummyGameNode node2 = new DummyGameNode("node1", 20, 20); // Different coordinates, same ID.
        DummyGameNode node3 = new DummyGameNode("node3", 10, 10);

        // Nodes with the same ID should be equal.
        assertEquals(node1, node2, "Nodes with the same ID should be equal.");
        // And have the same hash code.
        assertEquals(node1.hashCode(), node2.hashCode(), "Nodes with the same ID should have the same hash code.");

        // Nodes with different IDs should not be equal.
        assertNotEquals(node1, node3, "Nodes with different IDs should not be equal.");
    }

    // ----------------------- Negative Tests -----------------------

    /**
     * Negative Test: Verifies that constructing a DummyGameNode with a null ID
     * throws an IllegalArgumentException.
     */
    @Test
    @DisplayName("Negative Test: Constructor with Null ID")
    public void testConstructorNullId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DummyGameNode(null, 0, 0);
        });
        assertTrue(exception.getMessage().contains("Node ID empty"), "Expected error message for null ID.");
    }

    /**
     * Negative Test: Verifies that constructing a DummyGameNode with an empty ID
     * throws an IllegalArgumentException.
     */
    @Test
    @DisplayName("Negative Test: Constructor with Empty ID")
    public void testConstructorEmptyId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DummyGameNode("", 0, 0);
        });
        assertTrue(exception.getMessage().contains("Node ID empty"), "Expected error message for empty ID.");
    }

    /**
     * Negative Test: Verifies that calling {@link GameNode#setId(String)} with a null or empty value
     * throws an IllegalArgumentException.
     */
    @Test
    @DisplayName("Negative Test: setId with Invalid Value")
    public void testSetIdInvalid() {
        DummyGameNode node = new DummyGameNode("node1", 0, 0);

        Exception exceptionNull = assertThrows(IllegalArgumentException.class, () -> {
            node.setId(null);
        });
        assertTrue(exceptionNull.getMessage().contains("New ID cannot be null or empty"),
                   "Expected error message for null new ID.");

        Exception exceptionEmpty = assertThrows(IllegalArgumentException.class, () -> {
            node.setId("   ");
        });
        assertTrue(exceptionEmpty.getMessage().contains("New ID cannot be null or empty"),
                   "Expected error message for empty new ID.");
    }

    /**
     * Negative Test: Verifies that constructing a DummyGameNode with a negative x coordinate
     * throws an IllegalArgumentException.
     */
    @Test
    @DisplayName("Negative Test: Constructor with Negative X")
    public void testConstructorNegativeX() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DummyGameNode("node1", -1, 10);
        });
        assertTrue(exception.getMessage().contains("x cannot be negative"), "Expected error message for negative x.");
    }

    /**
     * Negative Test: Verifies that constructing a DummyGameNode with a negative y coordinate
     * throws an IllegalArgumentException.
     */
    @Test
    @DisplayName("Negative Test: Constructor with Negative Y")
    public void testConstructorNegativeY() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new DummyGameNode("node1", 10, -5);
        });
        assertTrue(exception.getMessage().contains("y cannot be negative"), "Expected error message for negative y.");
    }
}
