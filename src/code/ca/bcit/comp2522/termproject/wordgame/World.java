package ca.bcit.comp2522.termproject.wordgame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * The World class is responsible for constructing a complete, immutable map of countries by reading
 * country data from text files located in a specified resource directory.
 * <p>
 * Each text file contains information about one or more countries, including the country's name,
 * its capital city, and up to three related facts. The class processes these files to create {@code Country}
 * objects and stores them in a map with the formatted country names as keys.
 * </p>
 * <p>
 * The class includes a helper method to reformat country names that contain commas (e.g., converting
 * "Last, First" into "First Last") and a public static method {@link #buildCountries()} that returns the final
 * immutable map of country data.
 * </p>
 * <p>
 * If the resource directory is empty or no valid country files are found, the {@code buildCountries()} method
 * returns an empty map.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

final class World
{
    private static final int        TEXT_SPLIT_INDEX_START     = 0;
    private static final int        TEXT_SPLIT_INDEX_END       = 1;
    private static final int        MAX_WORD_SPLIT             = 2;
    private static final int        TOTAL_FACTS                = 3;
    private static final String     filepath                   = "src/resources/";

    /*
     * Reformats a raw country name.
     *
     * If the provided countryName contains a comma (as in the format "Last, First"),
     * this method splits the string into two parts using the constant MAX_WORD_SPLIT as the limit,
     * trims each part, and then returns a new string in the format "First Last"
     * (i.e. the second part followed by a space
     * and then the first part). If the countryName does not contain a comma, it is returned unchanged.
     * </p>
     *
     * @param countryName the raw country name as read from a file.
     * @return a reformatted country name in "First Last" order if a comma is present;
     * otherwise, the original countryName.
     */
    private static String formatCountryName(final String countryName)
    {
        if (countryName.contains(","))
        {
            final String[] parts;
            final String format;

            parts   = countryName.split(",", MAX_WORD_SPLIT);
            format  = parts[TEXT_SPLIT_INDEX_END].trim() +
                    " " + parts[TEXT_SPLIT_INDEX_START].trim();

            return format;
        }
        return countryName;
    }

    /**
     * Builds an immutable map of country names to {@code Country} objects by reading country data from text files.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Creates a {@code File} object for the directory specified by the constant {@code filepath}.</li>
     *   <li>Filters the files in that directory to include only those whose names
     *       match the regular expression "^[a-zA-Z]\\.txt$".</li>
     *   <li>If no valid files are found or the directory is empty, an empty map is returned.</li>
     *   <li>For each valid file:
     *     <ol>
     *       <li>A {@link Scanner} is opened for the file using a try-with-resources statement.</li>
     *       <li>The scanner reads lines until a non-empty line containing a colon (":") is found.
     *       This line is assumed to include both the country name and its capital city, separated by a colon.</li>
     *       <li>The line is split into two parts using {@code MAX_WORD_SPLIT} as the limit:
     *           <ul>
     *             <li>The first part (before the colon) is the raw country name,
     *                 which is reformatted using {@link #formatCountryName(String)}.</li>
     *             <li>The second part (after the colon) is the capital city name.</li>
     *           </ul>
     *       </li>
     *       <li>Up to {@code TOTAL_FACTS} subsequent lines are read (if available)
     *           and stored as facts about the country.</li>
     *       <li>A new {@code Country} object is created with the formatted country name and the capital city,
     *           and its facts are set.</li>
     *       <li>The {@code Country} object is then added to a map with the formatted country name as the key.</li>
     *     </ol>
     *   </li>
     *   <li>After processing all files, the map is wrapped in an unmodifiable map and returned.</li>
     * </ol>
     * </p>
     *
     * @return an immutable {@code Map} where each key is a formatted country name and
     *         each value is the corresponding {@code Country} object.
     * @throws FileNotFoundException if the resource directory or any required file is not found.
     */
    static Map<String, Country> buildCountries()
           throws FileNotFoundException
    {

        final File                  folder;
        final File[]                files;
        final Map<String, Country>  countryMap;

        folder      = new File(World.filepath);
        files       = folder.listFiles((_, name) -> name.matches("^[a-zA-Z]\\.txt$"));
        countryMap  = new HashMap<>();

        if (files == null || files.length == TEXT_SPLIT_INDEX_START)
        {
            return Collections.emptyMap();
        }

        for (final File file : files)
        {
            final Scanner scan;
            scan = new Scanner(file);

            try (scan)
            {
                while (scan.hasNextLine())
                {
                    String countryCapitalLine = "";
                    while (scan.hasNextLine())
                    {
                        countryCapitalLine = scan.nextLine().trim();
                        if (!countryCapitalLine.isEmpty())
                        {
                            break;
                        }
                    }

                    if (countryCapitalLine.isEmpty())
                    {
                        break;
                    }

                    if (!countryCapitalLine.contains(":"))
                    {
                        continue;
                    }

                    final String[]      countryCapitalSplit;
                    final String        countryName;
                    final String        capital;
                    final List<String>  facts;

                    countryCapitalSplit = countryCapitalLine.split(":", MAX_WORD_SPLIT);
                    countryName         = formatCountryName(countryCapitalSplit[TEXT_SPLIT_INDEX_START].trim());
                    capital             = countryCapitalSplit[TEXT_SPLIT_INDEX_END].trim();
                    facts               = new ArrayList<>();

                    for (int i = 0; i < TOTAL_FACTS && scan.hasNextLine(); i++)
                    {
                        final String factLine;
                        factLine = scan.nextLine().trim();

                        facts.add(factLine);
                    }

                    final Country country;
                    country = new Country(countryName, capital);

                    country.setFacts(facts.toArray(new String[TEXT_SPLIT_INDEX_START]));

                    countryMap.put(countryName, country);
                }
            }
        }

        final Map<String, Country> immutableCountryMap;
        immutableCountryMap = Collections.unmodifiableMap(countryMap);

        return immutableCountryMap;
    }
}
