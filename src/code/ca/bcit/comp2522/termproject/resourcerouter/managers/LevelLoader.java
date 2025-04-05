package ca.bcit.comp2522.termproject.resourcerouter.managers;

import ca.bcit.comp2522.termproject.resourcerouter.util.LevelLoadException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
 * If the level file does not exist, LevelLoader logs an error message and returns null.
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
    private static final int ZERO_INDEX                 = 0;
    private static final int SPLIT_VALUE                = 2;
    private static final int FIRST_SPLIT_VALUE          = 1;

    /*
     * Parses the level data from a stream of lines and constructs a LevelManager object.
     *
     * This method reads each trimmed line from the provided lines stream,
     * ignoring empty or commented lines.
     * Recognized lines are processed to extract either:
     *
     *   A PROMPT directive that sets the level prompt text.
     *   A TIME_LIMIT directive that updates the level's time limit.
     *   A NODE definition, which is further parsed to create a LevelManager.NodeDefinition
     *       and added to the definitions list.
     *
     *
     * Any unrecognized lines are logged with a warning and skipped.
     * If no valid NODE definitions are found, this method
     * logs a warning and returns null.
     *
     *
     * @param levelNumber the numeric identifier for the level being parsed
     * @param lines       a stream of String lines from the level file
     *
     * @return a new LevelManager instance representing the parsed level,
     * or null if no valid NODE definitions were found
     *
     * @throws LevelLoadException if a fatal parsing error occurs during line processing
     */
    private static LevelManager parseLevelData(final int levelNumber,
                                               final Stream<String> lines)
                                               throws LevelLoadException
    {
        final List<LevelManager.NodeDefinition> definitions;
        final double[]                          timeLimit;
        final String[]                          prompt;

        definitions     = new ArrayList<>();
        timeLimit       = new double[]{ LevelManager.DEFAULT_TIME_LIMIT_SECONDS };
        prompt          = new String[]{ "" };

        lines.map(String::trim)
                .filter(line -> !line.isEmpty() &&
                !line.startsWith(COMMENT_PREFIX))
                .forEach(line -> {
                    try
                    {
                        final String upperLine;
                        upperLine = line.toUpperCase();

                        if (upperLine.startsWith(PROMPT_KEYWORD))
                        {
                            prompt[ZERO_INDEX] = line.substring(PROMPT_KEYWORD.length()).trim();
                        }
                        else if (upperLine.startsWith(TIME_LIMIT_KEYWORD))
                        {

                            final String[] parts;
                            parts = line.split("\\s+");

                            if (parts.length == SPLIT_VALUE)
                            {
                                try
                                {
                                    final double parsedTime;
                                    parsedTime = Double.parseDouble(parts[FIRST_SPLIT_VALUE]);

                                    if (parsedTime > ZERO_INDEX)
                                    {
                                        timeLimit[ZERO_INDEX] = parsedTime;
                                    }
                                    else
                                    {
                                        System.err.println(
                                        "Warning: Invalid non-positive TIME_LIMIT ignored: " + line);
                                    }
                                }
                                catch (final NumberFormatException e)
                                {
                                    System.err.println(
                                    "Warning: Invalid number format for TIME_LIMIT, ignoring line: " + line);
                                }
                            }
                            else
                            {
                                System.err.println("Warning: TIME_LIMIT line ignored: " + line);
                            }
                        }
                        else if (upperLine.startsWith(NODE_DEFINITION_KEYWORD))
                        {
                            parseLine(line, definitions, timeLimit);
                        }
                        else
                        {
                            System.err.println("Warning: Skipping unrecognized line: " + line);
                        }
                    }
                    catch (final LevelLoadException e)
                    {
                        System.err.println(e.getMessage());
                    }
                });

        if (definitions.isEmpty())
        {
            System.err.println("Warning: Level " + levelNumber + " contains no valid NODE definitions.");
            return null;
        }

        final LevelManager levelManager;
        levelManager = new LevelManager(levelNumber,
                                        definitions,
                                        timeLimit[ZERO_INDEX],
                                        prompt[ZERO_INDEX]);

        return levelManager;
    }

    /*
     * Parses a single non-empty, non-comment line from the level file, handling both TIME_LIMIT and
     * NODE directives.
     *
     * If the line is identified as a TIME_LIMIT directive, it attempts to parse a double value and update
     * the timeLimit array accordingly. If it is identified as a NODE directive, the method extracts
     * the node type, ID, X/Y coordinates, and any optional configuration token, adding a new
     * LevelManager.NodeDefinition to definitions.
     *
     *
     * An ArrayIndexOutOfBoundsException or NumberFormatException may occur if the line is malformed,
     * in which case a LevelLoadException is thrown with a descriptive error message.
     *
     *
     * @param line        the original line to parse
     * @param definitions a mutable list where valid LevelManager.NodeDefinition objects are added
     * @param timeLimit   an array holding the time limit value (index 0) that may be updated by TIME_LIMIT lines
     *
     * @throws LevelLoadException if the line is missing required fields (TYPE, ID, X, Y) for a NODE or has bad numeric values
     */
    private static void parseLine(final String line,
                                  final List<LevelManager.NodeDefinition> definitions,
                                  final double[] timeLimit) throws LevelLoadException
    {
        final String[] parts;
        parts = line.split("\\s+", NODE_PARTS_MAX);

        if (parts.length < NODE_PART_INDEX_TYPE)
        {
            return;
        }

        final String keyword;
        keyword = parts[NODE_PART_INDEX_KEYWORD].toUpperCase();

        switch (keyword)
        {
            case TIME_LIMIT_KEYWORD:
                if (parts.length == SPLIT_VALUE)
                {
                    try
                    {
                        final double parsedTime;
                        parsedTime = Double.parseDouble(parts[FIRST_SPLIT_VALUE]);

                        if (parsedTime > ZERO_INDEX)
                        {
                            timeLimit[ZERO_INDEX] = parsedTime;
                        }
                        else
                        {
                            System.err.println("Warning: Invalid non-positive TIME_LIMIT ignored: " + line);
                        }
                    }
                    catch (final NumberFormatException e)
                    {
                        System.err.println("Warning: Invalid number format for TIME_LIMIT, ignoring line: " + line);
                    }
                }
                else
                {
                    System.err.println("Warning: Malformed TIME_LIMIT line ignored: " + line);
                }
                break;

            case NODE_DEFINITION_KEYWORD:
                if (parts.length >= NODE_PARTS_MIN)
                {
                    try
                    {
                        final String type;
                        final String id;
                        final double x;
                        final double y;
                        final String config;

                        type = parts[NODE_PART_INDEX_TYPE];
                        id = parts[NODE_PART_INDEX_ID];
                        x = Double.parseDouble(parts[NODE_PART_INDEX_X]);
                        y = Double.parseDouble(parts[NODE_PART_INDEX_Y]);

                        if (parts.length > NODE_PART_INDEX_CONFIG)
                        {
                            config = parts[NODE_PART_INDEX_CONFIG];
                        }
                        else
                        {
                            config = null;
                        }
                        if (id == null || id.trim().isEmpty())
                        {
                            throw new LevelLoadException("Node ID cannot be empty in line: " + line);
                        }

                        definitions.add(new LevelManager.NodeDefinition(type, id, x, y, config));

                    }
                    catch (final NumberFormatException e)
                    {
                        throw new LevelLoadException("Invalid number format for X or Y " +
                                                             "coordinate in line: " + line, e);

                    }
                    catch (final ArrayIndexOutOfBoundsException e)
                    {
                        throw new LevelLoadException("Missing required parts (TYPE ID X Y) " +
                                                             "for NODE in line: " + line, e);
                    }
                }
                else
                {
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
     * Loads a level by its number from the resource directory and returns a parsed {@link LevelManager} object.
     * <p>
     * This method locates the level file with the naming pattern {@code level{levelNumber}.txt} inside the
     * {@code /levels/} directory. If the file is found, it is read line by line, and each line is passed to
     * {@link #parseLevelData(int, Stream)} for parsing into a structured level configuration.
     * </p>
     * <p>
     * If the level file does not exist, or if any I/O or parsing errors occur (e.g., invalid numeric
     * format), an error is logged and {@code null} is returned.
     * </p>
     *
     * @param levelNumber a positive integer identifying the desired level
     * @return a {@link LevelManager} representing the level data, or {@code null} if the file is not found
     *         or could not be successfully parsed
     */
    public static LevelManager loadLevel(final int levelNumber)
    {
        if (levelNumber <= ZERO_INDEX)
        {
            System.err.println("Invalid level number requested: " + levelNumber);
            return null;
        }

        final String fileName;
        final String resourcePath;

        fileName        = LEVEL_FILENAME_PREFIX + levelNumber + LEVEL_FILENAME_SUFFIX;
        resourcePath    = LEVEL_RESOURCE_DIRECTORY + fileName;

        final InputStream is;
        is = LevelLoader.class.getResourceAsStream(resourcePath);

        if (is == null)
        {
            System.err.println("Level resource not found: " + resourcePath);
            return null;
        }
        try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             final BufferedReader reader = new BufferedReader(isr))
        {
            final LevelManager levelManager;
            levelManager = parseLevelData(levelNumber, reader.lines());
            return levelManager;
        }
        catch (final IOException e)
        {
            System.err.println("Error reading level file " + resourcePath + ": " + e.getMessage());
            return null;

        }
        catch (final LevelLoadException e)
        {
            System.err.println("Error parsing level " + resourcePath + ": " + e.getMessage());
            return null;

        }
        catch (final Exception e)
        {
            System.err.println("Unexpected error loading level " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
