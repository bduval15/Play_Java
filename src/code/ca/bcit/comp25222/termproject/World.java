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

    public static Map<String, Country> buildCountries(String filepath)
            throws FileNotFoundException
    {

        // new file setup to parse through multiple .txt files
        final File folder;
        folder = new File(filepath);

        final File[] files;
        files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        final Map<String, Country> countryMap;
        countryMap = new HashMap<>();

        if (files != null)
        {
            for (File file : files)
            {
                final Scanner scan;
                scan = new Scanner(file);

                try (scan)
                {

                    // Temp Containers for current country's data
                    String currentCountryName = null;
                    String currentCapital = null;

                    // List to store current country facts
                    List<String> currentFacts;
                    currentFacts = new ArrayList<>();

                    while (scan.hasNextLine())
                    {
                        final String line;
                        line = scan.nextLine();

                        if (line.isEmpty())
                        {
                            continue;
                        }

                        //Checks if line has colon
                        if (line.contains(":"))
                        {
                            // If country is already being made, make it final
                            if (currentCountryName != null)
                            {
                                final Country country;
                                country = new Country(currentCountryName, currentCapital);

                                // Converts list of facts to an array and assigns it to the country object
                                country.setFacts(currentFacts.toArray(new String[0]));

                                //Put country in HashMap
                                countryMap.put(currentCountryName, country);
                            }

                            //Splits the CountryName and Capital
                            String[] splitNames;
                            splitNames = line.split(":", 2);
                            currentCountryName = splitNames[0];
                            currentCapital = splitNames[1];

                            // Resets facts list for new entries
                            currentFacts = new ArrayList<>();
                        } else
                        {
                            //if line does not contain a colon, add it to the facts
                            currentFacts.add(line);
                        }
                    }
                    // Processing the last Country in the .txt file
                    if (currentCountryName != null)
                    {
                        final Country country;
                        country = new Country(currentCountryName, currentCapital);
                        country.setFacts(currentFacts.toArray(new String[0]));
                        countryMap.put(currentCountryName, country);
                    }
                }

            }
        }
        return countryMap;
    }
    //HashMap test
    public static void main(final String[] args)
    {
        try
        {
            Map<String, Country> countries = buildCountries(filepath);

            for(Map.Entry<String, Country> entry : countries.entrySet())
            {
                Country country = entry.getValue();
                System.out.println(country.getCountryName());
                System.out.println(country.getCapitalCityName());
                for(String fact : country.getFacts())
                {
                    System.out.println(fact);
                }
                System.out.println();
            }
        } catch (FileNotFoundException e)
        {
            System.out.println("File not found" + filepath);
        }
    }

}
