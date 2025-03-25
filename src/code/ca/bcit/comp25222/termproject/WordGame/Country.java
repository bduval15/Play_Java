package ca.bcit.comp25222.termproject.WordGame;

/**
 * Represents a country with its name, capital city, and optional facts.
 * This class ensures that the country name and capital city name are
 * properly validated upon creation.
 *
 * @author Braeden Duval
 */
class Country
{
    private final String countryName;
    private final String capitalCityName;
    private String[] facts;

    /**
     * Constructs a Country object with the given name and capital city name.
     * Ensures that both parameters are not null or empty.
     *
     * @param countryName       The name of the country.
     * @param capitalCityName   The name of the capital city.
     */
    Country(final String countryName,
            final String capitalCityName)
    {
        this.countryName = countryName;
        this.capitalCityName = capitalCityName;

        validateCountryName();
        validateCapitalCityName();
    }

    /*
     * Validates that the country name is not null or empty.
     *
     * @throws IllegalArgumentException if the country name is null or empty.
     */
    private void validateCountryName()
    {
        if(countryName == null || countryName.isEmpty())
        {
            throw new IllegalArgumentException("Country name cannot be null or empty");
        }
    }

    /*
     * Validates that the capital city name is not null or empty.
     *
     * @throws IllegalArgumentException if the capital city name is null or empty.
     */
    private void validateCapitalCityName()
    {
        if(capitalCityName == null || capitalCityName.isEmpty())
        {
            throw new IllegalArgumentException("Capital city name cannot be null or empty");
        }
    }

    /**
     * Sets the facts related to the country.
     *
     * @param facts An array of facts about the country.
     */
    void setFacts(final String[] facts)
    {
        this.facts = facts;
    }

    /**
     * Retrieves the facts about the country.
     *
     * @return An array of facts about the country.
     */
    String[] getFacts()
    {
        return facts;
    }

    /**
     * Retrieves the name of the country.
     *
     * @return The name of the country.
     */
    String getCountryName()
    {
        return countryName;
    }

    /**
     * Retrieves the name of the capital city of the country.
     *
     * @return The name of the capital city.
     */
    String getCapitalCityName()
    {
        return capitalCityName;
    }
}
