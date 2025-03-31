package ca.bcit.comp25222.termproject.CustomGame.managers;

import ca.bcit.comp25222.termproject.CustomGame.util.LevelLoadException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * LevelLoader is a final utility class responsible for loading level configurations from text files.
 * <p>
 * Level files are stored in the resource directory (specifically under "/levels/") and follow a naming pattern
 * of "level{number}.txt", where {number} is a positive integer representing the level number.
 * </p>
 * <p>
 * The class reads the level file line by line, processing each line to extract configuration directives and
 * node definitions for a level. The following configuration directives are recognized:
 * <ul>
 *   <li>
 *     <strong>PROMPT:</strong> Lines starting with the keyword {@value #PROMPT_KEYWORD} indicate the level prompt.
 *     The text following the keyword is used as a prompt to provide instructions or narrative to the player.
 *   </li>
 *   <li>
 *     <strong>TIME_LIMIT:</strong> Lines beginning with {@value #TIME_LIMIT_KEYWORD} define the time limit for the level.
 *     The expected format is "TIME_LIMIT {number}", where {number} is a positive value representing the time limit in seconds.
 *   </li>
 *   <li>
 *     <strong>NODE:</strong> Lines beginning with {@value #NODE_DEFINITION_KEYWORD} define a game node.
 *     A valid node definition must contain at least five parts (NODE, type, ID, X-coordinate, and Y-coordinate) and
 *     may include an optional sixth part for additional configuration data (such as a resource recipe, demand, or delay).
 * </ul>
 * </p>
 * <p>
 * During parsing, any lines that are empty or start with the comment prefix {@value #COMMENT_PREFIX} are ignored.
 * The class uses these directives to build a {@link LevelManager} object, which encapsulates the level number,
 * a list of immutable node definitions, the time limit for the level, and the prompt text.
 * </p>
 * <p>
 * If the level file does not exist, LevelLoader logs an error message and returns an empty {@link Optional}.
 * If any I/O or parsing errors occur (for example, due to malformed numeric values or missing required tokens),
 * these are caught and logged, and an empty {@code Optional} is returned.
 * </p>
 * <p>
 * In summary, LevelLoader converts the raw text data of a level file into a structured {@link LevelManager} object,
 * providing the necessary configuration for initializing a level in the game.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

public final class LevelLoader
{

    private static final String LEVEL_RESOURCE_DIRECTORY    = "/levels/";
    private static final String LEVEL_FILENAME_PREFIX       = "level";
    private static final String LEVEL_FILENAME_SUFFIX       = ".txt";
    private static final String COMMENT_PREFIX              = "#";
    private static final String NODE_DEFINITION_KEYWORD     = "NODE";
    private static final String TIME_LIMIT_KEYWORD          = "TIME_LIMIT";
    private static final String PROMPT_KEYWORD              = "PROMPT";

    private static final int NODE_PARTS_MIN             = 5;
    private static final int NODE_PARTS_MAX             = 6;
    private static final int NODE_PART_INDEX_KEYWORD    = 0;
    private static final int NODE_PART_INDEX_TYPE       = 1;
    private static final int NODE_PART_INDEX_ID         = 2;
    private static final int NODE_PART_INDEX_X          = 3;
    private static final int NODE_PART_INDEX_Y          = 4;
    private static final int NODE_PART_INDEX_CONFIG     = 5;

    /*
     * Parses the level data from a stream of lines.
     *
     * @param levelNumber the level number being parsed.
     * @param lines       a stream of lines from the level file.
     * @return an Optional containing the parsed LevelManager.
     * @throws LevelLoadException if fatal parsing errors occur.
     */
    private static Optional<LevelManager> parseLevelData(final int levelNumber,
                                                         final Stream<String> lines)
                                                         throws LevelLoadException
    {
        final List<LevelManager.NodeDefinition> definitions;
        final double[] timeLimit;
        final String[] prompt;

        definitions = new ArrayList<>();
        timeLimit = new double[]{ LevelManager.DEFAULT_TIME_LIMIT_SECONDS };
        prompt = new String[]{ "" };

        lines.map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith(COMMENT_PREFIX))
                .forEach(line -> {
                    try
                    {
                        final String upperLine;
                        upperLine = line.toUpperCase();

                        if (upperLine.startsWith(PROMPT_KEYWORD))
                        {
                            prompt[0] = line.substring(PROMPT_KEYWORD.length()).trim();
                        } else if (upperLine.startsWith(TIME_LIMIT_KEYWORD)) {

                            final String[] parts;
                            parts = line.split("\\s+");

                            if (parts.length == 2)
                            {
                                try {
                                    final double parsedTime;
                                    parsedTime = Double.parseDouble(parts[1]);

                                    if (parsedTime > 0)
                                    {
                                        timeLimit[0] = parsedTime;
                                    } else {
                                        System.err.println("Warning: Invalid non-positive TIME_LIMIT ignored: " + line);
                                    }
                                } catch (final NumberFormatException e) {
                                    System.err.println("Warning: Invalid number format for TIME_LIMIT, ignoring line: " + line);
                                }
                            } else {
                                System.err.println("Warning: TIME_LIMIT line ignored: " + line);
                            }
                        } else if (upperLine.startsWith(NODE_DEFINITION_KEYWORD))
                        {
                            parseLine(line, definitions, timeLimit);
                        } else {
                            System.err.println("Warning: Skipping unrecognized line: " + line);
                        }
                    } catch (final LevelLoadException e) {
                        System.err.println(e.getMessage());
                    }
                });

        if (definitions.isEmpty())
        {
            System.err.println("Warning: Level " + levelNumber + " contains no valid NODE definitions.");
            return Optional.empty();
        }

        final LevelManager levelManager;
        levelManager = new LevelManager(levelNumber, definitions, timeLimit[0], prompt[0]);

        return Optional.of(levelManager);
    }

    /*
     * Parses a single non-empty, non-comment line from the level file.
     *
     * @param line        the line to parse.
     * @param definitions the list to add node definitions to.
     * @param timeLimit   an array holding the time limit (updated if found).
     * @throws LevelLoadException if the line has a fatal syntax error.
     */
    private static void parseLine(final String line,
            final List<LevelManager.NodeDefinition> definitions,
            final double[] timeLimit) throws LevelLoadException
    {
        final String[] parts;
        parts = line.split("\\s+", NODE_PARTS_MAX);

        if (parts.length < 1)
        {
            return;
        }

        final String keyword;
        keyword = parts[NODE_PART_INDEX_KEYWORD].toUpperCase();

        switch (keyword)
        {
            case TIME_LIMIT_KEYWORD:
                if (parts.length == 2)
                {
                    try {
                        final double parsedTime;
                        parsedTime = Double.parseDouble(parts[1]);

                        if (parsedTime > 0)
                        {
                            timeLimit[0] = parsedTime;
                        } else {
                            System.err.println("Warning: Invalid non-positive TIME_LIMIT ignored: " + line);
                        }
                    } catch (final NumberFormatException e) {
                        System.err.println("Warning: Invalid number format for TIME_LIMIT, ignoring line: " + line);
                    }
                } else {
                    System.err.println("Warning: Malformed TIME_LIMIT line ignored: " + line);
                }
                break;

            case NODE_DEFINITION_KEYWORD:
                if (parts.length >= NODE_PARTS_MIN)
                {
                    try {
                        final String type;
                        type = parts[NODE_PART_INDEX_TYPE];
                        final String id;
                        id = parts[NODE_PART_INDEX_ID];
                        final double x;
                        x = Double.parseDouble(parts[NODE_PART_INDEX_X]);
                        final double y;
                        y = Double.parseDouble(parts[NODE_PART_INDEX_Y]);
                        final String config;
                        if (parts.length > NODE_PART_INDEX_CONFIG)
                        {
                            config = parts[NODE_PART_INDEX_CONFIG];
                        } else {
                            config = null;
                        }
                        if (id == null || id.trim().isEmpty())
                        {
                            throw new LevelLoadException("Node ID cannot be empty in line: " + line);
                        }

                        definitions.add(new LevelManager.NodeDefinition(type, id, x, y, config));

                    } catch (final NumberFormatException e) {
                        throw new LevelLoadException("Invalid number format for X or Y " +
                                                             "coordinate in line: " + line, e);

                    } catch (final ArrayIndexOutOfBoundsException e) {
                        throw new LevelLoadException("Missing required parts (TYPE ID X Y) " +
                                                             "for NODE in line: " + line, e);
                    }
                } else {
                    throw new LevelLoadException("Malformed NODE line (missing parts): " + line);
                }
                break;

            default:
                System.err.println("Warning: Skipping unrecognized line keyword '" +
                                           keyword + "' in line: " + line);
                break;
        }
    }

    /**
     * Loads a level by its number from the resource directory.
     *
     * @param levelNumber the positive integer level number.
     * @return an Optional containing the LevelManager if found and parsed successfully,
     *         otherwise Optional.empty().
     */
    public static Optional<LevelManager> loadLevel(final int levelNumber)
    {
        if (levelNumber <= 0)
        {
            System.err.println("Invalid level number requested: " + levelNumber);
            return Optional.empty();
        }

        final String fileName;
        final String resourcePath;

        fileName = LEVEL_FILENAME_PREFIX + levelNumber + LEVEL_FILENAME_SUFFIX;
        resourcePath = LEVEL_RESOURCE_DIRECTORY + fileName;

        final InputStream is;
        is = LevelLoader.class.getResourceAsStream(resourcePath);

        if (is == null)
        {
            System.err.println("Level resource not found: " + resourcePath);
            return Optional.empty();
        }
        try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             final BufferedReader reader = new BufferedReader(isr))
        {
            final Optional<LevelManager> result;
            result = parseLevelData(levelNumber, reader.lines());
            return result;
        } catch (final IOException e) {
            System.err.println("Error reading level file " + resourcePath + ": " + e.getMessage());
            return Optional.empty();

        } catch (final LevelLoadException e) {
            System.err.println("Error parsing level " + resourcePath + ": " + e.getMessage());
            return Optional.empty();

        } catch (final Exception e) {
            System.err.println("Unexpected error loading level " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
