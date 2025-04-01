package ca.bcit.comp25222.termproject.resourcerouter.util;

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
 *     {@link #fromString(String)} attempts to parse a {@code ResourceType} from a given string. It performs a case-insensitive
 *     match against the enum constants. If the provided string is null, empty, or does not correspond to any defined
 *     resource, the method returns {@code null} and logs a warning message.
 *   </li>
 * </ul>
 * </p>
 *
 * @see javafx.scene.paint.Color
 * @see javafx.scene.text.Text
 * @author Braeden Duval
 * @version 1.0
 */

public enum ResourceType
{
    RED("resource-red", Color.RED),
    BLUE("resource-blue", Color.BLUE),
    YELLOW("resource-yellow", Color.YELLOW),
    ORANGE("resource-orange", Color.ORANGE),
    GREEN("resource-green", Color.GREEN),
    PURPLE("resource-purple", Color.PURPLE),
    VIOLET("resource-violet", Color.MEDIUMORCHID),
    TURQUOISE("resource-turquoise", Color.TURQUOISE),
    PINK("resource-pink", Color.PINK),
    BROWN("resource-brown", Color.BROWN),
    GRAY("resource-gray", Color.GRAY),
    BLACK("resource-black", Color.BLACK),
    WHITE("resource-white", Color.WHITE),
    CRIMSON("resource-crimson", Color.CRIMSON),
    SKYBLUE("resource-skyblue", Color.SKYBLUE),
    LIME("resource-lime", Color.LIME),
    AQUA("resource-aqua", Color.AQUA),
    MAROON("resource-maroon", Color.MAROON),
    OLIVE("resource-olive", Color.OLIVE),
    TEAL("resource-teal", Color.TEAL),
    NAVY("resource-navy", Color.NAVY),
    GOLDENROD("resource-goldenrod", Color.GOLDENROD),
    SIENNA("resource-sienna", Color.SIENNA),
    INDIGO("resource-indigo", Color.INDIGO),
    PLUM("resource-plum", Color.PLUM);

    private final String styleClass;
    private final Color displayColor;

    /**
     * Constructs a ResourceType with the specified style class and display color.
     *
     * @param styleClass   the CSS style class name used to style UI elements that represent this resource type;
     *                     should be a non-null, non-empty string.
     * @param displayColor the JavaFX Color that visually represents this resource type in the game UI.
     */
    ResourceType(String styleClass,
                 Color displayColor)
    {
        this.styleClass = styleClass;
        this.displayColor = displayColor;
    }


    /**
     * Retrieves the display color associated with this resource type.
     *
     * @return the JavaFX Color used for displaying this resource.
     */
    public Color getDisplayColor()
    {
        return displayColor;
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
    public static ResourceType fromString(String text)
    {
        if (text == null || text.trim().isEmpty())
            return null;
        try
        {
            return ResourceType.valueOf(text.trim().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Warning: Unknown ResourceType string: " + text);
            return null;
        }
    }
}
