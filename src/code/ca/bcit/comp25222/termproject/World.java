package ca.bcit.comp25222.termproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
/**
 *
 * @author Braeden Duval
 *
 */
public class World
{
    private static final String filepath = "src/resources/";

     static Map<String, Country> buildCountries()
            throws FileNotFoundException
    {

        final File folder;
        folder = new File(World.filepath);

        final File[] files;
        files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        final Map<String, Country> countryMap;
        countryMap = new HashMap<>();

        if(files == null || files.length == 0)
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

                    String[] countryCapitalSplit;
                    countryCapitalSplit = countryCapitalLine.split(":", 2);

                    String countryName;
                    countryName = formatCountryName(countryCapitalSplit[0].trim());

                    String capital;
                    capital = countryCapitalSplit[1].trim();

                    List<String> facts;
                    facts = new ArrayList<>();

                    for (int i = 0; i < 3 && scan.hasNextLine(); i++)
                    {
                        String factLine = scan.nextLine().trim();
                        facts.add(factLine);
                    }

                    Country country;
                    country = new Country(countryName, capital);

                    country.setFacts(facts.toArray(new String[0]));

                    countryMap.put(countryName, country);
                }
            }
        }

        final Map<String, Country> immutableCountryMap;
        immutableCountryMap = Collections.unmodifiableMap(countryMap);

        return immutableCountryMap;
    }

    private static String formatCountryName(String countryName)
    {
        if (countryName.contains(","))
        {
            String[] parts = countryName.split(",", 2);
            return parts[1].trim() + " " + parts[0].trim();
        }
        return countryName;
    }
}




