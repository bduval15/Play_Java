package ca.bcit.comp2522.termproject.resourcerouter.util;

import javafx.scene.paint.Color;

/**
 * The {@code ResourceType} enum defines the various resource types available in the game.
 * <p>
 * Each resource type is characterized by a CSS style class string and a corresponding JavaFX
 * {@link javafx.scene.paint.Color} used for display. These resources can be basic colors such as RED, BLUE, and YELLOW,
 * or composite/mixed colors such as ORANGE, GREEN, and PURPLE, as well as additional colors like VERMILION, CHARTREUSE,
 * and others.
 * </p>
 * <p>
 * The style class string is intended for use in CSS-based styling of game elements, while the display color
 * is used in the JavaFX UI to visually represent the resource.
 * </p>
 * <p>
 * The enum also provides utility methods:
 * <ul>
 *   <li>
 *     {@link #getDisplayColor()} returns the {@link javafx.scene.paint.Color} associated with the resource.
 *   </li>
 *   <li>
 *     {@link #getResourceType(String)} attempts to parse a {@code ResourceType} from a given string. It performs a case-insensitive
 *     match against the enum constants. If the provided string is null, empty, or does not correspond to any defined
 *     resource, the method returns {@code null} and logs a warning message.
 *   </li>
 * </ul>
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 * @see javafx.scene.paint.Color
 * @see javafx.scene.text.Text
 */

public enum ResourceType
{
    RED(Color.RED),
    BLUE(Color.BLUE),
    YELLOW(Color.YELLOW),
    ORANGE(Color.ORANGE),
    GREEN(Color.GREEN),
    PURPLE(Color.PURPLE),
    VIOLET(Color.MEDIUMORCHID),
    TURQUOISE(Color.TURQUOISE),
    PINK(Color.PINK),
    BROWN(Color.BROWN),
    GRAY(Color.GRAY),
    BLACK(Color.BLACK),
    WHITE(Color.WHITE),
    DARKRED(Color.DARKRED),
    SKYBLUE(Color.SKYBLUE),
    LIME(Color.LIME),
    AQUA(Color.AQUA),
    MAROON(Color.MAROON),
    OLIVE(Color.OLIVE),
    TEAL(Color.TEAL),
    NAVY(Color.NAVY),
    GOLD(Color.GOLD),
    SIENNA(Color.SIENNA),
    INDIGO(Color.INDIGO),
    PLUM(Color.PLUM),
    MAGENTA(Color.MAGENTA),
    CRIMSON(Color.CRIMSON),
    LAVENDER(Color.LAVENDER);

    private final Color displayColor;

    /**
     * Constructs a ResourceType with the specified display color.
     * <p>
     * Each enum constant is created by passing a {@link javafx.scene.paint.Color} that
     * represents the visual appearance of the resource. This color is stored and later
     * returned by the {@link #getDisplayColor()} method.
     * </p>
     *
     * @param displayColor the JavaFX Color that represents this resource; must not be null.
     * @throws IllegalArgumentException if {@code displayColor} is null.
     */
    ResourceType(final Color displayColor)
    {
        validateDisplayColor(displayColor);

        this.displayColor = displayColor;
    }

    /*
     * Validates that the given display color is not null.
     *
     * If the passed color is null, an IllegalArgumentException is thrown with a descriptive message.
     *
     *
     * @param displayColor the color to validate.
     * @throws IllegalArgumentException if displayColor is null.
     */
    private static void validateDisplayColor(final Color displayColor)
    {
        if (displayColor == null)
        {
            throw new IllegalArgumentException("displayColor cannot be null");
        }
    }

    /**
     * Converts the specified string to its corresponding ResourceType.
     * <p>
     * This method trims the input string, converts it to upper case, and attempts to match it against the
     * defined enum constants. If the input is null, empty, or does not correspond to any defined resource,
     * the method returns null and prints a warning message to the error stream.
     * </p>
     *
     * @param text the string to convert into a ResourceType.
     * @return the matching ResourceType, or null if the input is invalid or does not match any known type.
     */
    public static ResourceType getResourceType(final String text)
    {
        if (text == null ||
            text.trim().isEmpty())

            return null;
        try
        {
            final ResourceType resourceType;
            resourceType = ResourceType.valueOf(text.trim().toUpperCase());

            return resourceType;
        }
        catch (final IllegalArgumentException e)
        {
            return null;
        }
    }

    /**
     * Returns the JavaFX Color associated with this resource type.
     * <p>
     * This display color is used throughout the game to visually represent the resource,
     * whether in UI components, CSS styling, or graphical elements.
     * </p>
     *
     * @return the display color for this resource.
     */
    public Color getDisplayColor()
    {
        return displayColor;
    }
}
