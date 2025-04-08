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
 * The LevelLoader class is a final utility that reads and interprets level
 * configuration files from the resource directory, ultimately producing a
 * LevelManager for each loaded level.
 *
 * <p>
 * Level files must follow the naming pattern "level{number}.txt" (e.g.,
 * "level3.txt"), and are stored under "/levels/". Each line in these files can
 * define one of the following directives:
 * </p>
 * <ul>
 *   <li><strong>PROMPT:</strong> Declares the text used to narrate or provide
 *   instructions for the level.</li>
 *   <li><strong>TIME_LIMIT:</strong> Specifies the maximum time (in seconds)
 *   allowed for the level (e.g., "TIME_LIMIT 120"). Must be a positive value.</li>
 *   <li><strong>NODE:</strong> Describes a game node's type, ID, position
 *   coordinates (X, Y), and optionally a configuration token.</li>
 * </ul>
 *
 * <p>
 * During file parsing, any empty lines or lines beginning with
 * {@code COMMENT_PREFIX} ("#") are ignored. For recognized directives, the class
 * extracts relevant data such as node attributes or the time limit. If a line
 * is malformed (e.g., missing tokens or invalid numeric values), a
 * {@link ca.bcit.comp2522.termproject.resourcerouter.util.LevelLoadException}
 * is thrown to indicate the error. If no valid node definitions are found after
 * processing, the loader returns null to signal an unsuccessful load.
 * </p>
 *
 * <p>
 * In typical usage, {@code loadLevel(int levelNumber)} will:
 * <ol>
 *   <li>Construct the filename using "level{levelNumber}.txt" and locate it
 *       under the "/levels/" directory.</li>
 *   <li>Open the file using UTF-8 encoding, read each line into a stream,
 *       and ignore any lines that are empty or start with "#".</li>
 *   <li>Process each recognized directive by delegating to internal parsing
 *       methods (e.g., {@code parseLine}) to build a list of node definitions
 *       and update time limits or prompts.</li>
 *   <li>Construct a new {@code LevelManager} with the resulting definitions
 *       and settings, or return null if none are valid.</li>
 * </ol>
 * </p>
 *
 * <p>
 * This approach ensures that all level data is parsed consistently, with any
 * critical errors represented as exceptions, while non-critical lines (such as
 * comments) are ignored. This design keeps the loading mechanism robust,
 * structured, and easy to maintain or extend for additional directives.
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
     * This method processes the level file provided as a stream of lines.
     *
     * The steps are:
     * 1. Map each line to its trimmed version.
     * 2. Filter out lines that are either empty or begin with the COMMENT_PREFIX.
     * 3. For each remaining line:
     *    a. Convert the line to uppercase to perform case-insensitive comparisons.
     *    b. If the line starts with PROMPT_KEYWORD, extract the prompt text (everything after the keyword)
     *       and store it in a one-element array.
     *    c. If the line starts with TIME_LIMIT_KEYWORD, split the line into tokens by whitespace.
     *       If there are exactly 2 tokens, attempt to parse the second token into a double.
     *       If the parsed value is greater than zero, update the time limit in a one-element array.
     *       Otherwise, throw a LevelLoadException indicating that a non-positive time limit is invalid.
     *    d. If the line starts with NODE_DEFINITION_KEYWORD, pass the line along with the list of definitions
     *       and the timeLimit array to the parseLine method.
     *    e. If the line does not match any expected directive,
     *       throw a LevelLoadException to signal an unrecognized directive.
     * 4. After processing all lines, if the list of definitions is empty,
     *    throw a LevelLoadException because valid nodes are required.
     * 5. Otherwise, construct and return a new LevelManager using the levelNumber,
     *    collected definitions, final time limit, and prompt.
     *
     * Throws LevelLoadException if a critical parsing error occurs.
     */
    private static LevelManager parseLevelData(final int levelNumber,
                                               final Stream<String> lines)
                                               throws LevelLoadException
    {
        final List<LevelManager.NodeDefinition> definitions;
        final double[]                          timeLimit;
        final String[]                          prompt;

        definitions     = new ArrayList<>();
        timeLimit       = new double[] {LevelManager.getDefaultTime()};
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
                                        throw new LevelLoadException(
                                                "Invalid non-positive: " + line);
                                    }
                                }
                                catch (final NumberFormatException e)
                                {
                                    throw new LevelLoadException(
                                            "Invalid number format for TIME_LIMIT" + line + e);
                                }
                            }
                        }
                        else if (upperLine.startsWith(NODE_DEFINITION_KEYWORD))
                        {
                            parseLine(line, definitions, timeLimit);
                        }
                    }
                    catch (final LevelLoadException e)
                    {
                        e.printStackTrace();
                    }
                });

        if (definitions.isEmpty())
        {
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
     * This method takes a single line of text (assumed to be non-empty and not a comment) and attempts to parse it
     * according to the following rules:
     * 1. Split the line using whitespace as the delimiter, with a maximum of NODE_PARTS_MAX tokens.
     * 2. Check if the token count is sufficient; if not, throw a LevelLoadException.
     * 3. Examine the first token (converted to uppercase) to determine the directive:
     *    a. If the keyword is TIME_LIMIT_KEYWORD:
     *         i. Ensure exactly two tokens exist.
     *        ii. Parse the second token as a double.
     *       iii. If the parsed double is positive, update the timeLimit array with this value.
     *        iv. If not, throw a LevelLoadException.
     *    b. If the keyword is NODE_DEFINITION_KEYWORD:
     *         i. Verify that there are at least NODE_PARTS_MIN tokens.
     *        ii. Extract the node type (token at index NODE_PART_INDEX_TYPE), node ID (token at index NODE_PART_INDEX_ID),
     *            and parse the X and Y coordinates (tokens at indices NODE_PART_INDEX_X and NODE_PART_INDEX_Y) as doubles.
     *       iii. Optionally, extract a configuration token (if present).
     *        iv. Validate that the node ID is non-empty.
     *         v. Create a new NodeDefinition instance with these values and add it to the definitions list.
     *    c. If the keyword is not recognized, throw a LevelLoadException indicating the unrecognized directive.
     *
     * Any NumberFormatException or ArrayIndexOutOfBoundsException
     * encountered during parsing is caught and rethrown as a LevelLoadException.
     */
    private static void parseLine(final String line,
                                  final List<LevelManager.NodeDefinition> definitions,
                                  final double[] timeLimit)
                                  throws LevelLoadException
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
                            throw new LevelLoadException(
                                    "Invalid non-positive TIME_LIMIT" + line);
                        }
                    }
                    catch (final NumberFormatException e)
                    {
                        throw new LevelLoadException(
                                "Invalid number format for TIME_LIMIT" + line);
                    }
                }
                break;

            case NODE_DEFINITION_KEYWORD:

                if (parts.length >= NODE_PARTS_MIN)
                {
                    try
                    {
                        final String type;
                        final String nodeId;
                        final double xCord;
                        final double yCord;
                        final String config;

                        type    = parts[NODE_PART_INDEX_TYPE];
                        nodeId  = parts[NODE_PART_INDEX_ID];
                        xCord   = Double.parseDouble(parts[NODE_PART_INDEX_X]);
                        yCord   = Double.parseDouble(parts[NODE_PART_INDEX_Y]);

                        if (parts.length > NODE_PART_INDEX_CONFIG)
                        {
                            config = parts[NODE_PART_INDEX_CONFIG];
                        }
                        else
                        {
                            config = null;
                        }
                        if (nodeId == null || nodeId.trim().isEmpty())
                        {
                            throw new LevelLoadException("Node ID cannot be empty in line: " + line);
                        }

                        definitions.add(new LevelManager.NodeDefinition(type,
                                                                        nodeId,
                                                                        xCord,
                                                                        yCord,
                                                                        config));

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
                throw new LevelLoadException("Unrecognized line keyword '" +
                                             keyword + "' in line: " + line);
        }
    }

    /**
     * Loads a level configuration from a text file and returns a LevelManager representing the level's settings.
     * <p>
     * The method builds the filename using the pattern "level{levelNumber}.txt" by concatenating the
     * level filename prefix, the provided levelNumber, and the filename suffix. It then constructs the resource path
     * by prepending the resource directory ("/levels/") to the filename.
     * </p>
     * <p>
     * The method attempts to locate the file by calling getResourceAsStream() on the LevelLoader class.
     * If the file is not found, or if the levelNumber is invalid (i.e. less than or equal to zero),
     * the method returns null.
     * </p>
     * <p>
     * If the file is found, the method opens it using an InputStreamReader with UTF-8 encoding,
     * wraps the stream in a BufferedReader, and obtains a Stream<String> of its lines.
     * This stream is then passed to the private {@code parseLevelData(int, Stream)} method, which processes the
     * lines to extract configuration directives (such as prompt, time limit, and node definitions) and
     * constructs a LevelManager object.
     * </p>
     * <p>
     * If any IOException or LevelLoadException is encountered during file reading or parsing,
     * the exception is caught and the method returns null, indicating that the level configuration could not be loaded.
     * </p>
     *
     * @param levelNumber a positive integer representing the level number to load
     * @return a LevelManager instance representing the loaded level configuration, or null if the file is missing,
     *         levelNumber is invalid, or an error occurs during reading or parsing
     */
    public static LevelManager loadLevel(final int levelNumber)

    {
        if (levelNumber <= ZERO_INDEX)
        {
            return null;
        }

        final String fileName;
        final String resourcePath;

        fileName        = LEVEL_FILENAME_PREFIX + levelNumber + LEVEL_FILENAME_SUFFIX;
        resourcePath    = LEVEL_RESOURCE_DIRECTORY + fileName;

        final InputStream inputStream;
        inputStream = LevelLoader.class.getResourceAsStream(resourcePath);

        if (inputStream == null)
        {
            return null;
        }

        final InputStreamReader inputStreamReader;
        final BufferedReader reader;

        inputStreamReader = new InputStreamReader(inputStream,
                                                  StandardCharsets.UTF_8);
        reader            = new BufferedReader(inputStreamReader);

        try (inputStreamReader;
             reader)
        {
            final LevelManager levelManager;
            levelManager = parseLevelData(levelNumber,
                                          reader.lines());
            return levelManager;
        }
        catch (final LevelLoadException | IOException e)
        {
            return null;
        }
    }
}
