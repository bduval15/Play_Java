package ca.bcit.comp25222.termproject.WordGame;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * <p>The {@code World} class is responsible for building a map of countries
 * and their respective capital cities by reading data from text files.
 * Each file contains country information, including its capital city
 * and up to three related facts.</p>
 *
 * The country data is stored in an immutable map.
 *
 * @author Braeden Duval
 */
class World
{
    private static final int TEXT_SPLIT_INDEX_START = 0;
    private static final int TEXT_SPLIT_INDEX_END = 1;
    private static final int TOTAL_FACTS = 3;
    private static final String filepath = "src/resources/";

     /**
     * <p>Builds a map of country names mapped to {@code Country} objects by reading
     * country data from text files stored in the specified directory.
     * Each file contains country details, including the capital city
     * and up to three facts.</p>
     *
     * @return An immutable map containing country names as keys
     *         and {@code Country} objects as values.
     * @throws FileNotFoundException If the resource directory or any file is not found.
     */
     static Map<String, Country> buildCountries()
            throws FileNotFoundException
    {

        final File folder;
        folder = new File(World.filepath);

        final File[] files;
        files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        final Map<String, Country> countryMap;
        countryMap = new HashMap<>();

        if(files == null || files.length == TEXT_SPLIT_INDEX_START)
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

                    final String[] countryCapitalSplit;
                    countryCapitalSplit = countryCapitalLine.split(":", 2);

                    final String countryName;
                    countryName = formatCountryName(countryCapitalSplit[TEXT_SPLIT_INDEX_START].trim());

                    final String capital;
                    capital = countryCapitalSplit[TEXT_SPLIT_INDEX_END].trim();

                    final List<String> facts;
                    facts = new ArrayList<>();

                    for (int i = 0; i < TOTAL_FACTS && scan.hasNextLine(); i++)
                    {
                        String factLine;
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

    /*
     * Formats country names by rearranging names that contain a comma.
     * If the country name is in the format "Last, First", it is converted
     * to "First Last".
     *
     * @param countryName The raw country name as read from the file.
     * @return The formatted country name.
     */
    private static String formatCountryName(final String countryName)
    {
        if (countryName.contains(","))
        {
            final String[] parts;
            parts = countryName.split(",", 2);

            final String format;
            format = parts[TEXT_SPLIT_INDEX_END].trim() +
                    " " + parts[TEXT_SPLIT_INDEX_START].trim();

            return format;
        }
        return countryName;
    }
}




