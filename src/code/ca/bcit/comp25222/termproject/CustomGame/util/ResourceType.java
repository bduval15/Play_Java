package ca.bcit.comp25222.termproject.CustomGame.util;

import javafx.scene.paint.Color;

public enum ResourceType {
    // Primary colors
    RED("resource-red", Color.RED),
    BLUE("resource-blue", Color.BLUE),
    YELLOW("resource-yellow", Color.YELLOW),

    // Secondary colors (blends of primary colors)
    ORANGE("resource-orange", Color.ORANGE),         // Red + Yellow
    GREEN("resource-green", Color.GREEN),             // Blue + Yellow
    PURPLE("resource-purple", Color.PURPLE),          // Red + Blue

    // Additional blended hues
    VERMILLION("resource-vermillion", Color.rgb(227, 66, 52)),
    CHARTREUSE("resource-chartreuse", Color.rgb(127, 255, 0)),
    VIOLET("resource-violet", Color.MEDIUMORCHID),
    TURQUOISE("resource-turquoise", Color.TURQUOISE),

    // Extra colors for more mixing possibilities
    PINK("resource-pink", Color.PINK),
    BROWN("resource-brown", Color.BROWN),

    // Neutrals – to adjust brightness or tone
    GRAY("resource-gray", Color.GRAY),
    BLACK("resource-black", Color.BLACK),
    WHITE("resource-white", Color.WHITE),

    // Additional vivid shades
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

    ResourceType(String styleClass, Color displayColor) {
        this.styleClass = styleClass;
        this.displayColor = displayColor;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public Color getDisplayColor() {
        return displayColor;
    }

    public static ResourceType fromString(String text) {
        if (text == null || text.trim().isEmpty())
            return null;
        try {
            return ResourceType.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Unknown ResourceType string: " + text);
            return null;
        }
    }
}
